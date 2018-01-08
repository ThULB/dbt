/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.media;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.common.events.MCRShutdownHandler.Closeable;
import org.mycore.common.inject.MCRInjectorConfig;
import org.mycore.common.processing.MCRProcessableDefaultCollection;
import org.mycore.common.processing.MCRProcessableRegistry;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.util.concurrent.processing.MCRProcessableExecutor;
import org.mycore.util.concurrent.processing.MCRProcessableFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.urmel_dl.dbt.media.entity.ConverterJob;
import de.urmel_dl.dbt.media.entity.Sources;
import de.urmel_dl.dbt.media.entity.Sources.Source;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MediaService {

    public static final String CONFIG_PREFIX = "DBT.MediaService.";

    public static final String CONVERTER_ADD_JOB_PATH = "/converter/addjob";

    public static final String CONVERTER_REMOVE_JOB_PATH = "/converter/removejob/{0}";

    public static final String CONVERTER_DOWNLOAD_PATH = "/widget/converter/{0}/compress";

    public static final Path MEDIA_STORAGE_PATH;

    public static final Path THUMB_STORAGE_PATH;

    public static final String THUMB_FORMAT_SCALED;

    public static final String SERVER_ADDRESS;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    private static final MCRProcessableExecutor TASK_EXECUTOR;

    private static final MCRProcessableDefaultCollection TASK_COLLECTION;

    private static final MCRCache<String, List<Path>> MEDIA_FILES_CACHE = new MCRCache<>(1000L, "MediaFileCache");

    private static final MCRCache<String, List<Path>> THUMB_FILES_CACHE = new MCRCache<>(1000L, "ThumbFileCache");

    private static final Closeable TASK_SHUTDOWNHANDLER = new Closeable() {
        @Override
        public void prepareClose() {
            TASK_EXECUTOR.getExecutor().shutdown();
        }

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE + 11;
        }

        @Override
        public void close() {
            waitForShutdown(TASK_EXECUTOR.getExecutor());
        }

        private void waitForShutdown(ExecutorService service) {
            if (!service.isTerminated()) {
                try {
                    LOGGER.info("Waiting for shutdown of MediaService Handler.");
                    service.awaitTermination(10, TimeUnit.MINUTES);
                    LOGGER.info("MediaService Handler was shut down.");
                } catch (InterruptedException e) {
                    LOGGER.warn("Error while waiting for shutdown.", e);
                }
            }
        }
    };

    private static final List<String> CP_MEDIA_FILE_EXT = Arrays.asList(".smil", ".mp4");

    private static final List<String> CP_THUMB_FILE_EXT = Arrays.asList(".jpg");

    private static Path tempDirThumbs;

    static {
        MCRProcessableRegistry registry = MCRInjectorConfig.injector().getInstance(MCRProcessableRegistry.class);

        int poolSize = CONFIG.getInt(CONFIG_PREFIX + "ThreadCount", 4);

        final ExecutorService threadPool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
            MCRProcessableFactory.newPriorityBlockingQueue(),
            new ThreadFactoryBuilder().setNameFormat("MediaService-#%d").build());

        TASK_COLLECTION = new MCRProcessableDefaultCollection("MediaService");
        TASK_COLLECTION.setProperty("pool size (threads)", poolSize);

        registry.register(TASK_COLLECTION);
        TASK_EXECUTOR = MCRProcessableFactory.newPool(threadPool, TASK_COLLECTION);

        MCRShutdownHandler.getInstance().addCloseable(TASK_SHUTDOWNHANDLER);

        SERVER_ADDRESS = CONFIG.getString(CONFIG_PREFIX + "ServerAddress");

        MEDIA_STORAGE_PATH = Paths.get(CONFIG.getString(CONFIG_PREFIX + "Media.StoragePath"));
        THUMB_STORAGE_PATH = Paths.get(CONFIG.getString(CONFIG_PREFIX + "Thumb.StoragePath"));
        THUMB_FORMAT_SCALED = CONFIG.getString(CONFIG_PREFIX + "Thumb.FormatScaled", "JPG");
    }

    protected static MCRProcessableExecutor executor() {
        return TASK_EXECUTOR;
    }

    public static void encodeMediaFile(String id, Path mediaFile, int priority) {
        if (isMediaSupported(mediaFile)) {
            executor().submit(new EncodeTask(id, mediaFile, priority));
        }
    }

    public static void handleCompletedJob(ConverterJob job) {
        executor().submit(new CompletedJobTask(job), 10);
    }

    public static boolean isMediaSupported(Path path) {
        return CONFIG.getStrings(CONFIG_PREFIX + "SupportedExtensions", Arrays.asList(".avi", ".mp4", ".mov", ".mkv"))
            .stream().anyMatch(e -> {
                String fn = path.getFileName().toString();
                return fn.length() > e.length() && fn.substring(fn.length() - e.length()).equalsIgnoreCase(e);
            });
    }

    public static boolean hasMediaFiles(String id) {
        return Optional.ofNullable(getMediaFiles(id)).map(fs -> fs.stream()
            .map(f -> CP_MEDIA_FILE_EXT.stream().anyMatch(ext -> f.getFileName().toString().endsWith(ext))).findAny()
            .isPresent()).orElse(false);
    }

    public static boolean hasSMILFile(String id) {
        return Optional.ofNullable(getMediaFiles(id))
            .map(f -> f.stream().filter(p -> p.getFileName().toString().endsWith(".smil")).findAny().isPresent())
            .orElse(false);
    }

    public static String getSMILFile(String id) {
        return Optional.ofNullable(getMediaFiles(id))
            .map(f -> f.stream().filter(p -> p.getFileName().toString().endsWith(".smil")).findFirst()
                .map(op -> op.getFileName().toString()).orElse(null))
            .orElse(null);
    }

    public static Path getMediaFile(String id, String fileName) {
        return MEDIA_STORAGE_PATH.resolve(id).resolve(fileName);
    }

    public static List<Path> getMediaFiles(String id) {
        List<Path> files = MEDIA_FILES_CACHE.get(id);

        if (files == null) {
            try {
                Path parent = MEDIA_STORAGE_PATH.resolve(id);
                files = Files.walk(parent).filter(f -> !f.equals(parent))
                    .collect(Collectors.toList());
                MEDIA_FILES_CACHE.put(id, files);
            } catch (IOException e) {
                files = null;
            }
        }

        return files;
    }

    public static Path getThumbFile(String id, String fileName) {
        return THUMB_STORAGE_PATH.resolve(id).resolve(fileName);
    }

    public static Path getThumbFile(String id, String fileName, int width, int height) throws IOException {
        if (width <= 0 && height <= 0) {
            throw new IllegalArgumentException("At least one of width or height must be greater than 0.");
        }

        return scaleThumb(THUMB_STORAGE_PATH.resolve(id).resolve(fileName), width, height);
    }

    private static Path scaleThumb(Path file, int width, int height) throws IOException {
        if (tempDirThumbs == null) {
            tempDirThumbs = Files.createTempDirectory("thumbs");
            tempDirThumbs.toFile().deleteOnExit();
        }

        Path scaled = tempDirThumbs.resolve(scaledFileName(file, width, height));
        LOGGER.info("scaled: {}", scaled);
        if (Files.notExists(scaled)) {
            BufferedImage scaledImg = scale(ImageIO.read(file.toFile()), width, height);
            ImageIO.write(scaledImg, THUMB_FORMAT_SCALED.toUpperCase(Locale.ROOT), scaled.toFile());
        }

        return scaled;
    }

    private static BufferedImage scale(BufferedImage img, int width, int height) {
        double ratio = Double.max((double) width / (double) img.getWidth(), (double) height / (double) img.getHeight());

        int finalh = (int) (img.getHeight() * ratio);
        int finalw = (int) (img.getWidth() * ratio);

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, finalw, finalh, null);
        g2.dispose();

        return resizedImg;
    }

    private static String scaledFileName(Path file, int width, int height) {
        String fileName = file.getFileName().toString();
        return Optional.of(fileName.lastIndexOf(".")).filter(o -> o != -1).map(o -> fileName.substring(0, o))
            .orElse(fileName) + "-" + (width > 0 ? Integer.toString(width) + "w" : "")
            + (height > 0 ? Integer.toString(height) + "h" : "") + "." + THUMB_FORMAT_SCALED.toLowerCase(Locale.ROOT);
    }

    public static List<Path> getThumbFiles(String id) {
        List<Path> files = THUMB_FILES_CACHE.get(id);

        if (files == null) {
            try {
                Path parent = THUMB_STORAGE_PATH.resolve(id);
                files = Files.walk(parent).filter(f -> !f.equals(parent))
                    .collect(Collectors.toList());
                THUMB_FILES_CACHE.put(id, files);
            } catch (IOException e) {
                files = null;
            }
        }

        return files;
    }

    public static void deleteMediaFiles(String id) throws IOException {
        deleteFiles(MEDIA_STORAGE_PATH.resolve(id));
        MEDIA_FILES_CACHE.remove(id);
        deleteFiles(THUMB_STORAGE_PATH.resolve(id));
        THUMB_FILES_CACHE.remove(id);
    }

    private static void deleteFiles(Path path) throws IOException {
        Files.walk(path, FileVisitOption.FOLLOW_LINKS)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    public static String buildInternalId(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return String.format(Locale.ROOT, "%032X",
                new BigInteger(1, md.digest(input.getBytes(StandardCharsets.UTF_8))));
        } catch (NoSuchAlgorithmException e) {
            return input;
        }
    }

    public static Sources buildMediaSources(String id) {
        return Optional.ofNullable(getMediaFiles(id)).map(f -> Sources.build(id, f)).orElse(null);
    }

    public static Sources buildMediaSources(String id, String ipAddress, String... queryParameters) {
        return Optional.ofNullable(getMediaFiles(id)).map(f -> Sources.build(id, f, ipAddress, queryParameters))
            .orElse(null);
    }

    public static Sources buildThumbSources(String id) {
        return Optional.ofNullable(getThumbFiles(id))
            .map(f -> new Sources(id,
                f.stream().map(file -> file.getFileName()).sorted()
                    .map(file -> new Source("image/jpeg", file.toString()))
                    .collect(Collectors.toList())))
            .orElse(null);
    }

    /**
     * Task for media encoding.
     *
     * @author Ren\u00E9 Adler (eagle)
     *
     */
    static class EncodeTask implements Runnable {

        private final String id;

        private final Path mediaFile;

        private final int priority;

        EncodeTask(String id, Path mediaFile, int priority) {
            this.id = id;
            this.mediaFile = mediaFile;
            this.priority = priority;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                LOGGER.info("Send media file {} to encoder service.", mediaFile.getFileName());

                Path tmpFile = Files.createTempDirectory("media").resolve(mediaFile.getFileName().toString());
                try {
                    Client client = ClientBuilder.newBuilder()
                        .register(MultiPartFeature.class).build();
                    client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);
                    client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");

                    WebTarget webTarget = client
                        .target(MediaService.SERVER_ADDRESS + MediaService.CONVERTER_ADD_JOB_PATH);

                    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
                    formDataMultiPart.field("id", id);
                    formDataMultiPart.field("filename", mediaFile.getFileName().toString());
                    formDataMultiPart.field("priority", Integer.toString(priority));
                    formDataMultiPart.field("callback",
                        MCRFrontendUtil.getBaseURL() + "rsc/media/completeCallback");

                    Files.copy(mediaFile, tmpFile, StandardCopyOption.REPLACE_EXISTING);

                    formDataMultiPart.bodyPart(new FileDataBodyPart("file",
                        tmpFile.toFile(),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE));

                    Response response = webTarget.request(MediaType.TEXT_PLAIN_TYPE)
                        .post(Entity.entity(formDataMultiPart, formDataMultiPart.getMediaType()));

                    if (response.getStatus() != 200) {
                        LOGGER.error("Encoder service send status info {}.", response.getStatusInfo());
                    }
                } catch (Exception e) {
                    throw new MCRException(e);
                } finally {
                    Files.delete(tmpFile);
                }
            } catch (IOException e) {
                throw new MCRException(e);
            }
        }

    }

    /**
     * Task for encoded media files.
     *
     * @author Ren\u00E9 Adler (eagle)
     *
     */
    static class CompletedJobTask implements Runnable {

        private final ConverterJob job;

        CompletedJobTask(ConverterJob job) {
            this.job = job;
        }

        @Override
        public String toString() {
            return job.getId() + ": " + job.getFileName();
        }

        @Override
        public void run() {
            try {
                LOGGER.info("download media package for {}...", job.getId());

                Path tmpFile = Files.createTempDirectory("media").resolve(job.getId() + ".zip");
                try {
                    URL website = new URL(SERVER_ADDRESS + new MessageFormat(CONVERTER_DOWNLOAD_PATH, Locale.ROOT)
                        .format(new Object[] { job.getId().replaceAll(" ", "%20") }));
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());

                    try (FileOutputStream fos = new FileOutputStream(tmpFile.toFile())) {
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    }

                    String internalId = buildInternalId(job.getId());
                    Path mediaStorePath = MEDIA_STORAGE_PATH.resolve(internalId);
                    Path thumbStorePath = THUMB_STORAGE_PATH.resolve(internalId);

                    if (Files.notExists(mediaStorePath)) {
                        Files.createDirectories(mediaStorePath);
                    }

                    if (Files.notExists(thumbStorePath)) {
                        Files.createDirectories(thumbStorePath);
                    }

                    Map<String, String> env = new HashMap<>();
                    env.put("create", "false");

                    URI zipUri = URI.create("jar:" + tmpFile.toFile().toURI());
                    try (FileSystem fs = FileSystems.newFileSystem(zipUri, env)) {
                        Path root = fs.getPath("/");
                        Files.walk(root).filter(p -> Optional.ofNullable(p.getFileName())
                            .map(fn -> CP_MEDIA_FILE_EXT.stream().anyMatch(ext -> fn.toString().endsWith(ext)))
                            .orElse(false))
                            .forEach(p -> {
                                try {
                                    LOGGER.info("copy media file {} to {}", p,
                                        mediaStorePath.resolve(root.relativize(p).toString()));
                                    Files.copy(p, mediaStorePath.resolve(root.relativize(p).toString()),
                                        StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });

                        Files.walk(root).filter(p -> Optional.ofNullable(p.getFileName())
                            .map(fn -> CP_THUMB_FILE_EXT.stream().anyMatch(ext -> fn.toString().endsWith(ext)))
                            .orElse(false))
                            .forEach(p -> {
                                try {
                                    LOGGER.info("copy thumb {} to {}", p,
                                        thumbStorePath.resolve(root.relativize(p).toString()));
                                    Files.copy(p, thumbStorePath.resolve(root.relativize(p).toString()),
                                        StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                    }

                    removeJob();
                } catch (Exception e) {
                    throw new MCRException(e);
                } finally {
                    Files.delete(tmpFile);
                }
            } catch (IOException e) {
                throw new MCRException(e);
            }
        }

        private void removeJob() {
            Client client = ClientBuilder.newBuilder().build();

            WebTarget webTarget = client
                .target(MediaService.SERVER_ADDRESS + new MessageFormat(CONVERTER_REMOVE_JOB_PATH, Locale.ROOT)
                    .format(new Object[] { job.getId() }));

            Response response = webTarget.request(MediaType.TEXT_PLAIN_TYPE).get();

            if (response.getStatus() == 200) {
                if (Boolean.parseBoolean(response.readEntity(String.class))) {
                    LOGGER.info("Job with id {} was removed.", job.getId());
                } else {
                    LOGGER.warn("Job with id {} wasn't removed.", job.getId());
                }
            }
        }
    }

}

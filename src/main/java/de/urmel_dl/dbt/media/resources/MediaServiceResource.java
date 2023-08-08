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
package de.urmel_dl.dbt.media.resources;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.frontend.jersey.MCRStaticContent;

import de.urmel_dl.dbt.media.MediaService;
import de.urmel_dl.dbt.media.entity.ConverterJob;
import de.urmel_dl.dbt.media.entity.Media;
import de.urmel_dl.dbt.media.entity.Sources;
import de.urmel_dl.dbt.utils.EntityFactory;
import de.urmel_dl.dbt.utils.MimeType;
import de.urmel_dl.dbt.utils.RangeStreamingOutput;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRStaticContent
@Path("media")
public class MediaServiceResource {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern RANGE_PATTERN = Pattern.compile("([^=]+)=(\\d+)(?:-(\\d+)?)");

    @POST
    @Path("completeCallback")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response completeCallback(ConverterJob job) throws JAXBException, IOException {
        LOGGER.info("encoding of {} ({}) is done with exit code {}.", job.getFileName(), job.getId(),
            job.getExitValue());

        if (job.isDone() && job.getExitValue() == 0) {
            MediaService.handleCompletedJob(job);
        }

        return Response.ok().build();
    }

    @GET
    @Path("sources/{id:.+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Sources mediaSources(@Context HttpServletRequest req, @PathParam("id") String id) {
        return MediaService.buildMediaSources(id, req.getRemoteAddr());
    }

    @GET
    @Path("thumbs/{id:.+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Sources thumbSources(@PathParam("id") String id) {
        return MediaService.buildThumbSources(id);
    }

    @GET
    @Path("thumb/{id:.+}/{fileName:.+}")
    @Produces("*/*")
    public Response thumb(@PathParam("id") String id,
        @PathParam("fileName") String fileName) throws Exception {

        java.nio.file.Path file = MediaService.getThumbFile(id, fileName);
        if (file != null) {
            return buildStream(file, null);
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("thumb/{id:.+}/{fileName:.+}/{width:[^:]+}{height:(:(.*)?)?}")
    @Produces("*/*")
    public Response thumb(@PathParam("id") String id,
        @PathParam("fileName") String fileName, @PathParam("width") String width,
        @PathParam("height") String height) throws Exception {
        java.nio.file.Path file = MediaService.getThumbFile(id, fileName,
            Optional.of(width).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(-1),
            Optional.ofNullable(height.replaceAll(":", "")).filter(s -> !s.isEmpty()).map(Integer::parseInt)
                .orElse(-1));
        if (file != null) {
            return buildStream(file, null);
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @HEAD
    @Path("progressiv/{id:.+}/{fileName:.+}")
    public Response progressivDownloadHeader(@PathParam("id") String id, @PathParam("fileName") String fileName) {
        java.nio.file.Path path = MediaService.getMediaFile(id, fileName);
        if (path != null) {
            return Response.ok().status(Response.Status.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_LENGTH, path.toFile().length()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("progressiv/{id:.+}/{fileName:.+}")
    @Produces("*/*")
    public Response progressivDownload(@HeaderParam("Range") String range, @PathParam("id") String id,
        @PathParam("fileName") String fileName) throws Exception {
        java.nio.file.Path path = MediaService.getMediaFile(id, fileName);
        if (path != null) {
            return buildStream(path, range);
        } else {
            LOGGER.error("download path was empty.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("embed/{id:.+}")
    @Produces("text/html")
    public Response embed(@PathParam("id") String id) throws IOException {
        Media media = new Media();
        media.setId(id);

        return transformedResponse(media, "xsl/media/embed.xsl", Collections.emptyMap());
    }

    private Response buildStream(final java.nio.file.Path asset, final String range) throws Exception {
        final String mimeType = MimeType.detect(asset);

        return Stream.of(RANGE_PATTERN.matcher(Optional.ofNullable(range).orElse("")))
            .filter(rm -> rm.find()).findFirst()
            .map(rm -> {
                try {
                    final File assetFile = asset.toFile();
                    final long from = Long.parseLong(rm.group(2));
                    final long to = Optional.ofNullable(rm.group(3)).map(Long::parseLong)
                        .orElse(assetFile.length() - 1);

                    final String responseRange = String.format(Locale.ROOT, "bytes %d-%d/%d", from, to,
                        assetFile.length());
                    final RandomAccessFile raf = new RandomAccessFile(assetFile, "r");
                    raf.seek(from);

                    final long len = to - from + 1;
                    final RangeStreamingOutput streamer = new RangeStreamingOutput(len, raf);

                    return Response.ok(streamer, mimeType)
                        .status(Response.Status.PARTIAL_CONTENT)
                        .header("Accept-Ranges", "bytes")
                        .header("Content-Range", responseRange)
                        .header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
                        .header(HttpHeaders.LAST_MODIFIED, new Date(assetFile.lastModified())).build();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).orElseGet(() -> {
                final StreamingOutput streamer = output -> {
                    byte[] data = Files.readAllBytes(asset);
                    output.write(data);
                    output.flush();
                };

                return Response
                    .ok(streamer, mimeType)
                    .header("content-disposition",
                        "inline; filename = \"" + asset.getFileName().toString() + "\"")
                    .build();
            });
    }

    private <T> Response transformedResponse(T entity, String stylesheet, Map<String, String> parameters) {
        if (entity == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        boolean hasSession = MCRSessionMgr.hasCurrentSession();
        try {
            if (!hasSession) {
                MCRSessionMgr.unlock();
            }
            MCRParameterCollector pc = new MCRParameterCollector();
            pc.setParameters(parameters);

            MCRXSLTransformer transformer = MCRXSLTransformer.getInstance(stylesheet);
            MCRContent result = transformer
                .transform(new MCRJDOMContent(new EntityFactory<>(entity).toDocument()), pc);

            final StreamingOutput so = (OutputStream os) -> result.sendTo(os);
            return Response.ok().status(Response.Status.OK).entity(so).build();
        } catch (Exception e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        } finally {
            if (!hasSession && MCRSessionMgr.hasCurrentSession()) {
                MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
                MCRSessionMgr.releaseCurrentSession();
                mcrSession.close(); //created for XSLT transformation
            }
        }
    }
}

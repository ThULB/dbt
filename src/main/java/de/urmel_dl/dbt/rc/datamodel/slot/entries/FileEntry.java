/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.rc.datamodel.slot.entries;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import de.urmel_dl.dbt.rc.rest.v2.annotation.RCAccessCheck;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * The Class FileEntry.
 *
 * @author René Adler (eagle)
 */
@RCAccessCheck
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.NONE)
public class FileEntry implements Serializable {

    /** The Constant DEFAULT_HASH_TYPE. */
    public static final String DEFAULT_HASH_TYPE = "SHA-1";

    @Serial
    private static final long serialVersionUID = 2749951822001215240L;

    private static final Logger LOGGER = LogManager.getLogger(FileEntry.class);

    // Error code: for a empty file or empty file parameter
    private static final int ERROR_EMPTY_FILE = 100;

    // Error code: for a PDF document with exceeded page limit
    private static final int ERROR_PAGE_LIMIT_EXCEEDED = 101;

    // Error code: for a unsupported PDF document
    private static final int ERROR_NOT_SUPPORTED = 102;

    private static final String TEMP_FILE_EXTENSION = ".rctmp";

    private String name;

    private boolean copyrighted;

    private String hash;

    private long size = 0;

    private String comment;

    private Path path;

    /**
     * Creates an {@link FileEntry} from given {@link InputStream}.
     * If is copyrighted material, extra processing of content happens.
     *
     * @param entryId the SlotEntry id
     * @param fileName the file name
     * @param comment the comment
     * @param isCopyrighted <code>true</code> if material is copyrighted
     * @param is the file {@link InputStream}
     * @return the {@link FileEntry}
     * @throws FileEntryProcessingException thrown if file entry couldn't processed
     * @throws IOException thrown if file not found or other
     */
    public static FileEntry createFileEntry(final String entryId, final String fileName, final String comment,
        boolean isCopyrighted, final InputStream is) throws FileEntryProcessingException, IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileEntryProcessingException("empty file name", ERROR_EMPTY_FILE);
        }

        final FileEntry fileEntry = new FileEntry();

        fileEntry.setName(fileName);
        fileEntry.setComment(comment);
        fileEntry.setCopyrighted(isCopyrighted);

        createTempFileEntry(fileEntry, is);

        if (isCopyrighted) {
            processContent(entryId, fileEntry);
        }

        Optional.ofNullable(fileEntry.getPath()).ifPresent((p) -> {
            fileEntry.setSize(p.toFile().length());
            fileEntry.setHash(buildHash(p));
        });

        return fileEntry;
    }

    public static boolean isTempFile(Path file) {
        return file.getFileName().toString().endsWith(TEMP_FILE_EXTENSION);
    }

    private static void processContent(final String entryId, final FileEntry fileEntry)
        throws FileEntryProcessingException, IOException {

        if (entryAsInputStream(fileEntry, FileEntry::isPDF)) {
            final String fileName = fileEntry.getName();

            Path pdfCopy = null;

            try {
                int numPages = entryAsInputStream(fileEntry, FileEntry::getNumPagesFromPDF);

                LOGGER.info("Check num pages for \"{}\": {}", fileName, numPages);
                if (numPages == -1 || numPages > 50) {
                    throw new FileEntryProcessingException("page limit exceede", ERROR_PAGE_LIMIT_EXCEEDED);
                }

                LOGGER.info("Make an supported copy for \"{}\".", fileName);
                pdfCopy = entryInputStreamToPathConsumer(fileEntry, FileEntry::copyPDF);

                LOGGER.info("Encrypt \"{}\".", fileName);
                fileEntry.setPath(entryInputStreamToPathConsumer(fileEntry, (is, os) -> encryptPDF(entryId, is, os)));
            } catch (IOException e) {
                throw new FileEntryProcessingException(e.getMessage(), ERROR_NOT_SUPPORTED);
            } finally {
                if (pdfCopy != null) {
                    Files.deleteIfExists(pdfCopy);
                }
            }
        }
    }

    private static <T> T entryAsInputStream(FileEntry entry, Function<InputStream, T> func) throws IOException {
        try (InputStream is = Files.newInputStream(entry.getPath())) {
            return func.apply(is);
        }
    }

    private static Path entryInputStreamToPathConsumer(FileEntry entry,
        BiConsumer<InputStream, OutputStream> consumer) throws IOException {
        Path tmpFile = Files.createTempFile(entry.getName(), ".pdf");
        OutputStream os = Files.newOutputStream(tmpFile);
        try (InputStream is = Files.newInputStream(entry.getPath())) {
            consumer.accept(is, os);
        }
        return tmpFile;
    }

    private static void createTempFileEntry(final FileEntry fileEntry, final InputStream is) throws IOException {
        Path tmpFile = Files.createTempFile(fileEntry.getName(), TEMP_FILE_EXTENSION);
        Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        fileEntry.setPath(tmpFile);
    }

    private static boolean isPDF(final InputStream is) {
        try {
            try (is; RandomAccessRead readBuffer = new RandomAccessReadBuffer(is);
                PDDocument doc = Loader.loadPDF(readBuffer)) {
                return doc != null;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the number of pages from given PDF {@link InputStream}.
     *
     * @param pdfInput the {@link InputStream}
     * @return the number of pages
     * @throws UncheckedIOException thrown if file not found or other
     */
    private static int getNumPagesFromPDF(final InputStream pdfInput) {
        try (pdfInput; RandomAccessRead readBuffer = new RandomAccessReadBuffer(pdfInput);
            PDDocument doc = Loader.loadPDF(readBuffer)) {
            return doc.getNumberOfPages();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Makes an save copy of given PDF {@link InputStream} to an new {@link OutputStream}.
     *
     * @param pdfInput the PDF {@link InputStream}
     * @param pdfOutput the PDF {@link OutputStream}
     * @throws UncheckedIOException  hrown if file not found or other
     */
    private static void copyPDF(final InputStream pdfInput, final OutputStream pdfOutput) {
        try (pdfInput; RandomAccessRead readBuffer = new RandomAccessReadBuffer(pdfInput);
            PDDocument document = Loader.loadPDF(readBuffer); COSDocument doc = document.getDocument()) {
            COSWriter writer = new COSWriter(pdfOutput);
            writer.write(doc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Secures the PDF document and set the password.
     *
     * @param password the password
     * @param pdfInput the PDF {@link InputStream}
     * @param pdfOutput the PDF {@link OutputStream}
     * @throws UncheckedIOException thrown if file not found or other
     */
    private static void encryptPDF(final String password, final InputStream pdfInput, final OutputStream pdfOutput) {
        try (pdfInput; RandomAccessRead readBuffer = new RandomAccessReadBuffer(pdfInput);
            PDDocument doc = Loader.loadPDF(readBuffer)) {

            AccessPermission ap = new AccessPermission();

            ap.setCanAssembleDocument(false);
            ap.setCanExtractContent(false);
            ap.setCanExtractForAccessibility(false);
            ap.setCanFillInForm(false);
            ap.setCanModify(false);
            ap.setCanModifyAnnotations(false);
            ap.setCanPrint(false);
            ap.setCanPrintFaithful(false);
            ap.setReadOnly();

            if (!doc.isEncrypted()) {
                StandardProtectionPolicy spp = new StandardProtectionPolicy(password, null, ap);
                doc.protect(spp);

                doc.save(pdfOutput);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void decryptPDF(final String password, final InputStream pdfInput, final OutputStream pdfOutput)
        throws IOException {
        try (pdfInput; RandomAccessRead readBuffer = new RandomAccessReadBuffer(pdfInput);
            PDDocument doc = Loader.loadPDF(readBuffer, password)) {

            if (doc.isEncrypted()) {
                doc.setAllSecurityToBeRemoved(true);
                doc.save(pdfOutput);
            }
        }
    }

    private static String buildHash(Path file) {
        try {
            byte[] buffer = new byte[8192];
            MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
            try (InputStream is = Files.newInputStream(file);
                DigestInputStream dis = new DigestInputStream(is, md)) {
                long start = System.currentTimeMillis();
                int numRead;

                do {
                    numRead = dis.read(buffer);
                    if (numRead > 0) {
                        md.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);

                LOGGER.info("generate hash for {} in {}ms", file.getFileName(),
                    (System.currentTimeMillis() - start));
                return String.format(Locale.ROOT, "%032X", new BigInteger(1, md.digest()));
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error(e);
            return null;
        }
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlAttribute(name = "name", required = true)
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the id to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Checks if is copyrighted.
     *
     * @return the copyrighted
     */
    @XmlAttribute(name = "copyrighted")
    public boolean isCopyrighted() {
        return copyrighted;
    }

    /**
     * Sets the copyrighted.
     *
     * @param copyrighted the copyrighted to set
     */
    public void setCopyrighted(boolean copyrighted) {
        this.copyrighted = copyrighted;
    }

    /**
     * Gets the hash.
     *
     * @return the hash
     */
    @XmlAttribute(name = "hash")
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    protected void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    @XmlAttribute(name = "size")
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    protected void setSize(long size) {
        this.size = size;
    }

    /**
     * Gets the exportable content.
     *
     * @param entryId the entry id
     * @return the exportable path
     */
    public Path getExportablePath(final String entryId) {
        if (this.copyrighted) {
            try {
                if (isPDF(Files.newInputStream(getPath()))) {
                    ByteArrayOutputStream pdf = new ByteArrayOutputStream();

                    decryptPDF(entryId, Files.newInputStream(getPath()), pdf);

                    Path tmpFile = Files.createTempFile(entryId, TEMP_FILE_EXTENSION);

                    try (OutputStream fos = Files.newOutputStream(tmpFile)) {
                        pdf.writeTo(fos);
                    }

                    return tmpFile;
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        return getPath();
    }

    /**
     * Sets the content.
     *
     * @param is the InputStream
     * @throws IOException thrown if couldn't write to inputstream.
     */
    public void setContent(final InputStream is) throws IOException {
        Path tmpFile = Files.createTempFile(name, TEMP_FILE_EXTENSION);
        Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        setPath(tmpFile);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    @XmlValue
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     *
     * @param comment the comment to set
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(Path path) {
        this.path = path;
    }

    public boolean deleteIsTmpFile() throws IOException {
        if (isTempFile(path)) {
            return Files.deleteIfExists(path);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileEntry";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Long.hashCode(size);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileEntry)) {
            return false;
        }
        FileEntry other = (FileEntry) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (hash == null) {
            if (other.hash != null) {
                return false;
            }
        } else if (!hash.equals(other.hash)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        return size != other.size;
    }

    /**
     * The Class FileEntryProcessingException.
     *
     * @author René Adler (eagle)
     */
    public static class FileEntryProcessingException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;

        private final int errorCode;

        /**
         * Instantiates a new file entry processing exception.
         *
         * @param message the message
         * @param errorCode the error code
         */
        public FileEntryProcessingException(final String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        /**
         * Gets the error code.
         *
         * @return the error code
         */
        public int getErrorCode() {
            return errorCode;
        }
    }
}

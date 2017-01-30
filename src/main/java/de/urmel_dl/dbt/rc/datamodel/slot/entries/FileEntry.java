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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.mycore.common.content.MCRByteContent;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStreamContent;

/**
 * The Class FileEntry.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.NONE)
public class FileEntry implements Serializable {

    /** The Constant DEFAULT_HASH_TYPE. */
    public static final String DEFAULT_HASH_TYPE = "SHA-1";

    private static final long serialVersionUID = 2749951822001215240L;

    private static final Logger LOGGER = LogManager.getLogger(FileEntry.class);

    // Error code: for a empty file or empty file parameter
    private static final int ERROR_EMPTY_FILE = 100;

    // Error code: for a PDF document with exceeded page limit
    private static final int ERROR_PAGE_LIMIT_EXCEEDED = 101;

    // Error code: for a unsupported PDF document
    private static final int ERROR_NOT_SUPPORTED = 102;

    private String name;

    private boolean copyrighted;

    private String hash;

    private long size = 0;

    private MCRContent content;

    private String comment;

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
        if (fileName == null || fileName.length() == 0) {
            throw new FileEntryProcessingException("empty file name", ERROR_EMPTY_FILE);
        }

        final FileEntry fileEntry = new FileEntry();

        fileEntry.setName(fileName);
        fileEntry.setComment(comment);
        fileEntry.setCopyrighted(isCopyrighted);

        if (isCopyrighted) {
            processContent(entryId, fileEntry, is);
        } else {
            fileEntry.setContent(is);
        }

        return fileEntry;
    }

    private static void processContent(final String entryId, final FileEntry fileEntry, final InputStream is)
        throws FileEntryProcessingException, IOException {

        final MCRContent content = new MCRByteContent(IOUtils.toByteArray(is));
        if (isPDF(content.getInputStream())) {
            final String fileName = fileEntry.getName();

            ByteArrayOutputStream pdfCopy = null;
            ByteArrayOutputStream pdfEncrypted = null;

            try {
                final int numPages = getNumPagesFromPDF(content.getInputStream());

                LOGGER.info("Check num pages for \"" + fileName + "\": " + numPages);
                if (numPages == -1 || numPages > 50) {
                    throw new FileEntryProcessingException("page limit exceede", ERROR_PAGE_LIMIT_EXCEEDED);
                }

                LOGGER.info("Make an supported copy for \"" + fileName + "\".");
                pdfCopy = new ByteArrayOutputStream();
                copyPDF(content.getInputStream(), pdfCopy);

                LOGGER.info("Encrypt \"" + fileName + "\".");
                pdfEncrypted = new ByteArrayOutputStream();
                encryptPDF(entryId, new ByteArrayInputStream(pdfCopy.toByteArray()), pdfEncrypted);

                fileEntry.setContent(pdfEncrypted.toByteArray());
            } catch (IOException e) {
                throw new FileEntryProcessingException(e.getMessage(), ERROR_NOT_SUPPORTED);
            } finally {
                if (pdfCopy != null) {
                    pdfCopy.close();
                }
                if (pdfEncrypted != null) {
                    pdfEncrypted.close();
                }
            }
        } else {
            fileEntry.setContent(content.getInputStream());
        }
    }

    private static boolean isPDF(final InputStream is) {
        try {
            PDDocument doc = PDDocument.load(is);
            return doc != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the number of pages from given PDF {@link InputStream}.
     *
     * @param pdfInput the {@link InputStream}
     * @return the number of pages
     * @throws IOException thrown if file not found or other
     */
    private static int getNumPagesFromPDF(final InputStream pdfInput) throws IOException {
        PDDocument doc = PDDocument.load(pdfInput);
        return doc.getNumberOfPages();
    }

    /**
     * Makes an save copy of given PDF {@link InputStream} to an new {@link OutputStram}.
     *
     * @param pdfInput the PDF {@link InputStream}
     * @param pdfOutput the PDF {@link OutputStram}
     * @throws IOException  hrown if file not found or other
     */
    private static void copyPDF(final InputStream pdfInput, final OutputStream pdfOutput)
        throws IOException {
        COSWriter writer = null;
        try {
            PDDocument document = PDDocument.load(pdfInput);
            COSDocument doc = document.getDocument();

            writer = new COSWriter(pdfOutput);

            writer.write(doc);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Secures the PDF document and set the password.
     *
     * @param password the password
     * @param pdfInput the PDF {@link InputStream}
     * @param pdfOutput the PDF {@link OutputStram}
     * @throws IOException thrown if file not found or other
     */
    private static void encryptPDF(final String password, final InputStream pdfInput, final OutputStream pdfOutput)
        throws IOException {
        PDDocument doc = PDDocument.load(pdfInput);

        AccessPermission ap = new AccessPermission();

        ap.setCanAssembleDocument(false);
        ap.setCanExtractContent(false);
        ap.setCanExtractForAccessibility(false);
        ap.setCanFillInForm(false);
        ap.setCanModify(false);
        ap.setCanModifyAnnotations(false);
        ap.setCanPrint(false);
        ap.setCanPrintDegraded(false);
        ap.setReadOnly();

        if (!doc.isEncrypted()) {
            StandardProtectionPolicy spp = new StandardProtectionPolicy(password, null, ap);
            doc.protect(spp);

            doc.save(pdfOutput);
        }
    }

    private static void decryptPDF(final String password, final InputStream pdfInput, final OutputStream pdfOutput)
        throws IOException {
        PDDocument doc = PDDocument.load(pdfInput, password);

        if (doc.isEncrypted()) {
            doc.setAllSecurityToBeRemoved(true);
            doc.save(pdfOutput);
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
     * Gets the content.
     *
     * @return the content
     */
    public MCRContent getContent() {
        return content;
    }

    /**
     * Gets the exportable content.
     *
     * @param entryId the entry id
     * @return the exportable content
     */
    public MCRContent getExportableContent(final String entryId) {
        if (this.copyrighted) {
            try {
                if (isPDF(content.getInputStream())) {
                    ByteArrayOutputStream pdf = new ByteArrayOutputStream();

                    decryptPDF(entryId, content.getInputStream(), pdf);
                    final byte[] pdfBytes = pdf.toByteArray();
                    pdf.close();

                    return new MCRByteContent(pdfBytes);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }

        return content;
    }

    /**
     * Sets the content.
     *
     * @param content the content to set
     */
    public void setContent(final MCRContent content) {
        try {
            this.content = content;
            this.size = content.length();

            MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
            this.hash = String.format(Locale.ROOT, "%032X", new BigInteger(1, md.digest(content.asByteArray())));
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Sets the content.
     *
     * @param content the byte array
     */
    public void setContent(final byte[] content) {
        setContent(new MCRByteContent(content));
    }

    /**
     * Sets the content.
     *
     * @param is the InputStream
     * @throws IOException thrown if couldn't write to inputstream.
     */
    public void setContent(final InputStream is) throws IOException {
        setContent(new MCRStreamContent(is).asByteArray());
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
        result = prime * result + (int) (size ^ (size >>> 32));
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
     * @author Ren\u00E9 Adler (eagle)
     */
    public static class FileEntryProcessingException extends Exception {
        private static final long serialVersionUID = 1L;

        private int errorCode;

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

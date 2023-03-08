/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package de.urmel_dl.dbt.servlets;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.Deflater;

import jakarta.servlet.ServletOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.services.zipper.MCRCompressServlet;

import de.urmel_dl.dbt.media.MediaService;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DBTZipServlet extends MCRCompressServlet<ZipArchiveOutputStream> {

    public static final String POOLPRIVILEGE_ALLOW_MEDIA_COMPRESS = "allow-media-compress";

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isAllowed(MCRPath file) {
        return !MediaService.isMediaSupported(file)
            || MCRAccessManager.checkPermission(POOLPRIVILEGE_ALLOW_MEDIA_COMPRESS);
    }

    @Override
    protected void sendCompressedDirectory(MCRPath file, BasicFileAttributes attrs, ZipArchiveOutputStream container)
        throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(getFilename(file) + "/");
        entry.setTime(attrs.lastModifiedTime().toMillis());
        container.putArchiveEntry(entry);
        container.closeArchiveEntry();
    }

    @Override
    protected void sendCompressedFile(MCRPath file, BasicFileAttributes attrs, ZipArchiveOutputStream container)
        throws IOException {
        if (isAllowed(file)) {
            ZipArchiveEntry entry = new ZipArchiveEntry(getFilename(file));
            entry.setTime(attrs.lastModifiedTime().toMillis());
            entry.setSize(attrs.size());
            container.putArchiveEntry(entry);
            try {
                Files.copy(file, container);
            } finally {
                container.closeArchiveEntry();
            }
        } else {
            LOGGER.info("Exclude {} from archive.", file);
        }
    }

    @Override
    protected void sendMetadataCompressed(String fileName, byte[] content, long lastModified,
        ZipArchiveOutputStream container) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(fileName);
        entry.setSize(content.length);
        entry.setTime(lastModified);
        container.putArchiveEntry(entry);
        container.write(content);
        container.closeArchiveEntry();
    }

    @Override
    protected String getMimeType() {
        return "application/zip";
    }

    @Override
    protected String getFileExtension() {
        return "zip";
    }

    @Override
    protected ZipArchiveOutputStream createContainer(ServletOutputStream sout, String comment) {
        ZipArchiveOutputStream zout = new ZipArchiveOutputStream(new BufferedOutputStream(sout));
        zout.setComment(comment);
        zout.setLevel(Deflater.BEST_COMPRESSION);
        return zout;
    }

    @Override
    protected void disposeContainer(ZipArchiveOutputStream container) throws IOException {
        container.finish();
    }

}

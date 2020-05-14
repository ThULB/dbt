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
package de.urmel_dl.dbt.rc.servlets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.events.MCRStartupHandler.AutoExecutable;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class SlotServletDeployer implements AutoExecutable {

    private static final String MCR_FILE_UPLOAD_TEMP_STORAGE_PATH = "MCR.FileUpload.TempStoragePath";

    @Override
    public String getName() {
        return SlotServlet.class.getSimpleName() + " Deployer";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        if (servletContext != null) {
            String servletName = "RCSlotServlet";
            MultipartConfigElement multipartConfig = getMultipartConfig();
            try {
                checkTempStoragePath(multipartConfig.getLocation());
            } catch (IOException e) {
                throw new MCRConfigurationException("Could not setup " + servletName + "!", e);
            }
            Dynamic slotServlet = servletContext.addServlet(servletName, SlotServlet.class);
            slotServlet.addMapping("/servlets/RCSlotServlet", "/rcentry/*");
            slotServlet.setMultipartConfig(multipartConfig);
        }

    }

    private void checkTempStoragePath(String location) throws IOException {
        Path targetDir = Paths.get(location);
        if (!targetDir.isAbsolute()) {
            throw new MCRConfigurationException(
                "'" + MCR_FILE_UPLOAD_TEMP_STORAGE_PATH + "=" + location + "' must be an absolute path!");
        }
        if (Files.notExists(targetDir)) {
            LogManager.getLogger().info("Creating directory: {}", targetDir);
            Files.createDirectories(targetDir);
        }
        if (!Files.isDirectory(targetDir)) {
            throw new NotDirectoryException(targetDir.toString());
        }
    }

    private MultipartConfigElement getMultipartConfig() {
        String location = MCRConfiguration2.getStringOrThrow(MCR_FILE_UPLOAD_TEMP_STORAGE_PATH);
        long maxFileSize = MCRConfiguration2.getLong("MCR.FileUpload.MaxSize").orElse(5000000L);
        int fileSizeThreshold = MCRConfiguration2.getInt("MCR.FileUpload.MemoryThreshold").orElse(1000000);
        LogManager.getLogger()
            .info(() -> SlotServlet.class.getSimpleName() + " accept files and requests up to "
                + maxFileSize + " bytes and uses " + location + " as tempory storage for files larger "
                + fileSizeThreshold + " bytes.");
        return new MultipartConfigElement(location, maxFileSize, maxFileSize,
            fileSizeThreshold);
    }

}

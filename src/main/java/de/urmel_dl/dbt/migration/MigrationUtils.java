/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2016
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
package de.urmel_dl.dbt.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MigrationUtils {

    private static final Logger LOGGER = LogManager.getLogger(MigrationUtils.class);

    /**
     * Returns the content of file link. And try to detect charset and encode to UTF-8.
     * 
     * @param fileLink the file link
     * @return the file content
     */
    public static String getContentOfFile(final String fileLink) {
        MCRPath file = null;
        if (fileLink.contains("/")) {
            // assume thats a derivate with path
            try {
                MCRObjectID derivateID = MCRObjectID.getInstance(fileLink.substring(0, fileLink.indexOf("/")));
                String path = fileLink.substring(fileLink.indexOf("/"));
                file = MCRPath.getPath(derivateID.toString(), path);
            } catch (MCRException exc) {
                // just check if the id is valid, don't care about the exception
            }
        }
        if (file == null) {
            LOGGER.error("Couldn't read file for " + fileLink);
        }
        try {
            final MCRContent content = new MCRPathContent(file);
            final byte[] textBytes = content.asByteArray();

            CharsetDetector detector = new CharsetDetector();
            detector.setText(textBytes);
            CharsetMatch cm = detector.detect();

            if (cm != null) {
                int confidence = cm.getConfidence();
                LOGGER.info("Encoding: " + cm.getName() + " - Confidence: " + confidence + "%");

                String str = null;
                if (confidence > 50) {
                    str = new String(new String(textBytes, cm.getName()).getBytes("UTF-8"), "UTF-8");
                } else {
                    str = new String(new String(textBytes, "ISO-8859-1").getBytes("UTF-8"), "UTF-8");
                }

                return MCRXMLHelper.removeIllegalChars(str);
            }

            return MCRXMLHelper.removeIllegalChars(content.asString());
        } catch (Exception e) {
            LOGGER.error(e);
            return "";
        }
    }
}

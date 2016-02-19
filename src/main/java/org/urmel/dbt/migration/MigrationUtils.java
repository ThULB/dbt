/*
 * $Id$ 
 * $Revision$ $Date$
 *
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
package org.urmel.dbt.migration;

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
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

    public final static String tagStart = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";
    public final static String tagEnd = "\\</\\w+\\>";
    public final static String tagSelfClosing = "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";
    public final static String htmlEntity = "&[a-zA-Z][a-zA-Z0-9]+;";
    public final static Pattern htmlPattern = Pattern.compile(
            "(" + tagStart + ".*" + tagEnd + ")|(" + tagSelfClosing + ")|(" + htmlEntity + ")", Pattern.DOTALL);

    private static final String XMLPATTERN = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff" + "]";

    /**
     * Strips illegal XML chars from string.
     *  
     * @param str the string
     * @return the plain string
     */
    public static String stripIllegalChars(final String str) {
        return str.replaceAll(XMLPATTERN, "");
    }

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

                return stripIllegalChars(str);
            }

            return stripIllegalChars(content.asString());
        } catch (Exception e) {
            LOGGER.error(e);
            return "";
        }
    }

    /**
     * Return <code>true</code> if s contains HTML markup tags or entities.
     *
     * @param s String to test
     * @return true if string contains HTML
     */
    public static boolean isHtml(final String s) {
        boolean ret = false;
        if (s != null) {
            ret = htmlPattern.matcher(s).find();
        }
        return ret;
    }
}

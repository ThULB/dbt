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

import javax.xml.transform.TransformerException;

import org.mycore.common.MCRException;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MigrationUtils {

    /**
     * 
     */
    public MigrationUtils() {
        // TODO Auto-generated constructor stub
    }

    public static String getContentOfFile(String fileLink) throws TransformerException {
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
            throw new TransformerException("Couldn't read file for " + fileLink);
        }
        try {
            return new MCRPathContent(file).asString();
        } catch (Exception e) {
            throw new TransformerException(e);
        }
    }

}

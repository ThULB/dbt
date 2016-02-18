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

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "Migration Commands")
public class MigrationCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = Logger.getLogger(MigrationCommands.class);

    @MCRCommand(syntax = "fix objects for base {0} with file {1}", help = "transforms all mycore objects for base {0} with the given file or URL {1}")
    public static List<String> xsltObjects(final String base, final String xslFile) throws Exception {
        URL styleFile = MigrationCommands.class.getResource("/xsl/" + xslFile);
        if (styleFile == null) {
            final File file = new File(xslFile);

            if (!file.exists()) {
                LOGGER.error("Could not find the stylesheet \"" + xslFile + "\".");
                return null;
            }

            styleFile = file.toURI().toURL();
        }

        List<String> cmds = new ArrayList<String>();

        MCRMetadataStore store = MCRXMLMetadataManager.instance().getStore(base);
        Iterator<Integer> IDs = store.listIDs(true);
        while (IDs.hasNext()) {
            final String id = MCRObjectID.formatID(base, IDs.next());
            cmds.add(MessageFormat.format("xslt {0} with file {1}", id, styleFile.toString()));
        }

        return cmds;
    }
}

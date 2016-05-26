/*
 * $Id$ 
 * $Revision: 2748 $ $Date: 2016-02-18 10:54:41 +0100 (Do, 18 Feb 2016) $
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
package org.urmel.dbt.repair;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

@MCRCommandGroup(name = "Repair Commands")
public class RepairCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = Logger.getLogger(RepairCommands.class);

    @MCRCommand(
        syntax = "repair institution fields",
        help = "repairs all mods:name with @valueURI but without @authorityURI")
    public static List<String> updateInstitutions() {
        URL styleFile = RepairCommands.class.getResource("/xsl/repair/repair-institutions.xsl");
        if (styleFile == null) {
            LOGGER.error("Could not find migration stylesheet. File a bug!");
            return null;
        }
        TreeSet<String> ids = new TreeSet<>(MCRXMLMetadataManager.instance().listIDsOfType("mods"));
        ArrayList<String> cmds = new ArrayList<>(ids.size());
        for (String id : ids) {
            cmds.add("xslt " + id + " with file " + styleFile.toString());
        }
        return cmds;
    }
}

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
package org.urmel.dbt.rc.commandline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRVFSContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.datamodel.slot.entries.FileEntry;
import org.urmel.dbt.rc.utils.SlotTransformer;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "RC Migration Commands")
public class RCMigrationCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(RCMigrationCommands.class);

    @SuppressWarnings("unchecked")
    @MCRCommand(syntax = "migrate slot from file {0} to directory {1}", help = "migrate rc slot from given file {0} to directory {1}")
    public static void migrateSlot(final String filename, final String dirname) {
        final File file = new File(filename);
        if (!file.isFile()) {
            LOGGER.error(filename + " is not a file.");
            return;
        }

        if (!file.getName().endsWith(".xml")) {
            LOGGER.error(file + " does not end with *.xml");
            return;
        }

        File dir = new File(dirname);
        if (!dir.isDirectory()) {
            LOGGER.error(dirname + " is not a dirctory.");
            return;
        }

        try {
            final Element slotXML = MCRXMLParserFactory.getParser(false).parseXML(new MCRVFSContent(file.toURI()))
                    .getRootElement();

            Optional.ofNullable(slotXML.getChild("derivate").getAttributeValue("ID")).ifPresent(derId -> {
                final File derDir = new File(new File(file.getParent(), "derivates"), "derivate-" + derId);
                if (derDir.isDirectory()) {
                    final File msaFile = new File(derDir, "index.msa");
                    if (msaFile.exists()) {
                        try {
                            final Element msaXML = MCRXMLParserFactory.getParser(false)
                                    .parseXML(new MCRVFSContent(msaFile.toURI())).getRootElement();

                            Optional.ofNullable(msaXML.getChildren("entry")).ifPresent(x -> {
                                final Element root = new Element("entries");
                                final List<Element> entries = new ArrayList<Element>(x);
                                entries.forEach(e -> root.addContent(e.clone()));
                                slotXML.addContent(root);
                            });
                        } catch (Exception e) {
                            LOGGER.warn("Couldn't parse index.msa.", e);
                            return;
                        }
                    } else {
                        LOGGER.warn("Couldn't found a index.msa.");
                        return;
                    }
                } else {
                    LOGGER.warn("Couldn't found derivate folder " + derDir.getAbsolutePath() + ".");
                    return;
                }

                try {
                    final MCRParameterCollector params = new MCRParameterCollector();
                    params.setParameter("dirname", file.getParent());

                    MCRContent xml = MCRXSLTransformer.getInstance("xsl/migrate/slot.xsl")
                            .transform(new MCRJDOMContent(slotXML.clone()), params);

                    final Slot slot = SlotTransformer.buildSlot(xml.asXML());
                    File xmlOutput = new File(dir, "slot-" + slot.getSlotId() + ".xml");
                    SlotTransformer.sendTo(slot, xmlOutput);
                    LOGGER.info("Slot " + slot.getSlotId() + " saved to " + xmlOutput.getCanonicalPath() + ".");

                    if (slot.getEntries() != null) {
                        for (SlotEntry<?> entry : slot.getEntries()) {
                            if (entry.getEntry() instanceof FileEntry) {
                                SlotEntry<FileEntry> slotEntry = (SlotEntry<FileEntry>) entry;

                                File fileDir = new File(dir, slotEntry.getId());
                                if (fileDir.isDirectory() || fileDir.mkdirs()) {
                                    final String name = slotEntry.getEntry().getName();

                                    File oldFile = new File(new File(derDir, slotEntry.getId()), name);
                                    if (oldFile.exists()) {
                                        final File newFile = new File(fileDir, name);
                                        LOGGER.info("Copy " + oldFile.getAbsolutePath() + " to " + newFile + "...");
                                        Files.copy(oldFile, newFile);
                                    } else {
                                        LOGGER.warn("Couldn't found old file entry " + oldFile.getAbsolutePath() + ".");
                                        slot.removeEntry(entry);
                                    }
                                } else {
                                    LOGGER.error("Couldn't create a directory for file entry.");
                                    return;
                                }
                            }
                        }
                    }
                } catch (IOException | JDOMException | SAXException e) {
                    LOGGER.error("Couldn't migrate slot from file " + file.getAbsolutePath() + ".", e);
                    return;
                }
            });
        } catch (MCRException | IOException | SAXException e) {
            LOGGER.error("Couldn't process slot file " + file.getAbsolutePath() + ".", e);
        }
    }
}

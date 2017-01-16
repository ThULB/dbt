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
package de.urmel_dl.dbt.rc.commandline;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
import org.xml.sax.SAXException;

import com.google.common.io.Files;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.utils.EntityFactory;

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
                                final List<Element> entries = new ArrayList<>(x);
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

                    final Slot slot = new EntityFactory<>(Slot.class).fromDocument(xml.asXML());

                    if (slot.getEntries() != null) {
                        List<SlotEntry<?>> migEntries = new ArrayList<>();
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
                                        continue;
                                    }
                                } else {
                                    LOGGER.error("Couldn't create a directory for file entry.");
                                    continue;
                                }
                            }
                            migEntries.add(entry);
                        }
                        slot.setEntries(migEntries);
                    }

                    File xmlOutput = new File(dir, "slot-" + slot.getSlotId() + ".xml");
                    new MCRJDOMContent(new EntityFactory<>(slot).toDocument()).sendTo(xmlOutput);
                    LOGGER.info("Slot " + slot.getSlotId() + " saved to " + xmlOutput.getCanonicalPath() + ".");
                } catch (IOException | JDOMException | SAXException e) {
                    LOGGER.error("Couldn't migrate slot from file " + file.getAbsolutePath() + ".", e);
                    return;
                }
            });
        } catch (MCRException | IOException | SAXException e) {
            LOGGER.error("Couldn't process slot file " + file.getAbsolutePath() + ".", e);
        }
    }

    @MCRCommand(syntax = "migrate all slots from directory {0} to directory {1}", help = "migrates all rc slots from given directory {0} to directory {1}")
    public static List<String> migrateAllSlots(final String from, final String to) throws IOException {
        final File fromDir = new File(from);
        if (!fromDir.isDirectory()) {
            LOGGER.error(from + " is not a dirctory.");
            return Collections.emptyList();
        }

        final File toDir = new File(to);
        if (!toDir.isDirectory()) {
            LOGGER.error(to + " is not a dirctory.");
            return Collections.emptyList();
        }

        final String[] list = fromDir.list();

        if (list.length == 0) {
            LOGGER.warn("No files found in directory " + from);
            return Collections.emptyList();
        }

        List<String> cmds = new ArrayList<>();
        for (final String r : list) {
            final File fr = new File(fromDir, r);
            if (fr.isDirectory()) {
                for (final String c : fr.list()) {
                    if (c.endsWith(".xml") && c.contains("slot")) {
                        final File ft = new File(toDir, r);
                        if (ft.isDirectory() || ft.mkdirs()) {
                            String command = new MessageFormat("migrate slot from file {0} to directory {1}",
                                Locale.ROOT).format(new Object[] {
                                    new File(fr, c).getAbsolutePath(), ft.getAbsolutePath() });
                            cmds.add(command);
                        }
                    }
                }
            }
        }
        return cmds;
    }
}

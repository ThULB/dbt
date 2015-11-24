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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.urmel.dbt.common.MailQueue;
import org.urmel.dbt.rc.datamodel.Period;
import org.urmel.dbt.rc.datamodel.RCCalendar;
import org.urmel.dbt.rc.datamodel.Status;
import org.urmel.dbt.rc.datamodel.Warning;
import org.urmel.dbt.rc.datamodel.WarningDate;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.urmel.dbt.rc.datamodel.slot.entries.FileEntry;
import org.urmel.dbt.rc.persistency.FileEntryManager;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotTransformer;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "RC Commands")
public class RCCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(RCCommands.class);

    @SuppressWarnings("unchecked")
    @MCRCommand(syntax = "export slot {0} to directory {1}", help = "exports rc slot with id {0} to given directory {1}")
    public static void exportSlot(final String slotId, final String dirname) throws IOException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        final Slot slot = slotList.getSlotById(slotId);
        if (slot != null) {
            File dir = new File(dirname);
            if (!dir.isDirectory()) {
                LOGGER.error(dirname + " is not a dirctory.");
                return;
            }

            File xmlOutput = new File(dir, "slot-" + slotId + ".xml");
            SlotTransformer.sendTo(slot, xmlOutput);
            LOGGER.info("Slot " + slotId + " saved to " + xmlOutput.getCanonicalPath() + ".");

            if (slot.getEntries() != null) {
                for (SlotEntry<?> entry : slot.getEntries()) {
                    if (entry.getEntry() instanceof FileEntry) {
                        SlotEntry<FileEntry> fileEntry = (SlotEntry<FileEntry>) entry;

                        File fileDir = new File(dirname + File.separator + fileEntry.getId());
                        if (fileDir.isDirectory() || fileDir.mkdirs()) {
                            try {
                                MCRContent c = FileEntryManager.retrieve(slot, fileEntry);
                                File f = new File(fileDir, fileEntry.getEntry().getName());
                                c.sendTo(f);
                                LOGGER.info("File \"" + fileEntry.getEntry().getName() + "\" saved to "
                                        + f.getCanonicalPath() + ".");
                            } catch (Exception ex) {
                                LOGGER.error(ex.getMessage());
                                LOGGER.error("Exception while store file to " + fileDir.getAbsolutePath());
                                return;
                            }
                        } else {
                            LOGGER.error("Couldn't create a directory for file entry.");
                            return;
                        }
                    }
                }
            }
        } else {
            LOGGER.error("Couldn't found a rc slot with id " + slotId);
        }
    }

    @MCRCommand(syntax = "export all slots to directory {0}", help = "exports all rc slots to given directory {0}")
    public static List<String> exportAllSlots(final String dirname) throws IOException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        File dir = new File(dirname);
        if (!dir.isDirectory()) {
            LOGGER.error(dirname + " is not a dirctory.");
            return Collections.emptyList();
        }

        if (!slotList.getSlots().isEmpty()) {
            List<String> cmds = new ArrayList<String>(slotList.getSlots().size());
            for (final Slot slot : slotList.getSlots()) {
                File slotDir = new File(dir, slot.getSlotId());
                if (slotDir.isDirectory() || slotDir.mkdirs()) {
                    String command = MessageFormat.format("export slot {0} to directory {1}", slot.getSlotId(),
                            slotDir.getAbsolutePath());
                    cmds.add(command);
                }
            }
            return cmds;
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @MCRCommand(syntax = "import slot from file {0}", help = "imports a slot from given file")
    public static void importSlot(final String filename)
            throws IOException, MCRPersistenceException, MCRActiveLinkException {
        final SlotManager mgr = SlotManager.instance();

        File file = new File(filename);
        if (!file.isFile()) {
            LOGGER.error(filename + " is not a file.");
            return;
        }

        if (!file.getName().endsWith(".xml")) {
            LOGGER.error(file + " does not end with *.xml");
            return;
        }

        final Slot slot = SlotTransformer.buildSlot(file.toURI());
        final Slot oldSlot = mgr.getSlotById(slot.getSlotId());

        boolean update = oldSlot != null;
        if (update) {
            slot.setMCRObjectID(oldSlot.getMCRObjectID());
        }

        if (slot.getEntries() != null) {
            for (SlotEntry<?> entry : slot.getEntries()) {
                if (entry.getEntry() instanceof FileEntry) {
                    SlotEntry<FileEntry> fileEntry = (SlotEntry<FileEntry>) entry;

                    File f = new File(file.getParent(),
                            fileEntry.getId() + File.separator + fileEntry.getEntry().getName());
                    if (f.isFile()) {
                        InputStream is = null;
                        try {
                            is = new FileInputStream(f);
                            fileEntry.getEntry().setContent(is);
                        } catch (FileNotFoundException e) {
                            LOGGER.error("Couldn't not read file \"" + f.getCanonicalPath() + "\" for file entry.");
                            return;
                        } finally {
                            if (is != null)
                                is.close();
                        }
                    } else {
                        LOGGER.error("Couldn't find file for file entry.");
                        return;
                    }

                    if (update) {
                        LOGGER.info("Update File \"" + fileEntry.getEntry().getName() + "\" from "
                                + f.getCanonicalPath() + ".");
                        FileEntryManager.update(slot, fileEntry);
                    }
                }
            }
        }

        if (update) {
            LOGGER.info("Update Slot " + slot.getSlotId() + " from " + file.getCanonicalPath() + ".");
            mgr.setSlot(slot);
            mgr.saveOrUpdate(slot);
        } else {
            LOGGER.info("Import Slot " + slot.getSlotId() + " from " + file.getCanonicalPath() + ".");
            mgr.addSlot(slot);
            mgr.saveOrUpdate(slot);
        }
    }

    @MCRCommand(syntax = "import all slots from directory {0}", help = "imports all rc slots from given directory {0}")
    public static List<String> importAllSlots(final String dirname) throws IOException {
        final File dir = new File(dirname);
        if (!dir.isDirectory()) {
            LOGGER.error(dirname + " is not a dirctory.");
            return Collections.emptyList();
        }

        final String[] list = dir.list();

        if (list.length == 0) {
            LOGGER.warn("No files found in directory " + dirname);
            return Collections.emptyList();
        }

        List<String> cmds = new ArrayList<String>();
        for (final String r : list) {
            final File fr = new File(dir, r);
            if (fr.isDirectory()) {
                for (final String c : fr.list()) {
                    if (c.endsWith(".xml") && c.contains("slot")) {
                        String command = MessageFormat.format("import slot from file {0}",
                                new File(fr, c).getAbsolutePath());
                        cmds.add(command);
                    }
                }
            }
        }
        return cmds;
    }

    @MCRCommand(syntax = "rc inactivator", help = "send warning mails for reserve collections or inactivate, set new status")
    public static void rcInactivator() throws IOException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        if (!slotList.getSlots().isEmpty()) {
            for (final Slot slot : slotList.getSlots()) {
                if (slot.isActive()) {
                    LOGGER.info("Check slot with id \"" + slot.getSlotId() + "\"...");

                    final Date today = new Date();
                    final Date validTo = slot.getValidToAsDate();
                    final Period period = RCCalendar.getPeriod(slot.getLocation().toString(), validTo);

                    try {
                        if (today.after(validTo)) {
                            MCREvent evt = null;
                            boolean save = true;

                            switch (slot.getStatus()) {
                            case ARCHIVED:
                            case FREE:
                            case RESERVED:
                                save = false;
                                break;
                            case ACTIVE:
                                LOGGER.info("...archive slot");

                                slot.setStatus(Status.ARCHIVED);

                                evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);

                                break;
                            case PENDING:
                                switch (slot.getPendingStatus()) {
                                case ACTIVE:
                                    LOGGER.info("...reactivate slot");

                                    slot.setStatus(Status.ACTIVE);
                                    slot.setValidTo(RCCalendar
                                            .getPeriodBySetable(slot.getLocation().toString(), new Date()).getToDate());

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.REACTIVATE_EVENT);
                                    break;
                                case ARCHIVED:
                                    LOGGER.info("...archive slot");

                                    slot.setStatus(Status.ARCHIVED);

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                                    break;
                                case FREE:
                                    LOGGER.info("...delete slot.");

                                    mgr.delete(slot);

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.DELETE_EVENT);
                                    evt.put(SlotManager.SLOT_TYPE, slot);
                                    MCREventManager.instance().handleEvent(evt);
                                    continue;
                                case RESERVED:
                                    LOGGER.info("...empty slot.");

                                    slot.setStatus(Status.RESERVED);
                                    slot.getEntries().clear();

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.DELETE_EVENT);
                                    break;
                                case VALIDATING:
                                default:
                                    save = false;
                                }
                            }

                            if (save) {
                                mgr.setSlot(slot);
                                mgr.saveOrUpdate(slot);

                                if (evt != null) {
                                    evt.put(SlotManager.SLOT_TYPE, slot);
                                    MCREventManager.instance().handleEvent(evt);
                                }

                                continue;
                            }
                        } else if (slot.getStatus() == Status.ACTIVE) {
                            final Warning pWarning = period.getWarning(today);

                            if (pWarning != null) {
                                final WarningDate sWarning = slot.hasWarningDate(pWarning.getWarningDate()) ? null
                                        : new WarningDate(pWarning.getWarningDate());

                                if (sWarning != null) {
                                    LOGGER.info("...add warning");

                                    slot.addWarningDate(sWarning);
                                    mgr.saveOrUpdate(slot);

                                    final StringBuilder uri = new StringBuilder();

                                    uri.append("xslStyle:" + pWarning.getTemplate());
                                    uri.append("?warningDate=" + sWarning.getWarningDate());
                                    uri.append(":notnull:slot:");
                                    uri.append("slotId=" + slot.getSlotId());

                                    LOGGER.info("...send mail");
                                    MailQueue.addJob(uri.toString());

                                    continue;
                                }
                            }
                        }

                        LOGGER.info("...nothing to do.");
                    } catch (IllegalArgumentException | ParseException | CloneNotSupportedException
                            | MCRPersistenceException | MCRActiveLinkException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
    }
}

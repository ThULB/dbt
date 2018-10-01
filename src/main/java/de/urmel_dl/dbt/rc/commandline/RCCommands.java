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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRVFSContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;
import org.xml.sax.SAXParseException;

import de.urmel_dl.dbt.common.MailQueue;
import de.urmel_dl.dbt.rc.datamodel.PendingStatus;
import de.urmel_dl.dbt.rc.datamodel.Period;
import de.urmel_dl.dbt.rc.datamodel.RCCalendar;
import de.urmel_dl.dbt.rc.datamodel.Status;
import de.urmel_dl.dbt.rc.datamodel.Warning;
import de.urmel_dl.dbt.rc.datamodel.WarningDate;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry.FileEntryProcessingException;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.OPCRecordEntry;
import de.urmel_dl.dbt.rc.persistency.FileEntryManager;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "RC Commands")
public class RCCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(RCCommands.class);

    @SuppressWarnings("unchecked")
    @MCRCommand(syntax = "export slot {0} to directory {1}",
        help = "exports rc slot with id {0} to given directory {1}")
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

            final MIRAccessKeyPair accKP = MIRAccessKeyManager.getKeyPair(slot.getMCRObjectID());
            if (accKP != null) {
                slot.setReadKey(accKP.getReadKey());
                slot.setWriteKey(accKP.getWriteKey());
            }

            File xmlOutput = new File(dir, "slot-" + slotId + ".xml");
            new MCRJDOMContent(new EntityFactory<>(slot).toDocument()).sendTo(xmlOutput);
            LOGGER.info("Slot " + slotId + " saved to " + xmlOutput.getCanonicalPath() + ".");

            if (slot.getEntries() != null) {
                for (SlotEntry<?> entry : slot.getEntries()) {
                    if (entry.getEntry() instanceof FileEntry) {
                        SlotEntry<FileEntry> slotEntry = (SlotEntry<FileEntry>) entry;

                        File fileDir = new File(dir, slotEntry.getId());
                        if (fileDir.isDirectory() || fileDir.mkdirs()) {
                            try {
                                FileEntryManager.retrieve(slot, slotEntry);
                                File f = new File(fileDir, slotEntry.getEntry().getName());
                                MCRContent content = slotEntry.getEntry().getExportableContent(entry.getId());
                                content.sendTo(f);
                                LOGGER.info("File \"" + slotEntry.getEntry().getName() + "\" saved to "
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
            List<String> cmds = new ArrayList<>(slotList.getSlots().size());
            for (final Slot slot : slotList.getSlots()) {
                File slotDir = new File(dir, slot.getSlotId());
                if (slotDir.isDirectory() || slotDir.mkdirs()) {
                    String command = new MessageFormat("export slot {0} to directory {1}", Locale.ROOT)
                        .format(new Object[] { slot.getSlotId(),
                            slotDir.getAbsolutePath() });
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
        throws IOException, MCRActiveLinkException, MCRAccessException, MCRException, SAXParseException {
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

        final Slot slot = new EntityFactory<>(Slot.class)
            .fromDocument(MCRXMLParserFactory.getParser(false).parseXML(new MCRVFSContent(file.toURI())));
        final Slot oldSlot = mgr.getSlotById(slot.getSlotId());

        boolean update = oldSlot != null;
        if (update) {
            slot.setMCRObjectID(oldSlot.getMCRObjectID());
        }

        if (slot.getEntries() != null) {
            for (SlotEntry<?> entry : slot.getEntries()) {
                if (entry.getEntry() instanceof FileEntry) {
                    final SlotEntry<FileEntry> slotEntry = (SlotEntry<FileEntry>) entry;
                    final FileEntry fileEntry = ((SlotEntry<FileEntry>) entry).getEntry();

                    File f = new File(file.getParent(), entry.getId() + File.separator + fileEntry.getName());
                    if (f.isFile()) {
                        InputStream is = null;
                        try {
                            is = new FileInputStream(f);
                            slotEntry.setEntry(FileEntry.createFileEntry(entry.getId(), fileEntry.getName(),
                                fileEntry.getComment(), fileEntry.isCopyrighted(), is));
                        } catch (FileNotFoundException e) {
                            LOGGER.error("Couldn't not read file \"" + f.getCanonicalPath() + "\" for file entry.");
                            return;
                        } catch (FileEntryProcessingException e) {
                            LOGGER.error("File processing returns error code " + e.getErrorCode() + " for file \""
                                + f.getCanonicalPath() + "\".");
                            return;
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }

                        if (update) {
                            LOGGER.info(
                                "Update File \"" + fileEntry.getName() + "\" from " + f.getCanonicalPath() + ".");
                            FileEntryManager.update(slot, slotEntry);
                        }
                    } else {
                        LOGGER.error("Couldn't find file for file entry.");
                        return;
                    }
                }
            }
        }

        MCREvent evt = null;

        if (slot.getPendingStatus() == PendingStatus.OWNERTRANSFER) {
            evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.OWNER_TRANSFER_EVENT);
            // rebuild new keys
            String readKey = SlotManager.buildKey();
            String writeKey = null;
            // rebuild write key if match with read key
            while ((writeKey = SlotManager.buildKey()).equals(readKey)) {
                ;
            }

            slot.setReadKey(readKey);
            slot.setWriteKey(writeKey);
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

        if (evt != null) {
            evt.put(SlotManager.SLOT_TYPE, slot);
            MCREventManager.instance().handleEvent(evt);
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

        List<String> cmds = new ArrayList<>();
        for (final String r : list) {
            final File fr = new File(dir, r);
            if (fr.isDirectory()) {
                for (final String c : fr.list()) {
                    if (c.endsWith(".xml") && c.contains("slot")) {
                        String command = new MessageFormat("import slot from file {0}", Locale.ROOT)
                            .format(new Object[] { new File(fr, c).getAbsolutePath() });
                        cmds.add(command);
                    }
                }
            }
        }
        return cmds;
    }

    @MCRCommand(syntax = "rc inactivator",
        help = "send warning mails for reserve collections or inactivate, set new status")
    public static void rcInactivator() throws IOException, MCRAccessException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        if (!slotList.getSlots().isEmpty()) {
            for (int i = 0; i < slotList.getSlots().size(); i++) {
                final Slot slot = slotList.getSlots().get(i);

                try {
                    MCREvent evt = null;

                    if (slot.isActive() || Status.PENDING.equals(slot.getStatus())) {
                        final Date today = new Date();
                        final Date validTo = slot.getValidToAsDate();
                        final Period period = RCCalendar.getPeriod(slot.getLocation().toString(), validTo);

                        if (today.after(validTo)) {
                            boolean save = true;

                            switch (slot.getStatus()) {
                                case ARCHIVED:
                                case FREE:
                                case RESERVED:
                                    save = false;
                                    break;
                                case ACTIVE:
                                    LOGGER.info("archive slot with id \"" + slot.getSlotId() + "\"");

                                    slot.setStatus(Status.ARCHIVED);

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);

                                    break;
                                case PENDING:
                                    switch (slot.getPendingStatus()) {
                                        case ACTIVE:
                                            LOGGER.info("reactivate slot with id \"" + slot.getSlotId() + "\"");

                                            slot.setStatus(Status.ACTIVE);
                                            slot.setValidTo(RCCalendar
                                                .getPeriodBySetable(slot.getLocation().toString(), new Date())
                                                .getToDate());

                                            evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.REACTIVATE_EVENT);
                                            break;
                                        case ARCHIVED:
                                            LOGGER.info("archive slot with id \"" + slot.getSlotId() + "\"");

                                            slot.setStatus(Status.ARCHIVED);

                                            evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                                            break;
                                        case FREE:
                                            if (slot.isOnlineOnly() || slot.getEntries() == null ||
                                                slot.getEntries().stream()
                                                    .filter(se -> se.getEntry() instanceof OPCRecordEntry
                                                        && ((OPCRecordEntry) se.getEntry()).getEPN() != null)
                                                    .count() == 0) {
                                                LOGGER.info("delete slot with id \"" + slot.getSlotId() + "\"");
                                                mgr.delete(slot);
                                                evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.DELETE_EVENT);
                                            } else {
                                                evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                                            }

                                            evt.put(SlotManager.SLOT_TYPE, slot);
                                            MCREventManager.instance().handleEvent(evt);
                                            continue;
                                        case RESERVED:
                                            LOGGER.info("reserve slot with id \"" + slot.getSlotId() + "\"");

                                            slot.setStatus(Status.RESERVED);
                                            slot.getEntries().clear();

                                            evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.DELETE_EVENT);
                                            break;
                                        case VALIDATING:
                                        default:
                                            save = false;
                                    }
                                    break;
                                default:
                                    save = false;
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
                                    LOGGER.info("Add warning to slot with id \"" + slot.getSlotId() + "\"...");

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
                    } else if (slot.getStatus() == Status.FREE) {
                        LOGGER.info("delete slot with id \"" + slot.getSlotId() + "\"");

                        mgr.delete(slot);

                        evt = new MCREvent(SlotManager.SLOT_TYPE, MCREvent.DELETE_EVENT);
                        evt.put(SlotManager.SLOT_TYPE, slot);
                        MCREventManager.instance().handleEvent(evt);
                        continue;
                    }
                } catch (IllegalArgumentException | ParseException | CloneNotSupportedException
                    | MCRPersistenceException | MCRActiveLinkException e) {
                    LOGGER.error(e.getMessage());
                }
            }

            mgr.syncList();
        }
    }

    @MCRCommand(syntax = "sync slot list", help = "sync slot list")
    public static void syncSlotList() throws IOException {
        final SlotManager mgr = SlotManager.instance();
        mgr.syncList();
    }

    @MCRCommand(syntax = "rc resend mails {0}", help = "resend mails for reserve collections")
    public static void rcResendMails(String slotPrefix) throws IOException, MCRAccessException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        if (!slotList.getSlots().isEmpty()) {
            for (int i = 0; i < slotList.getSlots().size(); i++) {
                final Slot slot = slotList.getSlots().get(i);

                MCREvent evt = null;

                if ((slot.getStatus() == Status.ARCHIVED)
                    && (slotPrefix == null || slot.getSlotId().startsWith(slotPrefix))) {
                    LOGGER.info("resend mails for archived slot " + slot.getSlotId());
                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                }

                if (evt != null) {
                    evt.put(SlotManager.SLOT_TYPE, slot);
                    MCREventManager.instance().handleEvent(evt);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @MCRCommand(syntax = "rc stats", help = "build stats of fileentries for rc")
    public static void rcStats() {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        if (!slotList.getSlots().isEmpty()) {
            Map<Slot, List<SlotEntry<?>>> feMap = slotList.getSlots().stream()
                .filter(s -> s.getStatus() == Status.ACTIVE)
                .collect(Collectors.toMap(s -> s,
                    s -> Optional.ofNullable(s.getEntries()).orElse(Collections.emptyList()).stream()
                        .filter(e -> e.getEntry().getClass() == FileEntry.class)
                        .collect(Collectors.toList())));

            final AtomicInteger total = new AtomicInteger();
            final AtomicInteger copyTotal = new AtomicInteger();

            feMap.entrySet().stream().forEach(es -> {
                Slot slot = es.getKey();
                System.out.println(slot.getSlotId() + " : " + slot.getTitle() + " / "
                    + slot.getLecturers().stream().map(l -> l.getName()).collect(Collectors.joining("; ")));

                es.getValue().stream().collect(Collectors.groupingBy(e -> {
                    String name = ((SlotEntry<FileEntry>) e).getEntry().getName();
                    return name.lastIndexOf(".") != -1 ? name.substring(name.lastIndexOf(".")).toLowerCase(Locale.ROOT)
                        : name;
                })).forEach((ext, se) -> {
                    long copy = se.stream().filter(e -> ((SlotEntry<FileEntry>) e).getEntry().isCopyrighted())
                        .count();
                    System.out.println("\t" + ext + ": " + copy + "/" + se.size());
                    total.addAndGet(se.size());
                    copyTotal.addAndGet((int) copy);
                });
            });

            System.out.println("Count copyrighted files (total): " + copyTotal.get());
            System.out.println("Count files (total): " + total.get());
        }
    }
}

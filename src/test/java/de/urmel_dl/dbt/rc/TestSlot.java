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
package de.urmel_dl.dbt.rc;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.datamodel.classifications2.utils.MCRXMLTransformer;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs2.MCRStoreCenter;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.test.MCRJPAExtension;
import org.mycore.test.MCRJPATestHelper;
import org.mycore.test.MCRMetadataExtension;
import org.mycore.test.MyCoReTest;
import org.xml.sax.SAXException;

import de.urmel_dl.dbt.rc.datamodel.Lecturer;
import de.urmel_dl.dbt.rc.datamodel.PendingStatus;
import de.urmel_dl.dbt.rc.datamodel.Status;
import de.urmel_dl.dbt.rc.datamodel.TypedDate;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.HeadlineEntry;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.rc.utils.SlotWrapper;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * The {@link Slot} test cases.
 *
 * @author René Adler (eagle)
 *
 */
@MyCoReTest
@ExtendWith(MCRJPAExtension.class)
@ExtendWith(MCRMetadataExtension.class)
public class TestSlot {

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    private static SlotManager SLOT_MANAGER;

    @BeforeEach
    public void setUp() throws Exception {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.setCurrentIP("127.0.0.1");
        session.setUserInformation(MCRSystemUserInformation.SUPER_USER);

        // Clears all stores
        MCRStoreCenter.getInstance().clear();

        if (SLOT_MANAGER == null) {
            SLOT_MANAGER = SlotManager.instance();
        }
        SLOT_MANAGER.getSlotList().getSlots().clear();

        Document xml = new Document(MCRURIResolver.obtainInstance().resolve("resource:setup/classifications/RCLOC.xml"));
        MCRCategory category = MCRXMLTransformer.getCategory(xml);
        DAO.addCategory(null, category);
    }

    @Test
    public void testSlotTransform() throws IOException, ParseException {
        Slot slot = new Slot("3400.01.01.0001");

        slot.setStatus(Status.ACTIVE);
        slot.setPendingStatus(PendingStatus.ARCHIVED);
        slot.setOnlineOnly(false);

        slot.setValidTo(new SimpleDateFormat(TypedDate.LONG_DATE_FORMAT, Locale.ROOT).parse("31.03.2015 00:00:00"));

        slot.addWarningDate(new Date());

        slot.setReadKey("blah");
        slot.setWriteKey("blub");

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        slot.addWarningDate(cal.getTime());

        Assertions.assertEquals(1, slot.getWarningDates().get(1).compareTo(slot.getWarningDates().get(0)));

        Lecturer lecturer = new Lecturer();
        lecturer.setName("Mustermann, Max");
        lecturer.setEmail("max.mustermann@muster.de");
        lecturer.setOrigin("0815");

        slot.addLecturer(lecturer);

        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot.addEntry(slotEntry);

        Assertions.assertEquals(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01"), slot.getLocation());

        Document xml = new EntityFactory<>(slot).toDocument();

        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);

        Slot transSlot = new EntityFactory<>(Slot.class).fromElement(xml.getRootElement());

        Assertions.assertEquals(slot.getReadKey(), transSlot.getReadKey());
        Assertions.assertEquals(slot.getWriteKey(), transSlot.getWriteKey());
    }

    @Test
    public void testSlotListTransform() throws IOException, ParseException {
        Slot slot = new Slot("3400.01.01.0001");

        slot.setStatus(Status.ACTIVE);
        slot.setPendingStatus(PendingStatus.ARCHIVED);
        slot.setOnlineOnly(false);

        slot.setValidTo(new SimpleDateFormat(TypedDate.LONG_DATE_FORMAT, Locale.ROOT).parse("31.03.2015 00:00:00"));

        slot.addWarningDate(new Date());

        Lecturer lecturer = new Lecturer();
        lecturer.setName("Mustermann, Max");
        lecturer.setEmail("max.mustermann@muster.de");
        lecturer.setOrigin("0815");

        slot.addLecturer(lecturer);

        SlotList slotList = new SlotList();
        slotList.addSlot(slot);

        Document xSL = new EntityFactory<>(slotList).toDocument();
        new XMLOutputter(Format.getPrettyFormat()).output(xSL, System.out);

        Assertions.assertNotNull(xSL);
    }

    @Test
    public void testActiveSlotListTransform() throws IOException {
        Slot slot1 = new Slot("3400.01.01.0001");

        slot1.setStatus(Status.ACTIVE);

        Lecturer lecturer = new Lecturer();
        lecturer.setName("Mustermann, Max");
        lecturer.setEmail("max.mustermann@muster.de");
        lecturer.setOrigin("0815");

        slot1.addLecturer(lecturer);

        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot1.addEntry(slotEntry);

        SLOT_MANAGER.addSlot(slot1);

        Slot slot2 = new Slot("3400.01.01.0002");

        slot2.setStatus(Status.ARCHIVED);

        slot2.addLecturer(lecturer);

        SLOT_MANAGER.addSlot(slot2);

        Assertions.assertEquals(2, SLOT_MANAGER.getSlotList().getSlots().size());

        SlotList activeSlots = SLOT_MANAGER.getSlotList().getActiveSlots();

        Assertions.assertEquals(1, activeSlots.getSlots().size());
        Assertions.assertNull(activeSlots.getSlots().getFirst().getEntries());

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(activeSlots).toDocument(), System.out);
    }

    @Test
    public void testGetNextFreeId() throws IOException {
        Slot slot1 = new Slot("3400.01.01.0001");
        slot1.setStatus(Status.ACTIVE);
        SLOT_MANAGER.addSlot(slot1);

        Slot slot2 = new Slot("3400.01.01.0002");
        slot2.setStatus(Status.FREE);
        SLOT_MANAGER.addSlot(slot2);

        Assertions.assertEquals(3,
            SLOT_MANAGER.getNextFreeId(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01")));

        Assertions.assertEquals(1,
            SLOT_MANAGER.getNextFreeId(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "0027.01.01")));
    }

    @Test
    public void testGetSlotById() throws IOException {
        Slot slot1 = new Slot("3400.01.01.0001");
        slot1.setStatus(Status.ACTIVE);
        SLOT_MANAGER.addSlot(slot1);

        Slot slot2 = new Slot("3400.01.01.0002");
        slot2.setStatus(Status.FREE);
        SLOT_MANAGER.addSlot(slot2);

        Slot found = SLOT_MANAGER.getSlotById("3400.01.01.0001");

        Assertions.assertNotNull(found);
        Assertions.assertEquals(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01"), found.getLocation());
    }

    @Test
    public void testSaveSlot()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = activeSlot();

        SLOT_MANAGER.saveOrUpdate(slot);

        Assertions.assertNotNull(slot.getMCRObjectID());

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(slot.getMCRObjectID());

        Assertions.assertNotNull(obj);

        Slot ts = SlotWrapper.unwrapMCRObject(obj);

        Assertions.assertEquals(slot.getSlotId(), ts.getSlotId());

        Assertions.assertEquals(slot.getEntries().getFirst().getId(), ts.getEntries().getFirst().getId());
    }

    @Test
    public void testSaveSlotWithFileEntry()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = slotWithFileEntry();

        SLOT_MANAGER.saveOrUpdate(slot);

        Assertions.assertNotNull(slot.getMCRObjectID());

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(slot.getMCRObjectID());

        Assertions.assertNotNull(obj);

        Slot ts = SlotWrapper.unwrapMCRObject(obj);

        Assertions.assertEquals(slot.getSlotId(), ts.getSlotId());

        Assertions.assertEquals(slot.getEntries().getFirst().getId(), ts.getEntries().getFirst().getId());
    }

    @Test
    public void testDeleteSlot()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = activeSlot();

        SLOT_MANAGER.saveOrUpdate(slot);

        MCRJPATestHelper.startNewTransaction();

        SLOT_MANAGER.delete(slot);

        Assertions.assertNull(SLOT_MANAGER.getSlotById(slot.getSlotId()));
    }

    @Test
    public void testDeleteSlotWithFileEntry()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = slotWithFileEntry();

        SLOT_MANAGER.saveOrUpdate(slot);

        MCRJPATestHelper.startNewTransaction();

        SLOT_MANAGER.delete(slot);

        Assertions.assertNull(SLOT_MANAGER.getSlotById(slot.getSlotId()));
    }

    @Test
    public void testSaveSlotList()
        throws IOException, MCRPersistenceException, MCRActiveLinkException, JDOMException, SAXException,
        MCRAccessException {
        Slot slot1 = new Slot("3400.01.01.0001");
        slot1.setStatus(Status.ACTIVE);
        SLOT_MANAGER.addSlot(slot1);
        SLOT_MANAGER.saveOrUpdate(slot1);

        Slot slot2 = new Slot("3400.01.01.0002");
        slot2.setStatus(Status.FREE);
        SLOT_MANAGER.addSlot(slot2);
        SLOT_MANAGER.saveOrUpdate(slot2);

        Assertions.assertNotNull(slot1.getMCRObjectID());
        Assertions.assertNotNull(slot2.getMCRObjectID());

        SLOT_MANAGER.getSlotList().getSlots().clear();
        Assertions.assertEquals(0, SLOT_MANAGER.getSlotList().getSlots().size());

        SLOT_MANAGER.loadList();
        Assertions.assertEquals(2, SLOT_MANAGER.getSlotList().getSlots().size());
    }

    @Test
    public void testSlotEntries() {
        Slot slot = new Slot("3400.01.01.0001");
        slot.setStatus(Status.ACTIVE);

        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot.addEntry(slotEntry);

        SlotEntry<?> entry = slot.getEntryById(slotEntry.getId());

        Assertions.assertEquals(slotEntry.getId(), entry.getId());

        ((HeadlineEntry) entry.getEntry()).setText("Neue Überschrift");

        slot.setEntry(entry);

        Assertions.assertEquals("Neue Überschrift",
            ((HeadlineEntry) slot.getEntryById(slotEntry.getId()).getEntry()).getText());
    }

    @Test
    public void testSlotEntryRemove() {
        Slot slot = new Slot("3400.01.01.0001");
        slot.setStatus(Status.ACTIVE);

        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot.addEntry(slotEntry);

        SlotEntry<?> entry = slot.getEntryById(slotEntry.getId());

        Assertions.assertEquals(slotEntry.getId(), entry.getId());
        Assertions.assertEquals(1, slot.getEntries().size());

        Assertions.assertTrue(slot.removeEntry(slotEntry), "slot entry remove");
        Assertions.assertEquals(0, slot.getEntries().size());
    }

    private Slot activeSlot() {
        Slot slot = new Slot("3400.01.01.0001");
        slot.setTitle("Test ESA");
        slot.setStatus(Status.ACTIVE);
        slot.setValidTo(new Date());

        slot.setReadKey("blah");
        slot.setWriteKey("blub");

        Lecturer lecturer = new Lecturer();
        lecturer.setName("Mustermann, Max");
        lecturer.setEmail("max.mustermann@muster.de");
        lecturer.setOrigin("0815");

        slot.addLecturer(lecturer);

        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot.addEntry(slotEntry);
        return slot;
    }

    private Slot slotWithFileEntry() throws IOException {
        Slot slot = new Slot("3400.01.01.0001");
        slot.setTitle("Test ESA");
        slot.setStatus(Status.ACTIVE);
        slot.setValidTo(new Date());

        Lecturer lecturer = new Lecturer();
        lecturer.setName("Mustermann, Max");
        lecturer.setEmail("max.mustermann@muster.de");
        lecturer.setOrigin("0815");

        slot.addLecturer(lecturer);

        SlotEntry<FileEntry> slotEntry = new SlotEntry<>();

        FileEntry fileEntry = new FileEntry();

        fileEntry.setName("mycore.properties");
        fileEntry.setComment("This is a comment!");
        fileEntry.setContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("mycore.properties"));

        slotEntry.setEntry(fileEntry);

        slot.addEntry(slotEntry);
        return slot;
    }
}

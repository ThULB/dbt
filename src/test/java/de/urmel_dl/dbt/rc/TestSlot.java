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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJPATestCase;
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
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestSlot extends MCRJPATestCase {

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    private static SlotManager SLOT_MANAGER;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();
        config.set("MCR.datadir", folder.newFolder("data").getAbsolutePath());

        MCRSession session = MCRSessionMgr.getCurrentSession();
        session.setCurrentIP("127.0.0.1");
        session.setUserInformation(MCRSystemUserInformation.getSuperUserInstance());

        // Clears all stores
        MCRStoreCenter.instance().clear();

        if (SLOT_MANAGER == null) {
            SLOT_MANAGER = SlotManager.instance();
        }
        SLOT_MANAGER.getSlotList().getSlots().clear();

        Document xml = new Document(MCRURIResolver.instance().resolve("resource:setup/classifications/RCLOC.xml"));
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

        assertEquals(1, slot.getWarningDates().get(1).compareTo(slot.getWarningDates().get(0)));

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

        assertEquals(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01"), slot.getLocation());

        Document xml = new EntityFactory<>(slot).toDocument();

        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);

        Slot transSlot = new EntityFactory<>(Slot.class).fromElement(xml.getRootElement());

        assertEquals(slot.getReadKey(), transSlot.getReadKey());
        assertEquals(slot.getWriteKey(), transSlot.getWriteKey());
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

        assertNotNull(xSL);
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

        assertEquals(2, SLOT_MANAGER.getSlotList().getSlots().size());

        SlotList activeSlots = SLOT_MANAGER.getSlotList().getActiveSlots();

        assertEquals(1, activeSlots.getSlots().size());
        assertNull(activeSlots.getSlots().get(0).getEntries());

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

        assertEquals(3, SLOT_MANAGER.getNextFreeId(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01")));

        assertEquals(1, SLOT_MANAGER.getNextFreeId(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "0027.01.01")));
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

        assertNotNull(found);
        assertEquals(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, "3400.01.01"), found.getLocation());
    }

    @Test
    public void testSaveSlot()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = activeSlot();

        SLOT_MANAGER.saveOrUpdate(slot);

        assertNotNull(slot.getMCRObjectID());

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(slot.getMCRObjectID());

        assertNotNull(obj);

        Slot ts = SlotWrapper.unwrapMCRObject(obj);

        assertEquals(slot.getSlotId(), ts.getSlotId());

        assertEquals(slot.getEntries().get(0).getId(), ts.getEntries().get(0).getId());
    }

    @Test
    public void testSaveSlotWithFileEntry()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = slotWithFileEntry();

        SLOT_MANAGER.saveOrUpdate(slot);

        assertNotNull(slot.getMCRObjectID());

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(slot.getMCRObjectID());

        assertNotNull(obj);

        Slot ts = SlotWrapper.unwrapMCRObject(obj);

        assertEquals(slot.getSlotId(), ts.getSlotId());

        assertEquals(slot.getEntries().get(0).getId(), ts.getEntries().get(0).getId());
    }

    @Test
    public void testDeleteSlot()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = activeSlot();

        SLOT_MANAGER.saveOrUpdate(slot);

        startNewTransaction();

        SLOT_MANAGER.delete(slot);

        assertNull(SLOT_MANAGER.getSlotById(slot.getSlotId()));
    }

    @Test
    public void testDeleteSlotWithFileEntry()
        throws IOException, JDOMException, SAXException, MCRPersistenceException, MCRActiveLinkException,
        MCRAccessException {
        Slot slot = slotWithFileEntry();

        SLOT_MANAGER.saveOrUpdate(slot);

        startNewTransaction();

        SLOT_MANAGER.delete(slot);

        assertNull(SLOT_MANAGER.getSlotById(slot.getSlotId()));
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

        assertNotNull(slot1.getMCRObjectID());
        assertNotNull(slot2.getMCRObjectID());

        SLOT_MANAGER.getSlotList().getSlots().clear();
        assertEquals(0, SLOT_MANAGER.getSlotList().getSlots().size());

        SLOT_MANAGER.loadList();
        assertEquals(2, SLOT_MANAGER.getSlotList().getSlots().size());
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

        assertEquals(slotEntry.getId(), entry.getId());

        ((HeadlineEntry) entry.getEntry()).setText("Neue Überschrift");

        slot.setEntry(entry);

        assertEquals("Neue Überschrift", ((HeadlineEntry) slot.getEntryById(slotEntry.getId()).getEntry()).getText());
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

        assertEquals(slotEntry.getId(), entry.getId());
        assertEquals(1, slot.getEntries().size());

        assertTrue("slot entry remove", slot.removeEntry(slotEntry));
        assertEquals(0, slot.getEntries().size());
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

/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.rc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
import de.urmel_dl.dbt.rc.resources.RCResource;
import de.urmel_dl.dbt.rest.utils.EntityMessageBodyWriter;
import de.urmel_dl.dbt.test.JerseyTestCase;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestRCResource extends JerseyTestCase {

    private static final String TEST_SLOT_ID = "0027.01.01.0001";

    private static SlotManager slotMgr;

    private WebTarget webResource;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    protected Application configure() {
        return new ResourceConfig(RCResource.class, EntityMessageBodyWriter.class);
    }

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();

        if (slotMgr == null) {
            slotMgr = SlotManager.instance();
        }

        slotMgr.getSlotList().getSlots().clear();
        webResource = target();
    }

    @Test
    public void testSlotList() throws ParseException {
        Slot slot = createSlot();
        assertNotNull(slot);

        slotMgr.addSlot(slot);

        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            String response = webResource.path("rc/").request(mt).get(String.class);
            assertNotNull(response);
            SlotList slotList = new EntityFactory<>(SlotList.class).unmarshalByMediaType(response, mt);
            assertNotNull(slotList);
            assertEquals(1, slotList.getTotal());
        });
    }

    @Test
    public void testSlot() throws ParseException {
        Slot slot = createSlot();
        assertNotNull(slot);

        slotMgr.addSlot(slot);

        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            String response = webResource.path("rc/slot/" + TEST_SLOT_ID).request(mt).get(String.class);
            assertNotNull(response);

            Slot s = new EntityFactory<>(Slot.class).unmarshalByMediaType(response, mt);
            assertNotNull(s);
        });
    }

    @Test
    public void testEntry() throws ParseException {
        Slot slot = createSlot();
        assertNotNull(slot);

        slotMgr.addSlot(slot);

        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            String response = webResource.path("rc/slot/" + TEST_SLOT_ID + "/entry/1234567").request(mt)
                .get(String.class);
            assertNotNull(response);

            SlotEntry<?> entry = new EntityFactory<>(SlotEntry.class).unmarshalByMediaType(response, mt);
            assertNotNull(entry);
        });
    }

    @Test
    public void testFileEntry() throws ParseException, IOException {
        Slot slot = createSlot();

        SlotEntry<FileEntry> slotEntry = new SlotEntry<>();

        FileEntry fileEntry = new FileEntry();

        fileEntry.setName("mycore.properties");
        fileEntry.setComment("This is a comment!");
        fileEntry.setContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("mycore.properties"));

        slotEntry.setEntry(fileEntry);

        slot.addEntry(slotEntry);

        assertNotNull(slot);

        slotMgr.addSlot(slot);

        String response = webResource.path("rc/slot/" + TEST_SLOT_ID + "/file/" + slotEntry.getId()).request()
            .get(String.class);

        assertNotNull(response);
        assertTrue(0 != response.length());
    }

    private Slot createSlot() throws ParseException {
        Slot slot = new Slot(TEST_SLOT_ID);

        slot.setStatus(Status.ACTIVE);
        slot.setPendingStatus(PendingStatus.ARCHIVED);
        slot.setOnlineOnly(false);

        slot.setValidTo(new SimpleDateFormat(TypedDate.LONG_DATE_FORMAT, Locale.ROOT).parse("31.03.2017 00:00:00"));

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
        slotEntry.setId("1234567");

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        slot.addEntry(slotEntry);

        return slot;
    }
}

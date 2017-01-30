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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntryTypes;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.HeadlineEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.MCRObjectEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.OPCRecordEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.TextEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.WebLinkEntry;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestSlotEntry extends MCRTestCase {

    private SlotEntry<HeadlineEntry> newHeadLineEntry() {
        SlotEntry<HeadlineEntry> slotEntry = new SlotEntry<>();

        HeadlineEntry headline = new HeadlineEntry();
        headline.setText("Überschrift");

        slotEntry.setEntry(headline);

        return slotEntry;
    }

    private SlotEntry<MCRObjectEntry> newMCRObjectEntry() {
        SlotEntry<MCRObjectEntry> slotEntry = new SlotEntry<>();

        MCRObjectEntry mcrObjectEntry = new MCRObjectEntry();
        mcrObjectEntry.setId("mir_mods_00000001");
        mcrObjectEntry.setComment("Comment!");

        slotEntry.setEntry(mcrObjectEntry);

        return slotEntry;
    }

    private SlotEntry<TextEntry> newPlainTextEntry() {
        SlotEntry<TextEntry> slotEntry = new SlotEntry<>();

        TextEntry text = new TextEntry();
        text.setFormat(TextEntry.Format.PLAIN);
        text.setText("Hello World!\nHello Test Case!");

        slotEntry.setEntry(text);

        return slotEntry;
    }

    private SlotEntry<TextEntry> newHtmlTextEntry() {
        SlotEntry<TextEntry> slotEntry = new SlotEntry<>();

        TextEntry text = new TextEntry();
        text.setFormat(TextEntry.Format.HTML);
        text.setText("Hello World!<br>Hello Test Case!");

        slotEntry.setEntry(text);

        return slotEntry;
    }

    private SlotEntry<WebLinkEntry> newWebLinkEntry() {
        SlotEntry<WebLinkEntry> slotEntry = new SlotEntry<>();

        WebLinkEntry link = new WebLinkEntry();
        link.setURL("http://www.test.de");
        link.setLabel("Link Text");

        slotEntry.setEntry(link);

        return slotEntry;
    }

    private SlotEntry<OPCRecordEntry> newOPCRecordEntry() throws Exception {
        SlotEntry<OPCRecordEntry> slotEntry = new SlotEntry<>();

        OPCRecordEntry opcEntry = new OPCRecordEntry();

        OPCConnector opc = new OPCConnector("http://opac.lbs-ilmenau.gbv.de", "1");
        opcEntry.setEPN("1508496013");
        opcEntry.setRecord(opc.getRecord("785761829"));
        opcEntry.setComment("This is a comment!");

        slotEntry.setEntry(opcEntry);

        return slotEntry;
    }

    private SlotEntry<FileEntry> newFileEntry() throws IOException {
        SlotEntry<FileEntry> slotEntry = new SlotEntry<>();

        FileEntry fileEntry = new FileEntry();
        fileEntry.setName("test.txt");
        fileEntry.setCopyrighted(false);
        fileEntry.setComment("This is a comment!");
        fileEntry.setContent(Thread.currentThread().getContextClassLoader().getResourceAsStream("mycore.properties"));
        slotEntry.setEntry(fileEntry);

        return slotEntry;
    }

    @Test
    public void testHeadlineEntry() throws IOException {
        SlotEntry<HeadlineEntry> slotEntry = newHeadLineEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testMCRObjectEntry() throws IOException {
        SlotEntry<MCRObjectEntry> slotEntry = newMCRObjectEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testTextEntryPlain() throws IOException {
        SlotEntry<TextEntry> slotEntry = newPlainTextEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testTextEntryHTML() throws IOException {
        SlotEntry<TextEntry> slotEntry = newHtmlTextEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testWebLinkEntry() throws IOException {
        SlotEntry<WebLinkEntry> slotEntry = newWebLinkEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testOPCRecordEntry() throws Exception {
        SlotEntry<OPCRecordEntry> slotEntry = newOPCRecordEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testFileEntry() throws IOException {
        SlotEntry<FileEntry> slotEntry = newFileEntry();
        assertNotNull(slotEntry);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(slotEntry).toDocument(), System.out);
    }

    @Test
    public void testAddSlotEntries() throws Exception {
        Slot slot = new Slot("3400.01.01.0001");

        slot.addEntry(newHeadLineEntry());
        slot.addEntry(newMCRObjectEntry());
        slot.addEntry(newPlainTextEntry());
        slot.addEntry(newHtmlTextEntry());
        slot.addEntry(newWebLinkEntry());
        slot.addEntry(newOPCRecordEntry());
        slot.addEntry(newFileEntry());

        assertEquals(7, slot.getEntries().size());
    }

    @Test
    public void testInsertSlotEntries() throws IOException {
        Slot slot = new Slot("3400.01.01.0001");

        slot.addEntry(newHeadLineEntry());
        slot.addEntry(newMCRObjectEntry());

        SlotEntry<?> pText = newPlainTextEntry();
        slot.addEntry(pText);

        slot.addEntry(newWebLinkEntry());

        assertNotNull(pText.getId());
        assertEquals(4, slot.getEntries().size());

        SlotEntry<?> hText = newHtmlTextEntry();
        slot.addEntry(hText, pText.getId());

        assertEquals(5, slot.getEntries().size());

        int iPT = -1;
        int iHT = -1;

        for (int i = 0; i < slot.getEntries().size(); i++) {
            if (pText.getId().equals(slot.getEntries().get(i).getId())) {
                iPT = i;
            } else if (hText.getId().equals(slot.getEntries().get(i).getId())) {
                iHT = i;
            }
        }

        assertNotEquals(-1, iPT);
        assertNotEquals(-1, iHT);

        assertTrue(iPT < iHT);
    }

    @Test
    public void testSlotEntryTypes() throws IOException {
        SlotEntryTypes entryTypes = SlotEntryTypes.instance();
        assertNotNull(entryTypes);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(entryTypes).toDocument(), System.out);
    }
}

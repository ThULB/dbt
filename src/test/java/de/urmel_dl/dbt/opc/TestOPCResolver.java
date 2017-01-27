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
package de.urmel_dl.dbt.opc;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.xml.MCRURIResolver;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestOPCResolver extends MCRTestCase {

    private final String OPC_URL = "http://opac.lbs-ilmenau.gbv.de";

    private final String OPC_DB = "1";

    @Test
    public void testIKTList() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&iktList");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearch() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&search=papula");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearchWoDB() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&search=papula");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearchIKT() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
            "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&search=papula&ikt=1004");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testFamily() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&family=785761829");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testRecord() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=785761829");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testRecordBasicCopy() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
            "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=785761829&copys=false");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testBarcode() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
            "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&barcode=ILM1$005419999");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testBarcodeBasicCopy() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
            "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&barcode=ILM1$005419999&copys=false");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testPica2Mods() throws Exception {
        Element xml = MCRURIResolver.instance().resolve(
            "xslStyle:opc/transform/pica2mods?RecordIdSource=DE-ILM1"
                + ":opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=729763749");
        assertNotNull(xml);
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }
}

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
package org.urmel.dbt.opc;

import java.io.IOException;
import java.net.URLEncoder;

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
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearch() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&search=papula");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearchWoDB() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&search=papula");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testSearchIKT() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
                "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&search=papula&ikt=1004");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testFamily() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&family=785761829");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testRecord() throws IOException {
        Element xml = MCRURIResolver.instance().resolve("opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=785761829");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testRecordBasicCopy() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
                "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=785761829&copys=false");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testBarcode() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
                "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&barcode=ILM1$005419999");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    @Test
    public void testBarcodeBasicCopy() throws IOException {
        Element xml = MCRURIResolver.instance().resolve(
                "opc:url=" + OPC_URL + "&db=" + OPC_DB + "&barcode=ILM1$005419999&copys=false");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }

    // FIXME not working since MyCoRe commit 32453 (MCRFrontendUtil.getBaseURL)
    // @Test
    public void testPica2Mods() throws Exception {
        Element xml = MCRURIResolver.instance().resolve(
                "xslStyle:pica2mods?RecordIdSource=DEIlm1&PURLPrefix="
                        + URLEncoder.encode("http://service.bibliothek.tu-ilmenau.de/opac.php/search/ppn ", "UTF-8")
                        + ":opc:url=" + OPC_URL + "&db=" + OPC_DB + "&record=729763749");
        new XMLOutputter(Format.getPrettyFormat()).output(xml, System.out);
    }
}

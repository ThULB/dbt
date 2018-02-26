/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2018
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
package de.urmel_dl.dbt.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.content.MCRStreamContent;
import org.mycore.common.events.MCREvent;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.xml.sax.SAXParseException;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestIdentifierExtractorEventHandler extends MCRTestCase {

    @Test
    public void testIdentfierExtract() throws SAXParseException, IOException {
        for (String wantedPPN : Arrays.asList("101233726X", "1012341062", "502025298", "39170155X")) {
            MCRStreamContent sc = new MCRStreamContent(
                getClass().getClassLoader().getResourceAsStream("mods_id_extract_" + wantedPPN + ".xml"));
            MCRObject obj = new MCRObject(sc.asByteArray(), false);

            assertNotNull(obj);

            MCREvent evt = new MCREvent(MCREvent.OBJECT_TYPE, MCREvent.CREATE_EVENT);
            evt.put(MCREvent.OBJECT_KEY, obj);

            IdentifierExtractorEventHandler eh = new IdentifierExtractorEventHandler();
            eh.doHandleEvent(evt);

            new XMLOutputter(Format.getPrettyFormat()).output(obj.createXML(), System.out);

            MCRMODSWrapper mods = new MCRMODSWrapper(obj);
            Optional<String> ppnURI = mods.getElements("mods:identifier[@type='uri']").stream()
                .filter(elm -> elm.getTextTrim().contains("gvk:ppn"))
                .map(Element::getTextTrim).findFirst();

            assertTrue("we should have a ppn URI", ppnURI.isPresent());

            String ppn = ppnURI.map(uri -> uri.substring(uri.indexOf("ppn:") + 4, uri.length())).orElse(null);

            assertEquals("should match", wantedPPN, ppn);
        }
    }

}

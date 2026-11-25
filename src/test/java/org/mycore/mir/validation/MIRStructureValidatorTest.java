/*
 * This file is part of ***  M y C o R e  ***
 * See https://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.mir.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;
import org.mycore.common.MCRConstants;
import org.mycore.test.MyCoReTest;

@MyCoReTest
public class MIRStructureValidatorTest {

    @Test
    public void evaluatesXPathRelativeToStructure() {
        Element structure = new Element("structure")
            .addContent(new Element("parents")
                .addContent(new Element("parent").setAttribute("href", "mir_mods_00000001")))
            .addContent(new Element("children")
                .addContent(new Element("child").setAttribute("href", "mir_mods_00000002")))
            .addContent(new Element("derobjects")
                .addContent(new Element("derobject")
                    .addContent(new Element("classification").setAttribute("categid", "thumbnail"))));

        assertTrue(MIRStructureValidator.test(structure, "count(parents/parent) = 1"));
        assertTrue(MIRStructureValidator.test(structure, "count(children/child) > 0"));
        assertTrue(MIRStructureValidator.test(structure,
            "count(derobjects/derobject[classification/@categid='thumbnail']) = 1"));
        assertFalse(MIRStructureValidator.test(structure, "count(children/child) = 0"));
    }

    @Test
    public void evaluatesEmptyStructure() {
        assertTrue(MIRStructureValidator.test(new Element("structure"), "count(children/child) = 0"));
    }

    @Test
    public void rejectsMalformedXPath() {
        assertFalse(MIRStructureValidator.test(new Element("structure"), "count(children/child"));
    }

    @Test
    public void rejectsInvalidObjectId() {
        assertFalse(MIRStructureValidator.test("not-an-object-id", "count(children/child) = 0"));
    }

    @Test
    public void evaluatesXPathRelativeToMetadata() {
        Element genre = new Element("genre", MCRConstants.MODS_NAMESPACE)
            .setAttribute("type", "intern")
            .setAttribute("valueURI", "http://www.mycore.org/classifications/mir_genres#podcast_series");
        Element metadata = new Element("metadata")
            .addContent(new Element("def.modsContainer")
                .addContent(new Element("modsContainer")
                    .addContent(new Element("mods", MCRConstants.MODS_NAMESPACE).addContent(genre))));

        assertTrue(MIRStructureValidator.test(metadata,
            "def.modsContainer/modsContainer/mods:mods/mods:genre[@type='intern']/@valueURI"
                + " = 'http://www.mycore.org/classifications/mir_genres#podcast_series'"));
        assertFalse(MIRStructureValidator.test(metadata,
            "def.modsContainer/modsContainer/mods:mods/mods:genre[@type='intern']/@valueURI"
                + " = 'http://www.mycore.org/classifications/mir_genres#podcast_episode'"));
    }

    @Test
    public void rejectsMetadataOfInvalidObjectId() {
        assertFalse(MIRStructureValidator.testMetadata("not-an-object-id", "true()"));
    }
}

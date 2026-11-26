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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.mods.events;

import org.jdom2.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.common.MCRConstants;

/**
 * @author Maryam Tohidiyan
 *
 */
public class TestModsNormalizeEventHandler {

    /** A title with a line break, as a browser submits it for a textarea. */
    private static final String SUBMITTED_TITLE = "First part of the title\r\nsecond part of the title";

    private static final String NORMALIZED_TITLE = "First part of the title second part of the title";

    @Test
    public void testLineBreak() {
        Element mods = modsWithTitleInfo("title", SUBMITTED_TITLE);

        Assertions.assertTrue(ModsNormalizeEventHandler.normalizeTitleWhitespace(mods));
        Assertions.assertEquals(NORMALIZED_TITLE, titleText(mods));
    }

    @Test
    public void testTab() {
        Element mods = modsWithTitleInfo("subTitle", "First part\tsecond part");

        Assertions.assertTrue(ModsNormalizeEventHandler.normalizeTitleWhitespace(mods));
        Assertions.assertEquals("First part second part", titleText(mods));
    }

    /**
     * A title without line breaks has to stay untouched, even when it contains multiple spaces.
     */
    @Test
    public void testCleanTitle() {
        Element mods = modsWithTitleInfo("title", " A  clean title ");

        Assertions.assertFalse(ModsNormalizeEventHandler.normalizeTitleWhitespace(mods));
        Assertions.assertEquals(" A  clean title ", titleText(mods));
    }

    private static Element modsWithTitleInfo(String name, String text) {
        Element mods = new Element("mods", MCRConstants.MODS_NAMESPACE);
        mods.addContent(titleInfo(name, text));
        return mods;
    }

    private static Element titleInfo(String name, String text) {
        Element titleInfo = new Element("titleInfo", MCRConstants.MODS_NAMESPACE);
        titleInfo.addContent(new Element(name, MCRConstants.MODS_NAMESPACE).setText(text));
        return titleInfo;
    }

    private static String titleText(Element parent) {
        return parent.getChild("titleInfo", MCRConstants.MODS_NAMESPACE).getChildren().getFirst().getText();
    }

}

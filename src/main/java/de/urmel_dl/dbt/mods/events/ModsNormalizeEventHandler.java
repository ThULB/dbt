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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;

/**
 * Normalizes the MODS metadata of an object before it is stored.
 * This handler has to be registered before {@code org.mycore.datamodel.common.MCRXMLMetadataEventHandler}, which
 * stores the object.
 */
public class ModsNormalizeEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TITLE_INFO = "titleInfo";

    /** Whitespace that does not belong into a title, mainly the CR of a submitted line break. */
    private static final Pattern LINE_BREAK_OR_TAB = Pattern.compile("[\\r\\n\\t]");

    private static final Pattern WHITESPACE_SEQUENCE = Pattern.compile("\\s+");

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        normalize(obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        normalize(obj);
    }

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        normalize(obj);
    }

    private static void normalize(MCRObject obj) {
        if (!MCRMODSWrapper.isSupported(obj)) {
            return;
        }

        Element mods = new MCRMODSWrapper(obj).getMODS();
        if (mods != null && normalize(mods)) {
            LOGGER.info("Normalized mods metadata of {}.", obj.getId());
        }
    }

    /**
     * Applies all normalization rules to the given MODS document.
     */
    public static boolean normalize(Element mods) {
        return normalizeTitleWhitespace(mods);
    }

    /**
     * Replaces line breaks and tabs within the title elements of the document itself by a single space, those
     * are the children of every {@code mods:titleInfo} of {@code mods:mods}.
     */
    public static boolean normalizeTitleWhitespace(Element mods) {
        boolean changed = false;

        for (Element titleElement : titleElements(mods)) {
            String title = titleElement.getText();
            String normalized = normalizeWhitespace(title);
            if (!normalized.equals(title)) {
                titleElement.setText(normalized);
                changed = true;
            }
        }

        return changed;
    }

    private static List<Element> titleElements(Element mods) {
        List<Element> titleElements = new ArrayList<>();
        for (Element titleInfo : mods.getChildren(TITLE_INFO, MCRConstants.MODS_NAMESPACE)) {
            titleElements.addAll(titleInfo.getChildren());
        }
        return titleElements;
    }

    static String normalizeWhitespace(String text) {
        if (!LINE_BREAK_OR_TAB.matcher(text).find()) {
            return text;
        }
        return WHITESPACE_SEQUENCE.matcher(text).replaceAll(" ").trim();
    }

}

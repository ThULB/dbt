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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXPathEvaluator;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Evaluates XPath expressions against the current repository structure of an object.
 */
public final class MIRStructureValidator {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String STRUCTURE_CACHE_ATTRIBUTE = MIRStructureValidator.class.getName() + ".structureCache";

    private MIRStructureValidator() {
    }

    /**
     * Evaluates the given XPath with the object's {@code structure} element as context node.
     *
     * @param objectId the MyCoRe object ID
     * @param xPath an XPath expression relative to the {@code structure} element
     * @return the effective boolean value of the XPath expression
     */
    public static boolean test(String objectId, String xPath) {
        try {
            return test(getStructure(MCRObjectID.getInstance(objectId)), xPath);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to validate structure of {} with XPath {}", objectId, xPath, e);
            return false;
        }
    }

    static boolean test(Element structure, String xPath) {
        return new MCRXPathEvaluator(Map.of(), structure).test(xPath);
    }

    private static Element getStructure(MCRObjectID objectId) {
        return MCRFrontendUtil.getCurrentServletJob()
            .map(job -> getStructure(job.getRequest(), objectId))
            .orElseGet(() -> loadStructure(objectId));
    }

    private static Element getStructure(HttpServletRequest request, MCRObjectID objectId) {
        Object attribute = request.getAttribute(STRUCTURE_CACHE_ATTRIBUTE);
        StructureCache cache;
        if (attribute instanceof StructureCache structureCache) {
            cache = structureCache;
        } else {
            cache = new StructureCache();
            request.setAttribute(STRUCTURE_CACHE_ATTRIBUTE, cache);
        }
        return cache.structures.computeIfAbsent(objectId, MIRStructureValidator::loadStructure);
    }

    private static Element loadStructure(MCRObjectID objectId) {
        try {
            Document document = MCRXMLMetadataManager.getInstance().retrieveXML(objectId);
            if (document == null) {
                return new Element("structure");
            }
            Element structure = document.getRootElement().getChild("structure");
            return structure == null ? new Element("structure") : structure.clone();
        } catch (IOException | JDOMException e) {
            throw new MCRException("Unable to retrieve structure of " + objectId, e);
        }
    }

    private static final class StructureCache {

        private final Map<MCRObjectID, Element> structures = new HashMap<>();
    }
}

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
import java.util.Optional;

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
 * Evaluates XPath expressions against the stored repository version of an object.
 * <p>
 * {@link #test(String, String)} uses the object's {@code structure} element as context node,
 * {@link #testMetadata(String, String)} its {@code metadata} element. Objects are loaded at most once per request.
 */
public final class MIRStructureValidator {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String OBJECT_CACHE_ATTRIBUTE = MIRStructureValidator.class.getName() + ".objectCache";

    private MIRStructureValidator() {
    }

    /**
     * Evaluates the given XPath with the object's {@code structure} element as context node.
     * <p>
     * If the object is not stored yet, an empty {@code structure} element is used.
     *
     * @param objectId the MyCoRe object ID
     * @param xPath an XPath expression relative to the {@code structure} element
     * @return the effective boolean value of the XPath expression
     */
    public static boolean test(String objectId, String xPath) {
        try {
            Element structure = getObject(MCRObjectID.getInstance(objectId))
                .map(object -> object.getChild("structure"))
                .orElse(null);
            return test(structure == null ? new Element("structure") : structure, xPath);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to validate structure of {} with XPath {}", objectId, xPath, e);
            return false;
        }
    }

    /**
     * Evaluates the given XPath with the stored object's {@code metadata} element as context node.
     * <p>
     * Returns {@code false} if the object does not exist, so the result can be used to validate references to
     * other objects.
     *
     * @param objectId the MyCoRe object ID
     * @param xPath an XPath expression relative to the {@code metadata} element
     * @return the effective boolean value of the XPath expression, or {@code false} if the object does not exist
     */
    public static boolean testMetadata(String objectId, String xPath) {
        try {
            return getObject(MCRObjectID.getInstance(objectId))
                .map(object -> object.getChild("metadata"))
                .map(metadata -> test(metadata, xPath))
                .orElse(false);
        } catch (RuntimeException e) {
            LOGGER.error("Unable to validate metadata of {} with XPath {}", objectId, xPath, e);
            return false;
        }
    }

    static boolean test(Element context, String xPath) {
        return new MCRXPathEvaluator(Map.of(), context).test(xPath);
    }

    private static Optional<Element> getObject(MCRObjectID objectId) {
        return MCRFrontendUtil.getCurrentServletJob()
            .map(job -> getObject(job.getRequest(), objectId))
            .orElseGet(() -> loadObject(objectId));
    }

    private static Optional<Element> getObject(HttpServletRequest request, MCRObjectID objectId) {
        Object attribute = request.getAttribute(OBJECT_CACHE_ATTRIBUTE);
        ObjectCache cache;
        if (attribute instanceof ObjectCache objectCache) {
            cache = objectCache;
        } else {
            cache = new ObjectCache();
            request.setAttribute(OBJECT_CACHE_ATTRIBUTE, cache);
        }
        return cache.objects.computeIfAbsent(objectId, MIRStructureValidator::loadObject);
    }

    private static Optional<Element> loadObject(MCRObjectID objectId) {
        try {
            Document document = MCRXMLMetadataManager.getInstance().retrieveXML(objectId);
            return Optional.ofNullable(document).map(Document::getRootElement);
        } catch (IOException | JDOMException e) {
            throw new MCRException("Unable to retrieve " + objectId, e);
        }
    }

    private static final class ObjectCache {

        private final Map<MCRObjectID, Optional<Element>> objects = new HashMap<>();
    }
}

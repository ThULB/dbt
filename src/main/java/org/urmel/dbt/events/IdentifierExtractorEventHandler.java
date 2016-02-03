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
package org.urmel.dbt.events;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.urmel.dbt.opc.OPCConnector;
import org.urmel.dbt.opc.datamodel.pica.PPField;
import org.urmel.dbt.opc.datamodel.pica.PPSubField;
import org.urmel.dbt.opc.datamodel.pica.Record;
import org.urmel.dbt.opc.datamodel.pica.Result;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IdentifierExtractorEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger(IdentifierExtractorEventHandler.class);

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handleObjectCreated(org.mycore.common.events.MCREvent, org.mycore.datamodel.metadata.MCRObject)
     */
    @Override
    synchronized protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        MCRMODSWrapper mods = new MCRMODSWrapper(obj);

        try {
            if (mods.getElements("mods:identifier[@type='ppn']").isEmpty()) {
                final OPCConnector opc = new OPCConnector();
                final List<Element> titles = mods.getElements("mods:titleInfo/mods:title");
                for (final Element title : titles) {
                    final Result result = opc.search(title.getTextNormalize());
                    if (result.getRecords().isEmpty()) {
                        LOGGER.info("Nothing was found for title " + title.getTextNormalize());
                    } else {
                        for (final Record record : result.getRecords()) {
                            record.load(true); // load full record

                            final PPField f = record.getFieldByTag("002@");
                            if (f != null) {
                                final String matCode = f.getSubfieldByCode("0").getContent();
                                if (matCode.startsWith("O")) {
                                    LOGGER.info("Found PPN " + record.getPPN());

                                    final Element mId = mods.addElement("identifier");
                                    mId.setAttribute("type", "ppn");
                                    mId.addContent(record.getPPN());

                                    final List<Element> persons = mods.getElements("mods:name[@type='personal']");
                                    for (final Element person : persons) {
                                        if (buildXPath("mods:nameIdentifier[@type='gnd']")
                                                .evaluateFirst(person) == null) {
                                            final String gnd = getGND(
                                                    buildXPath("mods:displayForm").evaluateFirst(person), record, opc);
                                            if (gnd != null) {
                                                final Element mNId = new Element("nameIdentifier",
                                                        MCRConstants.MODS_NAMESPACE);
                                                mNId.setAttribute("type", "gnd");
                                                mNId.addContent(gnd);
                                                person.addContent(mNId);
                                            }
                                        }
                                    }

                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat())
                                                .outputString(obj.createXML()));
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on extract identifiers for object " + obj, e);
        }
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handleObjectUpdated(org.mycore.common.events.MCREvent, org.mycore.datamodel.metadata.MCRObject)
     */
    @Override
    synchronized protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        handleObjectCreated(evt, obj);
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handleObjectRepaired(org.mycore.common.events.MCREvent, org.mycore.datamodel.metadata.MCRObject)
     */
    @Override
    synchronized protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        handleObjectCreated(evt, obj);
    }

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handleObjectIndex(org.mycore.common.events.MCREvent, org.mycore.datamodel.metadata.MCRObject)
     */
    @Override
    synchronized protected void handleObjectIndex(MCREvent evt, MCRObject obj) {
        handleObjectCreated(evt, obj);
    }

    private XPathExpression<Element> buildXPath(String xPath) throws JDOMException {
        return XPathFactory.instance().compile(xPath, Filters.element(), null, MCRConstants.MODS_NAMESPACE,
                MCRConstants.XLINK_NAMESPACE);
    }

    private String getGND(final Element displayForm, final Record record, final OPCConnector opc) {
        if (displayForm != null && record != null) {
            List<String> nameParts = Arrays.asList(displayForm.getTextTrim().split(" "));
            nameParts.replaceAll(s -> s.replaceAll(",", "").trim());

            for (final PPField f : record.getFieldsByTag("028A")) {
                final Optional<PPSubField> pn = Optional.ofNullable(f.getSubfieldByCode("d"));
                final Optional<PPSubField> sn = Optional.ofNullable(f.getSubfieldByCode("a"));

                if (pn.isPresent() && sn.isPresent()) {
                    if (nameParts.contains(pn.get().getContent()) && nameParts.contains(sn.get().getContent())) {
                        final Optional<PPSubField> idn = Optional.ofNullable(f.getSubfieldByCode("9"));
                        if (idn.isPresent()) {
                            try {
                                final Record pr = opc.getRecord(idn.get().getContent());
                                final Optional<PPField> gndF = Optional.ofNullable(pr.getFieldByTag("007K"))
                                        .filter(x -> x.getSubfieldByCode("a").getContent().equals("gnd"));
                                if (gndF.isPresent()) {
                                    final String gnd = gndF.get().getSubfieldByCode("0").getContent();
                                    LOGGER.info("Found GND " + gnd + " for person \"" + sn.get().getContent() + ", "
                                            + pn.get().getContent() + "\".");
                                    return gnd;
                                }
                            } catch (Exception e) {
                                LOGGER.error("Couldn't read record for idn " + idn.get().getContent(), e);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}

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
package de.urmel_dl.dbt.events;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.pica.PPField;
import de.urmel_dl.dbt.opc.datamodel.pica.PPSubField;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class IdentifierExtractorEventHandler extends MCREventHandlerBase {

    private static final Logger LOGGER = LogManager.getLogger(IdentifierExtractorEventHandler.class);

    private static final IdentifierExtractorPrefixProvider PREFIX_SINGELTON = MCRConfiguration.instance()
        .getInstanceOf("MIR.IdentifierExtractor.Prefix.Class",
            IdentifierExtractorDefaultPrefixProvider.class.getCanonicalName());

    private static final String URI_SYNTAX = "http://uri.gbv.de/document/{0}:ppn:{1}";

    private static final String QUERY_FILTER = "[-+]";

    /* (non-Javadoc)
     * @see org.mycore.common.events.MCREventHandlerBase#handleObjectCreated(org.mycore.common.events.MCREvent, org.mycore.datamodel.metadata.MCRObject)
     */
    @Override
    synchronized protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        if (!obj.getId().getTypeId().equals("mods")) {
            return;
        }

        MCRMODSWrapper mods = new MCRMODSWrapper(obj);

        try {
            final String prefix = PREFIX_SINGELTON.getPrefix(mods);

            if (mods.getElements("mods:identifier[@type='uri']").stream()
                .noneMatch(e -> e.getText()
                    .contains(new MessageFormat(URI_SYNTAX, Locale.ROOT).format(new Object[] { prefix, "" })))) {
                final OPCConnector opc = new OPCConnector();
                opc.setMaxHits(50);

                final List<Element> titleInfos = mods.getElements("mods:titleInfo");
                titleInfos.parallelStream().forEach(titleInfo -> {
                    try {
                        final String query = buildQuery(titleInfo);
                        final Result result = opc.search(query);

                        if (result.getRecords().isEmpty()) {
                            LOGGER.info("Nothing was found for title " + query);
                        } else {
                            result.getRecords().parallelStream().map(record -> {
                                record.load(true);
                                return record;
                            }).filter(record -> Optional.ofNullable(record.getFieldByTag("002@"))
                                .map(f -> f.getSubfieldByCode("0")).map(sf -> sf.getContent().startsWith("O"))
                                .orElse(false) && matchTitle(titleInfo, record)
                                && matchPersons(mods.getElements("mods:name[@type='personal']"), record, opc))
                                .findFirst()
                                .ifPresent(record -> {
                                    LOGGER.info("Found PPN " + record.getPPN());

                                    final Element mId = mods.addElement("identifier");
                                    mId.setAttribute("type", "uri");
                                    mId.addContent(new MessageFormat(URI_SYNTAX, Locale.ROOT)
                                        .format(new Object[] { prefix, record.getPPN() }));

                                    final List<Element> persons = mods.getElements("mods:name[@type='personal']");
                                    persons.parallelStream()
                                        .filter(person -> buildXPath("mods:nameIdentifier[@type='gnd']")
                                            .evaluateFirst(person) == null)
                                        .forEach(person -> {
                                            final String gnd = extractPersonIdentifier("gnd",
                                                buildXPath("mods:displayForm").evaluateFirst(person), record, opc);
                                            if (gnd != null) {
                                                final Element mNId = new Element("nameIdentifier",
                                                    MCRConstants.MODS_NAMESPACE);
                                                mNId.setAttribute("type", "gnd");
                                                mNId.addContent(gnd);
                                                person.addContent(mNId);
                                            }
                                        });

                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat())
                                            .outputString(obj.createXML()));
                                    }
                                });
                        }
                    } catch (ExecutionException e1) {
                        LOGGER.error("Error on extract identifiers for object " + obj, e1);
                    }
                });
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

    private XPathExpression<Element> buildXPath(String xPath) {
        return XPathFactory.instance().compile(xPath, Filters.element(), null, MCRConstants.MODS_NAMESPACE,
            MCRConstants.XLINK_NAMESPACE);
    }

    private String buildQuery(final Element titleInfo) {
        final StringBuffer sb = new StringBuffer();

        final Element title = buildXPath("mods:title").evaluateFirst(titleInfo);
        final Element subTitle = buildXPath("mods:subTitle").evaluateFirst(titleInfo);

        if (title != null && title.getTextNormalize().length() > 3) {
            sb.append("tit " + title.getTextNormalize().replaceAll(QUERY_FILTER, ""));
        }
        if (subTitle != null) {
            if (sb.length() > 0) {
                sb.append(" or ");
            }
            sb.append("tit " + subTitle.getTextNormalize().replaceAll(QUERY_FILTER, ""));
        }

        return sb.toString();
    }

    private boolean matchTitle(final Element titleInfo, final Record record) {
        if (titleInfo != null && record != null) {
            final List<PPField> titFields = record.getFieldsByTag("021A");
            if (!titFields.isEmpty()) {
                final StringBuffer sb = new StringBuffer();

                final Element title = buildXPath("mods:title").evaluateFirst(titleInfo);
                final Element subTitle = buildXPath("mods:subTitle").evaluateFirst(titleInfo);

                if (title != null && title.getTextNormalize().length() > 3) {
                    sb.append(title.getTextNormalize());
                }
                if (subTitle != null) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(subTitle.getTextNormalize());
                }

                for (final PPField titField : titFields) {
                    int confidence = partsCompare(sb.toString(),
                        Arrays.stream("a,d".split(",")).map(s -> titField.getSubfieldByCode(s))
                            .filter(sc -> sc != null).map(sc -> sc.getContent())
                            .collect(Collectors.joining(", ")));
                    if (confidence > 75) {
                        LOGGER.info(
                            "Title \"" + sb.toString() + "\" matches with a confidence of " + confidence + "%.");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean matchPerson(final Element displayForm, final Record record,
        final OPCConnector opc) {
        if (displayForm != null && record != null) {
            List<PPField> nameFields = Arrays.stream("028A,028B,028C,028D,028E,028F,028G,028H,028L,028M".split(","))
                .map(tag -> record.getFieldsByTag(tag)).flatMap(l -> l.stream()).collect(Collectors.toList());

            for (final PPField f : nameFields) {
                int confidence = partsCompare(displayForm.getTextTrim(),
                    Arrays.stream("d,a,c".split(",")).map(s -> f.getSubfieldByCode(s)).filter(sc -> sc != null)
                        .map(sc -> sc.getContent()).collect(Collectors.joining(", ")));

                if (confidence > 50) {
                    LOGGER.info("Person \"" + displayForm.getTextTrim() + "\" matches with a confidence of "
                        + confidence + "%.");

                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchPersons(final List<Element> persons, final Record record,
        final OPCConnector opc) {
        long numMatching = persons.parallelStream().map(person -> matchPerson(
            buildXPath("mods:displayForm").evaluateFirst(person), record, opc)).count();

        return Math.round(100 / persons.size() * numMatching) > 75;
    }

    private String extractPersonIdentifier(final String idType, final Element displayForm, final Record record,
        final OPCConnector opc) {
        if (displayForm != null && record != null) {
            List<PPField> nameFields = Arrays.stream("028A,028B,028C,028D,028E,028F,028G,028H,028L,028M".split(","))
                .map(tag -> record.getFieldsByTag(tag)).flatMap(l -> l.stream()).collect(Collectors.toList());

            for (final PPField f : nameFields) {
                int confidence = partsCompare(displayForm.getTextTrim(),
                    Arrays.stream("d,a,c".split(",")).map(s -> f.getSubfieldByCode(s)).filter(sc -> sc != null)
                        .map(sc -> sc.getContent()).collect(Collectors.joining(", ")));

                if (confidence > 50) {
                    final Optional<PPSubField> idn = Optional.ofNullable(f.getSubfieldByCode("9"));
                    if (idn.isPresent()) {
                        final String id = getIdentifier(idType, opc, idn.get().getContent());
                        if (id != null) {
                            LOGGER.info("Found " + idType + " " + id + " for person \"" + displayForm.getTextTrim()
                                + "\".");
                            return id;
                        }
                    }
                }
            }
        }

        return null;
    }

    private String getIdentifier(final String idType, final OPCConnector opc, final String idn) {
        try {
            final Record record = opc.getRecord(idn);
            List<String> ids = Stream.of(record.getFieldsByTag("007K"), record.getFieldsByTag("007N"))
                .flatMap(l -> l.stream())
                .filter(f -> f.getSubfieldByCode("a").getContent().equalsIgnoreCase(idType))
                .map(f -> f.getSubfieldByCode("0").getContent()).collect(Collectors.toList());

            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception e) {
            LOGGER.error("Couldn't read record for idn " + idn, e);
            return null;
        }
    }

    private static String normalizeAccents(final String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}|´", "");
    }

    private static int partsCompare(final String n1, final String n2) {
        List<String> n1Parts = Arrays.stream(n1.split("[,\\s:]")).filter(s -> !s.isEmpty())
            .map(s -> s.toLowerCase(Locale.ROOT))
            .map(IdentifierExtractorEventHandler::normalizeAccents).collect(Collectors.toList());
        List<String> n2Parts = Arrays.stream(n2.split("[,\\s:]")).filter(s -> !s.isEmpty())
            .map(s -> s.toLowerCase(Locale.ROOT))
            .map(IdentifierExtractorEventHandler::normalizeAccents).collect(Collectors.toList());

        return Math.round(100 / (n1Parts.size() > n2Parts.size() ? n1Parts.size() : n2Parts.size())
            * n1Parts.stream().filter(s -> n2Parts.contains(s)).count());
    }
}

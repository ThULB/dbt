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
package de.urmel_dl.dbt.rc.resolver;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntryTypes;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * This resolver can be used to resolve a {@link Slot}, {@link SlotEntry}, {@link SlotEntryTypes} and also the set catalogId.
 * <br>
 * <br>
 * Syntax:
 * <ul>
 * <li><code>slot:slotId={slotId}[&amp;rev=revision]</code> to resolve an {@link Slot}</li>
 * <li><code>slot:slotId={slotId}&amp;entryId={entryId}[&amp;rev=revision]</code> to resolve an {@link SlotEntry}</li>
 * <li><code>slot:slotId={slotId}&amp;catalogId</code> to get the catalogId for slot (from RCLOC classification)</li>
 * <li><code>slot:slotId={slotId}&amp;mail[&amp;parent=true|false]</code> to get the mail address for slot (from RCLOC classification)</li>
 * <li><code>slot:slotId={slotId}&amp;objectId</code> to get the {@link MCRObjectID} for slot</li>
 * <li><code>slot:slotId={slotId}&amp;isActive</code> to get information about a slot is active</li>
 * <li><code>slot:entryTypes</code> to resolve {@link SlotEntryTypes}</li>
 * </ul>
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class SlotResolver implements URIResolver {

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    private static final MCRCategoryDAO DAO = new MCRCategoryDAOImpl();

    /* (non-Javadoc)
     * @see javax.xml.transform.URIResolver#resolve(java.lang.String, java.lang.String)
     */
    @Override
    public Source resolve(final String href, final String base) throws TransformerException {
        try {
            final String options = href.substring(href.indexOf(":") + 1);
            final HashMap<String, String> params = new HashMap<>();
            String[] param;
            final StringTokenizer tok = new StringTokenizer(options, "&");
            while (tok.hasMoreTokens()) {
                param = tok.nextToken().split("=");
                if (param.length == 1) {
                    params.put(param[0], "");
                } else {
                    params.put(param[0], param[1]);
                }
            }

            if (params.get("entryTypes") != null) {
                return new JDOMSource(new EntityFactory<>(SlotEntryTypes.instance()).toDocument());
            }

            final String slotId = params.get("slotId");
            final String entryId = params.get("entryId");
            final String revision = params.get("revision");

            final Slot slot = revision != null ? SLOT_MGR.getSlotById(slotId, Long.parseLong(revision))
                : SLOT_MGR.getSlotById(slotId);

            if (entryId != null) {
                final SlotEntry<?> entry = slot.getEntryById(entryId);

                return new JDOMSource(new EntityFactory<>(entry).toDocument());
            } else if (params.get("catalogId") != null) {
                List<MCRCategory> categories = DAO
                    .getParents(slot != null ? slot.getLocation() : getMCRCategoryForSlotId(slotId));

                String catalogId = null;
                for (MCRCategory category : categories) {
                    if (category.getLabel("x-catid").isPresent()) {
                        catalogId = category.getLabel("x-catid").get().getText();
                        break;
                    }
                }

                final Element root = new Element("catalog");
                root.setText(catalogId);

                return new JDOMSource(root);
            } else if (params.get("mail") != null) {
                final MCRCategory category = DAO
                    .getCategory(slot != null ? slot.getLocation() : getMCRCategoryForSlotId(slotId), 0);

                final String mailAddress = getLabelText(category, "x-mail", Boolean.parseBoolean(params.get("parent")));

                final Element root = new Element("mail");
                if (mailAddress != null) {
                    root.setText(mailAddress);
                }

                return new JDOMSource(root);
            } else if (params.get("objectId") != null) {
                final Element root = new Element("mcrobject");
                root.setText(slot.getMCRObjectID().toString());

                return new JDOMSource(root);
            } else if (params.get("isActive") != null) {
                final Element root = new Element("slot");
                root.setText(Boolean.toString(slot.isActive()));

                return new JDOMSource(root);
            }

            return new JDOMSource(new EntityFactory<>(slot).toDocument());
        } catch (final Exception ex) {
            throw new TransformerException("Exception resolving " + href, ex);
        }
    }

    private String getLabelText(final MCRCategory category, final String label, boolean parent) {
        String text = null;

        if (category.getLabel(label).isPresent()) {
            text = category.getLabel(label).get().getText();
        }

        if (text == null || parent) {
            List<MCRCategory> parents = DAO.getParents(category.getId());
            for (MCRCategory c : parents) {
                String t = getLabelText(c, label, parent);
                if (t != null) {
                    text = t;
                    if (!parent) {
                        break;
                    }
                }
            }
        }

        return text;
    }

    private MCRCategoryID getMCRCategoryForSlotId(String slotId) {
        final StringTokenizer st = new StringTokenizer(slotId, Slot.DEFAULT_ID_SPACER);

        String loc = null;
        for (int c = 0; c <= st.countTokens(); c++) {
            loc = loc == null ? st.nextToken() : loc + Slot.DEFAULT_ID_SPACER + st.nextToken();
        }
        final String id = st.nextToken();

        if (id != null && loc != null) {
            return new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, loc);
        }

        return null;
    }
}

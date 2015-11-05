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
package org.urmel.dbt.rc.resolver;

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
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotEntry;
import org.urmel.dbt.rc.datamodel.slot.SlotEntryTypes;
import org.urmel.dbt.rc.persistency.SlotManager;
import org.urmel.dbt.rc.utils.SlotEntryTransformer;
import org.urmel.dbt.rc.utils.SlotEntryTypesTransformer;
import org.urmel.dbt.rc.utils.SlotTransformer;

/**
 * This resolver can be used to resolve a {@link Slot}, {@link SlotEntry}, {@link SlotEntryTypes} and also the set catalogId. 
 * <br />
 * <br />
 * Syntax:
 * <ul> 
 * <li><code>slot:slotId={slotId}[&rev=revision]</code> to resolve an {@link Slot}</li>
 * <li><code>slot:slotId={slotId}&entryId={entryId}[&rev=revision]</code> to resolve an {@link SlotEntry}</li>
 * <li><code>slot:slotId={slotId}&catalogId</code> to get the catalogId for slot (from RCLOC classification)</li>
 * <li><code>slot:slotId={slotId}&objectId</code> to get the {@link MCRObjectID} for slot</li>
 * <li><code>slot:slotId={slotId}&isActive</code> to get information about a slot is active</li>
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
            final HashMap<String, String> params = new HashMap<String, String>();
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
                return new JDOMSource(SlotEntryTypesTransformer.buildExportableXML(SlotEntryTypes.instance()));
            }

            final String slotId = params.get("slotId");
            final String entryId = params.get("entryId");
            final String revision = params.get("revision");

            final Slot slot = revision != null ? SLOT_MGR.getSlotById(slotId, Long.parseLong(revision))
                    : SLOT_MGR.getSlotById(slotId);

            if (entryId != null) {
                final SlotEntry<?> entry = slot.getEntryById(entryId);

                return new JDOMSource(SlotEntryTransformer.buildExportableXML(entry));
            } else if (params.get("catalogId") != null) {
                List<MCRCategory> categories = DAO.getParents(slot.getLocation());

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
            } else if (params.get("objectId") != null) {
                final Element root = new Element("mcrobject");
                root.setText(slot.getMCRObjectID().toString());

                return new JDOMSource(root);
            } else if (params.get("isActive") != null) {
                final Element root = new Element("slot");
                root.setText(Boolean.toString(slot.isActive()));

                return new JDOMSource(root);
            }

            return new JDOMSource(SlotTransformer.buildExportableXML(slot));
        } catch (final Exception ex) {
            throw new TransformerException("Exception resolving " + href, ex);
        }
    }
}

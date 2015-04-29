/*
 * $Id: SlotManager.java 2117 2014-10-01 12:39:12Z adler $ 
 * $Revision$
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
package org.urmel.dbt.rc.persistency;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.urmel.dbt.rc.datamodel.Status;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.urmel.dbt.rc.utils.SlotWrapper;
import org.xml.sax.SAXException;

/**
 * @author René Adler (eagle)
 *
 */
public final class SlotManager {

    public static final String DEFAULT_PROJECT_ID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID.rcslot",
            "dbt");

    private static final Logger LOGGER = Logger.getLogger(SlotManager.class);

    private static SlotManager singelton;

    private SlotList slotList;

    private SlotManager() {
        if (slotList == null) {
            slotList = new SlotList();
            loadList();
        }
    }

    /**
     * Returns a instance of the {@link SlotManager}.
     * 
     * @return the SlotManager
     */
    public static SlotManager instance() {
        if (singelton == null) {
            singelton = new SlotManager();
        }

        return singelton;
    }

    /**
     * Loads the {@link Slot} metadata from content store.
     * You have to clear the {@link SlotManager#slotList} before.
     */
    public synchronized void loadList() {
        final MCRXMLMetadataManager xmlManager = MCRXMLMetadataManager.instance();
        final List<String> ids = xmlManager.listIDsForBase(SlotWrapper.getMCRObjectBaseID());

        for (String objId : ids) {
            try {
                if (MCRMetadataManager.exists(MCRObjectID.getInstance(objId))) {
                    final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objId);
                    final Slot slot = SlotWrapper.unwrapMCRObject(obj);
                    slotList.addSlot(slot);
                }
            } catch (final Exception e) {
                LOGGER.error("Error on loading " + objId + "!", e);
            }
        }
    }

    /**
     * Saves btw. updates current {@link SlotList}.
     * 
     * @throws MCRActiveLinkException 
     * @throws MCRPersistenceException 
     * @throws SAXException 
     * @throws JDOMException 
     * @throws IOException 
     */
    public synchronized void saveList() throws MCRPersistenceException, MCRActiveLinkException, IOException,
            JDOMException, SAXException {
        for (Slot slot : slotList.getSlots()) {
            final MCRObjectID objId = slot.getMCRObjectID();
            if (objId != null && MCRMetadataManager.exists(objId)) {
                final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objId);
                final Slot ts = SlotWrapper.unwrapMCRObject(obj);
                if (!ts.equals(slot)) {
                    saveOrUpdate(slot);
                }
            } else {
                saveOrUpdate(slot);
            }
        }
    }

    /**
     * Adds a new {@link Slot} to {@link SlotList}.
     * 
     * @param slot the slot
     */
    public void addSlot(final Slot slot) {
        if (slot.getId() == 0 && slot.getLocation() != null) {
            slot.setId(getNextFreeId(slot.getLocation()));
            slot.setStatus(Status.NEW);
        }

        slotList.addSlot(slot);
    }

    /**
     * Returns a slot by given id.
     * 
     * @param slotId the slot id
     * @return the slot
     */
    public Slot getSlotById(final String slotId) {
        return slotList.getSlotById(slotId);
    }

    /**
     * Returns the next free slot id for given reserve collection location.
     * 
     * @param rcLocation the reserve collection location
     * @return the next id
     */
    public synchronized int getNextFreeId(final MCRCategoryID rcLocation) {
        int nextId = -1;
        int lastId = -1;

        for (Slot slot : slotList.getSlots()) {
            if (slot.getLocation().equals(rcLocation)) {
                lastId = lastId == -1 || lastId <= slot.getId() ? slot.getId() + 1 : lastId;
                if (slot.getStatus() == Status.FREE) {
                    nextId = nextId == -1 || nextId > slot.getId() ? slot.getId() : nextId;
                }
            }
        }

        return nextId == -1 ? lastId == -1 ? 1 : lastId : nextId;
    }

    /**
     * Saves or updates the metadata of given {@link Slot}.
     * 
     * @param slot the slot
     * @throws MCRActiveLinkException 
     * @throws MCRPersistenceException 
     */
    public synchronized void saveOrUpdate(final Slot slot) throws MCRPersistenceException, MCRActiveLinkException {
        final MCRObjectID objID = slot.getMCRObjectID();

        if (objID != null && MCRMetadataManager.exists(objID)) {
            final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objID);
            final SlotWrapper wrapper = new SlotWrapper(obj);
            wrapper.setSlot(slot);
            MCRMetadataManager.update(wrapper.getMCRObject());
        } else {
            final MCRObject obj = SlotWrapper.wrapSlot(slot);
            obj.setId(MCRObjectID.getNextFreeId(obj.getId().getBase()));
            MCRMetadataManager.create(obj);
            slot.setMCRObjectID(obj.getId());
        }
    }

    /**
     * Returns the current {@link SlotList}.
     * 
     * @return the slot list
     */
    public SlotList getSlotList() {
        return slotList;
    }

    /**
     * Returns a {@link SlotList} with only active {@link Slot}s.
     * 
     * @return the {@link SlotList}
     */
    public SlotList getActiveSlotList() {
        return slotList.getActiveSlots();
    }
}

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
package de.urmel_dl.dbt.rc.utils;

import java.util.Collections;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRObjectService;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.persistency.SlotManager;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotWrapper {

    private static final String SLOT_CONTAINER = "rcSlotContainer";

    private static final String DEF_SLOT_CONTAINER = "def.rcSlotContainer";

    private static final String SLOT_DATAMODEL = "datamodel-rcslot.xsd";

    private MCRObject object;

    public static MCRObject wrapSlot(Slot slot) {
        return wrapSlot(slot, SlotManager.PROJECT_ID);
    }

    public static MCRObject wrapSlot(Slot slot, String projectID) {
        SlotWrapper wrapper = new SlotWrapper();
        wrapper.setID(projectID, 0);
        wrapper.setSlot(slot);
        return wrapper.getMCRObject();

    }

    public static Slot unwrapMCRObject(MCRObject object) {
        SlotWrapper wrapper = new SlotWrapper(object);
        return wrapper.getSlot();
    }

    /**
     *  Returns a new MCRObject
     */
    public SlotWrapper() {
        this(new MCRObject());
    }

    public SlotWrapper(MCRObject object) {
        this.object = object;
        object.setSchema(SLOT_DATAMODEL);
    }

    public MCRObject getMCRObject() {
        return object;
    }

    public MCRObjectID setID(String projectID, int ID) {
        MCRObjectID objID = ID == 0 ? MCRObjectID.getNextFreeId(SlotManager.getMCRObjectBaseID()) : MCRObjectID
            .getInstance(MCRObjectID.formatID(projectID, SlotManager.SLOT_TYPE, ID));
        object.setId(objID);
        return objID;
    }

    public Slot getSlot() {
        MCRMetaXML mx = (MCRMetaXML) (object.getMetadata().getMetadataElement(DEF_SLOT_CONTAINER).getElement(0));
        for (Content content : mx.getContent()) {
            if (content instanceof Element) {
                Slot slot = new EntityFactory<>(Slot.class).fromElement((Element) content);
                slot.setMCRObjectID(object.getId());
                return slot;
            }
        }
        return null;
    }

    public void setSlot(Slot slot) {
        MCRObjectMetadata om = object.getMetadata();
        if (om.getMetadataElement(DEF_SLOT_CONTAINER) != null) {
            om.removeMetadataElement(DEF_SLOT_CONTAINER);
        }

        MCRMetaXML slotContainer = new MCRMetaXML(SLOT_CONTAINER, null, 0);
        List<MCRMetaXML> list = Collections.nCopies(1, slotContainer);
        MCRMetaElement defSlotContainer = new MCRMetaElement(MCRMetaXML.class, DEF_SLOT_CONTAINER, false, true, list);
        om.setMetadataElement(defSlotContainer);

        Document doc = new EntityFactory<>(slot.getExportableCopy()).toDocument();
        slotContainer.addContent(doc.getRootElement());

        if (slot.getReadKey() != null) {
            setServiceFlag("readkey", slot.getReadKey());
            slot.setReadKey(null);
        }
        if (slot.getWriteKey() != null) {
            setServiceFlag("writekey", slot.getWriteKey());
            slot.setWriteKey(null);
        }
    }

    public String getServiceFlag(String type) {
        MCRObjectService os = object.getService();
        return (os.isFlagTypeSet(type) ? os.getFlags(type).get(0) : null);
    }

    public void setServiceFlag(String type, String value) {
        MCRObjectService os = object.getService();
        if (os.isFlagTypeSet(type)) {
            os.removeFlags(type);
        }
        if (value != null && !value.trim().isEmpty()) {
            os.addFlag(type, value.trim());
        }
    }
}

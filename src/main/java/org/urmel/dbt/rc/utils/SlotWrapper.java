/*
 * $Id: SlotWrapper.java 2124 2014-10-07 11:44:50Z adler $ 
 */
package org.urmel.dbt.rc.utils;

import java.util.Collections;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.urmel.dbt.rc.datamodel.Slot;

/**
 * @author René Adler (eagle)
 *
 */
public class SlotWrapper {

    private static final String DEFAULT_PROJECT_ID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID.rcslot",
            "dbt");

    private static final String SLOT_OBJECT_TYPE = "rcslot";

    private static final String SLOT_CONTAINER = "rcSlotContainer";

    private static final String DEF_SLOT_CONTAINER = "def.rcSlotContainer";

    private static final String SLOT_DATAMODEL = "datamodel-rcslot.xsd";

    private MCRObject object;

    /**
     * Returns the base id of the MCRObject.
     * 
     * @return the base id
     */
    public static String getMCRObjectBaseID() {
        return getMCRObjectBaseID(DEFAULT_PROJECT_ID);
    }

    public static String getMCRObjectBaseID(String projectID) {
        return projectID + "_" + SLOT_OBJECT_TYPE;
    }

    public static MCRObject wrapSlot(Slot slot) {
        return wrapSlot(slot, DEFAULT_PROJECT_ID);
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
        MCRObjectID objID = MCRObjectID.getInstance(MCRObjectID.formatID(projectID, SLOT_OBJECT_TYPE, ID));
        object.setId(objID);
        return objID;
    }

    public Slot getSlot() {
        MCRMetaXML mx = (MCRMetaXML) (object.getMetadata().getMetadataElement(DEF_SLOT_CONTAINER).getElement(0));
        for (Content content : mx.getContent()) {
            if (content instanceof Element) {
                Slot slot = SlotTransformer.buildSlot((Element) content);
                slot.setMCRObjectID(object.getId());
                return slot;
            }
        }
        return null;
    }

    public void setSlot(Slot slot) {
        MCRObjectMetadata om = object.getMetadata();
        if (om.getMetadataElement(DEF_SLOT_CONTAINER) != null)
            om.removeMetadataElement(DEF_SLOT_CONTAINER);

        MCRMetaXML slotContainer = new MCRMetaXML(SLOT_CONTAINER, null, 0);
        List<MCRMetaXML> list = Collections.nCopies(1, slotContainer);
        MCRMetaElement defSlotContainer = new MCRMetaElement(MCRMetaXML.class, DEF_SLOT_CONTAINER, false, true, list);
        om.setMetadataElement(defSlotContainer);

        Document doc = SlotTransformer.buildExportableXML(slot);
        slotContainer.addContent(doc.getRootElement());
    }

    public String getServiceFlag(String type) {
        MCRObjectService os = object.getService();
        return (os.isFlagTypeSet(type) ? os.getFlags(type).get(0) : null);
    }

    public void setServiceFlag(String type, String value) {
        MCRObjectService os = object.getService();
        if (os.isFlagTypeSet(type))
            os.removeFlags(type);
        if (value != null && !value.trim().isEmpty())
            os.addFlag(type, value.trim());
    }
}

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
package de.urmel_dl.dbt.rc.persistency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.access.MCRAccessManager;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImpl;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRCreatorCache;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRVersionedMetadata;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.mycore.user2.MCRUser;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;

import de.urmel_dl.dbt.rc.datamodel.Attendee;
import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.Status;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.utils.SlotWrapper;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author René Adler (eagle)
 *
 */
public final class SlotManager {

    public static final String POOLPRIVILEGE_ADMINISTRATE_SLOT = "administrate-slot";

    public static final String POOLPRIVILEGE_EDIT_SLOT = "edit-slot";

    public static final String POOLPRIVILEGE_CREATE_SLOT = "create-slot";

    public static final String ADMIN_GROUP = MCRConfiguration.instance().getString("DBT.RC.Administrator.GroupName");

    public static final String EDITOR_GROUP = MCRConfiguration.instance().getString("DBT.RC.Editor.GroupName");

    public static final String PROJECT_ID = "rc";

    public static final String SLOT_TYPE = "slot";

    public static final String ENTRY_TYPE = "entry";

    public static final String INACTIVATE_EVENT = "inactivate";

    public static final String REACTIVATE_EVENT = "reactivate";

    public static final String OWNER_TRANSFER_EVENT = "ownerTransfer";

    private static final Logger LOGGER = LogManager.getLogger(SlotManager.class);

    private static SlotManager singelton;

    private SlotList slotList;

    private SlotManager() {
        slotList = new SlotList();
        loadList();
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

    public static String buildKey() {
        final StringBuffer buf = new StringBuffer();
        buf.append(Long.toString(System.nanoTime(), 36));
        return buf.reverse().toString();
    }

    /**
     * Returns the base id of the MCRObject.
     *
     * @return the base id
     */
    public static String getMCRObjectBaseID() {
        return PROJECT_ID + "_" + SLOT_TYPE;
    }

    /**
     * Checks if current user allowed to access the {@link Slot} by given {@link MCRObjectID} and permission.
     * This method checks if current user is owner or the user has access from any strategy.
     *
     * @param objId the {@link MCRObjectID}
     * @param permission the permission
     * @return <code>true</code> if allowed or <code>false</code> if not
     */
    public static boolean checkPermission(final MCRObjectID objId, final String permission) {
        return checkPermission(objId.toString(), permission);
    }

    /**
     * Checks if current user allowed to access the {@link Slot} by given {@link MCRObjectID} and permission.
     * This method checks if current user is owner or the user has access from any strategy.
     *
     * @param objId the {@link MCRObjectID}
     * @param permission the permission
     * @return <code>true</code> if allowed or <code>false</code> if not
     */
    public static boolean checkPermission(final String objId, final String permission) {
        if (permission.equals(MCRAccessManager.PERMISSION_READ)
            || permission.equals(MCRAccessManager.PERMISSION_WRITE)) {
            if (hasAdminPermission() || hasEditorPermission() || isOwner(objId)) {
                return true;
            }
        } else if (permission.equals(MCRAccessManager.PERMISSION_DELETE) && hasAdminPermission()) {
            return true;
        }

        return false;
    }

    /**
     * Checks if current user is reserve collection administrator.
     *
     * @return <code>true</code> if is administrator
     */
    public static boolean hasAdminPermission() {
        return MCRAccessManager.checkPermission(POOLPRIVILEGE_ADMINISTRATE_SLOT);
    }

    /**
     * Checks if current user is reserve collection editor.
     *
     * @return <code>true</code> if is editor
     */
    public static boolean hasEditorPermission() {
        return MCRAccessManager.checkPermission(POOLPRIVILEGE_EDIT_SLOT);
    }

    /**
     * Checks if current user is owner of reserve collection.
     *
     * @param objId the {@link MCRObjectID}
     * @return <code>true</code> is owner
     */
    public static boolean isOwner(final String objId) {
        return isOwner(objId, MCRSessionMgr.getCurrentSession().getUserInformation());
    }

    /**
     * Checks if user is owner of reserve collection.
     *
     * @param objId the {@link MCRObjectID}
     * @param user the {@link MCRUserInformation}
     * @return <code>true</code> is owner
     */
    public static boolean isOwner(final String objId, final MCRUserInformation user) {
        try {
            return Optional.ofNullable(MCRCreatorCache.getCreator(objId)).map(o -> o.equals(user.getUserID()))
                .orElse(false);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return false;
    }

    public static void setOwner(final String objId)
        throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
        setOwner(objId, MCRSessionMgr.getCurrentSession().getUserInformation());
    }

    public static void setOwner(final String objId, final MCRUserInformation user)
        throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
        final MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objId));
        final MCRObjectService os = obj.getService();

        os.removeFlags("createdby");
        os.addFlag("createdby", user.getUserID());

        MCRMetadataManager.update(obj);

        MCRCreatorCache.invalidate(objId);
    }

    /**
     * Checks if given access key was previously used on slot.
     *
     * @param nodes the slot element
     * @return <code>true</code> if key is matching
     */
    public static boolean isMatchPreviousAccessKeys(List<Element> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            final Slot slot = new EntityFactory<>(Slot.class).fromElement(nodes.get(0));
            final Slot cSlot = SlotManager.instance().getSlotById(slot.getSlotId());

            final MIRAccessKeyPair accKP = MIRAccessKeyManager.getKeyPair(cSlot.getMCRObjectID());

            return accKP != null
                && (accKP.getReadKey().equals(slot.getReadKey()) || accKP.getWriteKey().equals(slot.getWriteKey()));
        }
        return false;
    }

    /**
     * Checks if given slot location and new number is free.
     *
     * @param nodes the location element
     * @return <code>true</code> if slot number is free
     */
    public static boolean isFreeId(List<Element> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            final Element xml = nodes.get(0);
            final Element location = xml.getChild("location");
            final String locId = location != null ? location.getAttributeValue("id") : null;
            final String newId = location != null ? location.getAttributeValue("newId") : null;

            if (locId != null && newId != null) {
                final Slot slot = new EntityFactory<>(Slot.class).fromElement(xml);
                final MCRCategoryID locCat = new MCRCategoryDAOImpl()
                    .getCategory(new MCRCategoryID(Slot.CLASSIF_ROOT_LOCATION, locId), 0).getId();
                int id = Integer.parseInt(newId);

                return slot.getLocation().equals(locCat) && slot.getId() == id || instance().isFreeId(locCat, id);
            }
        }
        return false;
    }

    /**
     * Synchronize the {@link SlotList}.
     */
    public synchronized void syncList() {
        slotList.getSlots().clear();
        loadList();
    }

    /**
     * Loads the {@link Slot} metadata from content store.
     * You have to clear the {@link SlotManager#slotList} before.
     */
    public synchronized void loadList() {
        final MCRXMLMetadataManager xmlManager = MCRXMLMetadataManager.instance();
        final List<String> ids = xmlManager.listIDsForBase(getMCRObjectBaseID());

        for (String objId : ids) {
            try {
                if (MCRMetadataManager.exists(MCRObjectID.getInstance(objId))) {
                    final MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objId));
                    final Slot slot = SlotWrapper.unwrapMCRObject(obj);
                    slotList.addSlot(slot);
                }
            } catch (final Exception e) {
                LOGGER.error("Error on loading " + objId + "!", e);
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
            slot.setStatus(Status.ACTIVE);
        }

        slotList.addSlot(slot);
    }

    /**
     * Set a {@link Slot} to {@link SlotList}.
     *
     * @param slot the slot
     */
    public void setSlot(final Slot slot) {
        slotList.setSlot(slot);
    }

    /**
     * Remove a {@link Slot} from {@link SlotList}.
     *
     * @param slot the slot
     */
    public void removeSlot(final Slot slot) {
        slotList.removeSlot(slot);
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
     * Returns a slot for given id and revision.
     *
     * @param slotId the slot id
     * @param revision the revision
     * @return the slot
     */
    public Slot getSlotById(final String slotId, final Long revision) {
        final Slot slot = getSlotById(slotId);

        if (slot != null && revision != null) {
            MCRVersionedMetadata vm;
            try {
                vm = MCRXMLMetadataManager.instance().getVersionedMetaData(slot.getMCRObjectID());
                MCRContent cont = vm.getRevision(revision).retrieve();
                return SlotWrapper.unwrapMCRObject(new MCRObject(cont.asXML()));
            } catch (IOException | JDOMException | SAXException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Returns the next free slot id for given reserve collection location.
     *
     * @param rcLocation the reserve collection location
     * @return the next id
     */
    public synchronized int getNextFreeId(final MCRCategoryID rcLocation) {
        int lastId = -1;

        for (Slot slot : slotList.getSlots()) {
            if (slot.getLocation().equals(rcLocation)) {
                lastId = lastId == -1 || lastId <= slot.getId() ? slot.getId() + 1 : lastId;
            }
        }

        return lastId == -1 ? 1 : lastId;
    }

    /**
     * Validates a given location and id if it is unused.
     *
     * @param rcLocation the reserve collection location
     * @param id the id to validate
     * @return <code>true</code> if id currently unused
     */
    public synchronized boolean isFreeId(final MCRCategoryID rcLocation, int id) {
        final Slot a = new Slot(rcLocation, id);
        final Slot b = getSlotById(a.getSlotId());

        return b == null;
    }

    /**
     * Returns the current SVN revision of an {@link Slot}.
     *
     * @param slot the {@link Slot}
     * @return an number or <code>null</code> on Exception
     */
    public synchronized Long getLastRevision(final Slot slot) {
        MCRVersionedMetadata vm;
        try {
            vm = MCRXMLMetadataManager.instance().getVersionedMetaData(slot.getMCRObjectID());
            return new Long(vm.getLastPresentRevision());
        } catch (SVNException | IOException e) {
            return null;
        }
    }

    /**
     * Saves or updates the metadata of given {@link Slot}.
     *
     * @param slot the slot
     * @throws MCRActiveLinkException thrown from underlying classes
     * @throws MCRPersistenceException thrown from underlying classes
     * @throws MCRAccessException thrown from underlying classes
     */
    @SuppressWarnings("unchecked")
    public synchronized void saveOrUpdate(final Slot slot)
        throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
        final MCRObjectID objID = slot.getMCRObjectID();

        if (objID != null && MCRMetadataManager.exists(objID)) {
            final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objID);
            final SlotWrapper wrapper = new SlotWrapper(obj);
            wrapper.setSlot(slot);
            MCRMetadataManager.update(wrapper.getMCRObject());
        } else {
            final MCRObject obj = SlotWrapper.wrapSlot(slot);
            slot.setMCRObjectID(obj.getId());
            MCRMetadataManager.create(obj);
        }

        if (slot.getEntries() != null) {
            for (SlotEntry<?> slotEntry : slot.getEntries()) {
                if (slotEntry.getEntry() instanceof FileEntry
                    && !FileEntryManager.exists(slot, (SlotEntry<FileEntry>) slotEntry)) {
                    FileEntryManager.create(slot, (SlotEntry<FileEntry>) slotEntry);
                }
            }
        }
    }

    /**
     * Delete a given {@link Slot}.
     *
     * @param slot the slot
     * @throws MCRPersistenceException thrown from underlying classes
     * @throws MCRActiveLinkException thrown from underlying classes
     * @throws MCRAccessException thrown from underlying classes
     */
    @SuppressWarnings("unchecked")
    public synchronized void delete(final Slot slot)
        throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
        final MCRObjectID objID = slot.getMCRObjectID();

        if (objID != null && MCRMetadataManager.exists(objID)) {
            if (slot.getEntries() != null) {
                for (SlotEntry<?> slotEntry : slot.getEntries()) {
                    if (slotEntry.getEntry() instanceof FileEntry
                        && !FileEntryManager.exists(slot, (SlotEntry<FileEntry>) slotEntry)) {
                        FileEntryManager.delete(slot, (SlotEntry<FileEntry>) slotEntry);
                    }
                }
            }

            final MCRObject obj = MCRMetadataManager.retrieveMCRObject(objID);
            final SlotWrapper wrapper = new SlotWrapper(obj);
            wrapper.setSlot(slot);
            MCRMetadataManager.delete(wrapper.getMCRObject());

            removeSlot(slot);
        } else {
            throw new MCRException("No reserve collection found for ID \"" + objID + "\".");
        }
    }

    /**
     * Returns a list of {@link Attendees} based on {@link MCRObjectID} without any check of valid key.
     *
     * @param slot the {@link Slot}
     * @return a list of {@link Attendee}
     */
    public Attendees getAttendees(final Slot slot) {
        final List<Attendee> attendees = new ArrayList<>();

        final String filterStr = MIRAccessKeyManager.ACCESS_KEY_PREFIX + slot.getMCRObjectID().toString();
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MCRUser> cq = cb.createQuery(MCRUser.class);
        Root<MCRUser> root = cq.from(MCRUser.class);
        MapJoin<MCRUser, String, String> attribs = root.joinMap("attributes");
        cq.where(cb.equal(attribs.key(), filterStr));

        TypedQuery<MCRUser> q = em.createQuery(cq);
        final List<MCRUser> results = q.getResultList();

        for (MCRUser user : results) {
            attendees.add(new Attendee(slot, user));
        }

        Attendees a = new Attendees();
        a.slotId = slot.getSlotId();
        a.attendees = attendees;

        return a;
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
     * Returns a filtered and sorted {@link SlotList}.
     *
     * @param search the search string
     * @param filter the extra filter
     * @param start the start position
     * @param rows the number of rows to return
     * @param sortClauses the clauses for sort
     * @return the slot list
     * @throws IOException thrown on wrong query
     * @throws SolrServerException thrown on SOLR error
     */
    public SlotList getFilteredSlotList(final String search, final String filter, Integer start, Integer rows,
        final List<SortClause> sortClauses) throws SolrServerException, IOException {
        final SlotList slotList = new SlotList();

        final SolrClient client = MCRSolrClientFactory.getMainSolrClient();

        final SolrQuery query = new SolrQuery();
        final String searchStr = "(slotId:%filter%) OR (slot.title:%filter%) OR (slot.lecturer:%filter%) OR (slot.location:%filter%) OR (slot.validTo:%filter%)"
            .replace("%filter%",
                search != null && !search.isEmpty() ? MCRSolrUtils.escapeSearchValue(search) : "*");

        query.setQuery(searchStr);
        query.addFilterQuery("objectProject:" + PROJECT_ID, "objectType:" + SLOT_TYPE,
            filter != null && !filter.isEmpty() ? filter : "");
        query.setFields("slotId");
        query.setSorts(sortClauses);
        query.setStart(start);
        query.setRows(rows);

        final QueryResponse response = client.query(query);

        SolrDocumentList results = response.getResults();
        for (SolrDocument doc : results) {
            for (Object val : (ArrayList<Object>) doc.getFieldValues("slotId")) {
                final Slot slot = getSlotById((String) val);
                if (slot != null) {
                    slotList.addSlot(slot);
                }
            }
        }
        slotList.setTotal(results.getNumFound());

        return slotList;
    }

}

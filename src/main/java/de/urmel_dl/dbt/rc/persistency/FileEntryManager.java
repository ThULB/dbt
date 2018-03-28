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

import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.ifs2.MCRDirectory;
import org.mycore.datamodel.ifs2.MCRFile;
import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRNode;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.ifs2.MCRStoredNode;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public final class FileEntryManager {

    private FileEntryManager() {

    }

    private static String getStoryKey() {
        return SlotManager.PROJECT_ID + "_" + SlotManager.ENTRY_TYPE;
    }

    private static MCRFileStore getStore() {
        String projectType = getStoryKey();
        MCRFileStore store = MCRStoreManager.getStore(projectType, MCRFileStore.class);

        if (store == null) {
            try {
                store = MCRStoreManager.createStore(projectType, MCRFileStore.class);
            } catch (ReflectiveOperationException ex) {
                throw new MCRPersistenceException("Exception while create store for " + projectType, ex);
            }
        }

        return store;
    }

    /**
     * Checks if {@link FileEntry} exists.
     *
     * @param slot the {@link Slot}
     * @param slotEntry the {@link SlotEntry} of type {@link FileEntry}
     * @return <code>true</code> if exists or <code>false</code>
     * @throws MCRPersistenceException thrown if {@link FileEntry} not found
     */
    public static boolean exists(final Slot slot, final SlotEntry<FileEntry> slotEntry) throws MCRPersistenceException {
        final int id = slot.getMCRObjectID().getNumberAsInteger();
        final FileEntry fileEntry = slotEntry.getEntry();

        try {
            final MCRFileStore store = getStore();
            if (!store.exists(id)) {
                return false;
            }

            MCRFileCollection col = store.retrieve(id);
            MCRNode dir = col.getNodeByPath(slotEntry.getId());

            return dir != null && dir.isDirectory();
        } catch (MCRException ex) {
            throw ex;
        } catch (Exception ex) {
            final String msg = "Exception while checking existence of fileEntry " + slotEntry.getId()
                + " with filename " + fileEntry.getName();
            throw new MCRPersistenceException(msg, ex);
        }
    }

    /**
     * Creates an new {@link FileEntry} on filesystem by IFS2.
     *
     * @param slot the {@link Slot}
     * @param slotEntry the {@link SlotEntry} of type {@link FileEntry}
     * @throws MCRPersistenceException thrown if {@link FileEntry} couldn't stored
     */
    public static void create(final Slot slot, final SlotEntry<FileEntry> slotEntry) throws MCRPersistenceException {
        final int id = slot.getMCRObjectID().getNumberAsInteger();
        final FileEntry fileEntry = slotEntry.getEntry();

        try {
            final MCRFileStore store = getStore();

            MCRFileCollection col = store.retrieve(id);
            if (col == null) {
                col = store.create(id);
            }

            MCRDirectory dir = col.createDir(slotEntry.getId());
            MCRFile file = dir.createFile(fileEntry.getName());
            file.setContent(fileEntry.getContent());
        } catch (Exception ex) {
            if (ex instanceof MCRException) {
                throw (MCRException) ex;
            }
            final String msg = "Exception while storing of fileEntry " + slotEntry.getId() + " with filename "
                + fileEntry.getName();
            throw new MCRPersistenceException(msg, ex);
        }
    }

    /**
     * Updates an {@link FileEntry} on filesystem and save old entry with Revision, if not <code>null</code>.
     *
     * @param slot the {@link Slot}
     * @param slotEntry the {@link SlotEntry} of type {@link FileEntry}
     * @throws MCRPersistenceException thrown if {@link FileEntry} coudln't stored
     */
    public static void update(final Slot slot, final SlotEntry<FileEntry> slotEntry) throws MCRPersistenceException {
        if (!exists(slot, slotEntry)) {
            create(slot, slotEntry);
            return;
        }

        final int id = slot.getMCRObjectID().getNumberAsInteger();
        final Long lastRev = SlotManager.instance().getLastRevision(slot);
        final FileEntry fileEntry = slotEntry.getEntry();

        try {
            final MCRFileStore store = getStore();

            MCRFileCollection col = store.retrieve(id);
            MCRStoredNode dir = (MCRStoredNode) col.getNodeByPath(slotEntry.getId());

            if (dir != null && dir.isDirectory()) {
                dir.renameTo(slotEntry.getId() + (lastRev != null ? "-" + lastRev.toString() : ""));
                create(slot, slotEntry);
            }
        } catch (MCRException ex) {
            throw ex;
        } catch (Exception ex) {
            final String msg = "Exception while storing of fileEntry " + slotEntry.getId() + " with filename "
                + fileEntry.getName();
            throw new MCRPersistenceException(msg, ex);
        }
    }

    /**
     * Deletes an {@link FileEntry} on filesystem.
     *
     * @param slot the {@link Slot}
     * @param slotEntry the {@link SlotEntry} of type {@link FileEntry}
     * @throws MCRPersistenceException thrown if {@link FileEntry} couldn't deleted
     */
    public static void delete(final Slot slot, final SlotEntry<FileEntry> slotEntry) throws MCRPersistenceException {
        if (!exists(slot, slotEntry)) {
            throw new MCRPersistenceException("Couldn't delete non existence fileEntry.");
        }

        final int id = slot.getMCRObjectID().getNumberAsInteger();
        final FileEntry fileEntry = slotEntry.getEntry();

        try {
            final MCRFileStore store = getStore();

            MCRFileCollection col = store.retrieve(id);
            MCRStoredNode dir = (MCRStoredNode) col.getNodeByPath(slotEntry.getId());

            if (dir != null && dir.isDirectory()) {
                dir.delete();
            }
        } catch (Exception ex) {
            if (ex instanceof MCRException) {
                throw (MCRException) ex;
            }
            final String msg = "Exception while deleting of fileEntry " + slotEntry.getId() + " with filename "
                + fileEntry.getName();
            throw new MCRPersistenceException(msg, ex);
        }
    }

    public static void retrieve(final Slot slot, final SlotEntry<FileEntry> slotEntry) throws MCRPersistenceException {
        if (!exists(slot, slotEntry)) {
            throw new MCRPersistenceException("Couldn't retrieve non existence fileEntry.");
        }

        final int id = slot.getMCRObjectID().getNumberAsInteger();
        final FileEntry fileEntry = slotEntry.getEntry();

        try {
            final MCRFileStore store = getStore();

            MCRFileCollection col = store.retrieve(id);
            MCRStoredNode dir = (MCRStoredNode) col.getNodeByPath(slotEntry.getId());
            MCRStoredNode fileNode = (MCRStoredNode) dir.getNodeByPath(fileEntry.getName());
            fileEntry.setContent(fileNode.getContent());
        } catch (Exception ex) {
            if (ex instanceof MCRException) {
                throw (MCRException) ex;
            }
            final String msg = "Exception while retrieving of fileEntry " + slotEntry.getId() + " with filename "
                + fileEntry.getName();
            throw new MCRPersistenceException(msg, ex);
        }
    }
}

/*
 * This file is part of the Digitale Bibliothek Thüringen repository software.
 * Copyright (c) 2000 - 2017
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
package de.urmel_dl.dbt.rc.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.solr.client.solrj.SolrServerException;
import org.mycore.common.content.MCRContent;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.persistency.FileEntryManager;
import de.urmel_dl.dbt.rc.persistency.SlotManager;

/**
 * The RC API Resource.
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("rc")
public class RCResource {

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    /**
     * Returns the {@link SlotList}.
     *
     * @return the {@link SlotList}
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list() {
        return SLOT_MGR.getSlotList().getBasicSlots();
    }

    /**
     * Returns the {@link SlotList} for given search term.
     *
     * @param search the search term
     * @return the {@link SlotList}
     * @throws SolrServerException the solr server exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @GET
    @Path("{search:.+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list(@PathParam("search") String search) throws SolrServerException, IOException {
        return SLOT_MGR.getFilteredSlotList(search, null, null, null, new ArrayList<>()).getBasicSlots();
    }

    /**
     * Returns the {@link SlotList} for given search term and filter.
     *
     * @param search the search
     * @param filter the filter
     * @return the {@link SlotList}
     * @throws SolrServerException the solr server exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @GET
    @Path("{search:.+}/{filter:.+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list(@PathParam("search") String search, @PathParam("filter") String filter)
        throws SolrServerException, IOException {
        return SLOT_MGR.getFilteredSlotList(search, filter, null, null, new ArrayList<>()).getBasicSlots();
    }

    /**
     * Returns the {@link Slot} for given id.
     *
     * @param id the slot id
     * @return the {@link Slot}
     */
    @GET
    @Path("slot/{id:[0-9\\.]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @MCRRestrictedAccess(RCResourcePermission.class)
    public Slot slot(@PathParam("id") String id) {
        return SLOT_MGR.getSlotById(id);
    }

    /**
     * Returns the {@link Attendees} for given id.
     *
     * @param id the slot id
     * @return the {@link Attendees}
     */
    @GET
    @Path("slot/{id:[0-9\\.]+}/attendees")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @MCRRestrictedAccess(RCResourcePermission.class)
    public Attendees attendees(@PathParam("id") String id) {
        return SLOT_MGR.getAttendees(SLOT_MGR.getSlotById(id));
    }

    /**
     * Returns the {@link SlotEntry} for given slot id and entry id.
     *
     * @param slotId the slot id
     * @param entryId the entry id
     * @return the {@link SlotEntry}
     */
    @SuppressWarnings("rawtypes")
    @GET
    @Path("slot/{slotId:[0-9\\.]+}/entry/{entryId:.+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @MCRRestrictedAccess(RCResourcePermission.class)
    public SlotEntry entry(@PathParam("slotId") String slotId, @PathParam("entryId") String entryId) {
        return SLOT_MGR.getSlotById(slotId).getEntryById(entryId);
    }

    /**
     * Returns the file content for given slot id and entry id.
     *
     * @param slotId the slot id
     * @param entryId the entry id
     * @return the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("slot/{slotId:[0-9\\.]+}/file/{entryId:.+}")
    @Produces("*/*")
    @MCRRestrictedAccess(RCResourcePermission.class)
    public Response file(@PathParam("slotId") String slotId, @PathParam("entryId") String entryId) throws IOException {
        String msg = null;
        Slot slot = SLOT_MGR.getSlotById(slotId);
        if (slot != null) {
            SlotEntry<?> entry = slot.getEntryById(entryId);
            if (entry != null && FileEntry.class.isAssignableFrom(entry.getEntry().getClass())) {
                FileEntry fileEntry = (FileEntry) entry.getEntry();
                MCRContent content = Optional.ofNullable(fileEntry.getContent()).orElseGet(() -> {
                    FileEntryManager.retrieve(slot, (SlotEntry<FileEntry>) entry);
                    return fileEntry.getContent();
                });

                StreamingOutput streamer = output -> content.sendTo(output);

                return Response.ok(streamer)
                    .header("content-disposition", "attachment; filename = \"" + fileEntry.getName() + "\"")
                    .tag(fileEntry.getHash()).type(content.getMimeType()).build();
            }

            msg = String.format(Locale.ROOT, "File for slot id %s and entry id %s not found.", slotId, entryId);
        } else {
            msg = String.format(Locale.ROOT, "Slot with id %s not found.", slotId);
        }

        return Response.status(Response.Status.NOT_FOUND).entity(msg).type(MediaType.TEXT_PLAIN).build();
    }

}

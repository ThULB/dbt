/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2018
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.rc.rest.v2;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
import org.mycore.access.MCRAccessManager;

import de.urmel_dl.dbt.rc.datamodel.Attendee.Attendees;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotEntry;
import de.urmel_dl.dbt.rc.datamodel.slot.SlotList;
import de.urmel_dl.dbt.rc.datamodel.slot.entries.FileEntry;
import de.urmel_dl.dbt.rc.persistency.FileEntryManager;
import de.urmel_dl.dbt.rc.persistency.SlotManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("rc")
public class RCResource {

    private static final SlotManager SLOT_MGR = SlotManager.instance();

    @Context
    protected ContainerRequestContext requestContext;

    /**
     * Returns the {@link SlotList}.
     *
     * @param filter the list filter
     * @param sortBy the settings for sorting
     * @return the {@link SlotList}
     * @throws IOException
     * @throws SolrServerException
     */
    @GET
    @Path("/list")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list(@QueryParam("filter") String filter, @QueryParam("sortBy") List<String> sortBy)
        throws SolrServerException, IOException {
        return list(filter, 0, 50, sortBy);
    }

    /**
     * Returns the {@link SlotList} with given start and rows.
     *
     * @param filter the list filter
     * @param start the start offset
     * @param rows the num of rows tio return
     * @param sortBy the settings for sorting
     * @return the {@link SlotList}
     * @throws SolrServerException
     * @throws IOException
     */
    @GET
    @Path("/list/{start:\\d+}/{rows:\\d+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list(@QueryParam("filter") String filter, @PathParam("start") int start,
        @PathParam("rows") int rows, @QueryParam("sortBy") List<String> sortBy)
        throws SolrServerException, IOException {
        return list(null, filter, start, rows, sortBy);
    }

    /**
     * Returns the {@link SlotList} with given search string, filter string, start and rows.
     *
     * @param search the search string
     * @param filter the list filter
     * @param rows the num of rows tio return
     * @param sortBy the settings for sorting
     * @return the {@link SlotList}
     * @throws SolrServerException
     * @throws IOException
     */
    @GET
    @Path("/list/{search:.+}/{start:\\d+}/{rows:\\d+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SlotList list(@PathParam("search") String search, @QueryParam("filter") String filter,
        @PathParam("start") int start, @PathParam("rows") int rows,
        @QueryParam("sortBy") List<String> sortBy)
        throws SolrServerException, IOException {

        List<SortClause> sort = Optional
            .ofNullable(sortBy).map(sl -> sl.stream().map(se -> se.split(" ", 2)).filter(sp -> sp.length == 2)
                .map(sp -> new SortClause(sp[0], ORDER.valueOf(sp[1].toLowerCase(Locale.ROOT))))
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());

        return SLOT_MGR.getFilteredSlotList(search, filter, start, rows, sort).getBasicSlots();
    }

    /**
     * Returns the {@link Slot} for given id.
     *
     * @param id the slot id
     * @return the {@link Slot}
     */
    @GET
    @Path("{id:[0-9\\.]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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
    @Path("{id:[0-9\\.]+}/attendees")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Attendees attendees(@PathParam("id") String id) {
        return SLOT_MGR.getAttendees(SLOT_MGR.getSlotById(id));
    }

    /**
     * Returns the {@link FileEntry} for given slot id and entry id.
     *
     * @see FileEntryMessageBodyWriter FileEntryMessageBodyWriter for output file content
     * @param id the slot id
     * @param entryId the entry id
     * @return the {@link FileEntry}
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("{id:[0-9\\.]+}/file/{entryId:.+}")
    @Produces("*/*")
    public FileEntry file(@PathParam("id") String id, @PathParam("entryId") String entryId) {
        String msg = null;

        Slot slot = SLOT_MGR.getSlotById(id);
        if (slot != null) {
            requestContext.setProperty(Slot.class.getName(), slot);

            SlotEntry<?> entry = slot.getEntryById(entryId);
            if (entry != null && FileEntry.class.isAssignableFrom(entry.getEntry().getClass())) {
                FileEntry fileEntry = (FileEntry) entry.getEntry();
                Optional.ofNullable(fileEntry.getPath()).orElseGet(() -> {
                    FileEntryManager.retrieve(slot, (SlotEntry<FileEntry>) entry);
                    return fileEntry.getPath();
                });

                return fileEntry;
            }

            msg = String.format(Locale.ROOT, "File for slot id %s and entry id %s not found.", id, entryId);
        } else {
            msg = String.format(Locale.ROOT, "Slot with id %s not found.", id);
        }

        throw new WebApplicationException(msg, Response.Status.NOT_FOUND);
    }

    /**
     * Check is streaming is supported.
     * 
     * @param id
     * @param entryId
     * @return {@link Response.Status#OK} is possible or {@link Response.Status.NOT_IMPLEMENTED} isn't
     */
    @GET
    @Path("{id:[0-9\\.]+}/streamable/{entryId:.+}")
    public Response isStreamingSupported(@PathParam("id") String id, @PathParam("entryId") String entryId) {
        return Response
            .status(
                SlotManager.isStreamingSupported(id, entryId) ? Response.Status.OK : Response.Status.NOT_IMPLEMENTED)
            .build();
    }

    /**
     * Checks permission by type.
     *
     * @param type the permission type
     * @return {@link Response.Status#OK} if allowed or {@link Response.Status#FORBIDDEN}
     */
    @GET
    @Path("/permission/{type:.+}")
    public Response permission(@PathParam("type") String type) {
        return Response.status(MCRAccessManager.checkPermission(type) ? Response.Status.OK : Response.Status.FORBIDDEN)
            .build();
    }

    /**
     * Checks permission by type on given slot id.
     *
     * @param type the permission type
     * @param id the slot id
     * @return {@link Response.Status#OK} if allowed or {@link Response.Status#FORBIDDEN}
     */
    @GET
    @Path("/permission/{type:.+}/{id:[0-9\\.]+}")
    public Response permission(@PathParam("type") String type, @PathParam("id") String id) {
        return Response.status(SlotManager.checkPermission(SLOT_MGR.getSlotById(id).getMCRObjectID(), type)
            ? Response.Status.OK
            : Response.Status.FORBIDDEN)
            .build();
    }

}

/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2016
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
package de.urmel_dl.dbt.opc.resources;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("opc")
public class OPCResource {

    private static final Catalogues CATALOGUES = Catalogues.instance();

    @GET
    @Path("catalogues")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response catalogues(@Context HttpServletRequest request) {
        return response(request, () -> CATALOGUES);
    }

    @GET
    @Path("ikts")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response ikts(@Context HttpServletRequest request) {
        return response(request, () -> opc(null).getIKTList());
    }

    @GET
    @Path("ikts/{catalog:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response ikts(@Context HttpServletRequest request, @PathParam("catalog") String catalog) {
        return response(request, () -> opc(catalog).getIKTList());
    }

    @GET
    @Path("search/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response search(@Context HttpServletRequest request, @PathParam("term") String term) {
        return response(request, () -> search(opc(null), term));
    }

    @GET
    @Path("search/{ikt:[0-9]+}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response search(@Context HttpServletRequest request, @PathParam("ikt") int ikt,
        @PathParam("term") String term) {
        return response(request, () -> search(opc(null), ikt, term));
    }

    @GET
    @Path("search/{catalog:.*}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response search(@Context HttpServletRequest request, @PathParam("catalog") String catalog,
        @PathParam("term") String term) {
        return response(request, () -> search(opc(catalog), term));
    }

    @GET
    @Path("search/{catalog:.*}/{ikt:[0-9]+}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response search(@Context HttpServletRequest request, @PathParam("catalog") String catalog,
        @PathParam("ikt") int ikt,
        @PathParam("term") String term) {
        return response(request, () -> search(opc(catalog), ikt, term));
    }

    @GET
    @Path("family/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response family(@Context HttpServletRequest request, @PathParam("ppn") String ppn) {
        return response(request, () -> family(opc(null), ppn));
    }

    @GET
    @Path("family/{catalog:.*}/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response family(@Context HttpServletRequest request, @PathParam("catalog") String catalog,
        @PathParam("ppn") String ppn) {
        return response(request, () -> family(opc(catalog), ppn));
    }

    @GET
    @Path("record/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response record(@Context HttpServletRequest request, @PathParam("ppn") String ppn) {
        return response(request, () -> record(opc(null), ppn));
    }

    @GET
    @Path("record/{catalog:.*}/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public Response record(@Context HttpServletRequest request, @PathParam("catalog") String catalog,
        @PathParam("ppn") String ppn) {
        return response(request, () -> record(opc(catalog), ppn));
    }

    private Response response(HttpServletRequest request, Callable<Object> entityCallable) {
        try {
            Object entity = entityCallable.call();
            return Response.ok().status(Response.Status.OK)
                .entity(
                    new EntityFactory<>(entity).marshalByMediaType(Optional.ofNullable(request.getHeader("accept"))))
                .build();
        } catch (Exception e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    };

    private OPCConnector opc(String catalogId) throws Exception {
        Optional<Catalog> catalog = Optional.empty();

        if (catalogId != null) {
            catalog = Optional.ofNullable(CATALOGUES.getCatalogById(catalogId));
            if (!catalog.isPresent()) {
                catalog = Optional.ofNullable(CATALOGUES.getCatalogByISIL(catalogId));
            }
        }

        return catalog.isPresent() ? catalog.get().getOPCConnector() : new OPCConnector();
    }

    private Result search(OPCConnector opc, String term) throws Exception {
        return opc.search(term);
    }

    private Result search(OPCConnector opc, int ikt, String term) throws Exception {
        return opc.search(term, Integer.toString(ikt));
    }

    private Result family(OPCConnector opc, String ppn) throws Exception {
        return opc.family(ppn);
    }

    private Record record(OPCConnector opc, String ppn) throws Exception {
        return opc.getRecord(ppn);
    }

}

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
package de.urmel_dl.dbt.opc.resources;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * The OPC Rest API.
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("opc")
public class OPCResource {

    private static final Catalogues CATALOGUES = Catalogues.instance();

    private static final String MODS_STYLESHEET = "xsl/opc/transform/pica2mods.xsl";

    private static final String MODS_PARAM_SOURCE = "RecordIdSource";

    private static final String MODS_PARAM_PREFIX = "RecordIdPrefix";

    /**
     * Returns a list of {@link Catalogues}.
     *
     * @return the catalogues
     */
    @GET
    @Path("catalogues")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Catalogues catalogues() {
        return CATALOGUES;
    }

    /**
     * Returns the {@link IKTList}.
     *
     * @return the IKT list
     * @throws Exception the exception
     */
    @GET
    @Path("ikts")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IKTList ikts() throws Exception {
        return opc(null).getIKTList();
    }

    /**
     * Returns the {@link IKTList} for given catalog.
     *
     * @param catalog the catalog
     * @return the IKT list
     * @throws Exception the exception
     */
    @GET
    @Path("ikts/{catalog:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public IKTList ikts(@PathParam("catalog") String catalog) throws Exception {
        return opc(catalog).getIKTList();
    }

    /**
     * Search for term.
     *
     * @param term the term
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("search/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result search(@PathParam("term") String term) throws Exception {
        return search(opc(null), term);
    }

    /**
     * Search for term on given ikt.
     *
     * @param ikt the ikt
     * @param term the term
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("search/{ikt:[0-9]+}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result search(@PathParam("ikt") int ikt, @PathParam("term") String term) throws Exception {
        return search(opc(null), ikt, term);
    }

    /**
     * Search for term within given catalog.
     *
     * @param catalog the catalog
     * @param term the term
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("search/{catalog:.*}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result search(@PathParam("catalog") String catalog, @PathParam("term") String term) throws Exception {
        return search(opc(catalog), term);
    }

    /**
     * Search for term within given catalog and ikt.
     *
     * @param catalog the catalog
     * @param ikt the ikt
     * @param term the term
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("search/{catalog:.*}/{ikt:[0-9]+}/{term:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result search(@PathParam("catalog") String catalog, @PathParam("ikt") int ikt,
        @PathParam("term") String term) throws Exception {
        return search(opc(catalog), ikt, term);
    }

    /**
     * Returns the family for given PPN.
     *
     * @param ppn the ppn
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("family/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result family(@PathParam("ppn") String ppn) throws Exception {
        return family(opc(null), ppn);
    }

    /**
     * Returns the family for given PPN within catalog.
     *
     * @param catalog the catalog
     * @param ppn the ppn
     * @return the result
     * @throws Exception the exception
     */
    @GET
    @Path("family/{catalog:.*}/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Result family(@PathParam("catalog") String catalog, @PathParam("ppn") String ppn) throws Exception {
        return family(opc(catalog), ppn);
    }

    /**
     * Returns the {@link Record} for given PPN.
     *
     * @param ppn the ppn
     * @return the record
     * @throws Exception the exception
     */
    @GET
    @Path("record/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Record record(@PathParam("ppn") String ppn) throws Exception {
        return record(opc(null), ppn);
    }

    /**
     * Returns the {@link Record} for given catalog and PPN.
     *
     * @param catalog the catalog
     * @param ppn the ppn
     * @return the record
     * @throws Exception the exception
     */
    @GET
    @Path("record/{catalog:.*}/{ppn:[0-9X]+}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Record record(@PathParam("catalog") String catalog, @PathParam("ppn") String ppn) throws Exception {
        return record(opc(catalog), ppn);
    }

    /**
     * Returns a MODS XML for given PPN.
     *
     * @param ppn the ppn
     * @return the response
     * @throws Exception the exception
     */
    @GET
    @Path("mods/{ppn:[0-9X]+}")
    @Produces(MediaType.APPLICATION_XML)
    public Response mods(@PathParam("ppn") String ppn) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(MODS_PARAM_SOURCE, "DE-601");
        params.put(MODS_PARAM_PREFIX, "");

        return transformedResponse(record(ppn), MODS_STYLESHEET, params);
    }

    /**
     * Returns a MODS XML from given catalog with given PPN.
     *
     * @param catalog the catalog
     * @param ppn the ppn
     * @return the response
     * @throws Exception the exception
     */
    @GET
    @Path("mods/{catalog:.*}/{ppn:[0-9X]+}")
    @Produces(MediaType.APPLICATION_XML)
    public Response mods(@PathParam("catalog") String catalog, @PathParam("ppn") String ppn) throws Exception {
        Optional<Catalog> c = catalog(catalog);
        if (c.isPresent()) {
            Map<String, String> params = new HashMap<>();
            params.put(MODS_PARAM_SOURCE, c.get().getISIL().get(0));
            params.put(MODS_PARAM_PREFIX, "");

            return transformedResponse(record(catalog, ppn), MODS_STYLESHEET, params);
        }

        return mods(ppn);
    }

    private Optional<Catalog> catalog(String catalogId) throws Exception {
        Optional<Catalog> catalog = Optional.empty();

        if (catalogId != null) {
            catalog = Optional.ofNullable(CATALOGUES.getCatalogById(catalogId));
            if (!catalog.isPresent()) {
                catalog = Optional.ofNullable(CATALOGUES.getCatalogByISIL(catalogId));
            }
        }

        return catalog;
    }

    private OPCConnector opc(String catalogId) throws Exception {
        Optional<Catalog> catalog = catalog(catalogId);
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

    private <T> Response transformedResponse(T entity, String stylesheet, Map<String, String> parameters) {
        if (entity == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        try {
            MCRParameterCollector pc = new MCRParameterCollector();
            pc.setParameters(parameters);

            MCRXSLTransformer transformer = MCRXSLTransformer.getInstance(stylesheet);
            MCRContent result = transformer
                .transform(new MCRJDOMContent(new EntityFactory<>(entity).toDocument()), pc);

            final StreamingOutput so = (OutputStream os) -> result.sendTo(os);
            return Response.ok().status(Response.Status.OK).entity(so).build();
        } catch (Exception e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }
}

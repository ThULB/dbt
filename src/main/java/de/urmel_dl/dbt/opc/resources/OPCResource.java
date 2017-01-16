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

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;

/**
 * The OPC Rest API.
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("opc")
public class OPCResource {

    private static final Catalogues CATALOGUES = Catalogues.instance();

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
     * Record the {@link Record} for given catalog and PPN.
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

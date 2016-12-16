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
package de.urmel_dl.dbt.opc;

import static org.junit.Assert.assertNotNull;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mycore.common.MCRTestCase;

import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.datamodel.pica.Result;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestOPCConnector extends MCRTestCase {

    private final String OPC_URL = "http://opac.lbs-ilmenau.gbv.de";

    private final String OPC_DB = "1";

    @Test
    public void testIKTList() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        IKTList iktList = opc.getIKTList();
        assertNotNull(iktList);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(iktList).toDocument(), System.out);
    }

    @Test
    public void testSearchWoIKT() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        Result result = opc.search("papula");
        assertNotNull(result);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(result).toDocument(), System.out);
    }

    @Test
    public void testSearchWIKT() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        Result result = opc.search("papula", "1004");
        assertNotNull(result);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(result).toDocument(), System.out);
    }

    @Test
    public void testFamily() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        Result result = opc.family("785761829");
        assertNotNull(result);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(result).toDocument(), System.out);
    }

    @Test
    public void testRecord() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        Record record = opc.getRecord("785761829");
        assertNotNull(record);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(record).toDocument(), System.out);
    }

    @Test
    public void testRecordBasicCopy() throws Exception {
        OPCConnector opc = new OPCConnector(OPC_URL, OPC_DB);
        Record record = opc.getRecord("785761829");
        assertNotNull(record);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(record.getBasicCopy()).toDocument(),
            System.out);
    }

    @Test
    public void testCatalogues() throws Exception {
        Catalogues catalogues = Catalogues.instance();
        assertNotNull(catalogues);

        new XMLOutputter(Format.getPrettyFormat()).output(new EntityFactory<>(catalogues).toDocument(), System.out);
    }

    @Test
    public void testSetCatalog() throws Exception {
        Catalog catalog = Catalogues.instance().getCatalogByISIL("DE-ILM1");

        OPCConnector opc = new OPCConnector(catalog.getOPC().getURL(), catalog.getOPC().getDB());
        Result result = opc.search("papula");
        result.setCatalog(catalog);

        Document doc = new EntityFactory<>(result).toDocument();

        assertNotNull(doc.getRootElement().getAttribute("catalogId"));
    }
}

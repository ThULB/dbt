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
package de.urmel_dl.dbt.opc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.spi.container.TestContainerException;

import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.resources.OPCResource;
import de.urmel_dl.dbt.test.JerseyTestCase;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestOPCResource extends JerseyTestCase {

    private WebResource webResource;

    /**
     * @throws TestContainerException
     */
    public TestOPCResource() throws TestContainerException {
        super(OPCResource.class.getPackage().getName());
        webResource = resource();
    }

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();
        config.set("DBT.EntityFactory.Marshaller.eclipselink.json.include-root", true);
    }

    @Test
    public void testCatalogues() {
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            String response = webResource.path("opc/catalogues").accept(mt).get(String.class);
            assertNotNull(response);

            Catalogues catalogues = new EntityFactory<>(Catalogues.class).unmarshalByMediaType(response, mt);
            assertNotNull(catalogues);
        });
    }

    @Test
    public void testIKTs() {
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/ikts" + cat).accept(mt)
                    .get(String.class);
                assertNotNull(response);

                IKTList ikts = new EntityFactory<>(IKTList.class).unmarshalByMediaType(response, mt);
                assertNotNull(ikts);
            });
        });
    }

    @Test
    public void testSearch() {
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                Stream.of("", "/4").forEach(ikt -> {
                    String response = webResource.path("opc/search" + cat + ikt + "/duden").accept(mt)
                        .get(String.class);
                    assertNotNull(response);
                });
            });
        });
    }

    @Test
    public void testRecord() {
        final String PPN = "844149659";
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/record" + cat + "/" + PPN).accept(mt)
                    .get(String.class);
                assertNotNull(response);

                Record record = new EntityFactory<>(Record.class).unmarshalByMediaType(response, mt);
                assertEquals(PPN, record.getPPN());
            });
        });
    }

    @Test
    public void testFamily() {
        final String PPN = "837382513";
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/family" + cat + "/" + PPN).accept(mt)
                    .get(String.class);
                assertNotNull(response);
            });
        });
    }
}

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import de.urmel_dl.dbt.opc.datamodel.Catalogues;
import de.urmel_dl.dbt.opc.datamodel.IKTList;
import de.urmel_dl.dbt.opc.datamodel.pica.Record;
import de.urmel_dl.dbt.opc.resources.OPCResource;
import de.urmel_dl.dbt.rest.utils.EntityMessageBodyWriter;
import de.urmel_dl.dbt.test.JerseyTestCase;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestOPCResource extends JerseyTestCase {

    private WebTarget webResource;

    @Override
    protected Application configure() {
        return new ResourceConfig(OPCResource.class, EntityMessageBodyWriter.class);
    }

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();
        config.set("DBT.EntityFactory.Marshaller.eclipselink.json.include-root", true);
        webResource = target();
    }

    @Test
    public void testCatalogues() {
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            String response = webResource.path("opc/catalogues").request(mt).get(String.class);
            assertNotNull(response);

            Catalogues catalogues = new EntityFactory<>(Catalogues.class).unmarshalByMediaType(response, mt);
            assertNotNull(catalogues);
        });
    }

    @Test
    public void testIKTs() {
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/ikts" + cat).request(mt).get(String.class);
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
                    String response = webResource.path("opc/search" + cat + ikt + "/duden").request(mt)
                        .get(String.class);
                    assertNotNull(response);
                });
            });
        });
    }

    @Test
    public void testRecord() {
        final String PPN = "837382513";
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/record" + cat + "/" + PPN).request(mt).get(String.class);
                assertNotNull(response);

                Record record = new EntityFactory<>(Record.class).unmarshalByMediaType(response, mt);
                assertEquals(PPN, record.getPPN());
            });
        });
    }

    @Test
    public void testNullRecord() {
        final String PPN = "X44149659";
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                Response response = webResource.path("opc/record" + cat + "/" + PPN).request(mt).head();
                assertEquals(204, response.getStatus());
            });
        });
    }

    @Test
    public void testFamily() {
        final String PPN = "837382513";
        Stream.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).forEach(mt -> {
            Stream.of("", "/DE-27").forEach(cat -> {
                String response = webResource.path("opc/family" + cat + "/" + PPN).request(mt).get(String.class);
                assertNotNull(response);
            });
        });
    }

    @Test
    public void testMods() {
        Stream.of("", "/DE-27"/*, "/TEST"*/).forEach(cat -> {
            Stream.of(/*"333183061", "126649847", "13027304X", "560310706",*/ "625181425"/*, "877411565", "875185347"*/)
                .forEach(PPN -> {
                    String response = webResource.path("opc/mods" + cat + "/" + PPN).request(MediaType.APPLICATION_XML)
                        .get(String.class);
                    assertNotNull(response);

                    try {
                        SAXBuilder builder = new SAXBuilder();
                        Document doc = builder
                            .build(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
                        new XMLOutputter(Format.getPrettyFormat()).output(doc, System.out);
                    } catch (JDOMException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        });
    }
}

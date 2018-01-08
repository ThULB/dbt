/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2017
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
package de.urmel_dl.dbt.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import de.urmel_dl.dbt.media.entity.ConverterJob;
import de.urmel_dl.dbt.media.entity.ConverterJob.File;
import de.urmel_dl.dbt.media.resources.MediaServiceResource;
import de.urmel_dl.dbt.rest.utils.EntityMessageBodyReader;
import de.urmel_dl.dbt.rest.utils.EntityMessageBodyWriter;
import de.urmel_dl.dbt.test.JerseyTestCase;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestMediaServiceResource extends JerseyTestCase {

    private WebTarget webResource;

    @Override
    protected Application configure() {
        return new ResourceConfig(MediaServiceResource.class, EntityMessageBodyReader.class,
            EntityMessageBodyWriter.class);
    }

    @Override
    @Before()
    public void setUp() throws Exception {
        super.setUp();

        config.set("MCR.Media.Wowza.BaseURL", "http://localhost/woza");
        config.set("MCR.Media.Wowza.RTMPBaseURL", "rtmp://localhost/woza");
        config.set("MCR.Media.Wowza.SMILContentPathPrefix", "test/_definst_/smil:");
        config.set("MCR.Media.Wowza.ContentPathPrefix", "test/_definst_/mp4:");
        config.set("MCR.Media.Wowza.SharedSecred", "test");

        webResource = target();
    }

    @Test
    public void testCompleteCallbackRealWorldExample() {
        assertNotNull(this.getClass().getClassLoader().getResourceAsStream("converter-job.json"));

        Response response = webResource.path("media/completeCallback").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(this.getClass().getClassLoader().getResourceAsStream("converter-job.json"),
                MediaType.APPLICATION_JSON));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompleteCallbackJSON() {
        Response response = webResource.path("media/completeCallback").request(MediaType.APPLICATION_XML)
            .post(Entity.entity(new EntityFactory<>(buildJob()).toJSON(), MediaType.APPLICATION_JSON));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompleteCallbackXML() {
        Response response = webResource.path("media/completeCallback").request(MediaType.APPLICATION_XML)
            .post(Entity.entity(new EntityFactory<>(buildJob()).toXML(), MediaType.APPLICATION_XML));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    private ConverterJob buildJob() {
        ConverterJob job = new ConverterJob();

        job.setId("1234567890");
        job.setFileName("test.mp4");
        job.setExitValue(1);
        job.setRunning(false);
        job.setDone(true);
        job.setAddTime(Instant.parse("2017-11-03T10:30:22.850Z"));
        job.setStartTime(Instant.parse("2017-11-03T10:30:22.867Z"));
        job.setEndTime(Instant.parse("2017-11-03T10:30:28.029Z"));

        List<File> files = new ArrayList<>();

        File f1 = new File();
        f1.setFileName("test-1080p.mp4");
        f1.setFormat("mp4");
        f1.setScale("-2:1080");
        files.add(f1);

        File f2 = new File();
        f2.setFileName("test-720p.mp4");
        f2.setFormat("mp4");
        f2.setScale("-2:720");
        files.add(f2);

        job.setFiles(files);

        return job;
    }
}

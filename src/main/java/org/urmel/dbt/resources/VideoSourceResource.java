/*
 * $Id$ 
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.urmel.dbt.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.media.video.MCRMediaSourceProvider;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.sun.jersey.spi.resource.Singleton;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("video")
@Singleton
public class VideoSourceResource {

    private static final String[] EMPTY_ARRAY = new String[0];

    private static String wowzaHashParameter = MCRConfiguration2.getString("MCR.Media.Wowza.HashParameter")
        .orElse("wowzatokenhash");

    @GET
    @Path("sources/{derivateId:.*}/{path:.*}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSourcesAsXML(@PathParam("derivateId") String derivateId, @PathParam("path") String path) {
        try {
            return Response.ok().status(Response.Status.OK).entity(
                buildSources(derivateId, URLDecoder.decode(path, StandardCharsets.UTF_8.toString())))
                .build();
        } catch (IOException | URISyntaxException e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }

    @GET
    @Path("sources/{derivateId:.*}/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourcesAsJSON(@PathParam("derivateId") String derivateId, @PathParam("path") String path) {
        try {
            return Response.ok().status(Response.Status.OK)
                .entity(toJSON(buildSources(derivateId, URLDecoder.decode(path, StandardCharsets.UTF_8.toString()))))
                .build();
        } catch (IOException | URISyntaxException e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }

    private static VideoSources buildSources(String derivateId, String path) throws IOException, URISyntaxException {
        MCRMediaSourceProvider msp = new MCRMediaSourceProvider(derivateId, path, Optional.ofNullable("Safari"),
            () -> EMPTY_ARRAY);
        return new VideoSources(msp.getSources().stream().filter(s -> !s.getUri().contains("MCRFileNodeServlet"))
            .map(s -> new VideoSource(removeToken(s.getUri()), s.getType().getMimeType()))
            .collect(Collectors.toList()));
    }

    private static String removeToken(String src) {
        String queryString = src.lastIndexOf("?") != -1 ? src.substring(src.lastIndexOf("?")) : null;
        if (queryString != null) {
            String keyValue = wowzaHashParameter + "=[^&]*?";
            return src.replace(queryString,
                queryString.replaceAll("(&" + keyValue + "(?=(&|$))|^\\?" + keyValue + "(&|$))", ""));
        }

        return src;
    }

    private <T> String toJSON(T entity) throws JsonGenerationException, JsonMappingException, IOException {
        StringWriter sw = new StringWriter();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.writeValue(sw, entity);

        return sw.toString();
    }

    @XmlRootElement(name = "sources")
    static class VideoSources {
        @XmlElement(name = "source")
        private List<VideoSource> sources;

        protected VideoSources() {
        }

        protected VideoSources(List<VideoSource> sources) {
            this.sources = sources;
        }
    }

    @XmlRootElement(name = "source")
    static class VideoSource {
        @XmlAttribute(name = "src")
        private String src;

        @XmlAttribute(name = "type")
        private String type;

        protected VideoSource() {
        }

        protected VideoSource(String src, String type) {
            this.src = src;
            this.type = type;
        }
    }
}

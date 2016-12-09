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
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.media.video.MCRMediaSourceProvider;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/**
 * A Jersey resource to return all video sources for video file in derivate.
 * 
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("video")
public class VideoSourceResource {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String[] EMPTY_ARRAY = new String[0];

    public static final String DEFAULT_HASH_TYPE = "SHA-1";

    private static String wowzaHashParameter = MCRConfiguration2.getString("MCR.Media.Wowza.HashParameter")
        .orElse("wowzatokenhash");

    private static List<String> allowedIPs = MCRConfiguration2.getStrings("DBT.VideoSource.AllowedIPs");

    private static Optional<String> sharedSecret = MCRConfiguration2.getString("DBT.VideoSource.SharedSecret");

    @GET
    @Path("sources/{derivateId:.*}/{path:.*}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSourcesAsXML(@Context HttpServletRequest request, @PathParam("derivateId") String derivateId,
        @PathParam("path") String path,
        @QueryParam("accessToken") String accessToken) {
        try {
            int errorCode = checkPermission(derivateId, request.getRemoteAddr(), accessToken);
            if (errorCode == ErrorCode.OK) {
                return Response.ok().status(Response.Status.OK).entity(
                    buildSources(derivateId, URLDecoder.decode(path, StandardCharsets.UTF_8.toString())))
                    .build();
            } else {
                return Response.serverError().status(Response.Status.FORBIDDEN).entity(new ErrorCode(errorCode))
                    .build();
            }
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }

    @GET
    @Path("sources/{derivateId:.*}/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourcesAsJSON(@Context HttpServletRequest request, @PathParam("derivateId") String derivateId,
        @PathParam("path") String path,
        @QueryParam("accessToken") String accessToken) {
        try {
            int errorCode = checkPermission(derivateId, request.getRemoteAddr(), accessToken);
            if (errorCode == ErrorCode.OK) {
                return Response.ok().status(Response.Status.OK)
                    .entity(
                        toJSON(buildSources(derivateId, URLDecoder.decode(path, StandardCharsets.UTF_8.toString()))))
                    .build();
            } else {
                return Response.serverError().status(Response.Status.FORBIDDEN).entity(toJSON(new ErrorCode(errorCode)))
                    .build();
            }
        } catch (IOException | URISyntaxException | NoSuchAlgorithmException e) {
            final StreamingOutput so = (OutputStream os) -> e
                .printStackTrace(new PrintStream(os, false, StandardCharsets.UTF_8.toString()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(so).build();
        }
    }

    private static int checkPermission(String derivateId, String remoteAddr, String accessToken)
        throws NoSuchAlgorithmException {
        if (!sharedSecret.isPresent()) {
            throw new MCRConfigurationException("No shared secret defined!");
        }

        if (remoteAddr == null || !allowedIPs.contains(remoteAddr)) {
            LOGGER.debug("Remote address: {}", remoteAddr);
            return ErrorCode.CLIENT_NOT_ALLOWED;
        }

        if (MCRAccessManager.checkPermission(derivateId, MCRAccessManager.PERMISSION_READ)) {
            MCRObjectID mcrobj = MCRMetadataManager.getObjectId(MCRObjectID.getInstance(derivateId), 10,
                TimeUnit.MINUTES);

            if (!MCRAccessManager.checkPermission(mcrobj, MCRAccessManager.PERMISSION_READ)) {
                if (MIRAccessKeyManager.existsKeyPair(mcrobj)) {
                    if (accessToken != null) {
                        MIRAccessKeyPair keyPair = MIRAccessKeyManager.getKeyPair(mcrobj);
                        String token = buildAccessToken(keyPair.getReadKey());
                        LOGGER.debug("Generated access token: {}", token);
                        if (token.equalsIgnoreCase(accessToken)) {
                            return ErrorCode.OK;
                        }

                        return ErrorCode.BAD_ACCESSTOKEN;
                    }
                    return ErrorCode.NEED_ACCESSTOKEN;
                }

                return ErrorCode.OK;
            }

            return ErrorCode.NO_OBJECT_ACCESS;
        }

        return ErrorCode.NO_DERIVATE_ACCESS;
    }

    private static String buildAccessToken(String accessKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
        return String.format(Locale.ROOT, "%032X",
            new BigInteger(1, md.digest((sharedSecret + ":" + accessKey).getBytes(StandardCharsets.UTF_8))));
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

    /**
     * A wrapper of all video sources.
     * 
     * @author Ren\u00E9 Adler (eagle)
     *
     */
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

    /**
     * A wrapper for video source.
     * 
     * @author Ren\u00E9 Adler (eagle)
     *
     */
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

    /**
     * A wrapper for error codes.
     * 
     * @author Ren\u00E9 Adler (eagle)
     *
     */
    @XmlRootElement(name = "error")
    static class ErrorCode {
        // no error occurs
        public static final int OK = 0;

        // client address not in allowed list
        public static final int CLIENT_NOT_ALLOWED = 0x1000;

        // derivate access is not granted
        public static final int NO_DERIVATE_ACCESS = 0x1001;

        // object access is not granted
        public static final int NO_OBJECT_ACCESS = 0x1002;

        // need access token to grant access
        public static final int NEED_ACCESSTOKEN = 0x1003;

        // access token doesn't match
        public static final int BAD_ACCESSTOKEN = 0x1004;

        @XmlAttribute(name = "code")
        private int errorCode;

        protected ErrorCode() {
            this.errorCode = 0;
        }

        protected ErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }
    }
}

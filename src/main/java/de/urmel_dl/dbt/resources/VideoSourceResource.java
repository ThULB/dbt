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
package de.urmel_dl.dbt.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;

import de.urmel_dl.dbt.media.MediaService;
import de.urmel_dl.dbt.media.entity.Sources;
import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * A Jersey resource to return all video sources for video file in derivate.
 *
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@Path("video")
public class VideoSourceResource {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String DEFAULT_HASH_TYPE = "SHA-1";

    private static List<String> allowedIPs = MCRConfiguration2.getString("DBT.VideoSource.AllowedIPs")
        .map(MCRConfiguration2::splitValue).orElseGet(Stream::empty).collect(Collectors.toList());

    private static String sharedSecret = MCRConfiguration2.getString("DBT.VideoSource.SharedSecret")
        .orElseThrow(() -> new MCRConfigurationException("No shared secret defined!"));

    @GET
    @Path("sources/{derivateId:.*}/{path:.*}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getSourcesAsXML(@Context HttpServletRequest request, @PathParam("derivateId") String derivateId,
        @PathParam("path") String path,
        @QueryParam("accessToken") String accessToken) {
        try {
            int errorCode = checkPermission(derivateId, request.getRemoteAddr(), accessToken);
            if (errorCode == ErrorCode.OK) {
                return Response.ok().status(Response.Status.OK)
                    .entity(new EntityFactory<>(
                        buildSources(derivateId, URLDecoder.decode(path, StandardCharsets.UTF_8.toString())))
                            .marshalByMediaType(Optional.ofNullable(request.getHeader("accept"))))
                    .build();
            } else {
                return Response.serverError().status(Response.Status.FORBIDDEN)
                    .entity(new EntityFactory<>(new ErrorCode(errorCode))
                        .marshalByMediaType(Optional.ofNullable(request.getHeader("accept"))))
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

                return ErrorCode.NO_OBJECT_ACCESS;
            }

            return ErrorCode.OK;
        }

        return ErrorCode.NO_DERIVATE_ACCESS;
    }

    private static String buildAccessToken(String accessKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
        return DatatypeConverter
            .printHexBinary(md.digest((sharedSecret + ":" + accessKey).getBytes(StandardCharsets.UTF_8)));
    }

    private static Sources buildSources(String derivateId, String path) throws IOException, URISyntaxException {
        String mediaId = MediaService.buildInternalId(derivateId + "_" + path);
        Sources sources = Sources.build(mediaId, MediaService.getMediaFiles(mediaId));
        sources.setSources(sources.getSources().stream()
            .filter(s -> !"video/mp4".equals(s.getType())).collect(Collectors.toList()));
        return sources;
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

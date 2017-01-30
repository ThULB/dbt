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
package de.urmel_dl.dbt.rc.resources;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.PathSegment;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;

import de.urmel_dl.dbt.rc.datamodel.slot.Slot;
import de.urmel_dl.dbt.rc.persistency.SlotManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class RCResourcePermission implements MCRResourceAccessChecker {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String DEFAULT_HASH_TYPE = "SHA-1";

    private static final String HEADER_ACCESS_TOKEN = "Access-Token";

    private static Optional<String> sharedSecret = MCRConfiguration2.getString("DBT.RC.SharedSecret");

    /* (non-Javadoc)
     * @see org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker#isPermitted(com.sun.jersey.spi.container.ContainerRequest)
     */
    @Override
    public boolean isPermitted(ContainerRequestContext context) {
        if (context.getUriInfo().getPath().contains("/slot/")) {
            String slotId = null;
            Iterator<PathSegment> it = context.getUriInfo().getPathSegments().iterator();
            while (it.hasNext() && slotId == null) {
                if ("slot".equals(it.next().getPath())) {
                    slotId = it.next().getPath();
                }
            }

            if (slotId != null) {
                if (!sharedSecret.isPresent()) {
                    throw new MCRConfigurationException("No shared secret defined!");
                }

                LOGGER.info("Check permission for slot id {}", slotId);

                Slot slot = SlotManager.instance().getSlotById(slotId);
                String accessToken = context.getHeaderString(HEADER_ACCESS_TOKEN);

                if (MIRAccessKeyManager.existsKeyPair(slot.getMCRObjectID())) {
                    if (accessToken != null) {
                        MIRAccessKeyPair keyPair = MIRAccessKeyManager.getKeyPair(slot.getMCRObjectID());
                        try {
                            if (buildAccessToken(keyPair.getReadKey()).equalsIgnoreCase(accessToken)
                                || buildAccessToken(keyPair.getWriteKey()).equalsIgnoreCase(accessToken)) {
                                return true;
                            } else {
                                LOGGER.warn("Invalid access token: {}", accessToken);
                            }
                        } catch (NoSuchAlgorithmException e) {
                            LOGGER.warn("Couldn't build token.", e);
                        }
                    } else {
                        LOGGER.warn("Missing access token header.");
                    }
                }

                return false;
            }
        }
        return true;
    }

    private static String buildAccessToken(String accessKey) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
        return DatatypeConverter
            .printHexBinary(md.digest((sharedSecret.get() + ":" + accessKey).getBytes(StandardCharsets.UTF_8)));
    }

}

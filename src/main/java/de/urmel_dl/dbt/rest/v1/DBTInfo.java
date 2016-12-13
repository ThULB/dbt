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
package de.urmel_dl.dbt.rest.v1;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mycore.common.MCRCoreVersion;
import org.mycore.mir.common.MIRCoreVersion;

import de.urmel_dl.dbt.common.DBTVersion;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
@Path("/v1/dbt")
public class DBTInfo {

    @GET
    @Path("version")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Properties getGitInfos() {
        Properties properties = new Properties();
        properties.putAll(MCRCoreVersion.getVersionProperties().entrySet().stream()
            .collect(Collectors.toMap(e -> "mycore." + e.getKey(), Map.Entry::getValue)));
        properties.putAll(MIRCoreVersion.getVersionProperties().entrySet().stream()
            .collect(Collectors.toMap(e -> "mir." + e.getKey(), Map.Entry::getValue)));
        properties.putAll(DBTVersion.getVersionProperties().entrySet().stream()
            .collect(Collectors.toMap(e -> "dbt." + e.getKey(), Map.Entry::getValue)));
        return properties;
    }
}

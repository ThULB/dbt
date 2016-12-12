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
package de.urmel_dl.dbt.utils;

import java.util.Map;

import org.mycore.user2.MCRUserAttributeConverter;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class DisplayNameConverter implements MCRUserAttributeConverter<String, String> {

    @Override
    public String convert(String value, String separator, Map<String, String> valueMapping) throws Exception {
        return new String(((String) value).getBytes("ISO-8859-1"), "UTF-8");
    }

}

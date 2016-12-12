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
package de.urmel_dl.dbt.rc.datamodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents the {@link Warning#type} type.
 * 
 * @author René Adler (eagle)
 */
@XmlType(name = "warningType")
@XmlEnum
public enum WarningType {

    /**
     * A warning at period end (semester end) date.
     */
    @XmlEnumValue("periodEnd")
    PERIODEND("periodEnd"),

    /**
     * A warning at lecture end date.
     */
    @XmlEnumValue("lectureEnd")
    LECTUREEND("lectureEnd"),

    /**
     * A warning to a valid to date.
     */
    @XmlEnumValue("validTo")
    VALIDTO("validTo");

    private final String value;

    WarningType(final String value) {
        this.value = value;
    }

    /**
     * Returns the set status.
     * 
     * @return the set status
     */
    public String value() {
        return value;
    }

    /**
     * Returns the Slot status from given value.
     * 
     * @param value the status value
     * @return the slot status
     */
    public static WarningType fromValue(final String value) {
        for (WarningType type : WarningType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value);
    }
}

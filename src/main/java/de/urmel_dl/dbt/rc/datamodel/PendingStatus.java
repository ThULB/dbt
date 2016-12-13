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

import de.urmel_dl.dbt.annotation.EnumValue;
import de.urmel_dl.dbt.rc.datamodel.slot.Slot;

/**
 * Represents the {@link Slot#Slot()} status.
 * 
 * @author René Adler (eagle)
 */
@XmlType(name = "pendingStatus")
@XmlEnum
public enum PendingStatus {

    /**
     * Stands for a active slot.
     */
    @XmlEnumValue("active")
    ACTIVE("active"),

    /**
     * Stands for a archived slot for late reactivation.
     */
    @XmlEnumValue("archived")
    ARCHIVED("archived"),
    
    /**
     * Stands for an reserved slot.
     */
    @XmlEnumValue("reserved")
    RESERVED("reserved"),
    
    /**
     * Stands for an free and unused slot.
     */
    @XmlEnumValue("free")
    FREE("free"),
    
    /**
     * Stands for a archived slot for late reactivation.
     */
    @EnumValue(visible = false)
    @XmlEnumValue("validating")
    VALIDATING("validating"),
    
    /**
     * Stands for a slot with pending ownership transfer.
     */
    @EnumValue(visible = false)
    @XmlEnumValue("ownerTransfer")
    OWNERTRANSFER("ownerTransfer");

    private final String value;

    PendingStatus(final String value) {
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
    public static PendingStatus fromValue(final String value) {
        for (PendingStatus status : PendingStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(value);
    }
}

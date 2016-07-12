/*
 * $Id$
 */
package org.urmel.dbt.rc.datamodel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.urmel.dbt.annotation.EnumValue;
import org.urmel.dbt.rc.datamodel.slot.Slot;

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

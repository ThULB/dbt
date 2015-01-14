/*
 * $Id: WarningType.java 2116 2014-10-01 12:14:43Z adler $
 */
package org.urmel.dbt.rc.datamodel;

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

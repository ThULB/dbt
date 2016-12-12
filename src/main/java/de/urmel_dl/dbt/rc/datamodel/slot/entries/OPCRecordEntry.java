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
package de.urmel_dl.dbt.rc.datamodel.slot.entries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.urmel_dl.dbt.opc.datamodel.pica.Record;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "opcrecord")
@XmlAccessorType(XmlAccessType.NONE)
public class OPCRecordEntry implements Serializable {

    private static final long serialVersionUID = -4540182175442477505L;

    private String epn;
    
    private Boolean deletionMark;

    private Record record;

    private String comment;

    /**
     * @return the epn
     */
    @XmlAttribute(name = "epn")
    public String getEPN() {
        return epn;
    }

    /**
     * @param epn the epn to set
     */
    public void setEPN(String epn) {
        this.epn = epn;
    }

    /**
     * @return the deletionMark
     */
    @XmlAttribute(name = "deleted")
    public Boolean getDeletionMark() {
        return deletionMark;
    }

    /**
     * @param deletionMark the deletionMark to set
     */
    public void setDeletionMark(Boolean deletionMark) {
        this.deletionMark = deletionMark;
    }

    /**
     * @return the record
     */
    @XmlElement(name = "record", namespace="http://www.mycore.de/dbt/opc/pica-xml-1-0.xsd")
    public Record getRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * @return the comment
     */
    @XmlElement(name = "comment")
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OPCRecordEntry";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((epn == null) ? 0 : epn.hashCode());
        result = prime * result + ((record == null) ? 0 : record.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OPCRecordEntry)) {
            return false;
        }
        OPCRecordEntry other = (OPCRecordEntry) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (epn == null) {
            if (other.epn != null) {
                return false;
            }
        } else if (!epn.equals(other.epn)) {
            return false;
        }
        if (record == null) {
            if (other.record != null) {
                return false;
            }
        } else if (!record.equals(other.record)) {
            return false;
        }
        return true;
    }
}

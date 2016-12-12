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
package de.urmel_dl.dbt.opc.datamodel.pica;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "field")
public class PPField {

    private String tag;

    private String occurrence;

    private List<PPSubField> subfields = new ArrayList<PPSubField>();

    /**
     * @return the tag
     */
    @XmlAttribute(name = "tag", required = true)
    public String getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /**
     * @return the occurrence
     */
    @XmlAttribute
    public String getOccurrence() {
        return occurrence;
    }

    /**
     * @param occurrence the occurrence to set
     */
    public void setOccurrence(final String occurrence) {
        this.occurrence = occurrence;
    }

    /**
     * @return the subfields
     */
    public List<PPSubField> getSubfields() {
        return subfields;
    }

    /**
     * @param subfields the subfields to set
     */
    @XmlElement(name = "subfield")
    public void setSubfields(final List<PPSubField> subfields) {
        this.subfields = subfields;
    }

    public void addSubfield(final PPSubField subfield) {
        subfields.add(subfield);
    }

    public PPSubField getSubfieldByCode(final String code) {
        for (PPSubField ppSubfield : subfields) {
            if (code.equals(ppSubfield.getCode())) {
                return ppSubfield;
            }
        }

        return null;
    }
}

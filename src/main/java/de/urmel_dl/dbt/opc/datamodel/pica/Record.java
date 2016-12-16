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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRException;

import de.urmel_dl.dbt.opc.OPCConnector;

/**
 * The Class Record.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "record")
public class Record {

    private OPCConnector connection;

    private String ppn;

    private List<PPField> fields;

    protected Record() {
    }

    /**
     * Instantiates a new record.
     *
     * @param ppn the ppn
     */
    public Record(final String ppn) {
        this.setPPN(ppn);
    }

    /**
     * Instantiates a new record.
     *
     * @param connection the connection
     * @param ppn the ppn
     */
    public Record(final OPCConnector connection, final String ppn) {
        this.setConnection(connection);
        this.setPPN(ppn);
    }

    /**
     * Returns the OPC connection.
     *
     * @return the opc
     */
    public OPCConnector getConnection() {
        return connection;
    }

    /**
     * Set the OPC connection.
     *
     * @param connection the opc to set
     */
    protected void setConnection(final OPCConnector connection) {
        this.connection = connection;
    }

    /**
     * Returns the PPN.
     *
     * @return the ppn
     */
    @XmlAttribute(name = "ppn", required = true)
    public String getPPN() {
        return ppn;
    }

    /**
     * Set the PPN.
     *
     * @param ppn the ppn to set
     */
    public void setPPN(final String ppn) {
        this.ppn = ppn;
    }

    /**
     * Returns a list of fields.
     *
     * @return the fields
     */
    @XmlElement(name = "field")
    public List<PPField> getFields() {
        return fields;
    }

    /**
     * Set a list of fields.
     *
     * @param fields the fields to set
     */
    public void setFields(final List<PPField> fields) {
        this.fields = fields;
    }

    /**
     * Adds the field.
     *
     * @param field the field
     */
    public void addField(final PPField field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
    }

    /**
     * Search field by given tag.
     *
     * @param tag the pica plus tag
     * @return PPField
     */
    public PPField getFieldByTag(final String tag) {
        for (PPField ppField : fields) {
            if (tag.equals(ppField.getTag())) {
                return ppField;
            }
        }

        return null;
    }

    /**
     * Search fields by given tag.
     *
     * @param tag the pica plus tag
     * @return a List of PPField
     */
    public List<PPField> getFieldsByTag(final String tag) {
        final List<PPField> res = new ArrayList<>();

        for (PPField ppField : fields) {
            if (tag.equals(ppField.getTag())) {
                res.add(ppField);
            }
        }

        return res;
    }

    /**
     * Returns a copy of current {@link Record} without local fields.
     *
     * @return a basic copy of current record
     */
    public Record getBasicCopy() {
        final Record copy = new Record();

        copy.connection = this.connection;
        copy.ppn = this.ppn;

        // local title fields
        final List<String> excluded_fields = Arrays.asList("101@", "201@", "201B", "201D", "201F", "201U", "203@",
            "208@", "209A", "209B", "209C", "209F", "209G", "209J", "209O", "209R", "209W", "220B", "220C", "220D",
            "231@", "231A", "231B", "231C", "231D", "237A", "237B", "244Z", "245P", "245Z");

        copy.fields = this.fields.stream().filter(f -> !excluded_fields.contains(f.getTag()))
            .collect(Collectors.toList());

        return copy;
    }

    /**
     * Load the record.
     */
    public void load() {
        load(false);
    }

    /**
     * Load the record.
     *
     * @param force the force
     */
    public void load(final boolean force) {
        if (connection != null && (force || fields == null || fields.size() == 0)) {
            try {
                setFields(connection.getPPFields(getPPN()));
            } catch (final Exception e) {
                throw new MCRException("Couldn't retrieve PICA+ fields for " + getPPN() + ".", e);
            }
        }
    }
}

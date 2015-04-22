/*
 * $Id: Record.java 2160 2014-12-11 12:45:09Z adler $ 
 * $Revision$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */
package org.urmel.dbt.opc.datamodel.pica;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRException;
import org.urmel.dbt.opc.OPCConnector;

@XmlRootElement(name = "record")
public class Record {

    private OPCConnector connection;

    private String ppn;

    private List<PPField> fields = new ArrayList<PPField>();

    protected Record() {
    }

    public Record(final String ppn) {
        this.setPPN(ppn);
    }

    public Record(final OPCConnector connection, final String ppn) {
        this.setConnection(connection);
        this.setPPN(ppn);
    }

    /**
     * @return the opc
     */
    public OPCConnector getConnection() {
        return connection;
    }

    /**
     * @param connection the opc to set
     */
    protected void setConnection(final OPCConnector connection) {
        this.connection = connection;
    }

    /**
     * @return the ppn
     */
    @XmlAttribute(name = "ppn", required = true)
    public String getPPN() {
        return ppn;
    }

    /**
     * @param ppn the ppn to set
     */
    public void setPPN(final String ppn) {
        this.ppn = ppn;
    }

    /**
     * @return the fields
     */
    @XmlElement(name = "field")
    public List<PPField> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(final List<PPField> fields) {
        this.fields = fields;
    }

    public void addField(final PPField field) {
        fields.add(field);
    }

    /**
     * Search field by given tag.
     * 
     * @param tag
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
     * @param tag
     * @return a List of PPField
     */
    public List<PPField> getFieldsByTag(final String tag) {
        final List<PPField> res = new ArrayList<PPField>();

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

        for (PPField field : this.fields) {
            if (!excluded_fields.contains(field.getTag())) {
                copy.fields.add(field);
            }
        }

        return copy;
    }

    public void load() {
        load(false);
    }

    public void load(final boolean force) {
        if (connection != null && (force || fields.size() == 0)) {
            try {
                setFields(connection.getPPFields(getPPN()));
            } catch (final Exception e) {
                throw new MCRException("Couldn't retrieve PICA+ fields for " + getPPN() + ".", e);
            }
        }
    }
}

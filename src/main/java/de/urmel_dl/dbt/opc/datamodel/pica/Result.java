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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.urmel_dl.dbt.opc.OPCConnector;
import de.urmel_dl.dbt.opc.datamodel.Catalog;
import de.urmel_dl.dbt.opc.datamodel.Catalogues;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "result")
public class Result {

    private OPCConnector connection;

    private Catalog catalog;

    private List<Record> records = new ArrayList<>();

    protected Result() {
    }

    public Result(final OPCConnector opc) {
        setConnection(opc);
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

    @XmlAttribute(name = "url", required = true)
    public URL getURL() {
        return connection.getURL();
    }

    @XmlAttribute(name = "db", required = true)
    public String getDB() {
        return connection.getDB();
    }

    /**
     * @return the catalog
     */
    public Catalog getCatalog() {
        return catalog;
    }

    /**
     * @param catalog the catalog to set
     */
    public void setCatalog(final Catalog catalog) {
        this.catalog = catalog;
    }

    @XmlAttribute(name = "catalogId")
    protected String getCatalogId() {
        return catalog != null ? catalog.getIdentifier() : null;
    }

    protected void setCatalogId(final String id) {
        catalog = Catalogues.instance().getCatalogById(id);
    }

    /**
     * @return the records
     */
    @XmlElement(name = "record")
    public List<Record> getRecords() {
        return records;
    }

    /**
     * @param records the records to set
     */
    public void setRecords(final List<Record> records) {
        this.records = records;
    }

    public void addRecord(final Record record) {
        if (record.getConnection() == null) {
            record.setConnection(getConnection());
        }

        records.add(record);
    }
}

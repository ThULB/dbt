/*
 * $Id$ 
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.urmel.dbt.opc.OPCConnector;
import org.urmel.dbt.opc.datamodel.Catalog;
import org.urmel.dbt.opc.datamodel.Catalogues;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.NONE)
public class Result {

    private OPCConnector connection;

    private Catalog catalog;

    private List<Record> records = new ArrayList<Record>();

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

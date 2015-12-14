/*
 * $Id$ 
 * $Revision$ $Date$
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
package org.urmel.dbt.rc.datamodel.slot;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.urmel.dbt.rc.datamodel.TypedDate;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotEntry<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static String lastID = null;

    private String id;

    private TypedDate created = new TypedDate(TypedDate.Type.CREATED);

    private TypedDate modified = new TypedDate(TypedDate.Type.MODIFIED);

    private V entry;

    private static synchronized String findNewID() {
        String newID = null;
        while ((newID = buildNewID()).equals(lastID)) {
            noop();
        }
        return (lastID = newID);
    }

    private static String buildNewID() {
        final StringBuffer buf = new StringBuffer();
        buf.append(Long.toString(System.nanoTime(), 36));
        return buf.reverse().toString();
    }

    /**
     * @return the id
     */
    @XmlAttribute(name = "id")
    public String getId() {
        if (id == null) {
            id = findNewID();
        }
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    private void setDate(final TypedDate entryDate) {
        switch (entryDate.getType()) {
        case CREATED:
            created = entryDate;
            break;
        case MODIFIED:
            modified = entryDate;
            break;
        default:
            break;
        }
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        if (created == null || created.getDate() == null) {
            setCreated(new Date());
        }
        return created.getDate();
    }

    /**
     * @param created the created to set
     */
    public void setCreated(final Date created) {
        if (created != null) {
            this.created = new TypedDate(TypedDate.Type.CREATED, new Date(created.getTime()));
        }
    }

    /**
     * @return the created
     */
    @XmlElement(name = "date", required = true)
    public TypedDate getDateCreated() {
        if (created == null || created.getDate() == null) {
            setCreated(new Date());
        }
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setDateCreated(final TypedDate created) {
        setDate(created);
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        if (modified == null || modified.getDate() == null) {
            setModified(new Date());
        }
        return modified.getDate();
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(final Date modified) {
        if (modified != null) {
            this.modified = new TypedDate(TypedDate.Type.MODIFIED, new Date(modified.getTime()));
        }
    }

    /**
     * @return the changed
     */
    @XmlElement(name = "date", required = true)
    public TypedDate getDateModified() {
        if (modified == null || modified.getDate() == null) {
            setModified(new Date());
        }
        return modified;
    }

    /**
     * @param modified the changed to set
     */
    public void setDateModified(final TypedDate modified) {
        setDate(modified);
    }

    /**
     * @return the entry
     */
    @XmlAnyElement(lax = true)
    public V getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(final V entry) {
        this.entry = entry;
    }

    private static int noop() {
        return 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SlotEntry [id=" + getId() + ", " + (created != null ? "created=" + created + ", " : "")
                + (modified != null ? "modified=" + modified + ", " : "") + (entry != null ? "entry=" + entry : "")
                + "]";
    }
}

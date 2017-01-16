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
package de.urmel_dl.dbt.rc.datamodel.slot;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;

import de.urmel_dl.dbt.utils.EntityFactory;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "entry-types")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotEntryTypes implements Serializable {

    private static final long serialVersionUID = 6722322996282291627L;

    private static SlotEntryTypes singleton;

    private List<SlotEntryType> entryTypes;

    /**
     * Returns a singleton instance of {@link SlotEntryTypes} with configured {@link SlotEntryType}s.
     *
     * @return a instance of configured {@link SlotEntryType}
     */
    public static SlotEntryTypes instance() {
        if (singleton == null) {
            final Element xml = MCRURIResolver.instance().resolve("resource:slot-entry-types.xml");
            if (xml != null) {
                singleton = new EntityFactory<>(SlotEntryTypes.class).fromElement(xml);
            }
        }

        return singleton;
    }

    /**
     * @return the entryTypes
     */
    @XmlElement(name = "entry-type")
    public List<SlotEntryType> getEntryTypes() {
        return entryTypes;
    }

    /**
     * @param entryTypes the entryTypes to set
     */
    protected void setEntryTypes(final List<SlotEntryType> entryTypes) {
        this.entryTypes = entryTypes;
    }

    /**
     * Returns the {@link SlotEntryType} for given entryType parameter or <code>null</code> if nothing found.
     *
     * @param entryType the entryType to search
     * @return an {@link SlotEntryType} or <code>null</code> if nothing was found
     */
    public SlotEntryType getEntryType(final String entryType) {
        for (SlotEntryType t : entryTypes) {
            if (entryType.equals(t.getName())) {
                return t;
            }
        }

        return null;
    }
}

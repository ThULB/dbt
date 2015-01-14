/*
 * $Id: SlotEntryTypes.java 2134 2014-12-08 14:37:17Z adler $ 
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
package org.urmel.dbt.rc.datamodel;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;
import org.urmel.dbt.rc.utils.SlotEntryTypesTransformer;

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
                singleton = SlotEntryTypesTransformer.buildSlotEntryTypes(xml);
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
    void setEntryTypes(final List<SlotEntryType> entryTypes) {
        this.entryTypes = entryTypes;
    }
}

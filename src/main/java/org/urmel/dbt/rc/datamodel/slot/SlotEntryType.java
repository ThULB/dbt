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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "entry-type")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotEntryType implements Serializable {

    private static final long serialVersionUID = 6722322996282291627L;

    private String cls;

    private I18N i18n;

    /**
     * @return the cls
     */
    @XmlAttribute(name = "class")
    public String getCls() {
        return cls;
    }

    /**
     * @param cls the cls to set
     */
    void setCls(final String cls) {
        this.cls = cls;
    }

    /**
     * @return the name
     */
    @XmlAttribute(name = "name")
    public String getName() {
        try {
            final Class<?> clazz = getEntryClass();
            final XmlRootElement rootElm = clazz.getAnnotation(XmlRootElement.class);
            return rootElm.name();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return the entry class
     * @throws ClassNotFoundException 
     */
    public Class<?> getEntryClass() throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(getCls());
    }

    /**
     * @return the i18n
     */
    @XmlElement(name = "i18n")
    public I18N getI18n() {
        return i18n;
    }

    /**
     * @param i18n the i18n to set
     */
    void setI18n(final I18N i18n) {
        this.i18n = i18n;
    }

    @XmlRootElement(name = "i18n")
    static class I18N {
        @XmlAttribute
        public String single;
        @XmlAttribute
        public String multiple;
    }
}

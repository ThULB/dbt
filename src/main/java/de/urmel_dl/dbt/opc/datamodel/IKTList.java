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
package de.urmel_dl.dbt.opc.datamodel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class IKTList.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "iktlist")
public class IKTList {

    private List<IKT> ikts = new ArrayList<>();

    /**
     * Return a list of IKTs.
     *
     * @return the ikts
     */
    @XmlElement(name = "ikt")
    public List<IKT> getIKTs() {
        return ikts;
    }

    /**
     * Set a list of IKTs.
     *
     * @param ikts the ikts to set
     */
    public void setIKTs(List<IKT> ikts) {
        this.ikts = ikts;
    }

    /**
     * Adds the IKT.
     *
     * @param ikt the ikt
     */
    public void addIKT(IKT ikt) {
        ikts.add(ikt);
    }

    /**
     * Gets the IKT by key.
     *
     * @param key the key
     * @return the IKT by key
     */
    public IKT getIKTByKey(String key) {
        for (IKT ikt : ikts) {
            if (key.equals(ikt.getKey())) {
                return ikt;
            }
        }

        return null;
    }
}

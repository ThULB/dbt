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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The Class PPSubField.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "subfield")
public class PPSubField {

    private String code;

    private String content;

    /**
     * Returns the code.
     *
     * @return the code
     */
    @XmlAttribute(name = "code", required = true)
    public String getCode() {
        return code;
    }

    /**
     * Set the code.
     *
     * @param code the code to set
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * Returns the content.
     *
     * @return the content
     */
    @XmlValue
    public String getContent() {
        return content;
    }

    /**
     * Set the content.
     *
     * @param content the content to set
     */
    public void setContent(final String content) {
        this.content = content;
    }
}

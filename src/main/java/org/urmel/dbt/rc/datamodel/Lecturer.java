/*
 * $Id$
 */
package org.urmel.dbt.rc.datamodel;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author René Adler (eagle)
 */
@XmlRootElement(name = "lecturer")
public class Lecturer implements Serializable {

    private static final long serialVersionUID = 8985508188757389305L;

    private String name;

    private String email;

    private String origin;

    /**
     * @return the name
     */
    @XmlAttribute(name = "name", required = true)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    @XmlAttribute(name = "email", required = true)
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @return the origin
     */
    @XmlAttribute(name = "origin")
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(final String origin) {
        this.origin = origin;
    }
}

/*
 * $Id$
 */
package org.urmel.dbt.rc.datamodel;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author René Adler (eagle)
 */
@XmlRootElement(name = "lecturer")
public class Lecturer extends Person implements Serializable {

    private static final long serialVersionUID = 8985508188757389305L;
}

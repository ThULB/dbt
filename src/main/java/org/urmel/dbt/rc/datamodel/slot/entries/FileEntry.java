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
package org.urmel.dbt.rc.datamodel.slot.entries;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.IOUtils;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "file")
@XmlAccessorType(XmlAccessType.NONE)
public class FileEntry implements Serializable {

    public static final String DEFAULT_HASH_TYPE = "SHA-1";

    private static final long serialVersionUID = 2749951822001215240L;

    private String name;

    private boolean copyrighted;

    private String hash;

    private long size = 0;

    private byte[] content;

    private String comment;

    /**
     * @return the name
     */
    @XmlAttribute(name = "name", required = true)
    public String getName() {
        return name;
    }

    /**
     * @param name the id to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the copyrighted
     */
    @XmlAttribute(name = "copyrighted")
    public boolean isCopyrighted() {
        return copyrighted;
    }

    /**
     * @param copyrighted the copyrighted to set
     */
    public void setCopyrighted(boolean copyrighted) {
        this.copyrighted = copyrighted;
    }

    /**
     * @return the hash
     */
    @XmlAttribute(name = "hash")
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return the size
     */
    @XmlAttribute(name = "size")
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(final byte[] content) {
        this.content = content;
        this.size = content.length;

        try {
            MessageDigest md = MessageDigest.getInstance(DEFAULT_HASH_TYPE);
            this.hash = String.format("%032X", new BigInteger(1, md.digest(content)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param is the InputStream
     */
    public void setContent(final InputStream is) {
        try {
            setContent(IOUtils.toByteArray(is));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the comment
     */
    @XmlValue
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileEntry";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((hash == null) ? 0 : hash.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (size ^ (size >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileEntry)) {
            return false;
        }
        FileEntry other = (FileEntry) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (hash == null) {
            if (other.hash != null) {
                return false;
            }
        } else if (!hash.equals(other.hash)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (size != other.size) {
            return false;
        }
        return true;
    }

}

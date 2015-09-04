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
package org.urmel.dbt.rc.datamodel;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.mir.authorization.accesskeys.MIRAccessKeyManager;
import org.mycore.mir.authorization.accesskeys.MIRAccessKeyPair;
import org.mycore.user2.MCRUser;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.persistency.SlotManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@XmlRootElement(name = "attendee")
public class Attendee extends Person implements Serializable {

    private static final long serialVersionUID = 3345191603685207161L;

    private Slot slot;

    private MCRUser user;

    private MIRAccessKeyPair accKP;

    protected Attendee() {
    }

    public Attendee(final Slot slot, final MCRUser user) {
        this.slot = slot;
        this.user = user;
        this.accKP = MIRAccessKeyManager.getKeyPair(slot.getMCRObjectID());

        setName(user.getRealName());
        setEmail(user.getEMailAddress());
    }

    public String getSlotId() {
        return slot.getSlotId();
    }

    @XmlAttribute(name = "uid")
    public String getUID() {
        return user.getUserID();
    }

    @XmlAttribute(name = "owner")
    public boolean isOwner() {
        return SlotManager.isOwner(slot.getMCRObjectID().toString(), user);
    }

    @XmlAttribute(name = "readKey")
    public boolean isReadKeySet() {
        final String key = user
                .getUserAttribute(MIRAccessKeyManager.ACCESS_KEY_PREFIX + slot.getMCRObjectID().toString());
        return key != null && accKP != null ? key.equals(accKP.getReadKey()) : false;
    }

    @XmlAttribute(name = "writeKey")
    public boolean isWriteKeySet() {
        final String key = user
                .getUserAttribute(MIRAccessKeyManager.ACCESS_KEY_PREFIX + slot.getMCRObjectID().toString());
        return key != null && accKP != null ? key.equals(accKP.getWriteKey()) : false;
    }

    @XmlRootElement(name = "attendees")
    public static class Attendees {
        @XmlAttribute(name = "slotId")
        public String slotId;

        @XmlElement(name = "attendee")
        public List<Attendee> attendees;
    }
}

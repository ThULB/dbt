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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRException;

/**
 * @author René Adler (eagle)
 */
@XmlRootElement(name = "slots")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotList implements Serializable {

    private static final long serialVersionUID = 8484254848235412462L;

    private List<Slot> slots = Collections.synchronizedList(new ArrayList<Slot>());

    private Long total;

    /**
     * @return the slots
     */
    public List<Slot> getSlots() {
        return slots;
    }

    /**
     * @param slots the slots to set
     */
    @XmlElement(name = "slot")
    public void setSlots(final List<Slot> slots) {
        this.slots = Collections.synchronizedList(slots);
    }

    /**
     * @param slot the slot to add
     */
    public void addSlot(final Slot slot) {
        if (slot.getSlotId() != null && getSlotById(slot.getSlotId()) != null) {
            throw new MCRException("Slot with id " + slot.getSlotId() + " already exists!");
        }

        slots.add(slot);
    }

    /**
     * @param slot the slot to set
     */
    public void setSlot(final Slot slot) {
        if (slots != null) {
            for (int c = 0; c < slots.size(); c++) {
                if (slot.getSlotId().equals(slots.get(c).getSlotId())) {
                    slots.set(c, slot);
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Couldn't find Slot with id \"" + slot.getSlotId() + "\"!");
    }

    public void removeSlot(final Slot slot) {
        slots.remove(slot);
    }

    @XmlAttribute(name = "total")
    public long getTotal() {
        return total != null ? total : slots.size();
    }

    public void setTotal(final Long total) {
        this.total = total;
    }

    protected void setTotal(long total) {
        this.total = total;
    }

    /**
     * Returns a slot by given id.
     *
     * @param slotId the slot id
     * @return the slot
     */
    public Slot getSlotById(final String slotId) {
        synchronized (slots) {
            for (Slot slot : slots) {
                if (slotId.equals(slot.getSlotId())) {
                    return slot;
                }
            }
        }

        return null;
    }

    /**
     * Returns a {@link SlotList}.
     *
     * @return the {@link SlotList}
     */
    public SlotList getBasicSlots() {
        final SlotList slotList = new SlotList();
        slotList.total = this.total;

        synchronized (slots) {
            for (Slot slot : slots) {
                slotList.addSlot(slot.getBasicCopy());
            }
        }

        return slotList;
    }

    /**
     * Returns a {@link SlotList} with only active {@link Slot}s.
     *
     * @return the {@link SlotList}
     */
    public SlotList getActiveSlots() {
        final SlotList slotList = new SlotList();

        synchronized (slots) {
            for (Slot slot : slots) {
                if (slot.isActive()) {
                    slotList.addSlot(slot.getBasicCopy());
                }
            }
        }

        return slotList;
    }
}

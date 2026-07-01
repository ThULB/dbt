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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.mycore.common.MCRException;

import de.urmel_dl.dbt.rc.rest.v2.annotation.RCAccessCheck;

/**
 * @author René Adler (eagle)
 */
@RCAccessCheck
@XmlRootElement(name = "slots")
@XmlAccessorType(XmlAccessType.NONE)
public class SlotList implements Serializable {

    private static final long serialVersionUID = 8484254848235412462L;

    private List<Slot> slots;

    private Long total;

    public SlotList() {
        this(null);
    }

    public SlotList(List<Slot> slots) {
        Optional.ofNullable(slots).ifPresentOrElse(this::setSlots,
            () -> setSlots(Collections.synchronizedList(new ArrayList<Slot>())));
    }

    private <T> T syncronizedSlots(List<Slot> slots, Function<List<Slot>, T> func) {
        synchronized (slots) {
            return func.apply(slots);
        }
    }

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
        this.slots = Optional.ofNullable(slots).orElse(Collections.synchronizedList(new ArrayList<Slot>()));
    }

    /**
     * @param slot the slot to add
     */
    public void addSlot(final Slot slot) {
        if (slot.getSlotId() != null && getSlotById(slot.getSlotId()) != null) {
            throw new MCRException("Slot with id " + slot.getSlotId() + " already exists!");
        }

        syncronizedSlots(slots, (sl) -> sl.add(slot));
    }

    /**
     * @param slot the slot to set
     */
    public void setSlot(final Slot slot) {
        IntStream.range(0, slots.size())
            .filter(c -> slot.getSlotId().equals(syncronizedSlots(slots, (sl) -> sl.get(c).getSlotId()))).findFirst()
            .ifPresentOrElse(c -> slots.set(c, slot),
                () -> new IllegalArgumentException("Couldn't find Slot with id \"" + slot.getSlotId() + "\"!"));
    }

    public void removeSlot(final Slot slot) {
        slots.remove(slot);
    }

    @XmlAttribute(name = "total")
    public long getTotal() {
        return total != null ? total : (long) syncronizedSlots(slots, (sl) -> sl.size());
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
        return syncronizedSlots(slots,
            (sl) -> sl.stream().filter(slot -> slotId.equals(slot.getSlotId())).findFirst().orElse(null));
    }

    /**
     * Returns a {@link SlotList}.
     *
     * @return the {@link SlotList}
     */
    public SlotList getBasicSlots() {
        SlotList l = syncronizedSlots(slots,
            (sl) -> new SlotList(sl.stream().map(Slot::getBasicCopy).distinct().collect(Collectors.toList())));
        l.total = total;
        return l;
    }

    /**
     * Returns a {@link SlotList} with only active {@link Slot}s.
     *
     * @return the {@link SlotList}
     */
    public SlotList getActiveSlots() {
        return syncronizedSlots(slots, (sl) -> new SlotList(
            sl.stream().filter(Slot::isActive).map(Slot::getBasicCopy).distinct().collect(Collectors.toList())));
    }

}

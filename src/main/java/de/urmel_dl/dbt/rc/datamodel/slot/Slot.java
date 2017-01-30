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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRObjectID;

import de.urmel_dl.dbt.rc.datamodel.Contact;
import de.urmel_dl.dbt.rc.datamodel.Lecturer;
import de.urmel_dl.dbt.rc.datamodel.PendingStatus;
import de.urmel_dl.dbt.rc.datamodel.Status;
import de.urmel_dl.dbt.rc.datamodel.WarningDate;

/**
 * Represents an Reserve Collection slot.
 *
 * @author Ren\u00E9 Adler (eagle)
 */
@XmlRootElement(name = "slot")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "slot", propOrder = { "slotId", "status", "pendingStatus", "onlineOnly", "title", "lecturers",
    "contact", "validTo", "comment", "warningDates", "accessKeys", "entries" })
public class Slot implements Serializable {

    /**
     * The classification root id for reserve collection location.
     */
    public static final String CLASSIF_ROOT_LOCATION = "RCLOC";

    // valid spacers are [._-] and should match classification spacer
    public static final String DEFAULT_ID_SPACER = ".";

    public static final String DEFAULT_ID_FORMAT = "%04d";

    private static final long serialVersionUID = -3222935202548968539L;

    private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

    private int id;

    private MCRObjectID objId;

    private MCRCategoryID location;

    private Status status;

    private PendingStatus pendingStatus;

    private boolean onlineOnly;

    private String readKey;

    private String writeKey;

    private String title;

    private List<Lecturer> lecturers = new ArrayList<>();

    private Contact contact;

    private List<WarningDate> warningDates;

    private Date validTo;

    private String comment;

    private List<SlotEntry<?>> entries;

    /**
     * Creates a new {@link Slot}.
     */
    protected Slot() {
    }

    /**
     * Creates a new {@link Slot} by given {@link Slot#location} and {@link Slot#id}.
     *
     * @param location the slot location
     * @param id the slot id
     */
    public Slot(final MCRCategoryID location, final int id) {
        setId(id);
        setLocation(location);
        setStatus(Status.FREE);
        setOnlineOnly(false);
    }

    /**
     * Creates a new {@link Slot} from given slotId.
     * The slotId holds the formated {@link Slot#location} and {@link Slot#id} which will parsed by {@link Slot#setSlotId(String)}.
     *
     * @param slotId the slotId for example <code>3400:01:01:0001</code>
     */
    public Slot(final String slotId) {
        setSlotId(slotId);
        setStatus(Status.FREE);
        setOnlineOnly(false);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    @XmlAttribute(name = "id")
    public String getSlotId() {
        return location.getID() + DEFAULT_ID_SPACER + String.format(Locale.ROOT, DEFAULT_ID_FORMAT, id);
    }

    /**
     * @param slotId the slotId to set
     */
    protected void setSlotId(final String slotId) {
        final StringTokenizer st = new StringTokenizer(slotId, DEFAULT_ID_SPACER);

        String loc = null;
        for (int c = 0; c <= st.countTokens(); c++) {
            loc = loc == null ? st.nextToken() : loc + DEFAULT_ID_SPACER + st.nextToken();
        }
        final String id = st.nextToken();

        if (id != null && loc != null) {
            this.id = Integer.parseInt(id);
            this.location = new MCRCategoryID(CLASSIF_ROOT_LOCATION, loc);
        }
    }

    /**
     * @return the objId
     */
    public MCRObjectID getMCRObjectID() {
        return objId;
    }

    /**
     * @param objId the objId to set
     */
    public void setMCRObjectID(final MCRObjectID objId) {
        this.objId = objId;
    }

    /**
     * @return the location
     */
    @XmlTransient
    public MCRCategoryID getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(final MCRCategoryID location) {
        this.location = location;
    }

    /**
     * @return true if this slot an active one
     */
    public boolean isActive() {
        final Date today = new Date();
        return status == Status.ACTIVE || (status == Status.PENDING && pendingStatus != PendingStatus.VALIDATING
            && validTo != null && today.before(validTo));
    }

    /**
     * @return the status
     */
    @XmlAttribute(name = "status")
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final Status status) {
        if (this.status == Status.PENDING && status != Status.PENDING) {
            this.pendingStatus = null;
        }

        this.status = status;
    }

    /**
     * @return the pendingStatus
     */
    @XmlAttribute(name = "pendingStatus")
    public PendingStatus getPendingStatus() {
        return pendingStatus;
    }

    /**
     * @param pendingStatus the pendingStatus to set
     */
    public void setPendingStatus(final PendingStatus pendingStatus) {
        if (pendingStatus != PendingStatus.OWNERTRANSFER && this.status != Status.PENDING) {
            this.pendingStatus = null;
        } else {
            this.pendingStatus = pendingStatus;
        }
    }

    /**
     * @return the onlineOnly
     */
    @XmlAttribute(name = "onlineOnly")
    public boolean isOnlineOnly() {
        return onlineOnly;
    }

    /**
     * @param onlineOnly the onlineOnly to set
     */
    public void setOnlineOnly(final boolean onlineOnly) {
        this.onlineOnly = onlineOnly;
    }

    /**
     * @return the readKey
     */
    public String getReadKey() {
        return readKey;
    }

    /**
     * @param readKey the readKey to set
     */
    public void setReadKey(String readKey) {
        this.readKey = readKey;
    }

    /**
     * @return the writeKey
     */
    public String getWriteKey() {
        return writeKey;
    }

    /**
     * @param writeKey the writeKey to set
     */
    public void setWriteKey(String writeKey) {
        this.writeKey = writeKey;
    }

    @XmlElement(name = "accesskeys")
    protected AccessKeys getAccessKeys() {
        return AccessKeys.buildAccessKeys(readKey, writeKey);
    }

    protected void setAccessKeys(final AccessKeys accKeys) {
        readKey = accKeys.readKey;
        writeKey = accKeys.writeKey;
    }

    /**
     * @return the title
     */
    @XmlElement(name = "title")
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return the lecturers
     */
    @XmlElementWrapper(name = "lecturers", required = true)
    @XmlElement(name = "lecturer")
    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    /**
     * @param lecturers the lecturers to set
     */
    public void setLecturers(final List<Lecturer> lecturers) {
        this.lecturers = lecturers;
    }

    /**
     * @param lecturer the lecturer to set
     */
    public void addLecturer(final Lecturer lecturer) {
        lecturers.add(lecturer);
    }

    /**
     * @return the contact
     */
    @XmlElement(name = "contact")
    public Contact getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * @return the warning dates
     */
    @XmlElementWrapper(name = "warnings")
    @XmlElement(name = "warning")
    public List<WarningDate> getWarningDates() {
        if (warningDates != null) {
            Collections.sort(warningDates);
        }
        return warningDates;
    }

    /**
     * @param warningDates the warningDates to set
     */
    public void setWarningDates(final List<WarningDate> warningDates) {
        this.warningDates = warningDates;
    }

    /**
     * Checks if {@link WarningDate} is set on {@link Slot}.
     *
     * @param warningDate the warningDate to check
     * @return <code>true</code> if {@link Slot} has warning date
     */
    public boolean hasWarningDate(final WarningDate warningDate) {
        if (warningDates != null && !warningDates.isEmpty()) {
            for (final WarningDate wd : warningDates) {
                if (wd.compareTo(warningDate) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if {@link Date} is set as {@link WarningDate} on {@link Slot}.
     *
     * @param warningDate the warningDate to check
     * @return <code>true</code> if {@link Slot} has warning date
     */
    public boolean hasWarningDate(final Date warningDate) {
        return hasWarningDate(new WarningDate(warningDate));
    }

    /**
     * @param warningDate the warning date to set
     */
    public void addWarningDate(final Date warningDate) {
        if (warningDates == null) {
            warningDates = new ArrayList<>();
        }

        warningDates.add(new WarningDate(warningDate));
    }

    /**
     * @param warningDate the warning date to set
     */
    public void addWarningDate(final WarningDate warningDate) {
        if (warningDates == null) {
            warningDates = new ArrayList<>();
        }

        warningDates.add(warningDate);
    }

    /**
     * @return the validTo
     */
    @XmlElement(name = "validTo")
    public String getValidTo() {
        if (validTo == null) {
            return null;
        }

        final DateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.ROOT);
        return df.format(validTo);
    }

    /**
     * @return the validTo
     */
    public Date getValidToAsDate() {
        return validTo;
    }

    /**
     * @param validTo the validTo to set
     */
    public void setValidTo(final String validTo) {
        final DateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.ROOT);
        try {
            this.validTo = df.parse(validTo);
        } catch (ParseException e) {
            this.validTo = null;
        }
    }

    /**
     * @param validTo the validTo to set
     */
    public void setValidTo(final Date validTo) {
        this.validTo = validTo;
    }

    /**
     * @return the comment
     */
    @XmlElement(name = "comment")
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(final String comment) {
        this.comment = comment;
    }

    /**
     * @return the entries
     */
    @XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry")
    public List<SlotEntry<?>> getEntries() {
        return entries;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(final List<SlotEntry<?>> entries) {
        this.entries = entries;
    }

    /**
     * @param id the id of the entry
     * @return a SlotEntry or <code>null</code> if nothing was found
     */
    public SlotEntry<?> getEntryById(final String id) {
        if (entries != null) {
            for (SlotEntry<?> entry : entries) {
                if (id.equals(entry.getId())) {
                    return entry;
                }
            }
        }

        return null;
    }

    /**
     * @param entry the entry to set
     * @return <code>true</code> (as specified by {@link Collection#add})
     */
    public boolean addEntry(final SlotEntry<?> entry) {
        if (entries == null) {
            entries = new ArrayList<>();
        }

        return entries.add(entry);
    }

    /**
     * @param entry the entry to set
     * @param afterId the id from previous entry
     * @return <code>true</code> (as specified by {@link Collection#add})
     */
    public boolean addEntry(final SlotEntry<?> entry, final String afterId) {
        if (entries == null) {
            entries = new ArrayList<>();
        }

        if (afterId != null && afterId.length() > 0) {
            for (int i = 0; i < entries.size(); i++) {
                if (afterId.equals(entries.get(i).getId())) {
                    entries.add(i + 1, entry);
                    return true;
                }
            }
        }

        return entries.add(entry);
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(final SlotEntry<?> entry) {
        if (entries != null) {
            for (int c = 0; c < entries.size(); c++) {
                if (entry.getId().equals(entries.get(c).getId())) {
                    entry.setModified(new Date());
                    entries.set(c, entry);
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Couldn't find SlotEntry with id \"" + entry.getId() + "\"!");
    }

    /**
     * @param entry the entry to remove
     * @return <code>true</code> (as specified by {@link Collection#remove})
     */
    public boolean removeEntry(final SlotEntry<?> entry) {
        return entries.remove(entry);
    }

    /**
     * Returns a exportable copy of current {@link Slot}.
     *
     * @return a exportable copy of current slot
     */
    public Slot getExportableCopy() {
        final Slot copy = new Slot();

        copy.id = this.id;
        copy.objId = this.objId;
        copy.location = this.location;
        copy.status = this.status;
        copy.pendingStatus = this.pendingStatus;
        copy.onlineOnly = this.onlineOnly;
        copy.title = this.title;
        copy.lecturers = this.lecturers;
        copy.contact = this.contact;
        copy.warningDates = this.warningDates;
        copy.validTo = this.validTo;
        copy.comment = this.comment;
        copy.entries = this.entries;

        return copy;
    }

    /**
     * Returns a copy of current {@link Slot} without entries.
     *
     * @return a basic copy of current slot
     */
    public Slot getBasicCopy() {
        final Slot copy = new Slot();

        copy.id = this.id;
        copy.objId = this.objId;
        copy.location = this.location;
        copy.status = this.status;
        copy.pendingStatus = this.pendingStatus;
        copy.onlineOnly = this.onlineOnly;
        copy.title = this.title;
        copy.lecturers = this.lecturers;
        copy.contact = this.contact;
        copy.warningDates = this.warningDates;
        copy.validTo = this.validTo;
        copy.comment = this.comment;

        return copy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Slot [id=").append(id).append(", ");
        if (status != null) {
            builder.append("status=").append(status).append(", ");
        }
        if (pendingStatus != null) {
            builder.append("pendingStatus=").append(pendingStatus).append(", ");
        }
        builder.append("onlineOnly=").append(onlineOnly).append(", ");
        if (title != null) {
            builder.append("title=").append(title).append(", ");
        }
        if (validTo != null) {
            builder.append("validTo=").append(validTo).append(", ");
        }
        if (comment != null) {
            builder.append("comment=").append(comment);
        }
        builder.append("]");
        return builder.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + id;
        result = prime * result + ((lecturers == null) ? 0 : lecturers.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + (onlineOnly ? 1231 : 1237);
        result = prime * result + ((pendingStatus == null) ? 0 : pendingStatus.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((validTo == null) ? 0 : validTo.hashCode());
        result = prime * result + ((warningDates == null) ? 0 : warningDates.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Slot)) {
            return false;
        }
        final Slot other = (Slot) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (lecturers == null) {
            if (other.lecturers != null) {
                return false;
            }
        } else if (!lecturers.equals(other.lecturers)) {
            return false;
        }
        if (contact == null) {
            if (other.contact != null) {
                return false;
            }
        } else if (!contact.equals(other.contact)) {
            return false;
        }
        if (location == null) {
            if (other.location != null) {
                return false;
            }
        } else if (!location.equals(other.location)) {
            return false;
        }
        if (onlineOnly != other.onlineOnly) {
            return false;
        }
        if (pendingStatus != other.pendingStatus) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        if (validTo == null) {
            if (other.validTo != null) {
                return false;
            }
        } else if (!validTo.equals(other.validTo)) {
            return false;
        }
        if (warningDates == null) {
            if (other.warningDates != null) {
                return false;
            }
        } else if (!warningDates.equals(other.warningDates)) {
            return false;
        }
        return true;
    }

    @XmlRootElement(name = "accesskeys")
    private static class AccessKeys {
        @XmlAttribute(name = "readkey", required = true)
        public String readKey;

        @XmlAttribute(name = "writekey")
        public String writeKey;

        public static AccessKeys buildAccessKeys(final String readKey, final String writeKey) {
            if (readKey == null && writeKey == null) {
                return null;
            }

            final AccessKeys accKeys = new AccessKeys();

            accKeys.readKey = readKey;
            accKeys.writeKey = writeKey;

            return accKeys;
        }
    }
}

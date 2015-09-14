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
package org.urmel.dbt.rc.commandline;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventManager;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.urmel.dbt.common.MailQueue;
import org.urmel.dbt.rc.datamodel.Period;
import org.urmel.dbt.rc.datamodel.RCCalendar;
import org.urmel.dbt.rc.datamodel.Status;
import org.urmel.dbt.rc.datamodel.Warning;
import org.urmel.dbt.rc.datamodel.WarningDate;
import org.urmel.dbt.rc.datamodel.slot.Slot;
import org.urmel.dbt.rc.datamodel.slot.SlotList;
import org.urmel.dbt.rc.persistency.SlotManager;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
@MCRCommandGroup(name = "RC Commands")
public class RCCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(RCCommands.class);

    @MCRCommand(syntax = "rc inactivator", help = "send warning mails for reserve collections or inactivate, set new status")
    public static void rcInactivator() throws IOException {
        final SlotManager mgr = SlotManager.instance();
        final SlotList slotList = mgr.getSlotList();

        if (!slotList.getSlots().isEmpty()) {
            for (final Slot slot : slotList.getSlots()) {
                if (slot.isActive()) {
                    LOGGER.info("Check slot with id \"" + slot.getSlotId() + "\"...");

                    final Date today = new Date();
                    final Date validTo = slot.getValidToAsDate();
                    final Period period = RCCalendar.getPeriod(slot.getLocation().toString(), validTo);

                    try {
                        if (today.after(validTo)) {
                            MCREvent evt = null;
                            boolean save = true;

                            switch (slot.getStatus()) {
                            case ARCHIVED:
                            case FREE:
                            case RESERVED:
                                save = false;
                                break;
                            case ACTIVE:
                                LOGGER.info("...archive slot");

                                slot.setStatus(Status.ARCHIVED);

                                evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);

                                break;
                            case PENDING:
                                switch (slot.getPendingStatus()) {
                                case ACTIVE:
                                    LOGGER.info("...reactivate slot");

                                    slot.setStatus(Status.ACTIVE);
                                    slot.setValidTo(RCCalendar
                                            .getPeriodBySetable(slot.getLocation().toString(), new Date()).getToDate());

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.REACTIVATE_EVENT);
                                    break;
                                case ARCHIVED:
                                    LOGGER.info("...archive slot");

                                    slot.setStatus(Status.ARCHIVED);

                                    evt = new MCREvent(SlotManager.SLOT_TYPE, SlotManager.INACTIVATE_EVENT);
                                    break;
                                case FREE:
                                    LOGGER.info("...delete slot. (TODO");
                                case RESERVED:
                                    LOGGER.info("...empty slot. (TODO");
                                case VALIDATING:
                                default:
                                    save = false;
                                }
                            }

                            if (save) {
                                mgr.setSlot(slot);
                                mgr.saveOrUpdate(slot);

                                if (evt != null) {
                                    evt.put(SlotManager.SLOT_TYPE, slot);
                                    MCREventManager.instance().handleEvent(evt);
                                }

                                continue;
                            }
                        } else if (slot.getStatus() == Status.ACTIVE) {
                            final Warning pWarning = period.getWarning(today);

                            if (pWarning != null) {
                                final WarningDate sWarning = slot.hasWarningDate(pWarning.getWarningDate()) ? null
                                        : new WarningDate(pWarning.getWarningDate());

                                if (sWarning != null) {
                                    LOGGER.info("...add warning");

                                    slot.addWarningDate(sWarning);
                                    mgr.saveOrUpdate(slot);

                                    final StringBuilder uri = new StringBuilder();

                                    uri.append("xslStyle:" + pWarning.getTemplate());
                                    uri.append("?warningDate=" + sWarning.getWarningDate());
                                    uri.append(":notnull:slot:");
                                    uri.append("slotId=" + slot.getSlotId());

                                    LOGGER.info("...send mail");
                                    MailQueue.addJob(uri.toString());

                                    continue;
                                }
                            }
                        }

                        LOGGER.info("...nothing to do.");
                    } catch (IllegalArgumentException | ParseException | CloneNotSupportedException
                            | MCRPersistenceException | MCRActiveLinkException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
    }
}

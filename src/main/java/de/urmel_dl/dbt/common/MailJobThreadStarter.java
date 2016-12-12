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
package de.urmel_dl.dbt.common;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.services.queuedjob.MCRJobMaster;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class MailJobThreadStarter implements MCRStartupHandler.AutoExecutable {
    private static Logger LOGGER = LogManager.getLogger(MailJobThreadStarter.class);

    @Override
    public String getName() {
        return "Mailer Thread";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        if (servletContext != null && !MCRJobMaster.isRunning(MailJob.class)) {
            LOGGER.info("Starting Mailer thread.");
            System.setProperty("java.awt.headless", "true");
            Thread retrievingThread = new Thread(MCRJobMaster.getInstance(MailJob.class));
            retrievingThread.start();
        }
    }

}

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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletContext;

/**
 * Adds the podcast publication type to installations whose MIR genre classification
 * has already been persisted and is therefore not refreshed from setup resources.
 */
public final class PodcastClassificationInitializer implements MCRStartupHandler.AutoExecutable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MCRCategoryID GENRES_ID = new MCRCategoryID("mir_genres");

    private static final MCRCategoryID PODCAST_ID = new MCRCategoryID("mir_genres", "podcast");

    private static final int PRIORITY = 998;

    @Override
    public String getName() {
        return PodcastClassificationInitializer.class.getSimpleName();
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void startUp(final ServletContext servletContext) {
        MCRCategoryDAO categoryDAO = MCRCategoryDAOFactory.obtainInstance();
        EntityManager entityManager = MCREntityManagerProvider.getCurrentEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            if (categoryDAO.exist(PODCAST_ID)) {
                return;
            }

            if (!categoryDAO.exist(GENRES_ID)) {
                LOGGER.warn("Cannot add {} because classification {} does not exist",
                    PODCAST_ID, GENRES_ID);
                return;
            }

            categoryDAO.addCategory(GENRES_ID, createPodcastCategory());
            LOGGER.info("Added publication type {}", PODCAST_ID);
        } finally {
            if (transaction.isActive()) {
                if (transaction.getRollbackOnly()) {
                    transaction.rollback();
                } else {
                    transaction.commit();
                }
            }
        }
    }

    private static MCRCategory createPodcastCategory() {
        MCRCategory category = new MCRCategoryImpl();
        category.setId(PODCAST_ID);
        category.getLabels().add(new MCRLabel("de", "Podcast", null));
        category.getLabels().add(new MCRLabel("en", "Podcast", null));
        category.getLabels().add(new MCRLabel("it", "Podcast", null));
        category.getLabels().add(new MCRLabel("x-hosts", "standalone", null));
        category.getLabels().add(new MCRLabel("x-mapping",
            "marcgt:sound diniPublType:Sound diniPublType2022:Sound schemaOrg:PodcastEpisode "
                + "coarResourceTypes32:c_18cc openAIRE400resourceTypeGeneral:other_research_product",
            null));
        return category;
    }
}

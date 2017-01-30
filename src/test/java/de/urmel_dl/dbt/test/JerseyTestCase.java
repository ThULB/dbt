/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2016
 * See <https://www.db-thueringen.de/> and <https://github.com/ThULB/dbt/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.urmel_dl.dbt.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.mycore.common.config.MCRComponent;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationLoader;
import org.mycore.common.config.MCRConfigurationLoaderFactory;
import org.mycore.common.config.MCRRuntimeComponentDetector;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class JerseyTestCase extends JerseyTest {

    @ClassRule
    public static TemporaryFolder junitFolder = new TemporaryFolder();

    protected File properties = null;

    protected String oldProperties;

    protected MCRConfiguration config;

    @BeforeClass
    public static void initBaseDir() throws IOException {
        if (System.getProperties().getProperty("MCR.Home") == null) {
            File baseDir = junitFolder.newFolder("mcrhome");
            System.out.println("Setting MCR.Home=" + baseDir.getAbsolutePath());
            System.getProperties().setProperty("MCR.Home", baseDir.getAbsolutePath());
        }
        if (System.getProperties().getProperty("MCR.AppName") == null) {
            String currentComponentName = getCurrentComponentName();
            System.out.println("Setting MCR.AppName=" + currentComponentName);
            System.getProperties().setProperty("MCR.AppName", getCurrentComponentName());
        }
        File configDir = new File(System.getProperties().getProperty("MCR.Home"),
            System.getProperties().getProperty("MCR.AppName"));
        System.out.println("Creating config directory: " + configDir);
        configDir.mkdirs();
    }

    /**
     * initializes MCRConfiguration with an empty property file. This can be used to test MyCoRe classes without any
     * propties set, using default. You may want to set Properties per TestCase with the set() method of
     * <code>MCRConfiguration</code>
     *
     * @see MCRConfiguration#set(String, String)
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initProperties();
        String mcrComp = MCRRuntimeComponentDetector.getMyCoReComponents()
            .stream()
            .map(MCRComponent::toString)
            .collect(Collectors.joining(", "));
        String appMod = MCRRuntimeComponentDetector.getApplicationModules()
            .stream()
            .map(MCRComponent::toString)
            .collect(Collectors.joining(", "));
        System.out.printf("MyCoRe components detected: %s\nApplications modules detected: %s\n",
            mcrComp.isEmpty() ? "'none'" : mcrComp,
            appMod.isEmpty() ? "'none'" : appMod);
        config = MCRConfiguration.instance();
        MCRConfigurationLoader configurationLoader = MCRConfigurationLoaderFactory.getConfigurationLoader();
        HashMap<String, String> testProperties = new HashMap<>(configurationLoader.load());
        testProperties.putAll(getTestProperties());
        config.initialize(testProperties, true);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        if (properties != null) {
            properties.delete();
            properties = null;
        }
        MCRConfiguration.instance().initialize(Collections.<String, String> emptyMap(), true);
        super.tearDown();
    }

    protected Map<String, String> getTestProperties() {
        return new HashMap<>();
    }

    /**
     * Creates a temporary properties file if the system variable MCR.Configuration.File is not set.
     *
     * @throws IOException
     *             Thrown if the creation of the temporary properties file failed.
     * @author Marcel Heusinger &lt;marcel.heusinger[at]uni-due.de&gt;
     */
    protected void initProperties() throws IOException {
        String currentComponent = getCurrentComponentName();
        System.setProperty("MCRRuntimeComponentDetector.underTesting", currentComponent);
    }

    private static String getCurrentComponentName() {
        String userDir = System.getProperty("user.dir");
        String currentComponent = Paths.get(userDir).getFileName().toString();
        return currentComponent;
    }

    protected boolean isDebugEnabled() {
        return false;
    }

    /**
     * Retrieve the resource file<br> Example: /Classname/recource.file
     *
     * @param fileName
     * @return the resource file as InputStream
     */
    protected InputStream getResourceAsStream(String fileName) {
        String fileLocation = buildFileLocation(fileName);
        System.out.println("File location: " + fileLocation);
        return Class.class.getResourceAsStream(fileLocation);
    }

    /**
     * Retrieve the resource file as URI. Example: /Classname/recource.file
     *
     * @param fileName
     * @return the resource file as URL
     */
    protected URL getResourceAsURL(String fileName) {
        String fileLocation = buildFileLocation(fileName);
        System.out.println("File location: " + fileLocation);
        return Class.class.getResource(fileLocation);
    }

    private String buildFileLocation(String fileName) {
        return MessageFormat.format("/{0}/{1}", this.getClass().getSimpleName(), fileName);
    }
}

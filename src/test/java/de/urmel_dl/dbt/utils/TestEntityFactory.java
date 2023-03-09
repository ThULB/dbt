/*
 * This file is part of the Digitale Bibliothek Thüringen
 * Copyright (C) 2000-2019
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
package de.urmel_dl.dbt.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.config.MCRConfiguration2;

/**
 * @author Ren\u00E9 Adler (eagle)
 *
 */
public class TestEntityFactory extends MCRTestCase {

    /* (non-Javadoc)
     * @see org.mycore.common.MCRTestCase#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected Map<String, String> getTestProperties() {
        final Map<String, String> testProperties = super.getTestProperties();
        testProperties.put(
            EntityFactory.CONFIG_PREFIX + TestEntity.class.getPackage().getName() + "."
                + EntityFactory.CONFIG_MARSHALLER
                + "eclipselink.json.include-root",
            "false");
        return testProperties;
    }

    @Test
    public void testProperties() {
        EntityFactory<TestEntity> ef = new EntityFactory<>(testEntity());
        Map<String, ?> props = ef.properties(EntityFactory.CONFIG_MARSHALLER);
        assertFalse((Boolean) props.get("eclipselink.json.include-root"));
    }

    @Test
    public void testPropertiesClass() {
        MCRConfiguration2.set(
            EntityFactory.CONFIG_PREFIX + TestEntity.class.getName() + "."
                + EntityFactory.CONFIG_MARSHALLER
                + "eclipselink.json.include-root",
            "true");

        EntityFactory<TestEntity> ef = new EntityFactory<>(testEntity());

        Map<String, ?> props = ef.properties(EntityFactory.CONFIG_MARSHALLER);
        assertTrue((Boolean) props.get("eclipselink.json.include-root"));
    }

    private TestEntity testEntity() {
        TestEntity tst = new TestEntity();
        tst.num = 0;
        tst.value = "Test 0";

        return tst;
    }

    @XmlRootElement
    private static class TestEntity {

        @XmlAttribute
        private int num;

        @XmlValue
        private String value;

    }

}

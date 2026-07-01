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

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mycore.common.MCRTestConfiguration;
import org.mycore.common.MCRTestProperty;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.test.MyCoReTest;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * @author René Adler (eagle)
 *
 */
@MyCoReTest
@MCRTestConfiguration(
    properties = {
        @MCRTestProperty(key = EntityFactory.CONFIG_PREFIX + "de.urmel_dl.dbt.utils."
            + EntityFactory.CONFIG_MARSHALLER
            + "eclipselink.json.include-root", string = "false")
    }
)
public class TestEntityFactory {

    @Test
    public void testProperties() {
        EntityFactory<TestEntity> ef = new EntityFactory<>(testEntity());
        Map<String, ?> props = ef.properties(EntityFactory.CONFIG_MARSHALLER);
        Assertions.assertFalse((Boolean) props.get("eclipselink.json.include-root"));
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
        Assertions.assertTrue((Boolean) props.get("eclipselink.json.include-root"));
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

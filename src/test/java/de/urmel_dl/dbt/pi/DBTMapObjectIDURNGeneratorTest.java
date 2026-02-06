package de.urmel_dl.dbt.pi;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mycore.common.MCRTestConfiguration;
import org.mycore.common.MCRTestProperty;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.test.MyCoReTest;

@MyCoReTest
@MCRTestConfiguration(
    properties = {
        @MCRTestProperty(key = "MCR.PI.Generator.Test", empty = true),
        @MCRTestProperty(key = "MCR.PI.Generator.Test.Prefix.dbt_mods", string = "urn:nbn:de:test-dbt-")
    })
public class DBTMapObjectIDURNGeneratorTest {

    DBTMapObjectIDURNGenerator generator;

    @BeforeEach
    public void setUp() throws Exception {
        generator = new DBTMapObjectIDURNGenerator();
        generator.init("MCR.PI.Generator.Test");
        Map<String, String> subPropertiesMap = MCRConfiguration2.getSubPropertiesMap("MCR.PI.Generator.Test" + ".");
        System.out.println(subPropertiesMap);
        generator.setProperties(subPropertiesMap);
    }

    @Test
    public void getNamespace() {
        Assertions.assertEquals("urn:nbn:de:test", generator.getNamespace("test"));
        Assertions.assertEquals("urn:nbn:de:test", generator.getNamespace("urn:nbn:de:test"));
    }

    @Test
    public void buildURN() throws MCRPersistentIdentifierException {
        MCRObjectID mcrObjectID = MCRObjectID.getInstance("dbt_mods_4711");
        MCRDNBURN urn = generator.buildURN(mcrObjectID, "");
        Assertions.assertEquals("urn:nbn:de:test-dbt-4711-2", urn.asString());
    }
}

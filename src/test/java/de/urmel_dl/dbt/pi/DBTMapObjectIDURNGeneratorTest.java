package de.urmel_dl.dbt.pi;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRTestCase;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;

public class DBTMapObjectIDURNGeneratorTest extends MCRTestCase {

    private static final String CONFIG_PREFIX = "MCR.PI.Generator.Test";
    DBTMapObjectIDURNGenerator generator;

    @Override
    protected Map<String, String> getTestProperties() {
        Map<String, String> testProperties = super.getTestProperties();
        testProperties.put(CONFIG_PREFIX, "");
        testProperties.put(CONFIG_PREFIX+".Prefix.dbt_mods", "urn:nbn:de:test-dbt-");
        return testProperties;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        generator = new DBTMapObjectIDURNGenerator();
        generator.init(CONFIG_PREFIX);
        Map<String, String> subPropertiesMap = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX+".");
        System.out.println(subPropertiesMap);
        generator.setProperties(subPropertiesMap);
    }

    @Test
    public void getNamespace() {
        Assert.assertEquals("urn:nbn:de:test",generator.getNamespace("test"));
        Assert.assertEquals("urn:nbn:de:test",generator.getNamespace("urn:nbn:de:test"));
    }

    @Test
    public void buildURN() throws MCRPersistentIdentifierException {
        MCRObjectID mcrObjectID=MCRObjectID.getInstance("dbt_mods_4711");
        MCRDNBURN urn = generator.buildURN(mcrObjectID, "");
        Assert.assertEquals("urn:nbn:de:test-dbt-4711-2",urn.asString());
    }
}

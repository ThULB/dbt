package org.mycore.dbt.it.tests;

import org.junit.runners.Parameterized;
import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.dbt.it.model.DBTSearchTestDataLoader;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRSearchTestDataLoader;
import org.mycore.mir.it.tests.MIRSimpleSearchITCase;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DBTSimpleSearchITCase extends MIRSimpleSearchITCase {

    public DBTSimpleSearchITCase(String jsonFile, String idsString) throws IOException {
        super(jsonFile, idsString);
    }

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTControllerFactory();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> input() {
        return Stream.of(new Object[] { "simpleTest1.json", "dbt_mods_00010000" },
                        new Object[] { "simpleTest2.json", "dbt_mods_00010000" })
                .collect(Collectors.toList());
    }

    @Override
    protected MIRSearchTestDataLoader createSearchTestDataLoader() {
        return new DBTSearchTestDataLoader(controllerFactory);
    }

}

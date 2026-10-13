package org.mycore.dbt.it.model;

import java.util.List;

import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRSearchTestDataLoader;

public class DBTSearchTestDataLoader extends MIRSearchTestDataLoader {

    public DBTSearchTestDataLoader(MIRControllerFactory controllerFactory) {
        super(controllerFactory);
    }

    @Override
    protected List<String> getFileNames() {
        return List.of("dbt_mods_00010000.xml");
    }

    @Override
    protected String getTestDataQuery() {
        return "id:dbt_mods_0001000*";
    }
}

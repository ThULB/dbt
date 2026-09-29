package org.mycore.dbt.it.model;

import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRSearchTestDataLoader;
import java.util.List;


public class DBTSearchTestDataLoader extends MIRSearchTestDataLoader {

    public DBTSearchTestDataLoader(MIRControllerFactory controllerFactory){
        super(controllerFactory);
    }

    @Override
    protected List<String> getFileNames() {
        return List.of("dbt_mods_00010000.xml");
    }
}

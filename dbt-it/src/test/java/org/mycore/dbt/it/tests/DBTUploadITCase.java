package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTAdminControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRUploadITCase;

public class DBTUploadITCase extends MIRUploadITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTAdminControllerFactory();
    }
}

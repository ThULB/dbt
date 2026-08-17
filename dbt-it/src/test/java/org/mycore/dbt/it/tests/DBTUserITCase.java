package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRUserITCase;

public class DBTUserITCase extends MIRUserITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTControllerFactory();
    }
}

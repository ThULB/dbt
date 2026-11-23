package org.mycore.dbt.it.tests;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRUserITCase;

public class DBTUserITCase extends MIRUserITCase {

    @Override
    protected MIRControllerFactory createControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        return new DBTControllerFactory(driver, appURL);
    }
}

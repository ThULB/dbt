package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRModsEditorController;

public class DBTAdminControllerFactory extends DBTControllerFactory {

    public DBTAdminControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        super(driver, appURL);
    }

    @Override
    public MIRModsEditorController createModsEditorController() {
        return new DBTAdminModsEditorController(driver, appURL);
    }
}

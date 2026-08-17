package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRModsEditorController;

public class DBTAdminControllerFactory extends DBTControllerFactory {

    @Override
    public MIRModsEditorController createModsEditorController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTAdminModsEditorController(driver, appURL);
    }
}

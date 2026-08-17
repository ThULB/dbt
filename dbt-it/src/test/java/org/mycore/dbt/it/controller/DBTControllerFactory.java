package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.controller.MIRModsEditorController;
import org.mycore.mir.it.controller.MIRPublishEditorController;
import org.mycore.mir.it.controller.MIRSearchController;
import org.mycore.mir.it.controller.MIRUserController;

public class DBTControllerFactory extends MIRControllerFactory {

    @Override
    public MIRUserController createUserController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTUserController(driver, appURL);
    }

    @Override
    public MIRPublishEditorController createPublishEditorController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTPublishEditorController(driver, appURL);
    }

    @Override
    public MIRModsEditorController createModsEditorController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTModsEditorController(driver, appURL);
    }

    @Override
    public MIRSearchController createSearchController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTSearchController(driver, appURL);
    }

    /**
     * The Semesterapparat (reserve collection) is a DBT only feature, so this controller has no MIR counterpart to
     * override - it is added here instead.
     */
    public DBTSlotController createSlotController(MCRWebdriverWrapper driver, String appURL) {
        return new DBTSlotController(driver, appURL);
    }

}

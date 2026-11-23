package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.controller.MIRModsEditorController;
import org.mycore.mir.it.controller.MIRPublishEditorController;
import org.mycore.mir.it.controller.MIRSearchController;
import org.mycore.mir.it.controller.MIRUserController;

public class DBTControllerFactory extends MIRControllerFactory {

    public DBTControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        super(driver, appURL);
    }

    @Override
    public MIRUserController createUserController() {
        return new DBTUserController(driver, appURL);
    }

    @Override
    public MIRPublishEditorController createPublishEditorController() {
        return new DBTPublishEditorController(driver, appURL);
    }

    @Override
    public MIRModsEditorController createModsEditorController() {
        return new DBTModsEditorController(driver, appURL);
    }

    @Override
    public MIRSearchController createSearchController() {
        return new DBTSearchController(driver, appURL);
    }

    /**
     * The Semesterapparat (reserve collection) is a DBT only feature, so this controller has no MIR counterpart to
     * override - it is added here instead.
     */
    public DBTSlotController createSlotController() {
        return new DBTSlotController(driver, appURL);
    }

}

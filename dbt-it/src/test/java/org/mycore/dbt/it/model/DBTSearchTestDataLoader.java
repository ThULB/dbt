package org.mycore.dbt.it.model;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRSearchTestDataLoader;
import org.openqa.selenium.By;
import java.util.List;


public class DBTSearchTestDataLoader extends MIRSearchTestDataLoader {

    private static final By USER_MENU_TOGGLE = By.id("menu-user-desktop");

    public DBTSearchTestDataLoader(MIRControllerFactory controllerFactory){
        super(controllerFactory);
    }
    @Override
    protected void openWebCLI(MCRWebdriverWrapper webDriverWrapper) {
        webDriverWrapper.waitAndFindElement(USER_MENU_TOGGLE).click();
        webDriverWrapper.waitAndFindElement(By.linkText("WebCLI")).click();
    }

    @Override
    protected List<String> getFileNames() {
        return List.of("dbt_mods_00010000.xml");
    }
}

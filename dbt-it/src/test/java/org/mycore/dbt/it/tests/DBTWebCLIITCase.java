package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRWebCLIITCase;
import org.openqa.selenium.By;


public class DBTWebCLIITCase extends MIRWebCLIITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTControllerFactory();
    }

    @Override
    protected void openWebCLI(){
        driver.waitAndFindElement(By.id("menu-user-desktop")).click();
        driver.waitAndFindElement(
                By.cssSelector("div[aria-labelledby='menu-user-desktop'] a[href$='webcli/launchpad.xml']")).click();
    }
}

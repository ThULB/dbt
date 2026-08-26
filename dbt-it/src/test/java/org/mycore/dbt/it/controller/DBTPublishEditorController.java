package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.common.selenium.util.MCRBy;
import org.mycore.mir.it.controller.MIRPublishEditorController;
import org.openqa.selenium.By;

public class DBTPublishEditorController extends MIRPublishEditorController {

    public DBTPublishEditorController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    @Override
    public void open(Runnable assertion) {
        driver.waitAndFindElement(By.xpath(".//a[contains(@class,'dropdown-toggle') and contains(normalize-space(.), 'Publizieren')]")).click();
        driver.waitAndFindElement(MCRBy.partialLinkText("Publizieren")).click();
        if (assertion != null) {
            assertion.run();
        }
    }

    @Override
    public void openAdmin(Runnable assertion) {
        driver.waitAndFindElement(By.xpath(".//a[contains(@class,'dropdown-toggle') and contains(normalize-space(.), 'Publizieren')]")).click();
        driver.waitAndFindElement(MCRBy.partialLinkText("Publizieren (Admin)")).click();
        if (assertion != null) {
            assertion.run();
        }
    }

}

package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRSearchController;
import org.openqa.selenium.By;


public class DBTSearchController extends MIRSearchController {

    private static final By SEARCH_MENU = By.id("menu-search");

    public DBTSearchController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    @Override
    protected void openSimpleSearchForm() {
        driver.waitAndFindElement(SEARCH_MENU).click();
        driver.waitAndFindElement(By.linkText("einfach (intern)")).click();
    }

    @Override
    protected void openComplexSearchForm() {
        driver.waitAndFindElement(SEARCH_MENU).click();
        driver.waitAndFindElement(By.linkText("erweitert (intern)")).click();
    }
}

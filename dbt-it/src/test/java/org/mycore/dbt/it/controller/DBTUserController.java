package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRUserController;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class DBTUserController extends MIRUserController {
    String baseURL;

    MCRWebdriverWrapper driver;

    public DBTUserController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
        this.driver = driver;
        this.baseURL = baseURL;
    }

    private static final By USER_PROFILE_TOGGLE =
            By.xpath("//a[contains(@class,'dropdown-toggle')][.//i[contains(@class,'fa-user')]]");


    @Override
    public void CheckCurrentUser(String user){
        driver.waitAndFindElement(USER_PROFILE_TOGGLE);
    }

    @Override
    public void openUserMenu() {
        driver.waitAndFindElement(By.id("menu-user-desktop")).click();
    }

    @Override
    public String getPageTitle(){
        return "Willkommen! – Digitale Bibliothek Thüringen";
    }


    public void logOff() {
        driver.waitAndFindElement(USER_PROFILE_TOGGLE).click();
        driver.findElement(By.linkText("Abmelden")).click();
        assertEqualsIgnoreCase("Anmelden", driver.waitAndFindElement(By.id("loginURL")).getText());
    }

    public boolean isLoggedIn() {
        driver.waitAndFindElement(By.id("logo_modul"));
        try {
            driver.findElement(USER_PROFILE_TOGGLE);
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

}
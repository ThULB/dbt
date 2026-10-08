package org.mycore.dbt.it.controller;

import java.util.HashMap;
import java.util.Map;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRUserController;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class DBTUserController extends MIRUserController {

    private static final By WEBCLI_LINK =
        By.cssSelector("div[aria-labelledby='menu-user-desktop'] a[href$='webcli/launchpad.xml']");

    private static final By USER_PROFILE_TOGGLE =
        By.xpath("//a[contains(@class,'dropdown-toggle')][.//i[contains(@class,'fa-user')]]");

    /**
     * Real names of the users known to the tests, by login. The DBT navbar shows the real name of the current user
     * and falls back to the login if there is none (see <code>#user-profile</code> in dbt-layout.xsl). The
     * superuser gets the real name "Superuser" from <code>MCRUserCommands.initSuperuser()</code>.
     */
    private final Map<String, String> realNames = new HashMap<>(Map.of(ADMIN_LOGIN, "Superuser"));

    public DBTUserController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    @Override
    public void createUser(String user, String password, String name, String mail, Runnable assertion,
        String... roles) {
        if (name != null && !name.isBlank()) {
            realNames.put(user, name);
        }
        super.createUser(user, password, name, mail, assertion, roles);
    }

    @Override
    public void checkCurrentUser(String user) {
        // textContent instead of getText(): the name is hidden at md width and rendered in upper case
        String shownName = driver.waitAndFindElement(USER_PROFILE_TOGGLE).getDomProperty("textContent").trim();
        assertEqualsIgnoreCase(realNames.getOrDefault(user, user), shownName);
    }

    @Override
    public void openUserMenu() {
        driver.waitAndFindElement(By.id("menu-user-desktop")).click();
    }

    @Override
    public void openWebCLI() {
        openUserMenu();
        driver.waitAndFindElement(WEBCLI_LINK).click();
    }

    @Override
    public String getPageTitle() {
        return "Willkommen! – Digitale Bibliothek Thüringen";
    }

    @Override
    public void logOff() {
        driver.waitAndFindElement(USER_PROFILE_TOGGLE).click();
        driver.findElement(By.linkText("Abmelden")).click();
        assertEqualsIgnoreCase("Anmelden", driver.waitAndFindElement(By.id("loginURL")).getText());
    }

    @Override
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

package org.mycore.dbt.it.controller;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.openqa.selenium.By;

/**
 * Leaves an input field by clicking on a harmless spot of the DBT layout.
 * MIR's clickOutside() clicks next to the current mouse position, which can hit the DBT logo in the fixed header.
 */
public final class DBTClickHelper {

    private static final By SAFE_SPOT = By.cssSelector("footer p.info");

    private DBTClickHelper() {
    }

    public static void clickOutside(MCRWebdriverWrapper driver) {
        driver.waitAndFindElement(SAFE_SPOT).click();
    }

}

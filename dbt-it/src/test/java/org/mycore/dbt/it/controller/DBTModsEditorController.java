package org.mycore.dbt.it.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRModsEditorController;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public class DBTModsEditorController extends MIRModsEditorController {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String VALIDATION_CONTRACT = "Bitte stimmen Sie der Veröffentlichung zu.";

    private boolean autoAgree = true;

    public DBTModsEditorController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    public void setAutoAgree(boolean autoAgree) {
        this.autoAgree = autoAgree;
    }

    @Override
    public void save() {
        if (autoAgree) {
            agreeToPublishIfPresent();
        }
        super.save();
    }

    private void agreeToPublishIfPresent() {
        try {
            WebElement toggle = driver.findElement(By.id("iagree_true"));
            if (!toggle.isSelected()) {
                toggle.click();
            }
        } catch (NoSuchElementException e) {
            // not every editor has the publication contract, so there is nothing to agree to
        }
    }

    @Override
    public void clickOutside() {
        DBTClickHelper.clickOutside(driver);
    }

    public boolean isContractValidationMessageVisible() {
        return hasValidationText(VALIDATION_CONTRACT) && hasContractValidationError("iagree_true");
    }

    protected boolean hasContractValidationError(String inputId) {
        try {
            driver.waitAndFindElement(
                By.xpath(".//fieldset[contains(@class, 'mcr-invalid')]//input[@id='" + inputId + "']"));
        } catch (NoSuchElementException | TimeoutException e) {
            LOGGER.error("Could not find red validation border on fieldset!", e);
            return false;
        }
        return true;
    }

}

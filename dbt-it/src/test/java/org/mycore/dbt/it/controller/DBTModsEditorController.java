package org.mycore.dbt.it.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.controller.MIRModsEditorController;
import org.openqa.selenium.*;

public class DBTModsEditorController extends MIRModsEditorController {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String VALIDATION_CONTRACT = "Bitte stimmen Sie der Veröffentlichung zu.";
    private boolean autoAgree = true;

    public DBTModsEditorController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    @Override
    protected void setInputText(String childElementName, String text) {
        final WebElement input = driver.waitAndFindElement(
                By.xpath(".//input[contains(@name,'" + childElementName + "') and contains(@type, 'text')]"));

        Assert.assertTrue("Input is hidden: " + childElementName, input.isDisplayed());
        input.clear();
        input.click();
        input.sendKeys(text);
        input.sendKeys(Keys.TAB);

        driver.waitFor(webDriver -> inputHasNoFocus(input));

        Assert.assertEquals("Input value changed after blur: " + childElementName, text,
                input.getDomProperty("value"));
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

        }
    }

    public boolean isContractValidationMessageVisible() {
        return hasValidationText(VALIDATION_CONTRACT) && hasContractValidationError("iagree_true") ;
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
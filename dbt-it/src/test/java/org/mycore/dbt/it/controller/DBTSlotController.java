package org.mycore.dbt.it.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.common.selenium.util.MCRBy;
import org.mycore.mir.it.controller.MIREditorController;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DBTSlotController extends MIREditorController {

    public DBTSlotController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    public void openNewSlotForm() {
        driver.waitAndFindElement(By.id("menu-rc")).click();
        driver.waitAndFindElement(By.linkText("Einrichten")).click();
        driver.waitFor(ExpectedConditions.textToBePresentInElementLocated(By.xpath(".//h5[contains(@class,'card-header')]"),
            "Neuen Semesterapparat anlegen"));
        driver.waitAndFindElement(By.id("title"));
    }

    public void setTitle(String title) {
        setInputText(By.id("title"), title);
    }


    public void setAccessKeys(String readKey, String writeKey) {
        setInputText(By.id("readKey"), readKey);
        setInputText(By.id("writeKey"), writeKey);
    }

    public void selectLocation(String... path) {
        driver.waitAndFindElement(By
                .xpath(".//button[starts-with(@name,'_xed_submit_subselect:') and contains(@name,'/location')]")).click();

        for (int i = 0; i < path.length; i++) {
            driver.waitAndFindElement(By.linkText(path[i])).click();
            if (i < path.length - 1) {
                driver.waitFor(ExpectedConditions.visibilityOfElementLocated(By.linkText(path[i + 1])));
            }
        }

        driver.waitAndFindElement(By
                .xpath(".//button[starts-with(@name,'_xed_submit_subselect:') and contains(@name,'/location')]"));
        Assert.assertTrue("Location " + String.join(" - ", path) + " should be selected!",
            driver.findElement(By.tagName("body")).getText().contains(String.join(" - ", path)));
    }

    public void save() {
        driver.waitAndFindElement(By.xpath(".//button[starts-with(@name,'_xed_submit_servlet:')]")).click();
        driver.waitFor(ExpectedConditions.or(
            ExpectedConditions.urlMatches(".*/rc/\\d+(\\.\\d+)+.*"),
            ExpectedConditions.presenceOfElementLocated( By.xpath(".//div[contains(@class,'alert-danger')]")),
            ExpectedConditions.titleContains("Fehler")));

        // the servlet refused server side, e.g. SlotListServlet sends 403 when create-slot is not granted; that is
        // neither a slot page nor an XEditor validation error, so without this the wait would just time out
        String title = driver.getTitle();
        if (title != null && title.contains("Fehler")) {
            Assert.fail("Server refused to save the Semesterapparat: " + title
                + " - a 403 here means the create-slot permission is missing, see setup-dbt-acl.txt");
        }

        List<WebElement> alerts = driver.findElements( By.xpath(".//div[contains(@class,'alert-danger')]"));
        if (!alerts.isEmpty()) {
            Assert.fail("Editor rejected the Semesterapparat. Invalid fields: " + invalidFieldIds()
                + " - " + alerts.get(0).getText());
        }
    }

    private String invalidFieldIds() {
        String ids = driver.findElements(By.xpath(".//*[contains(@class,'mcr-invalid')]//*[@id]")).stream()
            .map(element -> element.getDomAttribute("id"))
            .filter(id -> id != null && !id.isEmpty())
            .distinct()
            .collect(Collectors.joining(", "));
        return ids.isEmpty() ? "none marked" : ids;
    }

    public boolean isSlotCreated() {
        try {
            return driver.waitAndFindElement(MCRBy.partialText("Ihr neuer Semesterapparat ist jetzt eingerichtet.")).isDisplayed();
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }
    private void setInputText(By locator, String text) {
        WebElement input = driver.waitAndFindElement(locator);
        Assert.assertTrue("Input is hidden: " + locator, input.isDisplayed());
        input.clear();
        input.click();
        input.sendKeys(text);
        clickOutside();
        driver.waitFor(webDriver -> inputHasNoFocus(input));

        String value = input.getDomProperty("value");
        Assert.assertEquals("Input value changed after blur: " + locator, text, value == null ? "" : value);
    }
}

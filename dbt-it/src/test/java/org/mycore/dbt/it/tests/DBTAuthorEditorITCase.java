package org.mycore.dbt.it.tests;

import org.junit.Assert;
import org.junit.Test;
import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.dbt.it.controller.DBTModsEditorController;
import org.mycore.dbt.it.model.DBTSampleInstitutes;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRInstitutes;
import org.mycore.mir.it.tests.MIRAuthorEditorITCase;

public class DBTAuthorEditorITCase extends MIRAuthorEditorITCase {

    @Override
    protected MIRControllerFactory createControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        return new DBTControllerFactory(driver, appURL);
    }

    @Override
    protected String getPageTitle (){
        return  "MODS-Dokument erstellen – Digitale Bibliothek Thüringen";
    }

    @Override
    protected void assertBaseValidation() {
        super.assertBaseValidation();
        Assert.assertTrue("Contract validation message should be visible!",
                ((DBTModsEditorController) this.editorController).isContractValidationMessageVisible());
    }

    @Override
    @Test
    public void testBaseValidation() {
        ((DBTModsEditorController) editorController).setAutoAgree(false);
        super.testBaseValidation();
    }

    @Override
    protected MIRInstitutes institutionTestValue() {
        return DBTSampleInstitutes.Friedrich_Schiller_Universitaet_Jena;
    }

    @Override
    protected String institutionValidationText() {
        return "Friedrich-Schiller-Universität Jena";
    }

}

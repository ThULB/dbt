package org.mycore.dbt.it.tests;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.dbt.it.controller.DBTAdminControllerFactory;
import org.mycore.dbt.it.model.DBTSampleInstitutes;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.model.MIRInstitutes;
import org.mycore.mir.it.tests.MIRComplexSearchITCase;


public class DBTComplexSearchITCase extends MIRComplexSearchITCase {

    @Override
    protected MIRControllerFactory createControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        return new DBTAdminControllerFactory(driver, appURL);
    }

    @Override
    protected String getPageTitle(){
        return "MODS-Dokument erstellen – Digitale Bibliothek Thüringen";
    }

    @Override
    protected MIRInstitutes institutionTestValue() {
        return DBTSampleInstitutes.Friedrich_Schiller_Universitaet_Jena;
    }

}

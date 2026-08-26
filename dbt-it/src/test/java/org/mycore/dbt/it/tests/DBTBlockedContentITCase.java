package org.mycore.dbt.it.tests;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRBlockedContentITCase;

public class DBTBlockedContentITCase extends MIRBlockedContentITCase {

    @Override
    protected MIRControllerFactory createControllerFactory(MCRWebdriverWrapper driver, String appURL) {
        return new DBTControllerFactory(driver, appURL);
    }

    @Override
    protected String getAdminEditorLinkText() {
        return "Bearbeiten dieses Dokumentes";
    }

}

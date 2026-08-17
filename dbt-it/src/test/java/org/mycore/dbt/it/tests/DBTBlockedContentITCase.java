package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRBlockedContentITCase;

public class DBTBlockedContentITCase extends MIRBlockedContentITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTControllerFactory();
    }

    @Override
    protected String getAdminEditorLinkText() {
        return "Bearbeiten dieses Dokumentes";
    }

}

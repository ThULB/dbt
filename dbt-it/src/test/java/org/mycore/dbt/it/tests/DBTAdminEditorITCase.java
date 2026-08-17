package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTAdminControllerFactory;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRAdminEditorITCase;

public class DBTAdminEditorITCase extends MIRAdminEditorITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTAdminControllerFactory();
    }

    @Override
    protected String getPageTitle(){
        return  "MODS-Dokument erstellen – Digitale Bibliothek Thüringen";
    }
}

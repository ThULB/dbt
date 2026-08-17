package org.mycore.dbt.it.tests;

import org.mycore.dbt.it.controller.DBTAdminControllerFactory;
import org.mycore.dbt.it.model.DBTInstitutes;
import org.mycore.mir.it.controller.MIRControllerFactory;
import org.mycore.mir.it.tests.MIRComplexSearchITCase;


public class DBTComplexSearchITCase extends MIRComplexSearchITCase {

    @Override
    protected MIRControllerFactory createControllerFactory() {
        return new DBTAdminControllerFactory();
    }

    @Override
    protected String getPageTitle(){
        return "MODS-Dokument erstellen – Digitale Bibliothek Thüringen";
    }

    @Override
    protected String institutionTestValue() {
        return DBTInstitutes.Friedrich_Schiller_Universitaet_Jena.getValue();
    }

}

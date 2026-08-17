package org.mycore.dbt.it.controller;

import java.util.List;

import org.mycore.common.selenium.drivers.MCRWebdriverWrapper;
import org.mycore.mir.it.model.MIRDNBClassification;

public class DBTAdminModsEditorController extends DBTModsEditorController {

    public DBTAdminModsEditorController(MCRWebdriverWrapper driver, String baseURL) {
        super(driver, baseURL);
    }

    @Override
    public void setClassifications(List<MIRDNBClassification> classifications, int offset) {
        // The DBT admin editor (editor-admins.xed) has an extra mods:classification (podcasts)
        // rendered before the sdnb/DNB one, shifting it one position later than in MIR.
        super.setClassifications(classifications, offset + 1);
    }
}

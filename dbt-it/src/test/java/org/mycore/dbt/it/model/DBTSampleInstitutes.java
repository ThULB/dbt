package org.mycore.dbt.it.model;

import org.mycore.mir.it.model.MIRInstitutes;

public enum DBTSampleInstitutes implements MIRInstitutes {
    Friedrich_Schiller_Universitaet_Jena("1"),
    Technische_Universitaet_Ilmenau("4"),
    Bauhaus_Universitaet_Weimar("3");

    private final String value;

    DBTSampleInstitutes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

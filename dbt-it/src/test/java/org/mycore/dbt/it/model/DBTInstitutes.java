package org.mycore.dbt.it.model;

public enum DBTInstitutes {
    Friedrich_Schiller_Universitaet_Jena("1"),
    Technische_Universitaet_Ilmenau("4"),
    Bauhaus_Universitaet_Weimar("3");

    private final String value;

    DBTInstitutes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

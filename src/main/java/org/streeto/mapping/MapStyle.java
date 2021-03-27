package org.streeto.mapping;

public enum MapStyle {
    STREETO("streeto"),
    PSEUDO("oterrain"),
    STREETO_GLOBAL("streeto_global"),
    PSEUDO_GLOBAL("oterrain_global");

    private final String value;

    public String getValue() {
        return value;
    }

    MapStyle(String value) {
        this.value = value;
    }
}

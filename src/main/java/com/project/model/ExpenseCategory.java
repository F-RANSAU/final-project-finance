package com.project.model;

/**
 * Catégories de dépenses pour le suivi budgétaire.
 */
public enum ExpenseCategory {
    SHOPPING("Shopping"),
    BEAUTY_AND_WELLNESS("Beauté & Bien-être"),
    FOOD_AND_DINING("Alimentation & Sorties"),
    LEISURE("Loisirs"),
    HOUSING_AND_BILLS("Logement & Charges"),
    TRANSPORT("Transport");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
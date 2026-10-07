package org.beltran;

public enum TransactionType {

    DEPOSIT("DEPOSIT"),
    WITHDRAWAL("WITHDRAWAL"),
    TRANSFER_IN("TRANSFER-IN"),
    TRANSFER_OUT("TRANSFER-OUT");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
package Models.Transaction;

import Models.ID.TransactionID;

public abstract class Transaction {
    private TransactionID transactionID;

    public Transaction() {
        this.transactionID = new TransactionID();
    }

    public Transaction(String transactionID) {
        this.transactionID = new TransactionID(transactionID);
    }

    public TransactionID getTransactionID() {
        return transactionID;
    }
}

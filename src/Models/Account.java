package Models;

import Models.ID.AccountID;
import Models.ID.ShortRandomID;
import Models.ID.TransactionID;
import Models.Transaction.Transaction;
import Models.Transaction.Transfer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private AccountID accountID;
    private User user;
    private Currency currency;
    private BigDecimal balance;
    private List<Transaction> transactions;

    public Account() {
        this.accountID = new AccountID();
        this.transactions = new ArrayList<>();
    }

    public Account(AccountID accountID, User user, Currency currency, BigDecimal balance, List<Transaction> transactions) {
        this.accountID = accountID;
        this.user = user;
        this.currency = currency;
        this.balance = balance;
        this.transactions = transactions;
    }

    public Account(AccountID accountID, Currency currency, User user) {
        this.accountID = accountID;
        this.currency = currency;
        this.user = user;
        this.balance = new BigDecimal(0);
        this.transactions = new ArrayList<>();
    }

    public Account(AccountID accountID, Currency currency, User user, BigDecimal balance) {
        this.accountID = accountID;
        this.currency = currency;
        this.user = user;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    public Account(Currency currency, User user, BigDecimal balance) {
        this.accountID = new AccountID();
        this.currency = currency;
        this.user = user;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    public Account(Currency currency, User user) {
        this.accountID = new AccountID();
        this.currency = currency;
        this.user = user;
        this.balance = new BigDecimal(0);
        this.transactions = new ArrayList<>();
    }

    public AccountID getAccountID() {
        return accountID;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public User getUser() {
        return user;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public boolean containsTransaction(TransactionID transactionID) {
        return transactions.stream().anyMatch(transaction -> transaction.getTransactionID().equals(transactionID));
    }

    public Transaction getATransaction(ShortRandomID transactionID) {
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionID().equals(transactionID)) {
                return transaction;
            }
        }
        return null;
    }

    public void generateNewAccountID() {
        this.accountID = new AccountID();
    }

}

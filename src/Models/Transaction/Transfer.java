package Models.Transaction;

import Models.ID.AccountID;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class Transfer extends Transaction {
    private BigDecimal amount;
    private AccountID fromAccountID;
    private AccountID toAccountID;

    public Transfer() {
    }

    public Transfer(AccountID fromAccountID, AccountID toAccountID, BigDecimal amount) {
        super();
        this.fromAccountID = fromAccountID;
        this.toAccountID = toAccountID;
        this.amount = amount;
    }

    public Transfer(String fromAccountID, String toAccountID, String amount) {
        super();
        this.fromAccountID = AccountID.of(fromAccountID);
        this.toAccountID = AccountID.of(toAccountID);
        this.amount = new BigDecimal(amount);
    }

    public Transfer(String fromAccountID, String toAccountID, String amount, String transactionID) {
        super(transactionID);
        this.fromAccountID = AccountID.of(fromAccountID);
        this.toAccountID = AccountID.of(toAccountID);
        this.amount = new BigDecimal(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountID getFromAccountID() {
        return fromAccountID;
    }

    public AccountID getToAccountID() {
        return toAccountID;
    }

    @JsonIgnore
    public String getFromAccountIDString() {
        return fromAccountID.getId();
    }

    @JsonIgnore
    public String getToAccountIDString() {
        return toAccountID.getId();
    }

}

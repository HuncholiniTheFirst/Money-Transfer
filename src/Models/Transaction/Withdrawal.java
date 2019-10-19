package Models.Transaction;

import Models.ID.AccountID;

import java.math.BigDecimal;

public class Withdrawal extends Transaction {
    private BigDecimal amount;
    private AccountID fromAccount;

    public Withdrawal() {
    }

    public Withdrawal(BigDecimal amount, AccountID fromAccount) {
        this.amount = amount;
        this.fromAccount = fromAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountID getFromAccount() {
        return fromAccount;
    }


}

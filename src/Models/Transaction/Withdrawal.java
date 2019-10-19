package Models.Transaction;

import Models.ID.AccountID;

import java.math.BigDecimal;

public class Withdrawal extends Transaction {
    private BigDecimal amount;
    private AccountID toAccount;

    public Withdrawal() {
    }

    public Withdrawal(BigDecimal amount, AccountID fromAccount) {
        this.amount = amount;
        this.toAccount = fromAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountID getToAccount() {
        return toAccount;
    }


}

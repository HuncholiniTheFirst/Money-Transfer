package Models.Transaction;

import Models.ID.AccountID;

import java.math.BigDecimal;

public class Deposit extends Transaction {
    private BigDecimal amount;
    private AccountID toAccount;

    public Deposit() {
    }

    public Deposit(BigDecimal amount, AccountID toAccount) {
        this.amount = amount;
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AccountID getToAccount() {
        return toAccount;
    }


}

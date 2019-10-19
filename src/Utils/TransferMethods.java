package Utils;

import Exceptions.TransferException;
import Models.Account;
import Models.ID.AccountID;
import Models.Transaction.Transaction;
import Models.Transaction.Transfer;

import java.util.Map;
import java.util.Set;

public class TransferMethods {
    public static String performTransfer(Transfer transfer, Map<AccountID, Account> existingAccounts, Set<Transaction> allTransactions) throws TransferException {
        String fromAccountID = transfer.getFromAccountIDString();
        String toAccountID = transfer.getToAccountIDString();
        validateTransferRequest(transfer, existingAccounts, transfer.getFromAccountID(), transfer.getToAccountID());

        Account fromAccount = getExistingAccount(existingAccounts,transfer.getFromAccountID());
        Account toAccount = getExistingAccount(existingAccounts,transfer.getToAccountID());

        fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transfer.getAmount()));
        fromAccount.addTransaction(transfer);
        toAccount.addTransaction(transfer);
        allTransactions.add(transfer);

        StringBuilder successMessage = new StringBuilder("Transfer Performed Successfully, new balance for account: ")
                .append(fromAccountID).append(" is: ").append(fromAccount.getBalance()).append("\n")
                .append("new balance for account: ").append(toAccountID).append(" is: ").append(toAccount.getBalance()).append("\n");

        return successMessage.toString();
    }

    private static void validateTransferRequest(Transfer transfer, Map<AccountID, Account> existingAccounts, AccountID fromAccountID, AccountID toAccountID) throws TransferException {
        // Check that both accounts exist
        if (!checkForExistingAccount(existingAccounts,fromAccountID) || !checkForExistingAccount(existingAccounts,toAccountID)) {
            StringBuilder missingAccountErrorMessage = new StringBuilder().append("The following accounts do not exist on our system: ").append("\n");
            if (!checkForExistingAccount(existingAccounts,fromAccountID)) {
                missingAccountErrorMessage.append(fromAccountID.getId()).append("\n");
            }
            if (!checkForExistingAccount(existingAccounts,toAccountID)) {
                missingAccountErrorMessage.append(toAccountID.getId()).append("\n");
            }
            throw new TransferException(missingAccountErrorMessage.toString());
        }

        Account fromAccount = getExistingAccount(existingAccounts,fromAccountID);
        Account toAccount = getExistingAccount(existingAccounts,toAccountID);

        // Check that the transaction is not a cross currency transaction
        if (fromAccount.getCurrency() != toAccount.getCurrency()) {
            throw new TransferException("At present, we do not support cross currency transfers");
        }

        // Check that fromAccount actually has enough money to perform this transaction
        if (fromAccount.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new TransferException(new StringBuilder().append("Account ").append(fromAccountID.getId()).append(" does not have enough funds to perform this transfer").toString());
        }
    }

    public static boolean checkForExistingAccount(Map<AccountID,Account> existingAccounts, AccountID accountID){
        return existingAccounts.keySet().stream().anyMatch(existingAccount -> existingAccount.equals(accountID));
    }

    public static Account getExistingAccount(Map<AccountID,Account> existingAccounts, AccountID accountID){
        for(Map.Entry<AccountID,Account> existingAccount : existingAccounts.entrySet()){
            if(existingAccount.getKey().equals(accountID)){
                return existingAccount.getValue();
            }
        }
        return null;
    }
}

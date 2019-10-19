package ServiceVerticle;

import Exceptions.TransferException;
import Models.Account;
import Models.Currency;
import Models.ID.AccountID;
import Models.ID.TransactionID;
import Models.Transaction.Deposit;
import Models.Transaction.Transaction;
import Models.Transaction.Transfer;
import Models.Transaction.Withdrawal;
import Models.User;
import Utils.TransferMethods;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MoneyTransferService extends AbstractVerticle {
    Map<AccountID, Account> existingAccounts = new HashMap<>();
    Set<Transaction> allTransactions = new HashSet<>();

    @Override
    public void start(Future<Void> future) {
        Router router = Router.router(vertx);
        // Add our routes to the router
        router.route().handler(BodyHandler.create());
        // POST Routes
        router.route().method(HttpMethod.POST).path("/transfer").handler(this::handleTransfer);
        router.route().method(HttpMethod.POST).path("/account/new").handler(this::addNewAccount);
        // PUT Routes
        router.route().method(HttpMethod.PUT).path("/deposit").handler(this::addFundsToAccount);
        router.route().method(HttpMethod.PUT).path("/withdraw").handler(this::withdrawFundsFromAccount);
        // GET Routes
        router.route().method(HttpMethod.GET).path("/accounts").handler(this::getAllAccounts);
        router.route().method(HttpMethod.GET).path("/accounts/:id").handler(this::getAnAccount);
        router.route().method(HttpMethod.GET).path("/accounts/:accID/transactions").handler(this::getAllTransactionsForAccount);
        router.route().method(HttpMethod.GET).path("/accounts/:accID/transactions/:transID").handler(this::getTransactionForAccount);
        router.route().method(HttpMethod.GET).path("/transactions").handler(this::getAllTransactions);
        // DELETE Routes
        router.route().method(HttpMethod.DELETE).path("/accounts/:id").handler(this::deleteAccount);

        initialiseTestData(existingAccounts);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                System.out.println("Listening on: " + config().getInteger("http.port", 8080));
                                future.complete();
                            } else {
                                future.fail(result.cause());
                            }
                        });
    }

    private void initialiseTestData(Map<AccountID, Account> existingAccounts) {
        AccountID testAccountID = new AccountID("100abc");
        Account testAccount1 = new Account(testAccountID, Currency.GBP, new User("Customer A"), new BigDecimal(10.00));

        AccountID testAccountID2 = new AccountID("999xyz");
        Account testAccount2 = new Account(testAccountID2, Currency.GBP, new User("Customer B"));

        AccountID testAccountIDDifferentCurrency = new AccountID("100xyz");
        Account testAccountDifferentCurrency = new Account(testAccountIDDifferentCurrency, Currency.USD, new User("Customer C"), new BigDecimal(20.00));

        AccountID testAccountIDWithTransactions = new AccountID("555abc");
        Transfer testTransfer = new Transfer("100abc", "555abc", "1.30", "tran01");
        Transfer testTransfer2 = new Transfer("100abc", "555abc", "2.30", "tran02");
        Account testAccountWithTransactions = new Account(testAccountIDWithTransactions, Currency.USD, new User("Customer C"), new BigDecimal(20.00));
        testAccountWithTransactions.addTransaction(testTransfer);
        testAccountWithTransactions.addTransaction(testTransfer2);

        existingAccounts.put(testAccountID, testAccount1);
        existingAccounts.put(testAccountID2, testAccount2);
        existingAccounts.put(testAccountIDDifferentCurrency, testAccountDifferentCurrency);
        existingAccounts.put(testAccountIDWithTransactions, testAccountWithTransactions);

        allTransactions.add(testTransfer);
        allTransactions.add(testTransfer2);
    }

    private void handleTransfer(RoutingContext routingContext) {
        String bodyAsString = routingContext.getBodyAsString();
        if (bodyAsString == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("Could not decode transfer details!");
        }

        // Take transfer request and create a Transfer object
        final Transfer transfer = Json.decodeValue(bodyAsString, Transfer.class);
        try {
            String transferResult = TransferMethods.performTransfer(transfer, existingAccounts, allTransactions);
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(transferResult);
        } catch (TransferException e) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(e.toString());
        }

    }

    private void addNewAccount(RoutingContext routingContext) {
        String bodyAsString = routingContext.getBodyAsString();
        if (bodyAsString == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("No body on POST request for new Account! Can't see any account details!");
        }

        // Take transfer request and create a Transfer object
        try {
            final Account newAccount = Json.decodeValue(bodyAsString, Account.class);
            while (TransferMethods.checkForExistingAccount(existingAccounts, newAccount.getAccountID())) {
                newAccount.generateNewAccountID();
            }
            existingAccounts.put(newAccount.getAccountID(), newAccount);
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(newAccount));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("Error when creating new account: " + e.toString());
        }
    }

    private void addFundsToAccount(RoutingContext routingContext) {
        String bodyAsString = routingContext.getBodyAsString();
        if (bodyAsString == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("No body on PUT request for new deposit! Can't see any deposit details!");
        }
        final Deposit newDeposit = Json.decodeValue(bodyAsString, Deposit.class);

        AccountID accountID = newDeposit.getToAccount();
        if (accountID == null) {
            routingContext.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                    .end("No account supplied in deposit request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).putHeader("content-type", "application/json; charset=utf-8")
                        .end("No accounts exist with the ID " + accountID.getId());
            } else {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enableDefaultTyping();

                    targetAccount.setBalance(targetAccount.getBalance().add(newDeposit.getAmount()));
                    targetAccount.addTransaction(newDeposit);
                    allTransactions.add(newDeposit);
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(mapper.writeValueAsString(targetAccount));
                } catch (Exception e) {
                    routingContext.response()
                            .setStatusCode(501)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end("Error when trying to deposit to account: " + e.toString());
                }

            }
        }
    }

    private void withdrawFundsFromAccount(RoutingContext routingContext) {
        String bodyAsString = routingContext.getBodyAsString();
        if (bodyAsString == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end("No body on PUT request for new withdrawal! Can't see any withdrawal details!");
        }
        final Withdrawal withdrawal = Json.decodeValue(bodyAsString, Withdrawal.class);

        AccountID accountID = withdrawal.getFromAccount();
        if (accountID == null) {
            routingContext.response().setStatusCode(400).putHeader("content-type", "application/json; charset=utf-8")
                    .end("No account supplied in deposit request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).putHeader("content-type", "application/json; charset=utf-8")
                        .end("No accounts exist with the ID " + accountID.getId());
            } else {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enableDefaultTyping();

                    if (targetAccount.getBalance().compareTo(withdrawal.getAmount()) < 1) {
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end("This account does not have enough funds for this withdrawal");
                        return;
                    }

                    targetAccount.setBalance(targetAccount.getBalance().subtract(withdrawal.getAmount()));
                    targetAccount.addTransaction(withdrawal);
                    allTransactions.add(withdrawal);
                    routingContext.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(mapper.writeValueAsString(targetAccount));
                } catch (Exception e) {
                    routingContext.response()
                            .setStatusCode(501)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end("Error when trying to withdraw from account: " + e.toString());
                }

            }
        }
    }

    private void getAllAccounts(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(existingAccounts.values()));
    }

    private void getAnAccount(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        AccountID accountID = new AccountID(id);
        if (id == null) {
            routingContext.response().setStatusCode(400).end("ID was null, no ID on the request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).end("No accounts exist with this ID!");
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(targetAccount));
            }
        }
    }

    private void getAllTransactionsForAccount(RoutingContext routingContext) {
        final String accID = routingContext.request().getParam("accID");
        AccountID accountID = new AccountID(accID);
        if (accID == null) {
            routingContext.response().setStatusCode(400).end("ID was null, no ID on the request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).end("No accounts exist with this ID!");
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(targetAccount.getTransactions()));
            }
        }
    }

    private void getTransactionForAccount(RoutingContext routingContext) {
        final String accID = routingContext.request().getParam("accID");
        if (accID == null) {
            routingContext.response().setStatusCode(400).end("No Account ID found on the request!");
            return;
        }
        AccountID accountID = new AccountID(accID);

        final String transactionID = routingContext.request().getParam("transID");
        if (transactionID == null) {
            routingContext.response().setStatusCode(400).end("No transaction ID found on the request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).end("No accounts exist with this ID!");
                return;
            }

            Transaction targetTransaction = targetAccount.getATransaction(TransactionID.of(transactionID));

            if (targetTransaction == null) {
                routingContext.response().setStatusCode(404).end("A transaction with this ID does not exist");
            } else {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(targetTransaction));
            }
        }
    }

    private void getAllTransactions(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(allTransactions));
    }

    private void deleteAccount(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        AccountID accountID = new AccountID(id);
        if (id == null) {
            routingContext.response().setStatusCode(400).end("ID was null, no ID on the request!");
        } else {
            Account targetAccount = TransferMethods.getExistingAccount(existingAccounts, accountID);
            if (targetAccount == null) {
                routingContext.response().setStatusCode(404).end("No accounts exist with this ID!");
            } else {
                if (TransferMethods.removeExistingAccount(existingAccounts, accountID)) {
                    routingContext.response().setStatusCode(204)
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end("Account deleted.");
                } else {
                    routingContext.response().setStatusCode(404).end("This account wasn't recognised as an existing account");
                }

            }
        }
    }


}

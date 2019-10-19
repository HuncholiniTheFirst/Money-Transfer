package Test;

import Models.Account;
import Models.ID.AccountID;
import Models.Transaction.Deposit;
import ServiceVerticle.MoneyTransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class DepositFundsTest {
    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port)
                );
        vertx.deployVerticle(MoneyTransferService.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testADepositForANonExistingAccount(TestContext context) {
        Async async = context.async();
        final String testDeposit = Json.encodePrettily(new Deposit(new BigDecimal("14.00"), new AccountID("unk4wn")));
        vertx.createHttpClient().put(port, "localhost", "/deposit")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testDeposit.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 404);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("No accounts exist with the ID"));
                        Assert.assertTrue(body.toString().contains("unk4wn"));
                        async.complete();
                    });
                })
                .write(testDeposit)
                .end();
        async.awaitSuccess(5000);

    }

    @Test
    public void testASuccessfulDeposit(TestContext context) {
        Async async = context.async();
        final String testDeposit = Json.encodePrettily(new Deposit(new BigDecimal("14.00"), new AccountID("100abc")));

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        vertx.createHttpClient().put(port, "localhost", "/deposit")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testDeposit.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        try {
                            Account account = mapper.readValue(body.toString(), Account.class);
                            Assert.assertTrue(account.getBalance().compareTo(new BigDecimal(24.00)) == 0);
                            Assert.assertTrue(account.getAccountID().getId().equals("100abc"));
                            Assert.assertFalse(account.getTransactions().isEmpty());
                            async.complete();
                        } catch (IOException e) {
                            System.out.println(e.toString());
                        }

                    });
                })
                .write(testDeposit)
                .end();
        async.awaitSuccess(5000);
    }
}

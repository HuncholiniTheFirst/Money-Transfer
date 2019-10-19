package Test;

import Models.*;
import Models.ID.AccountID;
import Models.ID.ShortRandomID;
import ServiceVerticle.MoneyTransferService;
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
public class GetAllAccountsTest {
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
    public void testGetAllAccounts(TestContext context) {
        AccountID testAccountID = new AccountID("100abc");
        Account testAccount1 = new Account(testAccountID, Currency.GBP, new User("Customer A"), new BigDecimal(10.00));

        AccountID testAccountID2 = new AccountID("999xyz");
        Account testAccount2 = new Account(testAccountID2, Currency.GBP, new User("Customer B"));

        AccountID testAccountIDDifferentCurrency = new AccountID("100xyz");
        Account testAccountDifferentCurrency = new Account(testAccountIDDifferentCurrency, Currency.USD, new User("Customer C"), new BigDecimal(20.00));

        String testAccount1JSON = Json.encodePrettily(testAccount1);
        String testAccount2JSON = Json.encodePrettily(testAccount2);
        String testAccountDiffCurJSON = Json.encodePrettily(testAccountDifferentCurrency);

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/accounts")
                .handler(response -> {
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains(testAccount1JSON));
                        Assert.assertTrue(body.toString().contains(testAccount2JSON));
                        Assert.assertTrue(body.toString().contains(testAccountDiffCurJSON));
                        async.complete();
                    });
                })
                .end();
        async.awaitSuccess(5000);

    }
}

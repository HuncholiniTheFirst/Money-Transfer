package Test;

import Models.Account;
import Models.Currency;
import Models.Transaction.Transfer;
import Models.User;
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
public class AddNewAccountTest {
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
    public void testAddingAnAccount(TestContext context) {
        Async async = context.async();
        final String testAccount = Json.encodePrettily(new Account(Currency.GBP, new User("Ismaaeel Ataullah"),
                new BigDecimal("10.99")));
        vertx.createHttpClient().post(port, "localhost", "/account/new")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testAccount.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains(testAccount));
                        async.complete();
                    });
                })
                .write(testAccount)
                .end();
        async.awaitSuccess(5000);

    }
}

package Test;

import Models.Transaction.Transfer;
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
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class TransferFundsTest {
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
    public void testATransferWithNonExistingAccounts(TestContext context) {
        Async async = context.async();
        final String testTransfer = Json.encodePrettily(new Transfer("unk4wn", "unk5wn",
                "10.99"));
        vertx.createHttpClient().post(port, "localhost", "/transfer")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testTransfer.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 400);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("The following accounts do not exist on our system"));
                        Assert.assertTrue(body.toString().contains("unk4wn"));
                        Assert.assertTrue(body.toString().contains("unk5wn"));
                        async.complete();
                    });
                })
                .write(testTransfer)
                .end();
                async.awaitSuccess(5000);

    }

    @Test
    public void testATransferWithInsufficientFunds(TestContext context) {
        Async async = context.async();
        final String testTransfer = Json.encodePrettily(new Transfer("100abc", "999xyz",
                "10.99"));
        vertx.createHttpClient().post(port, "localhost", "/transfer")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testTransfer.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 400);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("100abc does not have enough funds"));
                        async.complete();
                    });
                })
                .write(testTransfer)
                .end();
                async.awaitSuccess(5000);
    }

    @Test
    public void testATransferWithCrossCurrencyAccounts(TestContext context) {
        Async async = context.async();
        final String testTransfer = Json.encodePrettily(new Transfer("100abc", "100xyz",
                "10.99"));
        vertx.createHttpClient().post(port, "localhost", "/transfer")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testTransfer.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 400);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("At present, we do not support cross currency transfers"));
                        async.complete();
                    });
                })
                .write(testTransfer)
                .end();
                async.awaitSuccess(5000);
    }

    @Test
    public void testASuccessfulTransfer(TestContext context) {
        Async async = context.async();
        final String testTransfer = Json.encodePrettily(new Transfer("100abc", "999xyz",
                "9.99"));
        vertx.createHttpClient().post(port, "localhost", "/transfer")
                .putHeader("content-type", "application/json")
                .putHeader("content-length", Integer.toString(testTransfer.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("new balance for account: 100abc is: 0.01"));
                        Assert.assertTrue(body.toString().contains("new balance for account: 999xyz is: 9.99"));
                        async.complete();
                    });
                })
                .write(testTransfer)
                .end();
        async.awaitSuccess(5000);
    }
}

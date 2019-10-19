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
public class GetTransactionForAccountTest {
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
    public void testGetATransactionForAccount(TestContext context) {
        Transfer testTransfer = new Transfer("100abc", "555abc", "1.30", "tran01");
        String expectedTestTransfer = Json.encodePrettily(testTransfer);

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/accounts/555abc/transactions/tran01")
                .handler(response -> {
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().equals(expectedTestTransfer));
                        async.complete();
                    });
                })
                .end();
        async.awaitSuccess(5000);

    }

    @Test
    public void testWithNonExistentTransaction(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/accounts/100abc/transactions/someID")
                .handler(response ->
                        response.bodyHandler(body -> {
                            Assert.assertTrue(body.toString().contains("A transaction with this ID does not exist"));
                            async.complete();
                        })
                )
                .end();
        async.awaitSuccess(5000);

    }


}

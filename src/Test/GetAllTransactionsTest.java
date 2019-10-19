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
public class GetAllTransactionsTest {
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
    public void testGetAllTransactions(TestContext context) {

        Transfer testTransfer = new Transfer("100abc", "555abc", "1.30", "tran01");
        Transfer testTransfer2 = new Transfer("100abc", "555abc", "2.30", "tran02");

        String testTransferJSON = Json.encodePrettily(testTransfer);
        String testTransfer2JSON = Json.encodePrettily(testTransfer2);

        Transfer nonExistentTransfer = new Transfer("100abc", "555abc", "999.30", "tran01");
        String nonExistentTransferJSON = Json.encodePrettily(nonExistentTransfer);

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/transactions")
                .handler(response ->
                        response.bodyHandler(body -> {
                            Assert.assertTrue(body.toString().contains(testTransferJSON));
                            Assert.assertTrue(body.toString().contains(testTransfer2JSON));
                            Assert.assertFalse(body.toString().contains(nonExistentTransferJSON));
                            async.complete();
                        })
                )
                .end();
        async.awaitSuccess(5000);

    }
}

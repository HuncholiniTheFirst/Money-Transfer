package Test;

import ServiceVerticle.MoneyTransferService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
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
public class DeleteAccountTest {
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
    public void testDeleteAnAccount(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().delete(port, "localhost", "/accounts/100abc")
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 204);
                    async.complete();
                })
                .end();
        async.awaitSuccess(5000);
    }

    @Test
    public void testDeleteANonExistentAccount(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().delete(port, "localhost", "/accounts/Unk4wn")
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 404);
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains("No accounts exist with this ID!"));
                    });
                    async.complete();
                })
                .end();
        async.awaitSuccess(5000);
    }
}

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
public class GetAnAccountTest {
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
    public void testGetAnAccount(TestContext context) {
        AccountID testAccountID = new AccountID("100abc");
        Account testAccount1 = new Account(testAccountID, Currency.GBP, new User("Customer A"), new BigDecimal(10.00));

        String expectedTestAccountJSON = Json.encodePrettily(testAccount1);

        Async async = context.async();
        vertx.createHttpClient().get(port, "localhost", "/accounts/100abc")
                .handler(response -> {
                    response.bodyHandler(body -> {
                        Assert.assertTrue(body.toString().contains(expectedTestAccountJSON));
                        async.complete();
                    });
                })
                .end();
        async.awaitSuccess(5000);

    }
}

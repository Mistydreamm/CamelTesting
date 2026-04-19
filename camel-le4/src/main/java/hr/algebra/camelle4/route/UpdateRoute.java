package hr.algebra.camelle4.route;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateRoute extends RouteBuilder {

    private final ResponseProcessor responseProcessor;

    private UpdateRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in update-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("""
                        {"error": "${exception.message}", "routeId": "update-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=update-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));
        from("direct:update-" + AppConfig.ENTITY_NAME)
                .routeId("update-" + AppConfig.ENTITY_NAME)

                .loadBalance().roundRobin()
                .to("direct:payload1-id5")
                .to("direct:payload2-id10")
                .to("direct:payload3-id16")


                .log(">>> Triggered: udpate-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})")
                .setProperty(ResponseProcessor.OP_METHOD, constant("PUT"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL + "/${header.targetId}"))
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("file:%s?fileName=update-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: update-" + AppConfig.ENTITY_NAME + "${header.targetId}");

        from("direct:payload1-id5").setBody(simple("{\"title\" : \"update1\",\n\"content\" : \"updated with payload1!\"}"))
                .setHeader("targetId", constant(5));
        from("direct:payload2-id10").setBody(simple("{\"title\" : \"update2\",\n\"content\" : \"updated with payload2!\"}"))
                .setHeader("targetId", constant(10));
        from("direct:payload3-id16").setBody(simple("{\"title\" : \"update3\",\n\"content\" : \"updated with payload3!\"}"))
                .setHeader("targetId", constant(16));
    }
}



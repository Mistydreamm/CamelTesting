package hr.algebra.camelle4.route;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.builder.Builder.simple;

@Component
public class GetOneRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;
    private final Long[] idTab;

    private GetOneRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
        this.idTab = new Long[]{5L, 10L, 16L, 17L, 18L};
    }

    @Override
    public void configure() {

        onException(Exception.class)
                .handled(true)
                .log("ERROR in get-one-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("""
                        {"error": "${exception.message}", "routeId": "get-one-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-one-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));
        from("timer:getOneTimer?period=15000")
                .routeId("get-one-" + AppConfig.ENTITY_NAME)

                .loadBalance().roundRobin()
                .to("direct:set-id-5")
                .to("direct:set-id-10")
                .to("direct:set-id-16")
                .to("direct:set-id-17")
                .to("direct:set-id-18")
                .end()

                .log(">>> Triggered: get-one-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})")
                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL + "/${header.targetId}"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("file:%s?fileName=get-one-%s-id${header.targetId}-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: get-one-" + AppConfig.ENTITY_NAME + " (id = ${header.targetId})");

        from("direct:set-id-5").setHeader("targetId", constant(5));
        from("direct:set-id-10").setHeader("targetId", constant(10));
        from("direct:set-id-16").setHeader("targetId", constant(16));
        from("direct:set-id-17").setHeader("targetId", constant(17));
        from("direct:set-id-18").setHeader("targetId", constant(18));
    }
}

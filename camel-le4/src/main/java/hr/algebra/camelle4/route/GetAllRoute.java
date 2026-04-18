package hr.algebra.camelle4.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;

@Component
public class GetAllRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    public GetAllRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in get-all-" + AppConfig.ENTITY_NAME + ": ${exception.message}")
                .setBody(simple("""
                        {"error": "${exception.message}", "routeId": "get-all-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=get-all-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));
        from("direct:get-all-" + AppConfig.ENTITY_NAME)
                .routeId("get-all-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: get-all-" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("GET"))
                .setProperty(ResponseProcessor.OP_ENDPOINT, constant(AppConfig.BASE_URL))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("file:%s?fileName=get-all-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: get-all-" + AppConfig.ENTITY_NAME);
    }
}

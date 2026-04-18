package hr.algebra.camelle4.route;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.builder.Builder.simple;

@Component
public class CreateRoute extends RouteBuilder {
    private final ResponseProcessor responseProcessor;

    private CreateRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in create-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("""
                        {"error": "${exception.message}", "routeId": "create-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=create-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));
        from("direct:create-" + AppConfig.ENTITY_NAME)
                .routeId("create-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: create-" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL ))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("file:%s?fileName=create-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: create-" + AppConfig.ENTITY_NAME);
    }
}

package hr.algebra.camelle4.route;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class DeleteRoute extends RouteBuilder {

    private final ResponseProcessor responseProcessor;

    private DeleteRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .log("ERROR in delete-" + AppConfig.ENTITY_NAME + ": ${exception.message} ")
                .setBody(simple("""
                        {"error": "${exception.message}", "routeId": "delete-%s"}"""
                        .formatted(AppConfig.ENTITY_NAME)))
                .to("file:%s?fileName=delete-%s-error-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT));
        from("timer:deleteTimer?period=60000")
                .routeId("delete-" + AppConfig.ENTITY_NAME)
                .log(">>> Triggered: delete-" + AppConfig.ENTITY_NAME)
                .setProperty(ResponseProcessor.OP_METHOD, constant("POST"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                        simple(AppConfig.BASE_URL ))
                .setBody(simple("{\"title\" : \"deletedNote\",\n\"content\" : \"this will be deleted\"}"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .unmarshal().json()
                .setHeader("targetId", simple("${body[id]}"))
                .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
                .setHeader(Exchange.HTTP_PATH, simple("/${header.targetId}"))
                .setProperty(ResponseProcessor.OP_METHOD, constant("DELETE"))
                .setProperty(ResponseProcessor.OP_ENDPOINT,
                simple(AppConfig.BASE_URL + "body[id]" + "/${header.targetId}" ))
                .setBody(simple(null))
                .to(AppConfig.BASE_URL + "?bridgeEndpoint=true")
                .process(responseProcessor)
                .to("file:%s?fileName=delete-%s-${date:now:%s}.json"
                        .formatted(AppConfig.OUTPUT_DIR,
                                AppConfig.ENTITY_NAME,
                                AppConfig.TIMESTAMP_FORMAT))
                .log("<<< Completed: delete-" + AppConfig.ENTITY_NAME);






    }
}

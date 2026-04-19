package hr.algebra.camelle4.route;

import hr.algebra.camelle4.config.AppConfig;
import hr.algebra.camelle4.processor.ResponseProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class DeleteRoute extends RouteBuilder {
    //d'abord on fait le post

    private final ResponseProcessor responseProcessor;

    private DeleteRoute(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void configure() {


}

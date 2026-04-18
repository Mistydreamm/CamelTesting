package hr.algebra.camelle4.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.language.bean.Bean;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ResponseProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseProcessor.class);
    private static final int PREVIEW_LENGTH = 200;
    public static final String OP_METHOD = "opMethod";
    public static final String OP_ENDPOINT = "opEndpoint";
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void process(Exchange exchange) throws Exception {
        String routeId = exchange.getFromRouteId();
        String method = exchange.getProperty(OP_METHOD, "UNKNOWN", String.class);
        String endpoint = exchange.getProperty(OP_ENDPOINT, "UNKNOWN", String.class);
        Integer status = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE,
                Integer.class);
        String rawBody = exchange.getIn().getBody(String.class);
        if (rawBody == null) {
            rawBody = "";
        }
        String preview = rawBody.length() > PREVIEW_LENGTH
                ? rawBody.substring(0, PREVIEW_LENGTH) + "..."
                : rawBody;
        LOG.info("┌─ Response Metadata ──────────────────────────────");
        LOG.info("│ Route ID : {}", routeId);
        LOG.info("│ HTTP Method: {}", method);
        LOG.info("│ Endpoint : {}", endpoint);
        LOG.info("│ HTTP Status: {}", status);
        LOG.info("│ Preview : {}", preview);
        LOG.info("└──────────────────────────────────────────────────");
        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("capturedAt", LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        envelope.put("routeId", routeId);
        envelope.put("httpMethod", method);
        envelope.put("endpoint", endpoint);
        envelope.put("httpStatus", status != null ? status : 0);
        try {
            envelope.set("response", mapper.readTree(rawBody.isEmpty() ? "null" :
                    rawBody));
        } catch (Exception e) {
            envelope.put("response", rawBody);
        }
        String envelopeJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(envelope);
        exchange.getIn().setBody(envelopeJson);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
    }
}

package hr.algebra.camelle4.controller;

import hr.algebra.camelle4.config.AppConfig;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/camel/notes")
public class CamelTriggerController {
    private static final Logger LOG = LoggerFactory.getLogger(CamelTriggerController.class);
    private final ProducerTemplate producerTemplate;

    public CamelTriggerController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerGetAll() {
        LOG.info("HTTP trigger received: GET /camel/notes");
        String result = producerTemplate.requestBody(
                "direct:get-all-" + AppConfig.ENTITY_NAME, null, String.class);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerGetOne(@PathVariable Long id) {
        LOG.info("HTTP trigger received: GET /camel/notes/{}", id);
        Map<String, Object> headers = new HashMap<>();
        headers.put("targetId", id);
        String result = producerTemplate.requestBodyAndHeaders(
                "direct:get-one-" + AppConfig.ENTITY_NAME, null, headers, String.class);
        return ResponseEntity.ok(result);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerPostOne(@RequestBody String noteJson) {
        LOG.info("HTTP trigger received: POST camel/notes");
        String result = producerTemplate.requestBody(
                "direct:create-" + AppConfig.ENTITY_NAME, noteJson, String.class);
        return ResponseEntity.ok(result);

    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerPutOne(@RequestBody String noteJson, @PathVariable String id) {
        LOG.info("HTTP trigger received: PUT camel/notes{}", id);
        Map<String, Object> headers = new HashMap<>();
        headers.put("targetId", id);
        String result = producerTemplate.requestBodyAndHeaders(
                "direct:update-" + AppConfig.ENTITY_NAME, noteJson, headers, String.class);
        return ResponseEntity.ok(result);
    }

}

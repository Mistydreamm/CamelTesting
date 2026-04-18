package hr.algebra.camelle4.controller;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/notes")
public class CamelTriggerController {
    private static final Logger LOG = LoggerFactory.getLogger(CamelTriggerController.class);
    private ProducerTemplate producerTemplate;
    public CamelTriggerController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> triggerGetAll() {
        LOG.info("HTTP trigger received: GET /camel/notes");
        String result = producerTemplate.requestBody(
                "direct:get-all-notes", null, String.class);
        return ResponseEntity.ok(result);
    }
}

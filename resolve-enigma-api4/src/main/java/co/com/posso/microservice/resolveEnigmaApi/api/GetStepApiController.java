package co.com.posso.microservice.resolveEnigmaApi.api;

import co.com.posso.microservice.resolveEnigmaApi.model.GetEnigmaRequest;
import co.com.posso.microservice.resolveEnigmaApi.model.GetEnigmaStepResponse;
import co.com.posso.microservice.resolveEnigmaApi.model.Header;
import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyRequest;
import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyResponseErrors;
import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyResponseSuccess;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;

import org.apache.camel.EndpointInject;
import org.apache.camel.FluentProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2024-03-10T15:24:27.886-05:00[America/Bogota]")
@Controller
public class GetStepApiController implements GetStepApi {

    private static final Logger log = LoggerFactory.getLogger(GetStepApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    private Object response;
    
    @EndpointInject(uri="direct:get-step-one")
    private FluentProducerTemplate producerTemplateResolveEnigma;

    @org.springframework.beans.factory.annotation.Autowired
    public GetStepApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<List<JsonApiBodyResponseSuccess>> getStep() {
    	try {
    		response = producerTemplateResolveEnigma.request();
    		List<JsonApiBodyResponseSuccess> listResponse = new ArrayList<>();
    		listResponse.add((JsonApiBodyResponseSuccess) response);
    		return new ResponseEntity<List<JsonApiBodyResponseSuccess>>(listResponse, HttpStatus.OK);
    	} catch (Exception e) {
    		log.error("Couldn´t serialize response for content type application/json", e);
    		return new ResponseEntity<List<JsonApiBodyResponseSuccess>>(HttpStatus.BAD_GATEWAY);
    	}
    }
    
    @GetMapping("/getStepString")
    public ResponseEntity<?> getStepString(){
    	return new ResponseEntity<>("Step one: Open the refrigerator", HttpStatus.OK);
    }
    
    public ResponseEntity<List<JsonApiBodyResponseSuccess>> getStepPost(@ApiParam(value = "Get one enigma step API" ,required=true )  @Valid @RequestBody JsonApiBodyRequest body) {
        String accept = request.getHeader("Accept");
        
        List<GetEnigmaRequest> enigmas = body.getData();
        
        List<JsonApiBodyResponseSuccess> datosRespuesta = new ArrayList();
        
        Header header = enigmas.get(0).getHeader();
        String pasoEnigma = enigmas.get(0).getEnigma();

        
        
        header.setId(header.getType());
        header.setType(header.getType());
        GetEnigmaStepResponse enigmaResponse = new GetEnigmaStepResponse();
        enigmaResponse.setHeader(header);
        
        String respuesta = "1: Abrir el Refrigerador";
        
        if (!pasoEnigma.equals("1")) {
        	respuesta = "Id Incorrecto-Error";
        } 
        enigmaResponse.setAnswer(respuesta);
        datosRespuesta.add(new JsonApiBodyResponseSuccess().addDataItem(enigmaResponse));
        
        return new ResponseEntity<>(datosRespuesta, HttpStatus.OK);
    }

}
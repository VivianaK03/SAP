package co.com.posso.microservice.resolveEnigmaApi.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.component.http4.HttpMethods;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyRequest;
import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyResponseErrors;
import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyResponseSuccess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ResolveEnigmaTransactionRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		from("direct:resolve-enigma")
		.log("Request body ${body}")
		.routeId("resolveEnigma")
		.log("Request body ${body}")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				JsonApiBodyRequest serviceRequestBody =  (JsonApiBodyRequest) exchange.getIn().getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonBody = objectMapper.writeValueAsString(serviceRequestBody);

				
				exchange.getIn().setBody(jsonBody);
				exchange.getIn().setHeader("Content-Type", "application/json");
				exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);

				exchange.setProperty("ServiceId", serviceRequestBody.getData().get(0).getHeader().getId());
				exchange.setProperty("ServiceType", serviceRequestBody.getData().get(0).getHeader().getType());
				exchange.setProperty("ServiceEnigma", serviceRequestBody.getData().get(0).getEnigma());
				exchange.setProperty("Error", "0000");
				exchange.setProperty("descError", "No error");
			
				
			}        	
        })
		.to("http4://localhost:8082/v1/getOneEnigma/getStep")
		.convertBodyTo(String.class)
	    .process(new Processor() {
	        @Override
	        public void process(Exchange exchange) throws Exception {
				 
	        	 String jsonString = exchange.getIn().getBody(String.class);
	        	 exchange.getIn().setBody(jsonString);
	        	 processJsonResponse(jsonString, exchange);
	        	 exchange.getIn().setBody("");
	        	 exchange.getIn().setHeader("Content-Type", "application/json");

	        }
	    })
	    .to("http4://localhost:8084/v1/getOneEnigma/getStep")
		.convertBodyTo(String.class)
		.log("Response body ${body}")
	    .process(new Processor() {
	        @Override
	        public void process(Exchange exchange) throws Exception {
	        	 String jsonString = exchange.getIn().getBody(String.class);
	        	 processJsonResponse(jsonString, exchange);
	        	 exchange.getIn().setBody("");
	        	 exchange.getIn().setHeader("Content-Type", "application/json");

	        }
	    })
	    .to("http4://localhost:8086/v1/getOneEnigma/getStep")
		.convertBodyTo(String.class)
		.log("Response body ${body}")
	    .process(new Processor() {
	        @Override
	        public void process(Exchange exchange) throws Exception {
	        	 String jsonString = exchange.getIn().getBody(String.class);
	        	 processJsonResponse(jsonString, exchange);
	        }
	    })
		.to("freemarker:templates/ResolveEnigmaTransactionResponse.ftl")
		.log("Response ${body}")
		
		.choice()
    	.when(exchangeProperty("Error").isEqualTo("0000"))
    	 	.to("direct:generate-response-success")
        .otherwise()
         	.to("direct:generate-response-error")            	
         .end();
		
		
		from("direct:generate-response-success")
		.to("freemarker:templates/ResolveEnigmaTransactionResponse.ftl")
		.unmarshal().json(JsonLibrary.Jackson, JsonApiBodyResponseSuccess.class);
		//.to("seda:save-log?waitForTaskToComplete=never");
		
		from("direct:generate-response-error")
		.to("freemarker:templates/ResolveEnigmaTransactionError.ftl")
		.unmarshal().json(JsonLibrary.Jackson, JsonApiBodyResponseErrors.class);
		
	}
	
	
	private void processJsonResponse(String jsonString, Exchange exchange) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    List<JsonApiBodyResponseSuccess> responseList = mapper.readValue(jsonString, new TypeReference<List<JsonApiBodyResponseSuccess>>(){});

	    JsonApiBodyResponseSuccess firstElement = responseList.get(0);
	    exchange.getIn().setBody(firstElement);
	    
	    switch(firstElement.getData().get(0).getAnswer()) {
	    	case "1: Abrir el Refrigerador":
	    		exchange.setProperty("Step1", firstElement.getData().get(0).getAnswer());
	    	case "2: Poner la Jirafa dentro del refrigerador":
	    		exchange.setProperty("Step2", firstElement.getData().get(0).getAnswer());
	    	case "3: Cerrar el Refrigerador":
	    		exchange.setProperty("Step3", firstElement.getData().get(0).getAnswer());
	    }
	}
	
}
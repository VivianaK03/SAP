package co.com.posso.microservice.resolveEnigmaApi.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.com.posso.microservice.resolveEnigmaApi.model.JsonApiBodyResponseSuccess;

@Component
public class GetStepOneClientRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		// TODO Auto-generated method stub
		from("direct:get-step-two")
		//.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
		.to("freemarker:templates/GetStepOneClientTemplate.ftl")
		.log("Request microservice step one ${body}")
		.hystrix()
		.hystrixConfiguration().executionTimeoutInMilliseconds(2000).end()
		.to("http4://localhost:8084/v1/getOneEnigma/getStepPost")
		.convertBodyTo(String.class)
		.log("String Response microservice step one ${body}")
		.log("Java microservice step one ${body}")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception{
                ObjectMapper mapper = new ObjectMapper();
                String bodyInfo = exchange.getIn().getBody(String.class);
                List<JsonApiBodyResponseSuccess> answer = mapper.readValue(bodyInfo, new TypeReference<List<JsonApiBodyResponseSuccess>>(){});
                if (answer != null && !answer.isEmpty()) {
                    JsonApiBodyResponseSuccess firstElement = answer.get(0);
                    exchange.getIn().setBody(firstElement);
                    /*if(firstElement.getData().get(0).getAnswer().equals("Step 2: Put the jiraffe in ")) {
                  	  exchange.setProperty("Step one", firstElement.getData().get(0).getAnswer());
                  	  exchange.setProperty("Error", "0000");
                  	  exchange.setProperty("DescError", "No error");
                    }else {
                  	  exchange.setProperty("Error", "0001");
                  	  exchange.setProperty("DescError", "Error consulting step one");
                    }*/
                } else {
                    exchange.getIn().setBody(null); // or any default value if needed
                }
			}
		})
		.endHystrix()
	      .onFallback()
	      .process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.setProperty("error", "0002");
				exchange.setProperty("DescError", "Error consulting the step one");
				
			}        	
        })
	    .end()
		.log("Response code ${exchangeProperty[error]}")
		.log("Response description ${exchangeProperty[descError]}");
	}
	
}
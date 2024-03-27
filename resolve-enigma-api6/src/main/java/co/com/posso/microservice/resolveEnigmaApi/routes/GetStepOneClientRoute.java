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
		from("direct:get-step-three")
		.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
		.to("freemarker:templates/GetStepOneClientTemplate.ftl")
		.log("Request microservice step one ${body}")
		.hystrix()
		.hystrixConfiguration().executionTimeoutInMilliseconds(2000).end()
		.to("http4://localhost:8086/v1/getOneEnigma/getStepPost")
		.convertBodyTo(String.class)
		.log("String Response microservice step one ${body}")
		.log("Java microservice step one ${body}")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception{
                ObjectMapper mapper = new ObjectMapper();
                String bodyInfo = exchange.getIn().getBody(String.class);
                List<JsonApiBodyResponseSuccess> response = mapper.readValue(bodyInfo, new TypeReference<List<JsonApiBodyResponseSuccess>>(){});
                if (response != null && !response.isEmpty()) {
                    JsonApiBodyResponseSuccess firstElement = response.get(0);
                    exchange.getIn().setBody(firstElement);
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
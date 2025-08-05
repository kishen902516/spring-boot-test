package com.weather.api.routes;

import com.weather.api.processor.GenericHttpApiProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ApiRouteBuilder extends RouteBuilder {
    
    private final GenericHttpApiProcessor httpApiProcessor;
    
    public ApiRouteBuilder(GenericHttpApiProcessor httpApiProcessor) {
        this.httpApiProcessor = httpApiProcessor;
    }
    
    @Override
    public void configure() throws Exception {
        
        // Global exception handler
        onException(Exception.class)
            .handled(true)
            .process(httpApiProcessor::handleApiError)
            .to("log:api-error?level=ERROR");
        
        // Generic API invocation route
        from("direct:invokeApi")
            .routeId("generic-api-route")
            .process(httpApiProcessor)
            .choice()
                .when(exchangeProperty("retryRequest").isEqualTo(true))
                    .log("Retrying API request after token refresh")
                    .process(httpApiProcessor)
            .end()
            .toD("${header.CamelHttpUri}?bridgeEndpoint=true&throwExceptionOnFailure=true")
            .log("API Response Status: ${header.CamelHttpResponseCode}")
            .convertBodyTo(String.class);
        
        // Example GET route
        from("direct:getApiData")
            .routeId("get-api-data")
            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            .to("direct:invokeApi");
        
        // Example POST route
        from("direct:postApiData")
            .routeId("post-api-data")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .marshal().json(JsonLibrary.Jackson)
            .to("direct:invokeApi");
        
        // Example PUT route
        from("direct:putApiData")
            .routeId("put-api-data")
            .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
            .marshal().json(JsonLibrary.Jackson)
            .to("direct:invokeApi");
        
        // Example DELETE route
        from("direct:deleteApiData")
            .routeId("delete-api-data")
            .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
            .to("direct:invokeApi");
        
        // Example route with custom processing
        from("direct:customApiCall")
            .routeId("custom-api-call")
            .log("Processing custom API call to: ${header.targetUrl}")
            .process(exchange -> {
                // Set the target URL
                String targetUrl = exchange.getIn().getHeader("targetUrl", String.class);
                exchange.getIn().setHeader(Exchange.HTTP_URI, targetUrl);
                
                // Set HTTP method from header or default to GET
                String method = exchange.getIn().getHeader("httpMethod", String.class);
                if (method == null) {
                    method = "GET";
                }
                exchange.getIn().setHeader(Exchange.HTTP_METHOD, method);
            })
            .to("direct:invokeApi")
            .unmarshal().json(JsonLibrary.Jackson, Object.class);
    }
}
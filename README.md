# camel-poc
camel - poc using spring boot camel consul service discovery rabbitmq restlet:-

Brief Description:-
The poc is about trying out basic camel functionalities for better understanding. There is an UI project, a simple spring boot/ angularjs web app which works as a client. It communicates with the Gateway project build on Camel.

The gateway project consult the Consul server for service discovery and forwards the request to appropritae Accout or Customer project using the camel's serviceCall api.The Gateway also receives a post addCustomer request consuming JSON from which it creates an xml file in a designated folder for Customer service to consume.
Along with providing the basic search rest based api, The customer service consumes the xml created by gateway.After successful operation it forwards a rabbitmq message to a designated queue for the accounts to be created.
The account service acts a consumer to the rabbitmq messages which customer service provides.Along with this account service also provides rest api for regular search operation. 

Brief code samples:-
GatewayRoter.java
-----------------------------
```JAVA
//For recieving Customer json and creating xml file based on that-

  rest("/gateway").post("/customer/add").consumes("application/json").produces("application/json")
				.type(Customer.class).to("direct:createXML");
		from("direct:createXML").marshal(xmlFormal)
				.to("file:" + inputDir + "?fileName=customer-${date:now:yyyyMMddSSS}.xml").transform()
				.simple("Customer Created");
        
 //For calling Customer service for search
 // consul configuration
   ConsulServiceCallServiceDiscoveryConfiguration config = new ConsulServiceCallServiceDiscoveryConfiguration();
		config.setUrl("http://localhost:8500" //assuming the consul server is running on local m/c @ default 8500 port);
		ServiceCallConfigurationDefinition def = new ServiceCallConfigurationDefinition();
		def.setComponent("netty4-http");
		def.setServiceDiscoveryConfiguration(config);
		context.setServiceCallConfiguration(def);
    //service call
    rest("/gateway").get("/cust/{name}").type(Customer.class).param().name("name").type(RestParamType.path)
				.dataType("String").endParam().route().serviceCall("customer/customerService/{name}")
				.unmarshal(formatCusList).endRest();
   ```
        
   CustomerRouter.java
   ---------------------------------
   ```JAVA
   // for consuming xml created by gateway
   from("file:" + inputDir + "?noop=true&include=.*\\.xml").to("file:" + outputDir).to("direct:parseXML");
		from("direct:parseXML").unmarshal(xmlFormal)
				.to("bean:customerService?method=addCustomer").to("direct:sendXML");
        // for sending rabbitmq message for account service to consume
		from("direct:sendXML").marshal().json(JsonLibrary.Jackson,
		 Customer.class).to("rabbitmq:A?routingKey=B");
    
  ```
    


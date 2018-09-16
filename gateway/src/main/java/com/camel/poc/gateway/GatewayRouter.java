package com.camel.poc.gateway;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.consul.ConsulClientConfiguration;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.cloud.ConsulServiceCallServiceDiscoveryConfiguration;
import org.apache.camel.model.cloud.ServiceCallConfigurationDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;

@Component
public class GatewayRouter extends RouteBuilder {

	@Value("${input.dir}")
	private String inputDir;

	@Autowired
	CamelContext context;

	@Override
	public void configure() throws Exception {

		JaxbDataFormat xmlFormal = new JaxbDataFormat();
		xmlFormal.setContext(JAXBContext.newInstance(Customer.class));

		JacksonDataFormat jsonFormat = new JacksonDataFormat();
		jsonFormat.setPrettyPrint(true);

		JacksonDataFormat formatCusList = new JacksonDataFormat(Customer.class);
		formatCusList.useList();
		JacksonDataFormat formatAccList = new JacksonDataFormat(Account.class);
		formatAccList.useList();

		ConsulServiceCallServiceDiscoveryConfiguration config = new ConsulServiceCallServiceDiscoveryConfiguration();
		config.setUrl("http://localhost:8500");
		ServiceCallConfigurationDefinition def = new ServiceCallConfigurationDefinition();
		def.setComponent("netty4-http");
		def.setServiceDiscoveryConfiguration(config);
		context.setServiceCallConfiguration(def);

		restConfiguration().component("netty4-http").bindingMode(RestBindingMode.json).port(8081).enableCORS(true)
		/*
		 * .corsHeaderProperty("Access-Control-Allow-Headers",
		 * "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers,CustomHeader1, CustomHeader2"
		 * )
		 */;
		rest("/gateway").post("/customer/add").consumes("application/json").produces("application/json")
				.type(Customer.class).to("bean:gatewayService?method=addCustomer").to("direct:createXML");

		from("direct:createXML").marshal(xmlFormal)
				.to("file:" + inputDir + "?fileName=customer-${date:now:yyyyMMddSSS}.xml").transform()
				.simple("Customer Created");

		rest("/gateway").get("/cust/{name}").type(Customer.class).param().name("name").type(RestParamType.path)
				.dataType("String").endParam().route().serviceCall("customer/customerService/{name}")
				.unmarshal(formatCusList).endRest().get("/cust").type(Customer.class).route()
				.serviceCall("customer/customerService/").unmarshal(formatCusList).endRest();

		rest("/gateway").get("/acc/{name}").type(Account.class).param().name("name").type(RestParamType.path)
				.dataType("String").endParam().route().serviceCall("account/accountService/{name}")
				.unmarshal(formatAccList).endRest().get("/acc").type(Account.class).route()
				.serviceCall("account/accountService/").unmarshal(formatAccList).endRest().post("/acc/transact")
				.consumes("application/json").produces("application/json").type(Account.class).route()
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json")).serviceCall("account/accountService/transact")
				.endRest();

		/*
		 * from("direct:enrichAcc").enrich("direct:callAcc", new
		 * AccountAggregator()).to("direct:result");
		 * 
		 * from("direct:callAcc").autoStartup(false).process(exchange -> {
		 * List<Customer> lstCustomer = (List<Customer>) exchange.getIn().getBody();
		 * List<Account> lstAcc = new ArrayList<>(); lstCustomer.forEach(cust ->
		 * lstAcc.addAll(cust.getAccounts())); exchange.getIn().setBody(lstAcc);
		 * }).setHeader(Exchange.HTTP_METHOD, constant("POST"))
		 * .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
		 * .serviceCall("account/accountService/getByIds").unmarshal(formatAccList);
		 * 
		 * rest("/gateway").get("/acc/{name}").type(Account.class).param().name("name").
		 * type(RestParamType.path) .dataType("String").endParam().route().serviceCall(
		 * "account/accountService/{name}")
		 * .unmarshal(formatCusList).endRest().get("/acc").type(Account.class).route()
		 * .serviceCall("account/accountService/").unmarshal(formatAccList).endRest().
		 * post("/acc/transaction/")
		 * .consumes("application/json").produces("application/json").type(Account.
		 * class)
		 * .to("bean:gatewayService?method=accTransaction").to("direct:accTransaction");
		 * from("direct:accTransaction").log("yet to implement");
		 */

	}

}

package com.camel.poc.customerService;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.cloud.ConsulServiceCallServiceDiscoveryConfiguration;
import org.apache.camel.model.cloud.ServiceCallConfigurationDefinition;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class CustomerRoute extends RouteBuilder {

	@Value("${input.dir}")
	private String inputDir;

	@Value("${output.dir}")
	private String outputDir;

	@Value("${port}")
	private int port;

	@Value("${consul.url}")
	private String consulHost;

	@Autowired
	CamelContext context;

	@Bean
	public ConnectionFactory getRabbitCoonectionFactory() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setPort(5672);
		return factory;
	}

	@Override
	public void configure() throws Exception {
		/*
		 * context.disableJMX(); ActiveMQComponent component =
		 * ActiveMQComponent.activeMQComponent(
		 * "vm://localhost?broker.persistent=false&broker.useJmx=false");
		 * component.setUsername("admin"); component.setPassword("admin");
		 * context.addComponent("activemq", component);
		 */

		JaxbDataFormat xmlFormal = new JaxbDataFormat();
		xmlFormal.setContext(JAXBContext.newInstance(Customer.class));

		JaxbDataFormat accXmlFormat = new JaxbDataFormat();
		accXmlFormat.setContext(JAXBContext.newInstance(Account.class));

		JacksonDataFormat formatAccList = new JacksonDataFormat(Account.class);
		formatAccList.useList();

		from("file:" + inputDir + "?noop=true&include=.*\\.xml").to("file:" + outputDir).to("direct:parseXML");
		from("direct:parseXML").unmarshal(xmlFormal)
				.to("bean:customerService?method=addCustomer").to("direct:sendXML");
		from("direct:sendXML").marshal().json(JsonLibrary.Jackson,
		 Customer.class).to("rabbitmq:A?routingKey=B");

		from("direct:start").startupOrder(1).routeId("account-consul").marshal().json(JsonLibrary.Jackson)
				.setHeader(Exchange.HTTP_METHOD, constant("PUT"))
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
				.to(consulHost + "/v1/agent/service/register");

		from("direct:stop").startupOrder(2).shutdownRunningTask(ShutdownRunningTask.CompleteAllTasks)
				.toD(consulHost + "/v1/agent/service/deregister/${header.id}");

		ConsulServiceCallServiceDiscoveryConfiguration config = new ConsulServiceCallServiceDiscoveryConfiguration();
		config.setUrl("http://localhost:8500");
		ServiceCallConfigurationDefinition def = new ServiceCallConfigurationDefinition();
		def.setComponent("netty4-http");
		def.setServiceDiscoveryConfiguration(config);
		context.setServiceCallConfiguration(def);

		restConfiguration().component("netty4-http").bindingMode(RestBindingMode.json).port(8081).enableCORS(true);

		rest("/customerService").get("/").to("bean:customerService?method=findAll").to("direct:enrichAcc")
				.get("/{name}").to("bean:customerService?method=findByName(${header.name})")/*.to("direct:enrichAcc")*/;
		
		/*from("direct:enrichAcc").enrich("direct:callAcc", new AccountAggregator()).to("direct:result");

		
		from("direct:callAcc").autoStartup(false).process(exchange -> {
			List<Customer> lstCustomer = (List<Customer>) exchange.getIn().getBody();
			List<Account> lstAcc = new ArrayList<>();
			lstCustomer.forEach(cust -> lstAcc.addAll(cust.getAccounts()));
			exchange.getIn().setBody(lstAcc);
		}).setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json")).serviceCall("account/accountService/getByIds")
				.unmarshal(formatAccList);
		
		from("direct:result").to("bean:customerService?method=returnAll");*/

	}

}

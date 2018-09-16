package com.camel.poc.accountService;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.ShutdownRunningTask;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.camel.poc.bean.Account;
import com.camel.poc.bean.Customer;
import com.rabbitmq.client.ConnectionFactory;

@Component
public class AccountRouter extends RouteBuilder {

	@Value("${port}")
	public int port;

	@Value("${consul.url}")
	private String consulHost;

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
		 * CamelContext context = new DefaultCamelContext(); ActiveMQComponent component
		 * = ActiveMQComponent.activeMQComponent("tcp://localhost:8161");
		 * component.setUsername("admin"); component.setPassword("admin");
		 * context.addComponent("activemq", component);
		 * 
		 * final String destQueue =
		 * "activemq:topic:Consumer.accountConsumer.VirtualTopic.accountTopic";
		 */
		JaxbDataFormat xmlFormal = new JaxbDataFormat();
		xmlFormal.setContext(JAXBContext.newInstance(Customer.class));

		from("direct:start").routeId("account-consul").marshal().json(JsonLibrary.Jackson)
				.setHeader(Exchange.HTTP_METHOD, constant("PUT"))
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
				.to(consulHost + "/v1/agent/service/register");

		from("direct:stop").shutdownRunningTask(ShutdownRunningTask.CompleteAllTasks)
				.toD(consulHost + "/v1/agent/service/deregister/${header.id}");

		JacksonDataFormat jsonFormat = new JacksonDataFormat(Customer.class);
		jsonFormat.setPrettyPrint(true);

		from("rabbitmq:A?routingKey=B").to("bean:accountService?method=addAccount");
		/*from("file:" + "D://incoming" + "?noop=true&include=.*\\.xml").to("direct:parseXML");
		from("direct:parseXML").unmarshal(xmlFormal).to("bean:accountService?method=addAccount");*/

		restConfiguration().component("restlet").bindingMode(RestBindingMode.json).port(port);
		rest("/accountService").post("/getByIds").consumes("application/json").produces("application/json")
				.type(Account.class).to("bean:accountService?method=findById").post("/transact").consumes("application/json").produces("application/json")
				.type(Account.class).to("bean:accountService?method=transaction").get("/{number}")
				.to("bean:accountService?method=findByNumber(${header.number})").get("/")
				.to("bean:accountService?method=findAll");
	}

}

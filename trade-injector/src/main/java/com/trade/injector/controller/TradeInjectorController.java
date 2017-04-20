package com.trade.injector.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.trade.injector.application.Application;
import com.trade.injector.business.service.GenerateRandomInstruments;
import com.trade.injector.business.service.GenerateRandomParty;
import com.trade.injector.business.service.GenerateTradeData;
import com.trade.injector.dto.Instrument;
import com.trade.injector.dto.Party;
import com.trade.injector.dto.Trade;
import com.trade.injector.jto.TradeAcknowledge;
import com.trade.injector.jto.TradeInjectRunModes;
import com.trade.injector.jto.TradeInjectorMessage;
import com.trade.injector.jto.repository.MongoDBTemplate;
import com.trade.injector.jto.repository.TradeInjectorMessageRepository;

@SpringBootApplication(scanBasePackages = "com.trade.injector")
@EnableOAuth2Client
@RestController
@EnableMongoRepositories(basePackages = "com.trade.injector.jto.repository")
public class TradeInjectorController extends WebSecurityConfigurerAdapter {

	final Logger LOG = LoggerFactory.getLogger(TradeInjectorController.class);

	@Autowired
	OAuth2ClientContext oauth2ClientContext;

	@Autowired
	private SimpMessagingTemplate messageSender;

	@Autowired
	private MongoDBTemplate template;

	@Autowired
	private MongoTemplate coreTemplate;

	@Autowired
	GenerateTradeData tradeData;

	@Autowired(required = true)
	private TradeInjectorMessageRepository repo;

	private static boolean isKill = false;

	@RequestMapping("/user")
	public Principal user(Principal principal) {
		return principal;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**")
				.authorizeRequests()
				.antMatchers("/", "/login/**", "/webjars/**", "/dist/**",
						"/scripts/**", "/jumbotron.css", "/injectorUI/**")
				.permitAll()
				.anyRequest()
				.authenticated()
				.and()
				.logout()
				.logoutSuccessUrl("/")
				.permitAll()
				.and()
				.csrf()
				.csrfTokenRepository(
						CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);

	}

	private Filter ssoFilter() {

		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<Filter>();

		OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/facebook");
		OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(
				facebook(), oauth2ClientContext);
		facebookFilter.setRestTemplate(facebookTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(
				facebookResource().getUserInfoUri(), facebook().getClientId());
		tokenServices.setRestTemplate(facebookTemplate);
		facebookFilter.setTokenServices(tokenServices);

		filters.add(facebookFilter);

		OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/github");
		OAuth2RestTemplate githubTemplate = new OAuth2RestTemplate(github(),
				oauth2ClientContext);
		githubFilter.setRestTemplate(githubTemplate);
		tokenServices = new UserInfoTokenServices(githubResource()
				.getUserInfoUri(), github().getClientId());
		tokenServices.setRestTemplate(githubTemplate);
		githubFilter.setTokenServices(tokenServices);
		filters.add(githubFilter);

		filter.setFilters(filters);
		return filter;

	}

	@Bean
	@ConfigurationProperties("facebook.client")
	public AuthorizationCodeResourceDetails facebook() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("facebook.resource")
	public ResourceServerProperties facebookResource() {
		return new ResourceServerProperties();
	}

	@Bean
	public FilterRegistrationBean oauth2ClientFilterRegistration(
			OAuth2ClientContextFilter filter) {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@ConfigurationProperties("github.client")
	public AuthorizationCodeResourceDetails github() {
		return new AuthorizationCodeResourceDetails();
	}

	@Bean
	@ConfigurationProperties("github.resource")
	public ResourceServerProperties githubResource() {
		return new ResourceServerProperties();
	}

	@RequestMapping(value = "/tradeMessageStop", method = RequestMethod.POST)
	public void tradeStop(@RequestBody String messageId) throws Exception {

		LOG.info("Stop run for the following Id " + messageId);
		
		//we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());

		TradeInjectorMessage tradeInjectMessagetoStop = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorMessage.class);

		if (tradeInjectMessagetoStop != null) {
			tradeInjectMessagetoStop.setRun_mode(TradeInjectRunModes.STOP
					.getRunMode());
			repo.save(tradeInjectMessagetoStop);
			
			refreshTradeInjectQueue();

		} else
			LOG.error("Unable to find message for the following id "
					+ messageId);

	}

	@RequestMapping(value = "/purgeAllInjects", method = RequestMethod.POST)
	public void purgeAllInjects() {
		repo.deleteAll();
		LOG.info("successfully deleted all trade inject messages records");
	}

	@RequestMapping(value = "/retrieveAllInjects", method = RequestMethod.GET)
	public List<TradeInjectorMessage> retrieveAllInjects() {

		return repo.findAll();

	}

	@MessageMapping("/tradeMessageInject")
	// @RequestMapping(method = RequestMethod.POST)
	public void tradeInject(
			@RequestBody TradeInjectorMessage message) throws Exception {

		isKill = false;
		int numberOfTrades = new Integer(message.getNoOfTrades());
		int numberOfClients = new Integer(message.getNoOfClients());
		int numberOfInstruments = new Integer(message.getNoOfInstruments());
		int timedelay = 0;
		message.setRun_mode(TradeInjectRunModes.RUNNING.getRunMode());

		LOG.info("Injecting trades with the following user "
				+ message.getUserId());

		// save the trade inject message
		TradeInjectorMessage savedMessage = repo.save(message);

		if (message.getTimeDelay() != null)
			timedelay = new Integer(message.getTimeDelay());
		else {
			LOG.debug("Time Delay is not set, defaulting to 1000ms");
			timedelay = 1000;
		}

		if (numberOfTrades < 1 || numberOfClients < 1
				|| numberOfInstruments < 1)
			throw new Exception(
					"Minimum 1 must be specified for Trades, Clients and Instruments");
		List<Instrument> listOfInstruments = new GenerateRandomInstruments()
				.createRandomData(numberOfInstruments);
		List<Party> listOfParties = new GenerateRandomParty()
				.createRandomData(numberOfClients);

		for (int i = 0; i < numberOfTrades; i++) {

			Trade aTrade = tradeData.createTradeData(i, listOfParties,
					listOfInstruments);
			TradeAcknowledge ack = convertToAck(aTrade);
			messageSender.convertAndSend("/topic/tradeAck", ack);
			LOG.debug(("Following trade was generated " + aTrade.toString()));
			LOG.debug("Sleeping for " + timedelay + " ms");

			// update the counter push the updated message back to the client
			TradeInjectorMessage retrieveForUpdate = repo
					.findOne(savedMessage.id);
			retrieveForUpdate.setCurrenMessageCount(new Integer(i + 1)
					.toString());
			repo.save(retrieveForUpdate);

			refreshTradeInjectQueue();

			Thread.sleep(timedelay);

			// if the kill flag is set by the UI return the process.
			if (repo.findOne(savedMessage.id).getRun_mode() == TradeInjectRunModes.STOP
					.getRunMode())

				// kill it and return
				//return ResponseEntity.ok().build();
				break;

		}

		// finally set to complete
		TradeInjectorMessage retrieveForUpdate = repo.findOne(savedMessage.id);
		retrieveForUpdate.setRun_mode(TradeInjectRunModes.COMPLETED
				.getRunMode());
		repo.save(retrieveForUpdate);

		refreshTradeInjectQueue();

		//return ResponseEntity.ok().build();
	}

	private void refreshTradeInjectQueue() throws Exception {

		List<TradeInjectorMessage> listofMessages = repo.findAll();
		// now push it to the queue so that everyone can see the update
		messageSender.convertAndSend("/topic/tradeMessageInject",
				listofMessages);
	}

	private TradeAcknowledge convertToAck(Trade aTrade) {
		TradeAcknowledge ack = new TradeAcknowledge();
		if (aTrade != null) {
			ack.setClientName(aTrade.getClientName());
			ack.setInstrumentId(aTrade.getInstrumentId());
			ack.setSide(aTrade.getSide());
			ack.setTradeDate(aTrade.getTradeDate().toString());
			ack.setTradePx(new Double(aTrade.getTradePx()).toString());
			ack.setTradeQty(new Integer(aTrade.getTradeQty()).toString());
		}

		return ack;
	}

	public static void main(String[] args) {
		SpringApplication.run(TradeInjectorController.class, args);
	}

}

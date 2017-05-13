package com.trade.injector.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.springframework.scheduling.annotation.EnableScheduling;
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
import com.trade.injector.dto.Trade;
import com.trade.injector.enums.TradeInjectRunModes;
import com.trade.injector.jto.Instrument;
import com.trade.injector.jto.InstrumentReport;
import com.trade.injector.jto.Party;
import com.trade.injector.jto.PartyReport;
import com.trade.injector.jto.TradeAcknowledge;
import com.trade.injector.jto.TradeInjectorMessage;
import com.trade.injector.jto.TradeInjectorProfile;
import com.trade.injector.jto.TradeReport;
import com.trade.injector.jto.repository.MongoDBTemplate;
import com.trade.injector.jto.repository.TradeInjectorMessageRepository;
import com.trade.injector.jto.repository.TradeInjectorProfileRepository;
import com.trade.injector.jto.repository.TradeReportRepository;

@SpringBootApplication(scanBasePackages = "com.trade.injector")
@EnableOAuth2Client
@RestController
@EnableMongoRepositories(basePackages = "com.trade.injector.jto.repository")
@EnableScheduling
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

	@Autowired
	private TradeReportRepository reportRepo;

	@Autowired
	private TradeInjectorProfileRepository profileRepo;

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
	@RequestMapping(value = "/tradeMessageStopForProfile", method = RequestMethod.POST)
	public void tradeStopForProfile(@RequestBody String messageId) throws Exception {

		LOG.info("Stop run for the following Id " + messageId);

		// we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());

		TradeInjectorProfile profile = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorProfile.class);

		if (profile != null) {
			profile.setRun_mode(TradeInjectRunModes.STOP
					.getRunMode());
			profileRepo.save(profile);

			// refreshTradeInjectQueue();

		} else
			LOG.error("Unable to find message for the following id "
					+ messageId);

	}
	


	@Deprecated
	@RequestMapping(value = "/tradeMessageStop", method = RequestMethod.POST)
	public void tradeStop(@RequestBody String messageId) throws Exception {

		LOG.info("Stop run for the following Id " + messageId);

		// we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());

		TradeInjectorMessage tradeInjectMessagetoStop = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorMessage.class);

		if (tradeInjectMessagetoStop != null) {
			tradeInjectMessagetoStop.setRun_mode(TradeInjectRunModes.STOP
					.getRunMode());
			repo.save(tradeInjectMessagetoStop);

			// refreshTradeInjectQueue();

		} else
			LOG.error("Unable to find message for the following id "
					+ messageId);

	}
	
	@RequestMapping(value = "/tradeMessagePlayForProfile", method = RequestMethod.POST)
	public void tradePlayForProfile(@RequestBody String messageId) throws Exception {

		// we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());
		LOG.info("Playing for the following Id " + messageId);

		TradeInjectorProfile profile = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorProfile.class);

		if (profile != null) {
			runTradeInjectForTradeProfileId(profile);
		} else
			LOG.error("Unable to find profile for the following id "
					+ messageId);

	}

	
	@Deprecated
	@RequestMapping(value = "/tradeMessagePlay", method = RequestMethod.POST)
	public void tradePlay(@RequestBody String messageId) throws Exception {

		// we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());
		LOG.info("Replaying for the following Id " + messageId);

		TradeInjectorMessage tradeInjectMessagetoReplay = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorMessage.class);

		if (tradeInjectMessagetoReplay != null) {
			runTradeInjectForTradeInjectId(tradeInjectMessagetoReplay);
		} else
			LOG.error("Unable to find message for the following id "
					+ messageId);

	}

	@Deprecated
	@RequestMapping(value = "/tradeMessageRepeat", method = RequestMethod.POST)
	public void tradeRepeat(@RequestBody String messageId) throws Exception {

		// we need to remove the id= bit from message id
		messageId = messageId.substring(messageId.indexOf('=') + 1,
				messageId.length());
		LOG.info("Repeating for the following Id " + messageId);

		TradeInjectorMessage tradeInjectMessagetoRepeat = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(messageId)),
				TradeInjectorMessage.class);

		if (tradeInjectMessagetoRepeat != null) {

			// remove the reports for Trade Data
			TradeReport tradeReport = coreTemplate.findOne(
					Query.query(Criteria.where("injectorMessageId").is(
							messageId)), TradeReport.class);
			reportRepo.delete(tradeReport);

			// reset the message count to 0
			tradeInjectMessagetoRepeat.setCurrenMessageCount("0");
			runTradeInjectForTradeInjectId(tradeInjectMessagetoRepeat);

		} else
			LOG.error("Unable to find message for the following id "
					+ messageId);

	}
	
	@RequestMapping(value = "/tradeMessageRepeatForProfile", method = RequestMethod.POST)
	public void repeatRunOnProfile(@RequestBody String profileId) throws Exception {

		// we need to remove the id= bit from message id
		profileId = profileId.substring(profileId.indexOf('=') + 1,
				profileId.length());
		LOG.info("Running for the following Id " + profileId);

		TradeInjectorProfile profile = coreTemplate.findOne(
				Query.query(Criteria.where("id").is(profileId)),
				TradeInjectorProfile.class);

		if (profile != null) {

			// remove the reports for Trade Data
			TradeReport tradeReport = coreTemplate.findOne(
					Query.query(Criteria.where("injectorProfileId").is(
							profileId)), TradeReport.class);
			if (tradeReport != null)
					reportRepo.delete(tradeReport);

			// reset the message count to 0
			profile.setCurrentMessageCount(0);
			runTradeInjectForTradeProfileId(profile);

		} else
			LOG.error("Unable to find message for the following id "
					+ profileId);

	}
	

	@RequestMapping(value = "/tradeRunStart", method = RequestMethod.POST)
	public void injectTradesOnProfile(@RequestBody TradeInjectorProfile profile)
			throws Exception {

		
		LOG.info("Running for the following profile... "+profile.id);
		runTradeInjectForTradeProfileId(profile);
		LOG.info("Done running for the following Profile "+profile.id);
		

	}

	private void runTradeInjectForTradeProfileId(TradeInjectorProfile profile)
			throws Exception {

		List<Instrument> listOfInstruments = new GenerateRandomInstruments()
				.createRandomData(new Integer(profile.getNumberOfInstruments()));
		List<Party> listOfParties = new GenerateRandomParty()
				.createRandomData(new Integer(profile.getNumberOfParties()));

		int startFrom = new Integer(profile.getCurrentMessageCount());
		int numberOfTrades = new Integer(profile.getNumberOfTrades());

		// set it to run
		profile.setRun_mode(TradeInjectRunModes.RUNNING.getRunMode());
		while (startFrom != numberOfTrades) {

			startFrom++;
			Trade aTrade = tradeData.createTradeData(startFrom, listOfParties,
					listOfInstruments);
			convertToReportAndSaveForProfile(convertToAckForProfile(aTrade, profile.id),
					profile.getUserId());
			profile.setCurrentMessageCount(startFrom);
			
			//sleep for simulated wait time
			profileRepo.save(profile);Thread.sleep(profile.getSimulatedWaitTime());

			// if the kill flag is set by the UI return the process.
			if (profileRepo.findOne(profile.id).getRun_mode() == TradeInjectRunModes.STOP
					.getRunMode())

				// kill it and return
				
				break;
			}
		
		//finally mark it as complete
		if (startFrom == numberOfTrades) {
			profile
					.setCurrentMessageCount(startFrom);
			profile.setRun_mode(TradeInjectRunModes.COMPLETED
					.getRunMode());
			profileRepo.save(profile);
		}

	}

	@Deprecated
	private void runTradeInjectForTradeInjectId(
			TradeInjectorMessage tradeInjectMessagetoRun) throws Exception {

		List<Instrument> listOfInstruments = new GenerateRandomInstruments()
				.createRandomData(new Integer(tradeInjectMessagetoRun
						.getNoOfInstruments()));
		List<Party> listOfParties = new GenerateRandomParty()
				.createRandomData(new Integer(tradeInjectMessagetoRun
						.getNoOfClients()));

		int startFrom = new Integer(
				tradeInjectMessagetoRun.getCurrenMessageCount());
		int numberOfTrades = new Integer(
				tradeInjectMessagetoRun.getNoOfTrades());

		// set it to run
		tradeInjectMessagetoRun.setRun_mode(TradeInjectRunModes.RUNNING
				.getRunMode());

		while (startFrom != numberOfTrades) {

			startFrom++;
			Trade aTrade = tradeData.createTradeData(startFrom, listOfParties,
					listOfInstruments);
			convertToReportAndSave(
					convertToAck(aTrade, tradeInjectMessagetoRun.id),
					tradeInjectMessagetoRun.getUserId());
			// messageSender.convertAndSend("/topic/tradeAck", ack);
			LOG.debug(("Following trade was generated " + aTrade.toString()));

			tradeInjectMessagetoRun
					.setCurrenMessageCount(new Integer(startFrom).toString());
			repo.save(tradeInjectMessagetoRun);

			// refreshTradeInjectQueue();

			Thread.sleep(new Integer(tradeInjectMessagetoRun.getTimeDelay()));

			// if the kill flag is set by the UI return the process.
			if (repo.findOne(tradeInjectMessagetoRun.id).getRun_mode() == TradeInjectRunModes.STOP
					.getRunMode())

				// kill it and return
				// return ResponseEntity.ok().build();
				break;

		}

		// finally set to complete only if we have a genuine complete
		if (startFrom == numberOfTrades) {
			tradeInjectMessagetoRun
					.setCurrenMessageCount(new Integer(startFrom).toString());
			tradeInjectMessagetoRun.setRun_mode(TradeInjectRunModes.COMPLETED
					.getRunMode());
			repo.save(tradeInjectMessagetoRun);
		}

		// refreshTradeInjectQueue();

	}
	
	private void convertToReportAndSaveForProfile(TradeAcknowledge ack, String username) throws Exception{
		
		TradeReport tradeReport = coreTemplate.findOne(
				Query.query(Criteria.where("injectorProfileId").is(
						ack.getInjectIdentifier())), TradeReport.class);

		if (tradeReport == null) {
			// create a new one
			tradeReport = new TradeReport();
			tradeReport.setCurrentTradeProgress(1);
			tradeReport.setInjectorMessageId(ack.getInjectIdentifier());
			tradeReport.setInjectorProfileId(ack.getProfileIdentifier());
			tradeReport.setName("Report_" + ack.getInjectIdentifier());
			tradeReport.setReportDate(new Date(System.currentTimeMillis()));
			tradeReport.setTradeCount(1);
			tradeReport.setUserId(username);

			List<PartyReport> parties = new ArrayList<PartyReport>();
			PartyReport newParty = new PartyReport();
			newParty.setCurrentTradeCount(1);
			newParty.setPreviousTradeCount(1);
			newParty.setId(ack.getClientName());
			newParty.setName(ack.getClientName());
			parties.add(newParty);

			tradeReport.setParties(parties);

			// now add the newly created instrument
			List<InstrumentReport> instruments = new ArrayList<InstrumentReport>();
			InstrumentReport newInstrument = new InstrumentReport();
			newInstrument.setId(ack.getInstrumentId());
			newInstrument.setName(ack.getInstrumentId());
			newInstrument.setCurrentTradeCount(1);
			instruments.add(newInstrument);

			tradeReport.setInstruments(instruments);

		} else {
			// we have found it now update the all the counters
			int progress = tradeReport.getCurrentTradeProgress();
			tradeReport.setCurrentTradeProgress(++progress);
			List<PartyReport> parties = tradeReport.getParties();
			List<InstrumentReport> instruments = tradeReport.getInstruments();

			List<PartyReport> modifiedParties = parties.stream()
					.filter(a -> a.getId().equals(ack.getClientName()))
					.map(a -> a.incrementCountByOne())
					.collect(Collectors.toList());
			List<PartyReport> nonModifiedParties = parties.stream()
					.filter(a -> !a.getId().equals(ack.getClientName()))
					.collect(Collectors.toList());

			if (modifiedParties.size() == 0) {
				// add the new Party in
				PartyReport newParty = new PartyReport();
				newParty.setCurrentTradeCount(1);
				newParty.setId(ack.getClientName());
				newParty.setName(ack.getClientName());
				modifiedParties.add(newParty);
			}

			parties = Stream.concat(modifiedParties.stream(),
					nonModifiedParties.stream()).collect(Collectors.toList());

			// now do the same for the instruments
			List<InstrumentReport> modifiedInstruments = instruments.stream()
					.filter(a -> a.getId().equals(ack.getInstrumentId()))
					.map(a -> a.incrementCountByOne())
					.collect(Collectors.toList());
			List<InstrumentReport> nonModifiedInstruments = instruments
					.stream()
					.filter(a -> !a.getId().equals(ack.getInstrumentId()))
					.collect(Collectors.toList());

			if (modifiedInstruments.size() == 0) {

				InstrumentReport newInstrument = new InstrumentReport();
				newInstrument.setId(ack.getInstrumentId());
				newInstrument.setName(ack.getInstrumentId());
				newInstrument.setCurrentTradeCount(1);
				modifiedInstruments.add(newInstrument);

			}

			// finally concat the list
			instruments = Stream.concat(modifiedInstruments.stream(),
					nonModifiedInstruments.stream()).collect(
					Collectors.toList());

			tradeReport.setParties(parties);
			tradeReport.setInstruments(instruments);

		}

		reportRepo.save(tradeReport);

		
	}

	@Deprecated
	private void convertToReportAndSave(TradeAcknowledge ack, String username)
			throws Exception {

		TradeReport tradeReport = coreTemplate.findOne(
				Query.query(Criteria.where("injectorMessageId").is(
						ack.getInjectIdentifier())), TradeReport.class);

		if (tradeReport == null) {
			// create a new one
			tradeReport = new TradeReport();
			tradeReport.setCurrentTradeProgress(1);
			tradeReport.setInjectorMessageId(ack.getInjectIdentifier());
			tradeReport.setName("Report_" + ack.getInjectIdentifier());
			tradeReport.setReportDate(new Date(System.currentTimeMillis()));
			tradeReport.setTradeCount(1);
			tradeReport.setUserId(username);

			List<PartyReport> parties = new ArrayList<PartyReport>();
			PartyReport newParty = new PartyReport();
			newParty.setCurrentTradeCount(1);
			newParty.setPreviousTradeCount(1);
			newParty.setId(ack.getClientName());
			newParty.setName(ack.getClientName());
			parties.add(newParty);

			tradeReport.setParties(parties);

			// now add the newly created instrument
			List<InstrumentReport> instruments = new ArrayList<InstrumentReport>();
			InstrumentReport newInstrument = new InstrumentReport();
			newInstrument.setId(ack.getInstrumentId());
			newInstrument.setName(ack.getInstrumentId());
			newInstrument.setCurrentTradeCount(1);
			instruments.add(newInstrument);

			tradeReport.setInstruments(instruments);

		} else {
			// we have found it now update the all the counters
			int progress = tradeReport.getCurrentTradeProgress();
			tradeReport.setCurrentTradeProgress(++progress);
			List<PartyReport> parties = tradeReport.getParties();
			List<InstrumentReport> instruments = tradeReport.getInstruments();

			List<PartyReport> modifiedParties = parties.stream()
					.filter(a -> a.getId().equals(ack.getClientName()))
					.map(a -> a.incrementCountByOne())
					.collect(Collectors.toList());
			List<PartyReport> nonModifiedParties = parties.stream()
					.filter(a -> !a.getId().equals(ack.getClientName()))
					.collect(Collectors.toList());

			if (modifiedParties.size() == 0) {
				// add the new Party in
				PartyReport newParty = new PartyReport();
				newParty.setCurrentTradeCount(1);
				newParty.setId(ack.getClientName());
				newParty.setName(ack.getClientName());
				modifiedParties.add(newParty);
			}

			parties = Stream.concat(modifiedParties.stream(),
					nonModifiedParties.stream()).collect(Collectors.toList());

			// now do the same for the instruments
			List<InstrumentReport> modifiedInstruments = instruments.stream()
					.filter(a -> a.getId().equals(ack.getInstrumentId()))
					.map(a -> a.incrementCountByOne())
					.collect(Collectors.toList());
			List<InstrumentReport> nonModifiedInstruments = instruments
					.stream()
					.filter(a -> !a.getId().equals(ack.getInstrumentId()))
					.collect(Collectors.toList());

			if (modifiedInstruments.size() == 0) {

				InstrumentReport newInstrument = new InstrumentReport();
				newInstrument.setId(ack.getInstrumentId());
				newInstrument.setName(ack.getInstrumentId());
				newInstrument.setCurrentTradeCount(1);
				modifiedInstruments.add(newInstrument);

			}

			// finally concat the list
			instruments = Stream.concat(modifiedInstruments.stream(),
					nonModifiedInstruments.stream()).collect(
					Collectors.toList());

			tradeReport.setParties(parties);
			tradeReport.setInstruments(instruments);

		}

		reportRepo.save(tradeReport);
	}

	@Deprecated
	@RequestMapping(value = "/purgeAllInjects", method = RequestMethod.POST)
	public void purgeAllInjects() {
		repo.deleteAll();
		reportRepo.deleteAll();
		profileRepo.deleteAll();
		LOG.info("successfully deleted all trade inject messages records");
	}

	@Deprecated
	@RequestMapping(value = "/retrieveAllInjects", method = RequestMethod.GET)
	public List<TradeInjectorMessage> retrieveAllInjects() {

		return repo.findAll();

	}

	@RequestMapping(value = "/saveTradeInjectProfile", method = RequestMethod.POST)
	public ResponseEntity<TradeInjectorProfile> saveTradeInjectProfile(
			@RequestBody TradeInjectorProfile profile) throws Exception {

		//set it to stop so that it can show up as play on the profile
		profile.setRun_mode(TradeInjectRunModes.STOP.getRunMode());
		
		profileRepo.save(profile);

		return ResponseEntity.ok(profile);

	}

	@RequestMapping(value = "/getAllInjectProfiles", method = RequestMethod.GET)
	public ResponseEntity<List<TradeInjectorProfile>> saveTradeInjectProfile()
			throws Exception {

		return ResponseEntity.ok(profileRepo.findAll());

	}

	@Deprecated
	@MessageMapping("/tradeMessageInject")
	// @RequestMapping(method = RequestMethod.POST)
	public void tradeInject(@RequestBody TradeInjectorMessage message)
			throws Exception {

		int numberOfTrades = new Integer(message.getNoOfTrades());
		int numberOfClients = new Integer(message.getNoOfClients());
		int numberOfInstruments = new Integer(message.getNoOfInstruments());
		int timedelay = 0;
		message.setRun_mode(TradeInjectRunModes.RUNNING.getRunMode());
		String pattern = "MM/dd/yyyy";
		SimpleDateFormat format = new SimpleDateFormat(pattern);

		message.setInjectDate(format.format(new Date(System.currentTimeMillis())));
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

		int iterations = 0;

		for (int i = 0; i < numberOfTrades; i++) {

			Trade aTrade = tradeData.createTradeData(i, listOfParties,
					listOfInstruments);
			convertToReportAndSave(convertToAck(aTrade, message.id),
					message.getUserId());
			// messageSender.convertAndSend("/topic/tradeAck", ack);
			LOG.debug(("Following trade was generated " + aTrade.toString()));
			LOG.debug("Sleeping for " + timedelay + " ms");

			// update the counter push the updated message back to the client
			TradeInjectorMessage retrieveForUpdate = repo
					.findOne(savedMessage.id);
			retrieveForUpdate.setCurrenMessageCount(new Integer(i + 1)
					.toString());
			repo.save(retrieveForUpdate);

			// refreshTradeInjectQueue();

			Thread.sleep(timedelay);

			// if the kill flag is set by the UI return the process.
			if (repo.findOne(savedMessage.id).getRun_mode() == TradeInjectRunModes.STOP
					.getRunMode())

				// kill it and return
				// return ResponseEntity.ok().build();
				break;

			iterations++;

		}

		// finally set to complete only if we have a genuine complete
		if (iterations == numberOfTrades) {
			TradeInjectorMessage retrieveForUpdate = repo
					.findOne(savedMessage.id);
			retrieveForUpdate.setRun_mode(TradeInjectRunModes.COMPLETED
					.getRunMode());
			repo.save(retrieveForUpdate);
		}

		// refreshTradeInjectQueue();

		// return ResponseEntity.ok().build();
	}

	private TradeAcknowledge convertToAckForProfile(Trade aTrade, String id) {
		TradeAcknowledge ack = new TradeAcknowledge();
		if (aTrade != null) {
			ack.setProfileIdentifier(id);
			ack.setClientName(aTrade.getClientName());
			ack.setInstrumentId(aTrade.getInstrumentId());
			ack.setSide(aTrade.getSide());
			ack.setTradeDate(aTrade.getTradeDate().toString());
			ack.setTradePx(new Double(aTrade.getTradePx()).toString());
			ack.setTradeQty(new Integer(aTrade.getTradeQty()).toString());
		}

		return ack;
	}

	
	@Deprecated
	private TradeAcknowledge convertToAck(Trade aTrade, String id) {
		TradeAcknowledge ack = new TradeAcknowledge();
		if (aTrade != null) {
			ack.setInjectIdentifier(id);
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

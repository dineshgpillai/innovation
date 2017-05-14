package com.trade.injector.schedulers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trade.injector.controller.TradeInjectorController;
import com.trade.injector.jto.TradeInjectorMessage;
import com.trade.injector.jto.TradeInjectorProfile;
import com.trade.injector.jto.TradeReport;
import com.trade.injector.jto.repository.TradeInjectorMessageRepository;
import com.trade.injector.jto.repository.TradeInjectorProfileRepository;
import com.trade.injector.jto.repository.TradeReportRepository;

@Component
public class ReportScheduler {

	final Logger LOG = LoggerFactory.getLogger(ReportScheduler.class);

	@Autowired
	private SimpMessagingTemplate messageSender;

	@Autowired(required = true)
	private TradeInjectorMessageRepository repo;

	@Autowired
	private TradeReportRepository reportRepo;
	
	@Autowired
	private TradeInjectorProfileRepository profileRepo;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm:ss");

	@Scheduled(fixedDelay=1000)
	public void publishInjectProfiles(){
		List<TradeInjectorProfile> listOfProfiles = profileRepo.findAll();
		messageSender.convertAndSend("/topic/tradeMessageInject",
				listOfProfiles);
	}
	
	@Deprecated
	//@Scheduled(fixedDelay = 1000)
	public void publishUpdates() {

		// LOG.info("The time is now {}", dateFormat.format(new Date()));

		List<TradeInjectorMessage> listofMessages = repo.findAll();
		// now push it to the queue so that everyone can see the update
		messageSender.convertAndSend("/topic/tradeMessageInject",
				listofMessages);

		
	}

	@Scheduled(fixedDelay = 1000)
	public void pushCountStatistics() {
		

		List<TradeReport> listOfReports = reportRepo.findAll();
		messageSender.convertAndSend("/topic/tradeAck", listOfReports);

	}

}

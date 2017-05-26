package com.trade.injector.sinks;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static org.junit.Assert.*;

import com.trade.injector.controller.TradeInjectorController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeInjectorController.class)
public class KafkaProducerTest {
	
	@Value("${kafka.topic.marketData}")
	private String topic;
	
	@Autowired
	private KafkaSink sender;
	@Autowired
	private MarketDataReceiver receive;
	
	@Test
	public void loadStringTest() throws InterruptedException {

		/*ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("maket_data", "testpricemessage");
		future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
			@Override
			public void onSuccess(SendResult<String, String> result) {
				System.out.println("success");
			}

			@Override
			public void onFailure(Throwable ex) {
				System.out.println("failed");
			}
		});
		System.out.println(Thread.currentThread().getId());*/
		
		
		sender.send(topic,  "Hi there");
		assertTrue(this.receive.getLatch().await(60, TimeUnit.SECONDS));
		
		Thread.sleep(2000);
		
		//receive.receive(message);

	}

}

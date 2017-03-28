package com.trade.injector.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.trade.injector.application.Application;
import com.trade.injector.jto.TradeInjectorMessage;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class TradeInjectorControllerTest {

	private MediaType contentType = new MediaType(
			MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private MockMvc mockMvc;
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays
				.asList(converters)
				.stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
				.findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null",
				this.mappingJackson2HttpMessageConverter);
	}

	@Test
	public void dummyTest() {
	}

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		/*
		 * this.bookmarkRepository.deleteAllInBatch();
		 * this.accountRepository.deleteAllInBatch();
		 * 
		 * this.account = accountRepository.save(new Account(userName,
		 * "password")); this.bookmarkList.add(bookmarkRepository.save(new
		 * Bookmark(account, "http://bookmark.com/1/" + userName,
		 * "A description"))); this.bookmarkList.add(bookmarkRepository.save(new
		 * Bookmark(account, "http://bookmark.com/2/" + userName,
		 * "A description")));
		 */
	}

	@Test
	public void testMessageInjectTrigger() throws Exception {
		TradeInjectorMessage msg = new TradeInjectorMessage();
		msg.setNoOfClients("5");
		msg.setNoOfInstruments("3");
		msg.setNoOfTrades("200");
		msg.setTimeDelay("1000");
		
		String jsonMessage = this.json(msg);
		
		mockMvc.perform(
				get("/tradeInjector/tradeMessageInject").content(jsonMessage)
						.contentType(contentType)).andExpect(
				status().isOk());

	}
	
	protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}

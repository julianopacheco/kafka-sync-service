package com.julianopacheco.kafkasyncservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.julianopacheco.kafkasyncservice.model.Joke;
import com.julianopacheco.kafkasyncservice.model.Process;

@Component
public class ReplyingKafkaConsumer {
	
	Logger logger = LoggerFactory.getLogger(ReplyingKafkaConsumer.class.getName());

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${resources.url-joke}")
	private String urlResource;

	@KafkaListener(topics = "${kafka.topic.process.request}", containerFactory = "kafkaListenerContainerFactory")
	@SendTo
	public Process listen(Process process) throws InterruptedException {
		
		logger.info("entrada no ReplyingKafkaConsumer " + process);
		logger.info("###########################");
		
		Joke joke = getJoke();
		process.getAdditionalProperties().put("joke", joke);
		
		// simula um delay na resposta
		Thread.sleep(2000);
		
		logger.info("###########################");
		logger.info("retorno no ReplyingKafkaConsumer " + process);
		return process;
	}

	private Joke getJoke() {
		return  restTemplate.getForObject(urlResource, Joke.class);
	}
}

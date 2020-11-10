package com.julianopacheco.kafkasyncservice.controller;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.julianopacheco.kafkasyncservice.model.Process;

@RestController
public class KafkaSyncProcessController {
	
	@Autowired
	ReplyingKafkaTemplate<String, Process, Process> kafkaTemplate;

	@Value("${kafka.topic.process.request}")
	String requestTopic;

	@Value("${kafka.topic.process.reply}")
	String requestReplyTopic;
	
	@ResponseBody
	@PostMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Process classifier(@RequestBody Process process) throws InterruptedException, ExecutionException, TimeoutException {
		ProducerRecord<String, Process> record = new ProducerRecord<String, Process>(requestTopic, process);
		
		record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic.getBytes()));
		
		RequestReplyFuture<String, Process, Process> sendAndReceive = kafkaTemplate.sendAndReceive(record);
		
		ConsumerRecord<String, Process> consumerRecord = sendAndReceive.get();
		
		return consumerRecord.value();
	}
}
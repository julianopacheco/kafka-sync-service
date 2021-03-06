package com.julianopacheco.kafkasyncservice.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.julianopacheco.kafkasyncservice.model.Process;

@Configuration
public class KafkaConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroup;

	@Value("${kafka.topic.process.reply}")
	private String replyTopic;

	@Value("${kafka.reply-timeout.days}")
	private Long replyTimeout;

	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return props;
	}

	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
		return props;
	}

	@Bean
	public ProducerFactory<String, Process> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, Process> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public ReplyingKafkaTemplate<String, Process, Process> replyKafkaTemplate(ProducerFactory<String, Process> pf,
			KafkaMessageListenerContainer<String, Process> container) {
		ReplyingKafkaTemplate<String, Process, Process> replyingKafkaTemplate = new ReplyingKafkaTemplate<>(pf, container);
		replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofDays(replyTimeout));
		return replyingKafkaTemplate;
	}

	@Bean
	public KafkaMessageListenerContainer<String, Process> replyContainer(ConsumerFactory<String, Process> cf) {
		return new KafkaMessageListenerContainer<>(cf, new ContainerProperties(replyTopic));
	}

	@Bean
	public ConsumerFactory<String, Process> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), 
				new StringDeserializer(),
				new JsonDeserializer<>(Process.class));
	}

	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Process>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, Process> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setReplyTemplate(kafkaTemplate());
		return factory;
	}
}
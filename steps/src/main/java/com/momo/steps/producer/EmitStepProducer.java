package com.momo.steps.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmitStepProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;

	public EmitStepProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage(String message) {
		CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send("daily-steps", message);
		send.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("Sent message=[" + message +
						"] with offset=[" + result.getRecordMetadata().offset() + "]");
			} else {
				log.warn("Unable to send message=[" +
						message + "] due to : " + ex.getMessage());
			}
		});
	}
}

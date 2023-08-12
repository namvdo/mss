package com.momo.steps.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class StepEventSenderImpl implements StepEventSender {

	private final KafkaTemplate<String, String> kafkaTemplate;
	@Override
	public void sendEvent(StepMessage message) {
		String msg = message.toString();
		CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send("daily-steps", msg);
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

	public StepEventSenderImpl(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
}

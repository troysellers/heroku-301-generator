/*
 * This code is provided with no warranty, guarantee or any suggestion that it might work at all :)
 * 
 * Use at your peril.. 
 */
package com.force.aus.kafka.gen;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.aus.kafka.services.MessageService;
import com.google.inject.Guice;
import com.google.inject.Injector;
/**
 * Generate messages and send to a kafka topic. 
 * Simple demoware... 
 * 
 */
public class SimpleGenerator {

	protected Logger logger;
	protected MessageService messageService;
	protected Producer<String,String> kafkaProducer;
	public static String KAFKA_TOPIC; 
	public static void main (String[] args) {
      KAFKA_TOPIC = System.getenv("KAFKA_PREFIX") + System.getenv("KAFKA_TOPIC");
		new SimpleGenerator();
	}	
	
	public SimpleGenerator() {
		
		logger = LoggerFactory.getLogger(SimpleGenerator.class);
		
		testForEnvironmentVariables(); // crash if not configured properly.
		
		// get the injected message service that will generate the messages we send to Kafka
		// if you want to change the message, simply configure a concrete implementation of the message service
		// in the ApplicationInjector class
		Injector injector = Guice.createInjector(new ApplicationInjector());
		messageService = injector.getInstance(MessageService.class);	

		// initialise kafka producer
		KafkaConfig config = new KafkaConfig();
		kafkaProducer = new KafkaProducer<>(config.getKafkaProps());			
		
		// start the timer and run at the configured interval
		Timer timer = new Timer();
		timer.schedule(new ProducerTask(), 0, Integer.parseInt(System.getenv("INTERVAL"))); //start immediately and post every second
		
		// trap shutdown signal and close Kafka Producer neatly.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				kafkaProducer.close();
			}
		});
		
	}	
	
	/*
	 * Simple class that uses the producer to send message to topic. 
	 * Includes a simple callback that logs the offset of the message.
	 */
	class ProducerTask extends TimerTask {

		@Override
		public void run() {
	
			String message = messageService.getMessageData();
			ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(System.getenv("KAFKA_TOPIC"), message);
			
			// send and process asynchronous
			kafkaProducer.send(producerRecord, new Callback() {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if(exception != null) 
						exception.printStackTrace();
					else
						logger.info("We sent message to Kafka Topic as offset {}", metadata.offset());
				}
			});
		}
	}
	
	/*
	 * Will throw a runtime exception if required environment is not present. 
	 */
	private void testForEnvironmentVariables() {
		
		Map<String, String> environment = System.getenv();
		
		if(!environment.containsKey("INTERVAL") || 
				!environment.containsKey("KAFKA_URL") || 
				!environment.containsKey("KAFKA_CLIENT_CERT") ||
				!environment.containsKey("KAFKA_CLIENT_CERT_KEY") || 
				!environment.containsKey("KAFKA_TOPIC") ||
				!environment.containsKey("KAFKA_TRUSTED_CERT") ||
            !environment.containsKey("KAFKA_PREFIX")) {
			
			logger.error("You haven't configured your Kafka Generator environment variables correctly. See the README for full description of required values (most are provisioned by attaching a Kafka addon)");
			throw new RuntimeException("Environment variables have not been configured correctly... ");
			
		}
	}
}


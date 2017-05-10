package com.force.aus.kafka.gen;

import com.force.aus.kafka.services.MessageService;
import com.force.aus.kafka.services.SimpleMessageService;
import com.google.inject.AbstractModule;
/**
 * Google Guice configuration 
 */
public class ApplicationInjector extends AbstractModule{

	@Override
	protected void configure() {
		
		/*
		 * Change this binding if you want to provide a different message service implementation
		 */
		bind(MessageService.class).to(SimpleMessageService.class);
	}
}

/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.edyp.epims;


import fr.edyp.epims.json.AcquisitionFileMessageJson;
import fr.edyp.epims.preferences.PreferencesKeys;
import fr.edyp.epims.preferences.ServerEpimsPreferences;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;


import javax.jms.ConnectionFactory;

import org.springframework.util.ErrorHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

@EnableJms
@ComponentScan(basePackages = "fr.edyp.epims")
@SpringBootApplication
public class ServingWebContentApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServingWebContentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ServingWebContentApplication.class, args);
    }


    // Only required due to defining myFactory in the receiver
    @Bean
    public JmsListenerContainerFactory<?> myFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        // anonymous class
        factory.setErrorHandler(
                new ErrorHandler() {
                    @Override
                    public void handleError(Throwable t) {
                        LOGGER.error("An error has occurred in the transaction");
                    }
                });


        configurer.configure(factory, connectionFactory);
        return factory;
    }

    // Serialize message content to json using TextMessage
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        Map<String, Class<?>> typeIdMappings = new HashMap();
        typeIdMappings.put(AcquisitionFileMessageJson.class.getName(), AcquisitionFileMessageJson.class);
        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }

    @Bean
    public BrokerService brokerJMS() throws Exception {

        Preferences preferences = ServerEpimsPreferences.root();

        String jmsHost = preferences.get(PreferencesKeys.JMS_HOST, null);
        if (preferences.get(PreferencesKeys.JMS_HOST, null) == null) {
            // Parameter is not in the preference file
            jmsHost = "tcp://0.0.0.0:61617?jms.redeliveryPolicy.maximumRedeliveries=1";
            preferences.put(PreferencesKeys.JMS_HOST, jmsHost);
            preferences.flush();
        }

        BrokerService broker = new BrokerService();
        broker.addConnector(jmsHost);
        return broker;
    }

}

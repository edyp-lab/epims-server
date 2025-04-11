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

package fr.edyp.epims.jms;


import fr.edyp.epims.json.AcquisitionFileMessageJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jms.core.JmsTemplate;

import org.springframework.stereotype.Component;



@Component
public class JmsProducer {

    private JmsTemplate jmsTemplate;


    private static final Logger LOGGER = LoggerFactory.getLogger(JmsProducer.class);

    public JmsProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendAcquisitionFileAvailable(final AcquisitionFileMessageJson acqFileMessage) throws Exception {

        if (acqFileMessage == null) {
            throw new Exception("La lecture du message pour le fichier associé à l'acquisition au format xml a échouée ");
        }

        // acqFileMessage is automatically converted to json thanks to a Bean (search for jacksonJmsMessageConverter)
        LOGGER.info("Start Convert and Send to "+JMSConstant.JMS_ACQUISITION_TOPIC);
        jmsTemplate.convertAndSend(JMSConstant.JMS_ACQUISITION_TOPIC, acqFileMessage);
        LOGGER.info("End Convert and Send to "+JMSConstant.JMS_ACQUISITION_TOPIC);





    }

}

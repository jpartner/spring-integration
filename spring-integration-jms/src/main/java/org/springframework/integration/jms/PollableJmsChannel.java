/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.jms;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @since 2.0
 */
public class PollableJmsChannel extends AbstractJmsChannel implements PollableChannel {

	public PollableJmsChannel(JmsTemplate jmsTemplate) {
		super(jmsTemplate);
	}


	public Message<?> receive() {
		if (!this.getInterceptors().preReceive(this)) {
 			return null;
 		}
		Object object = this.getJmsTemplate().receiveAndConvert();
		
		if (object == null) {
			return null;
		}
		Message<?> replyMessage = null;
		if (object instanceof Message<?>) {
			replyMessage = (Message<?>) object;
		}
		else {
			replyMessage = MessageBuilder.withPayload(object).build();
		}
		return this.getInterceptors().postReceive(replyMessage, this) ;
	}

	public Message<?> receive(long timeout) {
		try {
			DynamicJmsTemplateProperties.setReceiveTimeout(timeout);
			return this.receive();
		}
		finally {
			DynamicJmsTemplateProperties.clearReceiveTimeout();
		}
	}

}

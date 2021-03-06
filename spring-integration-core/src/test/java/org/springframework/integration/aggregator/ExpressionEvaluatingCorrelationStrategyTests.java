/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.integration.aggregator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.message.GenericMessage;

/**
 * @author Alex Peters
 */
public class ExpressionEvaluatingCorrelationStrategyTests {

	private ExpressionEvaluatingCorrelationStrategy strategy;


	@Test(expected = IllegalArgumentException.class)
	public void testCreateInstanceWithEmptyExpressionFails() throws Exception {
		strategy = new ExpressionEvaluatingCorrelationStrategy("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateInstanceWithNullExpressionFails() throws Exception {
		Expression nullExpression = null;
		strategy = new ExpressionEvaluatingCorrelationStrategy(nullExpression);
	}

	@Test
	public void testCorrelationKeyWithMethodInvokingExpression() throws Exception {
		ExpressionParser parser = new SpelExpressionParser(new SpelParserConfiguration(true, true));
		Expression expression = parser.parseExpression("payload.substring(0,1)");
		strategy = new ExpressionEvaluatingCorrelationStrategy(expression);
		Object correlationKey = strategy.getCorrelationKey(new GenericMessage<String>("bla"));
		assertThat(correlationKey, is(String.class));
		assertThat((String) correlationKey, is("b"));
	}
}

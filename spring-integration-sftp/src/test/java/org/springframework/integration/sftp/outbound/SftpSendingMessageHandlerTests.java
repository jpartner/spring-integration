/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.sftp.outbound;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.remote.handler.FileTransferringMessageHandler;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpTestSessionFactory;
import org.springframework.util.FileCopyUtils;

import com.jcraft.jsch.ChannelSftp;

/**
 * @author Oleg Zhurakousky
 */
public class SftpSendingMessageHandlerTests {
	
	private static com.jcraft.jsch.Session jschSession = mock(com.jcraft.jsch.Session.class);

	@Test
	public void testHandleFileMessage() throws Exception {
		File file = new File("remote-target-dir", "template.mf.test");
		if (file.exists()){
			file.delete();
		}
		SessionFactory sessionFactory = new TestSftpSessionFactory();
		FileTransferringMessageHandler handler = new FileTransferringMessageHandler(sessionFactory);
		DefaultFileNameGenerator fGenerator = new DefaultFileNameGenerator();
		fGenerator.setExpression("payload + '.test'");
		handler.setFileNameGenerator(fGenerator);
		handler.setRemoteDirectoryExpression(new LiteralExpression("remote-target-dir"));

		handler.handleMessage(new GenericMessage<File>(new File("template.mf")));
		assertTrue(new File("remote-target-dir", "template.mf.test").exists());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testHandleStringMessage() throws Exception {
		File file = new File("remote-target-dir", "foo.txt");
		if (file.exists()){
			file.delete();
		}
		SessionFactory sessionFactory = new TestSftpSessionFactory();
		FileTransferringMessageHandler handler = new FileTransferringMessageHandler(sessionFactory);
		DefaultFileNameGenerator fGenerator = new DefaultFileNameGenerator();
		fGenerator.setExpression("'foo.txt'");
		handler.setFileNameGenerator(fGenerator);
		handler.setRemoteDirectoryExpression(new LiteralExpression("remote-target-dir"));

		handler.handleMessage(new GenericMessage("hello"));
		assertTrue(new File("remote-target-dir", "foo.txt").exists());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testHandleBytesMessage() throws Exception {
		File file = new File("remote-target-dir", "foo.txt");
		if (file.exists()){
			file.delete();
		}
		SessionFactory sessionFactory = new TestSftpSessionFactory();
		FileTransferringMessageHandler handler = new FileTransferringMessageHandler(sessionFactory);
		DefaultFileNameGenerator fGenerator = new DefaultFileNameGenerator();
		fGenerator.setExpression("'foo.txt'");
		handler.setFileNameGenerator(fGenerator);
		handler.setRemoteDirectoryExpression(new LiteralExpression("remote-target-dir"));

		handler.handleMessage(new GenericMessage("hello".getBytes()));
		assertTrue(new File("remote-target-dir", "foo.txt").exists());
	}
	
	public static class TestSftpSessionFactory extends DefaultSftpSessionFactory {
		
		@SuppressWarnings("rawtypes")
		public Session getSession() {
			try {
				ChannelSftp channel = mock(ChannelSftp.class);

				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation)
							throws Throwable {	
						File file = new File((String)invocation.getArguments()[1]);
						assertTrue(file.getName().endsWith(".writing"));
						FileCopyUtils.copy((InputStream)invocation.getArguments()[0], new FileOutputStream(file));
						return null;
					}
					
				}).when(channel).put(Mockito.any(InputStream.class), Mockito.anyString());
				
				doAnswer(new Answer() {
					public Object answer(InvocationOnMock invocation)
							throws Throwable {
						File file = new File((String) invocation.getArguments()[0]);
						assertTrue(file.getName().endsWith(".writing"));
						File renameToFile = new File((String) invocation.getArguments()[1]);
						file.renameTo(renameToFile);
						return null;
					}
					
				}).when(channel).rename(Mockito.anyString(), Mockito.anyString());
				when(jschSession.openChannel("sftp")).thenReturn(channel);
				return SftpTestSessionFactory.createSftpSession(jschSession);
			} catch (Exception e) {
				throw new RuntimeException("Failed to create mock sftp session", e);
			}
		}
	}

}

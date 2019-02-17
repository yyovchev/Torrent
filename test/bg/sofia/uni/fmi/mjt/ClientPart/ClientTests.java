package bg.sofia.uni.fmi.mjt.ClientPart;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTests {

	ByteArrayOutputStream out;
	Socket socketClient;
	ServerSocket serverSocket;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		socketClient = mock(Socket.class);
		out = new ByteArrayOutputStream();

		when(socketClient.getOutputStream()).thenReturn(out).thenReturn(out);

		serverSocket = mock(ServerSocket.class);
		when(serverSocket.accept()).thenReturn(socketClient).thenThrow(new IOException());

	}

	@Test
	public void downloadFile() throws IOException {
		StringBuilder message = new StringBuilder();
		message.append("test.txt").append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		ByteArrayOutputStream content = new ByteArrayOutputStream();

		when(socketClient.getOutputStream()).thenReturn(out).thenReturn(content);

		new ServerExecutor(serverSocket, Set.of("test.txt")).run();

		assertEquals("test\n", content.toString());
	}

}

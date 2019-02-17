package bg.sofia.uni.fmi.mjt.ServerPart;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ServerTests {

	public String ip;
	public int port;
	public Client client;

	public Hashtable<String, Client> users;

	String[] userNames;

	ByteArrayOutputStream out;
	Socket socketClient;

	@Before
	public void setUp() throws IOException {

		ip = "00.00.00.00";
		port = 1; 

		client = new Client(ip, port);

		userNames = new String[] { "username1", "username2", "username3" };

		users = new Hashtable<>();

		for (String userName : userNames) {
			users.put(userName, new Client(ip, port));
		}

		users.get(userNames[userNames.length - 1]).disconnect();

		socketClient = mock(Socket.class);
		out = new ByteArrayOutputStream();

		when(socketClient.getOutputStream()).thenReturn(out);
		when(socketClient.getInetAddress()).thenReturn(InetAddress.getByName(ip));

	}

	@Test
	public void clientReadValues() {
		assertTrue(ip.equals(client.getIp()));
		assertEquals(port, client.getPort());

		StringBuilder ipAndPort = new StringBuilder();

		ipAndPort.append(ip).append(":").append(port);
		String stringIpAndPort = ipAndPort.toString();
		assertTrue(stringIpAndPort.equals(client.getIPPort()));
	}

	@Test
	public void clientRegisterAndUnregisterFile() {

		Set<String> fileToRegister = Set.of("file1", "file2", "file3");

		client.addFiles(fileToRegister);

		assertEquals(fileToRegister, client.getFiles());

		Set<String> fileToUnegister = Set.of("file1");
		client.removeFiles(fileToUnegister);

		Set<String> finalRegisteredFile = Set.of("file2", "file3");

		assertEquals(finalRegisteredFile, client.getFiles());
	}

	@Test
	public void clientIsActiveAfterInit() {

		assertTrue(client.isActive().get());

		client.disconnect();

		assertFalse(client.isActive().get());

		client.init("", 0);

		assertTrue(client.isActive().get());

	}

	@Test
	public void serverGetActiveUserInfo() throws IOException {

		new UserInfoRequestHandler(socketClient, users).run();

		StringBuilder userNameIpPort = new StringBuilder();
		for (String userName : userNames) {

			if (users.get(userName).isActive().get()) {
				userNameIpPort.append(userName).append(" - ").append(client.getIPPort()).append(System.lineSeparator());
			}
		}

		assertArrayEquals(userNameIpPort.toString().lines().sorted().toArray(),
				out.toString().lines().sorted().toArray());

	}

	@Test
	public void serverRegisterNewUser() throws IOException {

		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append("testUserName").append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append(0).append(System.lineSeparator()).append("Welcome").append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());
	}

	@Test
	public void serverRegisterUserWithUsedUsername() throws IOException {
		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append(userNames[0]).append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append("User with username ").append(userNames[0]).append(" is active")
				.append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());
	}

	@Test
	public void serverRegisterUnactiveUsername() throws IOException {
		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append(userNames[userNames.length - 1])
				.append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append(0).append(System.lineSeparator()).append("Welcome").append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());
	}

	@Test
	public void serverUnknownCommandUsername() throws IOException {
		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append(userNames[userNames.length - 1])
				.append(System.lineSeparator()).append("testCommand").append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append(0).append(System.lineSeparator()).append("Welcome").append(System.lineSeparator())
				.append("unknow command").append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());
	}

	@Test
	public void serverRegisterUnregisterFile() throws IOException {

		String testUsername = "testUserName";
		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append(testUsername).append(System.lineSeparator())
				.append("register fileA fileB fileC").append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);

		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append(0).append(System.lineSeparator()).append("Welcome").append(System.lineSeparator())
				.append("Done").append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());

		Set<String> expectedFiles = Set.of("fileA", "fileB", "fileC");
		Set<String> actualFiles = users.get(testUsername).getFiles();

		assertEquals(expectedFiles, actualFiles);

		message.append("unregister fileC").append(System.lineSeparator());
		in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);
		users.get(testUsername).disconnect();
		new ClientRequestHandler(socketClient, users).run();

		expectedFiles = Set.of("fileA", "fileB");
		actualFiles = users.get(testUsername).getFiles();

		assertEquals(expectedFiles, actualFiles);

	}

	@Test
	public void serverGetFiles() throws IOException {
		String testUsername = "testUserName";
		StringBuilder message = new StringBuilder();
		message.append("9876").append(System.lineSeparator()).append(testUsername).append(System.lineSeparator())
				.append("register fileA fileB fileC").append(System.lineSeparator()).append("list-files")
				.append(System.lineSeparator());

		InputStream in = new ByteArrayInputStream(message.toString().getBytes());
		when(socketClient.getInputStream()).thenReturn(in);
		new ClientRequestHandler(socketClient, users).run();

		StringBuilder expectedReply = new StringBuilder();
		expectedReply.append(0).append(System.lineSeparator()).append("Welcome").append(System.lineSeparator())
				.append("Done").append(System.lineSeparator()).append("files from testUserName ")
				.append(users.get(testUsername).getFiles()).append(System.lineSeparator())
				.append(System.lineSeparator());

		assertEquals(expectedReply.toString(), out.toString());

	}

}

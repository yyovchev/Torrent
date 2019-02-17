package bg.sofia.uni.fmi.mjt.ServerPart;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorrentServer { 

	private static final int SERVER_PORT = 4444;
	private static final int SERVER_CLIENT_INFO_PORT = 4445;
	private static final int MAX_EXECUTOR_THREADS = 15;

	public static void main(String[] args) {

		ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

		Hashtable<String, Client> users = new Hashtable<>();

		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
				ServerSocket clientInfoServer = new ServerSocket(SERVER_CLIENT_INFO_PORT)) {
 
			System.out.println("Server started and listening for connect requests");

			Thread clientInfoServerThread = new ServerExecutor(clientInfoServer, users, executor);
			clientInfoServerThread.start();

			Socket clientSocket;

			while (true) {

				clientSocket = serverSocket.accept();

				System.out.println("(4444) Accepted connection request from client "
						+ clientSocket.getInetAddress().getHostAddress());

				ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket, users);

				executor.execute(clientHandler);
			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}

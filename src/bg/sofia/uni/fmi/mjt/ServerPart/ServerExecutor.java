package bg.sofia.uni.fmi.mjt.ServerPart;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

public class ServerExecutor extends Thread {

	private ServerSocket server;
	private Hashtable<String, Client> users;
	private ExecutorService executor;

	public ServerExecutor(ServerSocket server, Hashtable<String, Client> users, ExecutorService executor) {
		this.server = server;
		this.users = users;
		this.executor = executor;
	}
 
	@Override
	public void run() {

		Socket clientSocket;

		while (true) {

			try {
				clientSocket = server.accept();

				UserInfoRequestHandler request = new UserInfoRequestHandler(clientSocket, users);

				executor.execute(request);

			} catch (IOException e) {
				System.err.println("UserInfo - " + e.getMessage());
			}

		}

	}

}

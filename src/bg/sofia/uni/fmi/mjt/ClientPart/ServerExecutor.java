package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerExecutor extends Thread {

	ServerSocket serverSocket;
	Set<String> registeredFiles;

	private static final int MAX_EXECUTOR_THREADS = 15;

	public ServerExecutor(ServerSocket serverSocket, Set<String> registeredFiles) {
		this.serverSocket = serverSocket;
		this.registeredFiles = registeredFiles;
	}

	@Override
	public void run() {

		ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);

		Socket clientSocket;

		try {
			while (true) {

			 	clientSocket = serverSocket.accept();

				FileRequestHandler clientHandler = new FileRequestHandler(clientSocket, registeredFiles);

				executor.execute(clientHandler);

			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

}

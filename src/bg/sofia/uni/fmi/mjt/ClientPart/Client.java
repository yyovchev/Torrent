package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

	private static final int SERVER_PORT = 4444;
	private static final int MAX_DOWNLOADING_THREADS = 10;

	private ExecutorService executor;
	private ScheduledExecutorService scheduler;
	private String userInfoFile;
	private AtomicBoolean isDisconnect; 
	private Set<String> registaredFiles; 

	public Client() {
		executor = Executors.newFixedThreadPool(MAX_DOWNLOADING_THREADS);
		scheduler = Executors.newScheduledThreadPool(1);
		isDisconnect = new AtomicBoolean(false);
		registaredFiles = new HashSet<>();
	}

	public void start() {
		try (Socket s = new Socket("127.0.0.1", SERVER_PORT);
				ServerSocket serverSocket = new ServerSocket(0);
				PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

				Scanner sc = new Scanner(System.in);) {

			Thread listner = new Thread(new ServerMessagesListener(br, isDisconnect, registaredFiles));
			listner.setDaemon(true);
			listner.start();

			System.out.println("listening on port: " + serverSocket.getLocalPort());

			StringBuilder fileName = new StringBuilder();
			fileName.append(serverSocket.getLocalPort());
			fileName.append(".txt");

			userInfoFile = fileName.toString();

			scheduler.scheduleAtFixedRate(new UserFileUpdater(fileName.toString()), 0, 30, TimeUnit.SECONDS);

			Thread serverExecutor = new ServerExecutor(serverSocket, registaredFiles);
			serverExecutor.setDaemon(true);
			serverExecutor.start();

			pw.println(serverSocket.getLocalPort());

			String clientInput;
			while (!isDisconnect.get()) {
				clientInput = sc.nextLine();

				if (clientInput.startsWith("register")) {
					registerFiles(clientInput.replaceFirst("register", ""));
				} else if (clientInput.startsWith("unregister")) {
					unregisterFiles(clientInput.replaceFirst("unregister", ""));
				}

				if (clientInput.startsWith("download")) {
					executeDownloadMessage(clientInput);
				} else {
					pw.println(clientInput);
				}


			}

		} catch (IOException e) {
			System.err.println("(ex)Main" + e.getMessage());
		} finally {
			executor.shutdown();
			scheduler.shutdown();
		}
	}

	private void executeDownloadMessage(String message) {

		String[] arguments = message.split("\\s+");

		String ownerOfFile = arguments[1];
		String fileName = arguments[2];
		String path = arguments[3];

		String line = null;

		try (BufferedReader br = new BufferedReader(new FileReader(userInfoFile))) {
			while ((line = br.readLine()) != null) {
				if (line.startsWith(ownerOfFile + " ")) {
					break;
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		if (line == null) {
			System.out.println("System : user not found");
		} else {
			String[] linesPars = line.split("\\s+");

			String ownerOfFileIp = linesPars[2].split(":")[0];
			String ownerOfFilePort = linesPars[2].split(":")[1];

			executor.execute(new FileDownload(ownerOfFileIp, Integer.valueOf(ownerOfFilePort), fileName, path));

		}
	}

	private void registerFiles(String files) {
		String[] filesArray = files.split("\\s+");

		for (String file : filesArray) {
			registaredFiles.add(file);
		}
	}

	private void unregisterFiles(String files) {
		String[] filesArray = files.split("\\s+");

		for (String file : filesArray) {
			registaredFiles.remove(file);
		}
	}

	public static void main(String[] args) {

		Client client = new Client();
		client.start();
	}
}

package bg.sofia.uni.fmi.mjt.ServerPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class ClientRequestHandler implements Runnable {

	private Socket socket;
	private Hashtable<String, Client> users;
	private Client client;

	public ClientRequestHandler(Socket socket, Hashtable<String, Client> users) {
		this.socket = socket;
		this.users = users;
	} 

	@Override
	public void run() {

		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			if (!findClient(out, in)) {
				return;
			}

			String inputLine;

			sendFiles(client.getFiles(), out);

			out.println("Welcome");

			while ((inputLine = in.readLine()) != null) {

				out.println(parseMessage(inputLine));

			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (client != null) {
				client.disconnect();
			}
		}

	}

	private boolean findClient(PrintWriter out, BufferedReader in) throws IOException {

		String inputLine;

		int port = readPort(in);

		if (port < 0) {
			return false;
		}

		do {

			inputLine = in.readLine();

			if (inputLine == null) {
				return false;
			}

			String username = inputLine;

			synchronized (users) {
				if (users.containsKey(username)) {
					client = users.get(username);

					if (!client.isActive().get()) {
						client.init(socket.getInetAddress().getHostAddress(), port);
					} else {
						client = null;
						out.println("User with username " + username + " is active");
					}

				} else {
					client = new Client(socket.getInetAddress().getHostAddress(), port);
					users.put(username, client);

				}
			}

		} while (client == null);

		return true;
	}

	private int readPort(BufferedReader in) throws IOException {
		String inputLine;

		inputLine = in.readLine();

		if (inputLine == null) {
			return -1;
		}

		int port = 0;
		try {
			port = Integer.parseInt(inputLine);
		} catch (NumberFormatException e) {
			return -1;
		}

		return port;
	}

	private String parseMessage(String message) {

		if ("list-files".equals(message)) {
			return allFile();
		} else if (message.startsWith("register")) {
			registerFiles(message.replaceFirst("register", ""));
			return "Done";
		} else if (message.startsWith("unregister")) {
			unregisterFiles(message.replaceFirst("unregister", ""));
			return "Done";
		} else {
			return "unknow command";
		}
	}

	private void registerFiles(String files) {
		String[] filesArray = files.split("\\s+");

		HashSet<String> setOfFiles = new HashSet<>();

		for (String file : filesArray) {
			if (file.length() > 0) {
				setOfFiles.add(file);
			}
		}

		client.addFiles(setOfFiles);
	}

	private void unregisterFiles(String files) {
		String[] filesArray = files.split("\\s+");
		client.removeFiles(Set.of(filesArray));
	}

	private String allFile() {
		StringBuilder messageToSend = new StringBuilder();

		Set<String> set = users.keySet();
		Iterator<String> it = set.iterator();

		Client tempClient;

		while (it.hasNext()) {
			String username = it.next().toString();
			tempClient = users.get(username);

			if (tempClient.isActive().get()) {

				Set<String> fileSet = tempClient.getFiles();
				if (!fileSet.isEmpty()) {
					messageToSend.append("files from ");
					messageToSend.append(username);
					messageToSend.append(" ");
					messageToSend.append(tempClient.getFiles());
					messageToSend.append(System.lineSeparator());
				}

			}
		}

		return messageToSend.toString();
	}

	private void sendFiles(Set<String> files, PrintWriter out) {

		out.println(files.size());
		files.forEach(out::println);
	}

}

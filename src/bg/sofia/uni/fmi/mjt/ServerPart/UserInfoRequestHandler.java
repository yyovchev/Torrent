package bg.sofia.uni.fmi.mjt.ServerPart;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class UserInfoRequestHandler implements Runnable {
	private Socket socket;
	private Hashtable<String, Client> users;

	public UserInfoRequestHandler(Socket socket, Hashtable<String, Client> users) {
		this.socket = socket;
		this.users = users;
	}

	@Override 
	public void run() {
		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			Set<String> set = users.keySet();
			Iterator<String> it = set.iterator();

			Client tempClient;
			while (it.hasNext()) {
				String username = it.next().toString();
				tempClient = users.get(username);

				if (tempClient.isActive().get()) {
					out.println(username + " - " + tempClient.getIPPort());
				}
			}

		} catch (IOException e) {
			System.err.println("Exeption in UserInfoRequestHandler - " + e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Exeption in UserInfoRequestHandler  - " + e.getMessage());
			}
		}

	}

}

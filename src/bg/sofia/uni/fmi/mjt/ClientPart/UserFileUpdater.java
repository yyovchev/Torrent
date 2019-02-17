package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class UserFileUpdater implements Runnable {

	private static final int SERVER_CLIENT_INFO_PORT = 4445;
	String fileName;

	public UserFileUpdater(String fileName) {
		this.fileName = fileName; 
	}

	@Override
	public void run() {
		try (Socket clientInfo = new Socket("localhost", SERVER_CLIENT_INFO_PORT);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientInfo.getInputStream()));
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));) {

			String messageFromServer;
			while ((messageFromServer = in.readLine()) != null) {
				fileWriter.write(messageFromServer + System.lineSeparator());
			}

		} catch (IOException e) {
			System.err.println("Problem while update user info " + e.getMessage());
		}

	}

}

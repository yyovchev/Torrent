package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMessagesListener implements Runnable {

	private BufferedReader br;
	private AtomicBoolean isDisconnect;
	private Set<String> registaredFiles;

	public ServerMessagesListener(BufferedReader br, AtomicBoolean isDisconnect, Set<String> registaredFiles) {
		this.br = br; 
		this.isDisconnect = isDisconnect;
		this.registaredFiles = registaredFiles;

	}

	@Override
	public void run() {
		String reply;

		try {
			reply = br.readLine();

			if (reply == null) {
				return;
			}

			int numberOfRegisteredFiles = Integer.parseInt(reply);

			synchronized (registaredFiles) {

				for (int i = 0; i < numberOfRegisteredFiles; ++i) {
					reply = br.readLine();

					if (reply == null) {
						return;
					}

					registaredFiles.add(reply);
				}
			}

			while ((reply = br.readLine()) != null) {
				if (!"".equals(reply)) {
					System.out.println("Server : " + reply);
				}
			}
		} catch (IOException e) {
			System.out.println("fail");
			e.printStackTrace();
		} finally {
			isDisconnect.set(true);
		}

	}

}

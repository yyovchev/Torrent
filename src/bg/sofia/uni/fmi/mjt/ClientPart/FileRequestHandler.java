package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class FileRequestHandler implements Runnable {

	private Socket socket;
	private Set<String> registeredFile;

	public FileRequestHandler(Socket socket, Set<String> registeredFile) {
		this.socket = socket;
		this.registeredFile = registeredFile;
	}

	@Override
	public void run() {

		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

		 	String fileName = in.readLine();
			if (fileName == null) {
				return;
			}

			File file = new File(fileName);

			if (!registeredFile.contains(fileName) || !file.exists() || file.isDirectory()) {
				out.println(Long.valueOf(-1));
				return;
			} else {
				out.println(file.length());
			}

			byte[] bytearray = new byte[1024 * 32];

			try (FileInputStream fin = new FileInputStream(file);
					OutputStream output = socket.getOutputStream();
					BufferedInputStream bis = new BufferedInputStream(fin)) {

				int length = -1;
				while ((length = bis.read(bytearray)) > 0) {
					output.write(bytearray, 0, length);

				}

			} catch (Exception ex) {

				System.err.println(ex.getMessage());
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}

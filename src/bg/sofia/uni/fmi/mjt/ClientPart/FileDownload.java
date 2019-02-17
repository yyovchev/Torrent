package bg.sofia.uni.fmi.mjt.ClientPart;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FileDownload implements Runnable {

	private String ip;
	private int port;
	private String fileName;
	private String path;

	public FileDownload(String ip, int port, String fileName, String path) {
		this.ip = ip;
		this.port = port;
		this.fileName = fileName;
		this.path = path;
	} 

	@Override
	public void run() {

		try (Socket socket = new Socket(ip, port);
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				FileOutputStream fileOutputStream = new FileOutputStream(path);
				BufferedOutputStream fileBOSream = new BufferedOutputStream(fileOutputStream);) {

			pw.println(fileName);

			long size = Long.valueOf(br.readLine());

			if (size == -1) {
				System.out.println("File cannot be downloaded !");
				return;
			}

			byte[] bytearray = new byte[1024 * 32];

			int lenght = -1;
			while ((lenght = dis.read(bytearray)) > 0) {
				fileBOSream.write(bytearray, 0, lenght);
			}

			System.out.println("File downloaded");

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

}

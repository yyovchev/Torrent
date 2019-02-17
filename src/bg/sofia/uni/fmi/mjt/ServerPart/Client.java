package bg.sofia.uni.fmi.mjt.ServerPart;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

	private Object fileAccessMonitor;
	private Object statusChangeMonior;

	private String ip;
	private int miniServerPort;
	AtomicBoolean active;
	Set<String> files; 

	public Client(String ip, int port) {

		fileAccessMonitor = new Object();
		statusChangeMonior = new Object();

		files = new HashSet<>();
		active = new AtomicBoolean(true);

		this.ip = ip;
		this.miniServerPort = port;
	}

	public void disconnect() {
		this.active.set(false);
	}

	public AtomicBoolean isActive() {
		return active;
	}

	public void init(String ip, int port) {

		synchronized (statusChangeMonior) {
			this.ip = ip;
			this.miniServerPort = port;
			active.set(true);
		}

	}

	public String getIp() {

		synchronized (statusChangeMonior) {
			return ip;
		}

	}

	public int getPort() {

		synchronized (statusChangeMonior) {
			return miniServerPort;
		}
	}

	public String getIPPort() {

		synchronized (statusChangeMonior) {
			return ip + ":" + miniServerPort;
		}

	}

	public void addFiles(Set<String> files) {

		synchronized (fileAccessMonitor) {
			this.files.addAll(files);
		}

	}

	public Set<String> getFiles() {

		synchronized (fileAccessMonitor) {
			return new HashSet<String>(files);
		}

	}

	public void removeFiles(Set<String> files) {

		synchronized (fileAccessMonitor) {
			this.files.removeAll(files);
		}

	}
}

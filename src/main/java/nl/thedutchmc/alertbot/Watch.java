package nl.thedutchmc.alertbot;

import java.net.InetAddress;

public class Watch {

	private InetAddress address;
	private String name;
	private boolean isOnline;
	
	public Watch(InetAddress address, String name) {
		this.address = address;
		this.name = name;
		
		isOnline = false;
	}
	
	public InetAddress getInetAddress() {
		return this.address;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isOnline() {
		return this.isOnline;
	}
	
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}

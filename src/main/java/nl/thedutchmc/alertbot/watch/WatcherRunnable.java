package nl.thedutchmc.alertbot.watch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.json.JSONObject;

import nl.thedutchmc.alertbot.App;
import nl.thedutchmc.alertbot.Config;
import nl.thedutchmc.alertbot.Watch;

public class WatcherRunnable implements Runnable {

	@Override
	public void run() {
		
		while(true) {
			Config.getWatch().forEach(address -> {
				
				//Get the hostname and the ports from the address.
				final String[] parts = address.split(":");
				final String hostname = parts[0];
				final int port = Integer.valueOf(parts[1]);
				
				try (
					//Setup the socket, output and input
		            final Socket clientSock = new Socket(hostname, port);
		            final PrintWriter out = new PrintWriter(clientSock.getOutputStream(), true);
		            final BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
		        ) {					
					//Write to the Watch to see if it is alive
					out.write("isAlive\n");
					out.flush();
					
					//4096 character long char array, should be plenty.
					//Read the data from the input into the char array
					char[] response = new char[4096];
					in.read(response);
					
					//Parse the char array into a String, then into a JSON Object
					JSONObject responseJson = new JSONObject(new String(response));
					
					//Check if the Watches list contains this Watch
					//If no, add it.
					if(App.watches.containsKey(clientSock.getInetAddress())) {
						Watch w = App.watches.get(clientSock.getInetAddress());
						w.setOnline(true);
					} else {
						Watch w = new Watch(clientSock.getInetAddress(), responseJson.getString("name"));
						w.setOnline(true);
						
						App.watches.put(clientSock.getInetAddress(), w);
					}
					
					App.watchesByHostnameIsOnline.put(hostname, true);
					
				} catch (UnknownHostException e) {
					//Watch couldn't be reached, mark it as offline and continue					
					if(App.watchesByHostnameIsOnline.get(hostname) != null && App.watchesByHostnameIsOnline.get(hostname)) {
						App.watchesByHostnameIsOnline.put(hostname, false);
						App.outage(hostname);
					}
					
					
				} catch (SocketTimeoutException e) {
					//Watch timed out, likely offline. Mark it as offline and continue					
					if(App.watchesByHostnameIsOnline.get(hostname) != null && App.watchesByHostnameIsOnline.get(hostname)) {
						App.watchesByHostnameIsOnline.put(hostname, false);
						
						InetAddress addr = null; 
						try {
							addr = InetAddress.getByName(hostname);
						} catch(UnknownHostException e1) {
							App.outage(hostname);
							return;
						}
						
						if(App.watches.containsKey(addr)) {
							Watch w = App.watches.get(addr);
							w.setOnline(false);
							
							App.outage(w);
						} else {
							App.outage(hostname);
						}
					}
				} catch (ConnectException e) {
					//Connection is refused, likely offline. Mark it as offline and continue.					
					if(App.watchesByHostnameIsOnline.get(hostname) != null && App.watchesByHostnameIsOnline.get(hostname)) {
						App.watchesByHostnameIsOnline.put(hostname, false);
						
						InetAddress addr = null; 
						try {
							addr = InetAddress.getByName(hostname);
						} catch(UnknownHostException e1) {
							App.outage(hostname);
							return;
						}
						
						if(App.watches.containsKey(addr)) {
							Watch w = App.watches.get(addr);
							w.setOnline(false);
							
							App.outage(w);
						} else {
							App.outage(hostname);
						}
					}
				} catch (SocketException e) {					
					//Don't care.
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to " + hostname);
				}
			});
			
			//Sleep for however long the user configured. Option in config is in seconds, so also convert to miliseconds
			try {
				Thread.sleep(((int) Config.get("watchInterval")) * 1000);
			} catch (InterruptedException e) {}
		}
	}
}

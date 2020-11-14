package nl.thedutchmc.alertbot;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import javax.security.auth.login.LoginException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.dv8tion.jda.api.EmbedBuilder;
import nl.thedutchmc.alertbot.discord.JdaHandler;
import nl.thedutchmc.alertbot.watch.WatcherRunnable;

@SpringBootApplication
public class App {

	public static boolean twitterEnabled, discordEnabled;
	public static HashMap<InetAddress, Watch> watches = new HashMap<>();
	public static HashMap<String, Boolean> watchesByHostnameIsOnline = new HashMap<>();
	
    public static void main(String[] args) {
   
    	logInfo("Starting AlertBot...");
    	logInfo("Reading configuration file...");
    	
    	//Load and read the configuration file
    	Config c = new Config();
    	try {
    		c.load();
    	} catch (URISyntaxException e) {
    		logWarn("Failed to read configuration file. URISyntaxException.");
    		System.exit(1);
    	} catch (IOException e) {
    		logWarn("Failed to read configuration file. IOException.");
    		System.exit(1);
    	}
    	
    	//Loop over all Watch's and populate the watchesByHostnameIsOnline map
    	Config.getWatch().forEach(hostnameWithPort -> {
    		String hostname = hostnameWithPort.split(":")[0];
    		watchesByHostnameIsOnline.put(hostname, true);
    	});
    	
    	
    	logInfo("Reading configuration file done.");
    	logInfo("Starting alerting endpoints...");
    	
    	if((Boolean) Config.get("enableTwitter")) {
    		logInfo("Twitter API is enabled. Setting up...");
    		
    		twitterEnabled = true;
    		
    		logInfo("Completed Twitter setup.");
    	} else {
    		logInfo("Twitter API is disabled. Skipping.");
    	}
    	
    	if((Boolean) Config.get("enableDiscord")) {
    		logInfo("Discord API is enabled. Setting up...");
    	
    		discordEnabled = true;
    		
    		JdaHandler handler = new JdaHandler();
    		try {
    			handler.load();
    		} catch (LoginException e) {
    			logWarn("Failed to log in with Discord. Check your internet connection and bot token.");
    			discordEnabled = false;
    		}
    		
    		logInfo("Completed Discord setup.");
    	} else {
    		logInfo("Discord API is disabled. Skipping.");
    	}
    	
    	logInfo("Alerting Endpoints started.");
    	logInfo("Starting Watcher thread...");
    	
    	Runnable watcherRunnable = new WatcherRunnable();
    	Thread watcherThread = new Thread(watcherRunnable);
    	watcherThread.start();
    	
    	logInfo("Watcher thread started.");
		logInfo("Starting Spring Boot server...");
    	
		SpringApplication.run(App.class, args);
		
		logInfo("Spring boot server started.");
    	logInfo("Startup complete.");	
    }
    
	public static void logInfo(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][INFO] " + log);
	}
	
	public static void logWarn(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.err.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][WARN] " + log);
	}
	
	public static void outage(Watch w) {
		if(discordEnabled) {
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("Notice: " + w.getName() + " is offline!")
					.setDescription(w.getName() + " is offline!");
		
			JdaHandler.sendEmbed(builder.build());
		}
		
	}
	
	public static void outage(String hostname) {
		if(discordEnabled) {
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle("Notice: " + hostname + " is offline!")
					.setDescription("It looks like the node with the hostname " + hostname + " is offline. It is unknown what the function of this node is.");
			
			JdaHandler.sendEmbed(builder.build());
		}
	}
}

package nl.thedutchmc.alertbot.discord;

import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import nl.thedutchmc.alertbot.App;
import nl.thedutchmc.alertbot.Config;

public class JdaHandler {

	private static JDA jda;
	
	public void load() throws LoginException {
	
		final List<GatewayIntent> intents = Arrays.asList(new GatewayIntent[] {
				GatewayIntent.GUILD_MESSAGES
			});
		
		final Object[] listeners = new Object[] {};
		
		jda = JDABuilder.createDefault((String) Config.get("botToken"))
				.enableIntents(intents)
				.addEventListeners(listeners)
				.setActivity(Activity.playing("Alerting"))
				.build();
		
		try {
			jda.awaitReady();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendEmbed(MessageEmbed e) {
		TextChannel c = jda.getTextChannelById(Long.valueOf((String) Config.get("alertChannelId")));
		c.sendMessage(e).queue(message -> {
			//Crosspost the message ("Publish") if the target channel is a news Channel
			if(c.isNews()) {
				message.crosspost().queue();
			} else {
				App.logInfo("Not crossposting. Not a news channel.");
			}
		});
	}
}

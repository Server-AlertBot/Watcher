package nl.thedutchmc.alertbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Config {

	private static HashMap<String, Object> config = new HashMap<>();
	private static List<String> watch = new ArrayList<>();
	
	public void load() throws URISyntaxException, IOException {
		
		//Get the path of where the config file should be
		final File jarPath = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		final File folderPath = new File(jarPath.getParentFile().getPath());
		final File configFile = new File(folderPath, "alertbot.json");
		
		//Check if the config file exists.
		//If this isn't the case, create it and write the default values to it
		if(!configFile.exists()) {
			configFile.createNewFile();
			
			FileWriter fw = new FileWriter(configFile, true);
			
			fw.write("{\r\n" + 
					"    \"config\": {\r\n" + 
					"        \"watchInterval\": 120,\r\n" + 
					"        \"enableTwitter\": true,\r\n" + 
					"        \"twitterBearer\": \"\",\r\n" + 
					"        \"websiteUrl\": \"\",\r\n" + 
					"        \"enableDiscord\": true,\r\n" + 
					"        \"botToken\": \"\",\r\n" + 
					"        \"alertChannelId\": \"\"\r\n" + 
					"    },\r\n" + 
					"    \"watch\": [\r\n" + 
					"    ]\r\n" + 
					"}");

			fw.close();
		}
		
		//Parse the file into a JSON Object
		String content = new String(Files.readAllBytes(Paths.get(configFile.getAbsolutePath())));
		JSONObject configFileJson = new JSONObject(content);
				
		//Loop over the config JSON object and put it into the Config HashMap
		JSONObject configJson = configFileJson.getJSONObject("config");
		for(String key : configJson.keySet()) {
			config.put(key, configJson.get(key));
		}
		
		//Loop over the Watch JSON array, and add it to the list
		JSONArray watchJsonArr = configFileJson.getJSONArray("watch");
		watchJsonArr.forEach(value -> {
			watch.add((String) value);
		});	
	}
	
	//Used to fetch a config option
	public static Object get(String key) {
		return config.get(key);
	}
	
	public static List<String> getWatch() {
		return watch;
	}
}

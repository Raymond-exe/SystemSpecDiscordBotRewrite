package me.raymond.records;

import me.raymond.discordbot.DiscordBot;

import java.util.Map;

public class EnvironmentManager {

    private static Map<String, String> envVars;

    public static void instantiate() {
        envVars = System.getenv();


        if(DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - EnvironmentManager] Retrieved the following variables: " + envVars);
        }
    }

    public static String get(String key) {
        return envVars.get(key);
    }

}

package me.raymond.records;

import me.raymond.discordbot.DiscordBot;
import me.raymond.pcparts.UserSpecs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;


public class Recordkeeper {

    private static HashMap<String, String> guildPrefixes = new HashMap<>();
    private static HashMap<String, UserSpecs> userSpecsMap = new HashMap<>();


    public static HashMap<String, Object> getUserSpecsTemplate(String userId) {
        HashMap<String, Object> output = new HashMap<>();

        output.put("userId", userId);
        output.put("cpu", "[No Cpu: 0]");
        output.put("gpu", "[No Gpu: 0]");
        output.put("ram", 2);
        output.put("description", "null");
        output.put("privacy", false);

        return output;
    }

    public static UserSpecs getSpecsByUserId(String userId) {

        if (userSpecsMap.containsKey(userId)) {
            return userSpecsMap.get(userId);
        } else {
            userSpecsMap.put(userId, FirebaseController.getUserSpecs(userId));
            return getSpecsByUserId(userId);
        }
    }

    public static boolean addUserSpecs(UserSpecs userSpecs) {
        HashMap<String, Object> parsedSpecs = new HashMap<>();

        parsedSpecs.put("userId", userSpecs.getUserId());
        parsedSpecs.put("cpu", userSpecs.getUserCpu());
        parsedSpecs.put("gpu", userSpecs.getUserGpu());
        parsedSpecs.put("ram", userSpecs.getUserRam());
        parsedSpecs.put("privacy", userSpecs.getPrivacy());
        parsedSpecs.put("description", userSpecs.getPcDescription());

        userSpecsMap.put(userSpecs.getUserId(), userSpecs);
        return FirebaseController.addUserSpecs(parsedSpecs);
    }

    public static String getGuildPrefix(String guildId) {

        try {
            DiscordBot.getJda().getGuildById(guildId).getName();
        } catch (Exception e) {
            return "";
        }

        if (guildPrefixes.containsKey(guildId)) {
            return guildPrefixes.get(guildId);
        } else {
            String prefix = FirebaseController.getGuildPrefix(guildId);
            guildPrefixes.put(guildId, prefix);
            return getGuildPrefix(guildId);
        }
    }

    public static void setPrefix(String guildId, String prefix) {
        guildPrefixes.put(guildId, prefix);
        FirebaseController.setGuildPrefix(guildId, prefix);
    }

    public static String readFile(String filename) {
        StringBuilder output = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            String line = reader.readLine();
            while (line != null) {
                output.append(line).append("\n");
                line = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            System.out.println("Unable to find file \"" + filename + "\"");
        }

        return output.toString();
    }
}

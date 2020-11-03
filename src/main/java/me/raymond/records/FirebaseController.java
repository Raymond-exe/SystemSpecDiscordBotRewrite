package me.raymond.records;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import me.raymond.discordbot.DiscordBot;
import me.raymond.pcparts.Cpu;
import me.raymond.pcparts.Gpu;
import me.raymond.pcparts.UserSpecs;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FirebaseController {


    private static Firestore db;


    public static void connect() {

        if (DiscordBot.debugPrintouts)
            System.out.println("[DEBUG - FirebaseController] Connecting to Firestore Database...");

        GoogleCredentials googleCredentials;

        //try block will try to connect to Firebase
        try {

            //try block will try to locate credentials
            try {

                googleCredentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(
                                EnvironmentManager.get("SPECBOT_GOOGLE_CREDENTIALS")
                                        .getBytes(StandardCharsets.UTF_8)
                        ));

            } catch (Exception e) {

                if(DiscordBot.debugPrintouts)
                    System.out.println("[DBEUG - FirebaseController] Unable to locate Environment Variable \"SPECBOT_GOOGLE_CREDENTIALS\", using application default");

                googleCredentials = GoogleCredentials.getApplicationDefault();
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(googleCredentials)
                    .setDatabaseUrl("https://discord-specbot.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();

            if (DiscordBot.debugPrintouts)
                System.out.println("[DEBUG - FirebaseController] Successfully connected to Firestore Database!");

        } catch (Exception e) {
            e.printStackTrace();

            if (DiscordBot.debugPrintouts)
                System.out.println("[DEBUG - FirebaseController] Failed to connect to Firestore Database");
        }
    }

    public static String getGuildPrefix(String guildId) {
        //request the guildPrefix with the matching guildId
        DocumentReference docRef = db.collection("guildPrefixes").document(guildId.trim());

        if (DiscordBot.debugPrintouts)
            System.out.println("[DEBUG - FirebaseController] READ: A request for the guild prefix for guild " + guildId + " went out!");

        try {
            //If the requested guildId exists, return the prefix
            if (docRef.get().get().exists()) {
                return (String) docRef.get().get().get("prefix");
            }
            //if it doesn't exist, create a new guildPrefix entry and assign it the default prefix (~)
            else {

                HashMap<String, String> newPrefix = new HashMap<>();
                newPrefix.put("prefix", "~");

                docRef.set(newPrefix);
                return "~";
            }
        } catch (Exception e) {
            return "null";
        }

    }

    public static void setGuildPrefix(String guildId, String prefix) {
        HashMap<String, String> newPrefix = new HashMap<>();
        newPrefix.put("prefix", prefix);
        newPrefix.put("serverName", DiscordBot.getJda().getGuildById(guildId).getName());

        if (DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - FirebaseController] WRITE: A request was sent to change the guildPrefix for guild " + guildId + " to " + prefix);
        }

        DocumentReference docRef = db.collection("guildPrefixes").document(guildId.trim());

        try {
            if (docRef.get().get().exists()) {
                docRef.set(newPrefix);
            }
        } catch (Exception ignored) {
        }
    }

    public static UserSpecs getUserSpecs(String userId) {
        DocumentReference docRef = db.collection("userSpecs").document(userId.trim());

        if (DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - FirebaseController] READ: A request went out for the UserSpecs of user <@!" + userId + ">");
        }

        try {
            //If user alrady has existing specs in database
            if (docRef.get().get().exists()) {
                Map<String, Object> specsMap = docRef.get().get().getData();
                String parseToUserSpecs = "<specs>";

                parseToUserSpecs += "<user>" + userId + "</user>";
                parseToUserSpecs += "<cpu>" + specsMap.get("cpu") + "</cpu>";
                parseToUserSpecs += "<gpu>" + specsMap.get("gpu") + "</gpu>";
                parseToUserSpecs += "<ram>" + specsMap.get("ram") + "</ram>";
                parseToUserSpecs += "<privacy>" + specsMap.get("privacy") + "</privacy>";
                parseToUserSpecs += "<description>" + specsMap.get("description") + "</description>";
                parseToUserSpecs += "</specs>";

                if (DiscordBot.debugPrintouts) {
                    System.out.println("[DEBUG - FirebaseController] User <@!" + userId + ">'s UserSpecs were found!");
                    //System.out.println("[DEBUG - FirebaseController] " + specsMap);
                    //System.out.println("[DEBUG - FirebaseController] Output: " + parseToUserSpecs);
                }

                return new UserSpecs(parseToUserSpecs);

            } else {

                //UserSpecs for the userId weren't found
                if (DiscordBot.debugPrintouts)
                    System.out.println("[DEBUG - FirebaseController] No UserSpecs found for user <@!" + userId + ">");

                HashMap<String, Object> newSpecs = Recordkeeper.getUserSpecsTemplate(userId);
                docRef.set(newSpecs);

                return getUserSpecs(userId); //TODO Fix! Bad Code >:(
            }
        } catch (Exception e) {
            if(DiscordBot.debugPrintouts) {
                System.out.println("[DEBUG - FirebaseController] An error has occured!");
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void setUserSpecs(String userId, Object obj) {
        DocumentReference docRef = db.collection("userSpecs").document(userId.trim());
        String objClassAssignment;

        try {
            if (!docRef.get().get().exists()) {
                //UserSpecs for the userId didn't already exist
                if (DiscordBot.debugPrintouts)
                    System.out.println("[DEBUG - FirebaseController] No UserSpecs found for user #" + userId);

                HashMap<String, Object> newSpecs = Recordkeeper.getUserSpecsTemplate(userId);
                docRef.set(newSpecs);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (obj instanceof Integer) {
            objClassAssignment = "ram";

        } else if (obj instanceof Cpu) {
            objClassAssignment = "cpu";

        } else if (obj instanceof Gpu) {
            objClassAssignment = "gpu";

        } else if (obj instanceof String) {
            objClassAssignment = "description";

        } else if (obj instanceof Boolean) {
            objClassAssignment = "privacy";

        } else {
            if (DiscordBot.debugPrintouts) {
                System.out.println("[DEBUG - FirebaseController] setUserSpecs was run with an unaccepted object!");
            }
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(objClassAssignment, obj);

        docRef.update(map);
    }

    public static boolean addUserSpecs(HashMap<String, Object> specsToAdd) {
        if (!specsToAdd.containsKey("userId"))
            return false;

        try {

            String userId = (String) specsToAdd.get("userId");

            db.collection("userSpecs").document(userId).set(specsToAdd);
            return specsToAdd.equals(db.collection("userSpecs").document(userId).get().get().getData());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        connect();

        setUserSpecs("defaultTemplate", 99);
    }

}

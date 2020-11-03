package me.raymond.discordbot;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import me.raymond.pcparts.Cpu;
import me.raymond.pcparts.Gpu;
import me.raymond.pcparts.UserSpecs;
import me.raymond.records.Recordkeeper;
import me.raymond.webaccess.SteamGame;
import me.raymond.webaccess.SearchResult;
import me.raymond.webaccess.Searcher;
import me.raymond.webaccess.StringTools;


import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class Commands extends ListenerAdapter {

    private static final int CPU_INDEX = 0;
    private static final int GPU_INDEX = 1;
    private static final int RAM_INDEX = 2;
    private static String betaServers = "511968553021472781, 478770676937785355";
    private static String feedbackChannelId = "638183306642456577";
    private static String consoleChannelId = "711280957142990958";
    //private static String errorLogChannelId = "639894236183003157";

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        try {

            String message = event.getMessage().getContentRaw().trim();
            String prefix = Recordkeeper.getGuildPrefix(event.getGuild().getId());
            int endOfFirstArg = (message.contains(" ") ? message.indexOf(" ") : message.length());

            if (message.length() < prefix.length() + 2)
                return;

            //Checks if message starts with a mention to the bot
            if (message.startsWith("<@!" + DiscordBot.getJda().getSelfUser().getId() + ">")) {
                message = message.substring(message.indexOf(">") + 1).trim();


                switch (message) {
                    case "":
                        event.getChannel().sendMessage("Make sure to let me know what game you want to play by using \"" + DiscordBot.getJda().getSelfUser().getAsMention() + " `[YOUR GAME HERE]`\"").queue();
                        break;
                    case "help":
                        help(event);
                        break;
                    default:
                        canUserPlay(event, message);
                        break;
                }
            }

            //Checks if message starts with prefix
            if (message.startsWith(prefix)) {
                message = message.substring(prefix.length(), endOfFirstArg);

                if (!betaServers.contains(event.getGuild().getId())) {
                    event.getChannel().sendMessage("Sorry! " + DiscordBot.getJda().getSelfUser().getAsMention() + " is currently in beta and *not available in public servers*. Please message **@Ramen.exe#8147** to add your server to the beta testing list, thank you!").queue();
                    return;
                }
            } else return;

            switch (message.toLowerCase()) {
                case "ping":
                case "pong":
                    ping(event);
                    break;
                case "rules":
                    rules(event);
                    break;
                case "search":
                    search(event);
                    break;
                case "gamespecs":
                    gamespecs(event);
                    break;
                case "gameinfo":
                    gameinfo(event);
                    break;
                case "info":
                    info(event);
                    break;
                case "help":
                    help(event);
                    break;
                case "resetspecs":
                case "resetinfo":
                    resetspecs(event);
                    break;
                case "myspecs":
                case "my":
                case "specs":
                case "myinfo":
                    myspecs(event);
                    break;
                case "setspecs":
                case "set":
                    setspecs(event);
                    break;
                case "getspecs":
                    getspecs(event);
                    break;
                case "privacy":
                case "setprivacy":
                    setprivacy(event);
                    break;
                case "prefix":
                case "setprefix":
                    setprefix(event);
                    break;
                case "feedback":
                    feedback(event);
                    break;
                case "compare":
                    compare(event);
                    break;
                default:
                    event.getChannel().sendMessage(
                            "**\""
                                    + message
                                    + "\"** is an unrecognized command, try **\""
                                    + prefix +
                                    "help\"** for the list of commands.").queue();
            }
        } catch (Exception e) {


            //Logging error in console
            sendToConsole("Error occured after a user ran the command `" + event.getMessage().getContentRaw() + "`, check #caniplay-error-log for more info.");

            /*/Logging error in error log channel
            DiscordBot.getJda().getTextChannelById(errorLogChannelId)
                    .sendMessage("**__"+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) +" PST__**\n**Message at fault:** `" + event.getMessage().getContentRaw() + "`\n**StackTrace:**\n```\n" + StringTools.getErrorAsStackTrace(e) + "```")
                    .queue(); //*/

            //Send message to user indicating error
            event.getChannel().sendMessage("Uh-oh, I've run into an error! If you know what happened, use `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "feedback` to let us know what happened. Thanks!");
            throw e;
        }
    }


    /***** COMMAND METHODS *****/

    private void ping(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage(":ping_pong: **" + (event.getMessage().getContentRaw().contains("pong") ? "Ping" : "Pong") + "!**").queue();

        if(event.getAuthor().getId().equals("226113023775997952") && event.getMessage().getContentRaw().toLowerCase().contains("ping")) {
            OffsetDateTime currentTime = new Date(System.currentTimeMillis()).toInstant().atOffset(ZoneOffset.UTC);
            OffsetDateTime timeSent = event.getMessage().getTimeCreated();

            event.getChannel().sendMessage("**" + Math.abs((currentTime.getNano() - timeSent.getNano()) / 1000000) + "** ms").queue();
        }
    }

    private void rules(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("How am I supposed to know?").queue();
        event.getChannel().sendMessage("Ask an admin").queue();
    }

    private void cpuSearch(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String query = message.substring(message.toLowerCase().indexOf("cpu") + 3).trim();

        ArrayList<SearchResult> results = Searcher.searchSpecs("CPU", query);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("CPU Search Results for " + query)
                .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                .setDescription("To set your cpu, type `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs cpu [YOUR CPU]`")
                .setColor(Color.WHITE);

        if (results.isEmpty()) {
            embed.setTitle(":warning: No CPUs found for `" + query + "`.");
            embed.setDescription("Maybe try another search term?");
            embed.setColor(Color.ORANGE);
        } else {
            for (SearchResult result : results) {
                embed.addField(result.getName(), "[View info](" + result.getLink() + ") or use *" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "info cpu " + result.getName() + "*", false);
            }
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void gpuSearch(GuildMessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        String query = message.substring(message.toLowerCase().indexOf("gpu") + 3).trim();

        ArrayList<SearchResult> results = Searcher.searchSpecs("GPU", query);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("GPU Search Results for " + query)
                .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                .setDescription("To set your gpu, type `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs gpu [YOUR GPU]`")
                .setColor(Color.WHITE);

        if (results.isEmpty()) {
            embed.setTitle(":warning: No GPUs found for `" + query + "`.");
            embed.setDescription("Maybe try another search term?");
            embed.setColor(Color.ORANGE);
        } else {
            for (SearchResult result : results) {
                embed.addField(result.getName(), "[View info](" + result.getLink() + ") or use *" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "info gpu " + result.getName() + "*", false);
            }
        }

        event.getChannel().sendMessage(embed.build()).queue();
        //event.getChannel().sendMessage(results.toString()).queue();

    }

    private void search(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().split(" ");

        if (messageArgs[1].equalsIgnoreCase("cpu")) {
            cpuSearch(event);
            return;
        } else if (messageArgs[1].equalsIgnoreCase("gpu")) {
            gpuSearch(event);
            return;
        } else if (messageArgs[1].equalsIgnoreCase("ram")) {
            event.getChannel().sendMessage("You don't need to search for RAM, silly!").queue();
            event.getChannel().sendMessage("Set your amount of RAM by typing `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs RAM [YOUR RAM IN GB]`").queue();
            return;
        } else if (messageArgs[1].equalsIgnoreCase("os")) {
            event.getChannel().sendMessage("You don't need to search for your operating system, silly!").queue();
            event.getChannel().sendMessage("Currently, " + DiscordBot.getJda().getSelfUser().getAsMention() + " only works with Windows specs and games. If you use an apple product and would like support to be added in the future, send us feedback by typing \"" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "feedback\" to let us know!").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder().setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl());

        String query = getArgsAfter(0, messageArgs, false);
        //GameInfo tempGame = new GameInfo(Searcher.getSearchResult(query));
        try {
            long deltaTime = System.currentTimeMillis();
            int searchResultLimit = 10;
            ArrayList<String> tempArray = new ArrayList<>(Arrays.asList(StringTools.toStringArray(Searcher.searchFor(query).toArray())));

            embed.setTitle("System requirement search results for " + query, Searcher.getGameSiteLink(query, "steam"));
            embed.setDescription(":stopwatch: **" + (tempArray.size() >= 25 ? "25+" : tempArray.size()) + " search result" + (tempArray.size() == 1 ? "" : "s") + "** in " + (float) (System.currentTimeMillis() - deltaTime) / 1000 + " seconds." + (tempArray.size() > searchResultLimit ? "\nHere are the top " + searchResultLimit + " results:" : ""));
            //embed.setFooter("Type `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "gamespecs [GAME]` to see system requirements for the given game.", null);

            String title, link, prefix = Recordkeeper.getGuildPrefix(event.getGuild().getId());
            for (int i = 0; i < tempArray.size() && i < searchResultLimit; i++) {
                title = StringTools.cleanString(tempArray.get(i).substring(0, tempArray.get(i).lastIndexOf("[!(")).trim());
                link = tempArray.get(i).substring(tempArray.get(i).lastIndexOf("[!(") + 3, tempArray.get(i).lastIndexOf(")!]"));
                embed.addField(title, "[View page](" + link + ") or use *" + prefix + "gameinfo " + title + "*", false);
            }
        } catch (Exception e) {
            embed.setTitle(":warning: No games titled `" + query.trim() + "` were found.");
            embed.setDescription("Maybe try searching for another title?");
            embed.setColor(Color.ORANGE);
            e.printStackTrace();
        }
        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void gamespecs(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().split(" ");
        SteamGame gameInfo = new SteamGame(Searcher.getSearchResult(getArgsAfter(0, messageArgs, false)));
        ArrayList<String> minSpecs;
        try {
            minSpecs = gameInfo.getSpecs(0);
        } catch (Exception ex) {
            event.getChannel().sendMessage("I can't access any info for age-restricted games. Sorry!").queue();
            return;
        }


        try {
            EmbedBuilder embed = new EmbedBuilder()
                    .setImage(gameInfo.getImageUrl())
                    .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                    .setTitle("System Requirements for " + gameInfo.getTitle(), gameInfo.getWebsite())
                    .setFooter("Type \"" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "gameinfo\" to see information on this title.", null)
                    .setColor(Color.WHITE);

            String temp;
            String[] titles = new String[]{"CPU - Central Processing Unit", "GPU - Graphics Processing Unit", "RAM - Random Access Memory", "OS - Operating System", "Storage space needed"};
            for (int i = 0; i < minSpecs.size(); i++) {

                temp = minSpecs.get(i);


                embed.addField(titles[i], temp, false);
            }
            event.getChannel().sendMessage(embed.build()).queue();

        } catch (Exception e) {
            event.getChannel().sendMessage("No search results for " + getArgsAfter(0, messageArgs, false).trim() + ", maybe try searching for another title?").queue();
            e.printStackTrace();
        }
    }

    private void gameinfo(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().split(" ");
        SteamGame gameInfo = new SteamGame(Searcher.getSearchResult(getArgsAfter(0, messageArgs, false)));

        try {
            String[] result = StringTools.toStringArray(gameInfo.getInfo().toArray());
            EmbedBuilder embed = new EmbedBuilder()
                    .setImage(gameInfo.getImageUrl())
                    .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                    .setTitle(gameInfo.getTitle(), gameInfo.getWebsite())
                    .setFooter("Type \"" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "gamespecs\" to see system requirements for this game.", null)
                    .setColor(Color.WHITE);

            String temp;
            for (int i = 1; i < result.length; i++) {
                temp = result[i];
                embed.addField(temp.substring(0, temp.indexOf(":")), StringTools.removeHtmlTags(temp.substring(temp.indexOf(":") + 1)), false);
            }
            event.getChannel().sendMessage(embed.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage("No search results for " + getArgsAfter(0, messageArgs, false) + ", maybe try searching for another title?").queue();
        }
    }

    private void info(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().split(" ");

        if (messageArgs.length < 3) {
            event.getChannel().sendMessage("Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "info [CPU/GPU] [query]`").queue();
            return;
        }

        ArrayList<SearchResult> results;
        EmbedBuilder embed = new EmbedBuilder();

        switch (messageArgs[1].toLowerCase()) {
            case "cpu":
                results = Searcher.searchSpecs("CPU", getArgsAfter(1, messageArgs, false));

                if(results.isEmpty()) {
                    //TODO produce no results message
                }

                Cpu cpu = results.get(0).getCpu();

                embed
                        .setTitle(cpu.getName() + " Performance Info", results.get(0).getLink())
                        .setDescription("Use `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs cpu " + cpu.getName() + "`\n to set this as your system CPU.")
                        .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                        .addField("Core Count", cpu.getCoreCount() + " cores", false)
                        .addField("Thread Count", cpu.getThreadCount() + " threads", false)
                        .addField("Frequency", cpu.getFreqInGHz() + " GHz", false)
                        .addField("Turbo Clock", cpu.getTurboClock() + " GHz", false);

                break;
            case "gpu":
                results = Searcher.searchSpecs("GPU", getArgsAfter(1, messageArgs, false));

                if(results.isEmpty()) {
                    //TODO produce no results message
                }

                Gpu gpu = results.get(0).getGpu();

                embed
                        .setTitle(gpu.getName() + " Performance Info", results.get(0).getLink())
                        .setDescription("Use `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs gpu " + gpu.getName() + "`\n to set this as your system GPU.")
                        .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                        .addField("Base clock speed", gpu.getBaseClock() + " MHz", false)
                        .addField("Boosted clock speed", gpu.getBoostClock() + " MHz", false)
                        .addField("Memory clock speed", gpu.getMemClock() + " MHz", false)
                        .addField("DirectX version", "DirectX " + gpu.getDxVersion(), false);
                break;
            case "ram":
                event.getChannel().sendMessage("It's ram.").queue();
                event.getChannel().sendMessage("I don't know what else I need to say.").queue();
                return;
            default:
                gameinfo(event);
                return;
        }

        event.getChannel().sendMessage(embed.build()).queue();

    }

    private void help(GuildMessageReceivedEvent event) {
        String prefix = Recordkeeper.getGuildPrefix(event.getGuild().getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(DiscordBot.getJda().getSelfUser().getName() + " commands")
                .setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl())
                .addField("@__" + DiscordBot.getJda().getSelfUser().getName() + "__ [game]", "Ask me if you can play a game!", false)
                .addField(prefix + "help", "Displays this dialog box.", false)
                .addField(prefix + "search [query]", "Runs a search for any PC games.", false)
                .addField(prefix + "search [CPU/GPU] [query]", "Runs a search for any specified hardware.", false)
                .addField(prefix + "info [CPU/GPU] [query]", "Returns performance information on the given hardware.", false)
                .addField(prefix + "setspecs [GPU/CPU/RAM] [value]", "Allows users to enter their system specifications.", false)
                .addField(prefix + "myspecs", "Displays *your* system specifications.", false)
                .addField(prefix + "resetspecs [CPU/GPU/RAM]", "Resets your system specs if specified.", false)
                .addField(prefix + "getspecs [@user]", "Displays *another user's* system specifications (only if they disable user privacy).", false)
                .addField(prefix + "setprivacy [ON/OFF/TRUE/FALSE]", "Determines whether or not other users can view your system specifications. (On/True) will leave your hardware private.", false)
                .addField(prefix + "compare [@user]", "Compares your PC specs against another user's PC.", false)
                .addField(prefix + "gameinfo [game]", "Displays details on a given title.", false)
                .addField(prefix + "gamespecs [game]", "Displays system requirements for a given title.\n**Use " + DiscordBot.getJda().getSelfUser().getAsMention() + " `[game]` to see if you can play it!**", false)
                .addField(prefix + "feedback [text]", "Allows users to write feedback on this bot to an external text channel. Note: Your username and message will be recorded!", false)
                .addField(prefix + "ping", "Want to play a round of ping-pong?", false);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void resetspecs(GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().contains(" ")) {
            //if command has 1 or more arguments
            reset(event);
            return;
        }

        UserSpecs user = new UserSpecs(event.getAuthor().getId(), Cpu.getCpuDefault(), Gpu.getGpuDefault(), 0);
        String message;

        if (Recordkeeper.addUserSpecs(user)) {
            message = "Successfully reset all System specs.";
        } else
            message = "An error occurred. If you know what happened, please use " + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "feedback` to let us know what happened.";

        event.getChannel().sendMessage(message).queue();
    }

    private void reset(GuildMessageReceivedEvent event) {
        if(!event.getMessage().getContentRaw().trim().contains(" ")) {
            //if there are no args given, then do the general "resetspecs" command
            resetspecs(event);
            return;
        }

        String[] messageArgs = event.getMessage().getContentRaw().split(" ");
        String response;

        switch (messageArgs[1].toLowerCase()) {
            case "cpu":
            case "processor":
                Recordkeeper.getSpecsByUserId(event.getAuthor().getId()).setUserCpu(Cpu.getCpuDefault());
                response = "Successfully reset your PC's CPU";
                break;
            case "gpu":
            case "graphics":
                Recordkeeper.getSpecsByUserId(event.getAuthor().getId()).setUserGpu(Gpu.getGpuDefault());
                response = "Successfully reset your PC's GPU";
                break;
            case "ram":
                Recordkeeper.getSpecsByUserId(event.getAuthor().getId()).setUserRam(2);
                response = "Successfully reset your PC's RAM in GB (2GB by default)";
                break;
            case "desc":
            case "description":
                Recordkeeper.getSpecsByUserId(event.getAuthor().getId()).setPcDescription("null");
                response = "Successfully reset your PC's description field.";
                break;
            default:
                response = "Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "reset [CPU/GPU/RAM]`";
                break;
        }

        event.getChannel().sendMessage(response).queue();

    }

    private void myspecs(GuildMessageReceivedEvent event) {
        myspecs(event, event.getAuthor());
    }

    private void myspecs(GuildMessageReceivedEvent event, User user) {
        UserSpecs userSpecs = Recordkeeper.getSpecsByUserId(user.getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle((user.getId().equals("168376512272269313") ? "Kabrir" : user.getName()) + "'s PC specs")
                .setDescription("use `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs` to modify your PC specs.")
                .setThumbnail(user.getAvatarUrl())
                //.addField("Overall PC Score", "**" + userSpecs.getPcScore() + "** (" + getPcRank(userSpecs.getPcScore()) + ")", false)
                .setFooter("Privacy setting: " + (userSpecs.getPrivacy() ? "Private" : "Public"), null);

                if (userSpecs.getPcDescription() == null || !userSpecs.getPcDescription().equals("null")) {
                    embed.setDescription(userSpecs.getPcDescription());
                }

                if (userSpecs.getUserCpu().getName().equalsIgnoreCase("No Cpu")) {
                    embed.addField("CPU - Central Processing Unit", "No CPU", false);
                } else {
                    embed.addField("CPU - " + userSpecs.getUserCpu().getName(), userSpecs.getUserCpu().getCoreCount() + " cores @ "+ userSpecs.getUserCpu().getFreqInGHz() + " GHz", false);
                }
                if (userSpecs.getUserGpu().getName().equalsIgnoreCase("No Gpu")) {
                    embed.addField("GPU - Graphics Processing Unit", "No GPU", false);
                } else {
                    embed.addField("GPU - " + userSpecs.getUserGpu().getName(), "Base clock speed: " + userSpecs.getUserGpu().getBaseClock() + "MHz",false);
                }
                embed.addField("RAM - Random Access Memory", "**" + userSpecs.getUserRam() + "** Gigabytes", false);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void compare(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().trim().split(" ");
        User targetUser;

        if (messageArgs.length < 2) {
            event.getChannel().sendMessage("Please name a user to compare your PC specs against!").queue();
            event.getChannel().sendMessage("Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "compare [user]`").queue();
            return;
        }


        //declare the targetUser
        if (messageArgs[1].startsWith("<@!") && messageArgs[1].endsWith(">")) {
            targetUser = DiscordBot.getJda().getUserById(messageArgs[1].substring(3, messageArgs[1].indexOf(">")));
        } else {
            ArrayList<User> userList = new ArrayList<>(DiscordBot.getJda().getUsersByName(messageArgs[1], true));
            if (userList.isEmpty()) {
                event.getChannel().sendMessage("No users named `" + messageArgs[1] + "` were found on this server, try @mentioning them.").queue();
                return;
            } else {
                targetUser = userList.get(0);
            }
        }

        UserSpecs targetSpecs = Recordkeeper.getSpecsByUserId(targetUser.getId());
        UserSpecs authorSpecs = Recordkeeper.getSpecsByUserId(event.getAuthor().getId());

        //order is CPU, GPU, RAM
        Boolean[] betterSpecs = authorSpecs.isBetterThan(targetSpecs);

        int sumOfSpecs = 0; //positive means authors specs are better, negative means target specs are better
        for(int i = 0; i < betterSpecs.length - 1; i++) { //checks all values except for the last
            if(betterSpecs[i] == null) {
                //do nothing with sumOfSpecs
            }
            else if(betterSpecs[i])
                sumOfSpecs++;
            else
                sumOfSpecs--;
        }
        if(authorSpecs.getUserRam() != targetSpecs.getUserRam()) {
            sumOfSpecs += (betterSpecs[2] ? 1 : -1);
        }


        EmbedBuilder embed = new EmbedBuilder();
        if(sumOfSpecs == 0) {
            embed.setTitle("Your specs are **tied**!");
            embed.setThumbnail(DiscordBot.getJda().getSelfUser().getAvatarUrl());
        } else {
            String winner;
            if(sumOfSpecs > 0) {
                winner = event.getAuthor().getName();
                embed.setThumbnail(event.getAuthor().getAvatarUrl());
            } else {
                winner = targetUser.getName();
                embed.setThumbnail(targetUser.getAvatarUrl());
            }

            embed.setTitle("**" + winner + "'s** specs are better!");
        }
        embed.setDescription("Here's the breakdown...");

        String[] targetSpecNames = {targetSpecs.getUserCpu().getName(), targetSpecs.getUserGpu().getName(), targetSpecs.getUserRam() + " GB"};
        if (targetSpecs.getPrivacy()) {
            targetSpecNames[0] = "CPU";
            targetSpecNames[1] = "GPU";
            targetSpecNames[2] = "RAM";
            embed.setFooter(targetUser.getName() + "'s specs are private.");
        }

        String[] authorSpecNames = {authorSpecs.getUserCpu().getName(), authorSpecs.getUserGpu().getName()};
        if(authorSpecNames[0].equalsIgnoreCase("No Cpu")) {
            authorSpecNames[0] = "CPU";
        }
        if(authorSpecNames[1].equalsIgnoreCase("No Gpu")) {
            authorSpecNames[1] = "GPU";
        }

        String[] embedFields = new String[3];
        //entering field for CPU
        if(betterSpecs[0] == null) {
            embedFields[0] = "Both CPUs are tied!";
        } else if(betterSpecs[0]) {
            embedFields[0] = "*" + event.getMessage().getAuthor().getName() + "'s " + authorSpecNames[0] + "* beats " + targetUser.getName() + "'s " + targetSpecNames[0];
        } else {
            embedFields[0] = "*" + targetUser.getName() + "'s " + targetSpecNames[0] + "* beats " + event.getMessage().getAuthor().getName() + "'s " + authorSpecNames[0];
        }
        //entering field for GPU
        if(betterSpecs[1] == null) {
            embedFields[1] = "Both GPUs are tied!";
        } else if(betterSpecs[1]) {
            embedFields[1] = "*" + event.getMessage().getAuthor().getName() + "'s " + authorSpecNames[1] + "* beats " + targetUser.getName() + "'s " + targetSpecNames[1];
        } else {
            embedFields[1] = "*" + targetUser.getName() + "'s " + targetSpecNames[1] + "* beats " + event.getMessage().getAuthor().getName() + "'s " + authorSpecNames[1];
        }
        //entering field for RAM
        if(authorSpecs.getUserRam() == targetSpecs.getUserRam()) {
            embedFields[2] = "Both users have the same amount of RAM!";
        } else {
            if(betterSpecs[2]) {
                embedFields[2] = "*" + event.getAuthor().getName() + "* has more RAM than " + targetUser.getName();
            } else {
                embedFields[2] = "*" + targetUser.getName() + "* has more RAM than " + event.getAuthor().getName();
            }
        }

        String[] titles = {"CPU", "GPU", "RAM"};
        for (int i = 0; i < betterSpecs.length; i++) {
            embed.addField(titles[i] + " comparison", embedFields[i], false);
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private void setspecs(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().trim().split(" ");
        String message;
        UserSpecs user = Recordkeeper.getSpecsByUserId(event.getAuthor().getId());

        if (messageArgs.length < 3) {
            event.getChannel().sendMessage("Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs [CPU/GPU/RAM] [VALUE]`").queue();
            return;
        }

        switch (messageArgs[1].toLowerCase()) {
            case "cpu":
                ArrayList<SearchResult> cpuResults = Searcher.searchSpecs("CPU", getArgsAfter(1, messageArgs, false));

                if (cpuResults.isEmpty()) {
                    message = "Sorry, there were no CPU search results for " + getArgsAfter(1, messageArgs, false);
                } else {
                    user.setUserCpu(cpuResults.get(0).getCpu());
                    Recordkeeper.addUserSpecs(user);
                    message = "Successfully set your CPU to **" + user.getUserCpu().getName() + "**";
                }

                break;
            case "gpu":
                ArrayList<SearchResult> gpuResults = Searcher.searchSpecs("GPU", getArgsAfter(1, messageArgs, false));

                if (gpuResults.isEmpty()) {
                    message = "Sorry, there were no GPU search results for " + getArgsAfter(1, messageArgs, false);
                } else {
                    user.setUserGpu(gpuResults.get(0).getGpu());
                    Recordkeeper.addUserSpecs(user);
                    message = "Successfully set your GPU to **" + gpuResults.get(0).getGpu().getName() + "**";
                }

                break;
            case "ram":
                int ram;

                try {
                    ram = Integer.parseInt(messageArgs[2].trim());
                    if (ram < 2) {
                        ram = 2;
                    }

                    user.setUserRam(ram);
                    Recordkeeper.addUserSpecs(user);
                    message = "Successfully set your RAM to **" + user.getUserRam() + "** GB";
                } catch (Exception e) {
                    message = "Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs RAM [INTEGER]`";
                }
                break;
            case "description":
                String description = getArgsAfter(1, messageArgs, false);

                if (description.contains("@")) {
                    message = "Sorry, your PC description cannot contain any mentions!";
                    break;
                }

                user.setPcDescription(getArgsAfter(1, messageArgs, false));
                Recordkeeper.addUserSpecs(user);

                message = "Your PC's description has been updated to \"" + user.getPcDescription() + "\"";
                break;
            case "privacy":
                setprivacy(event);
                return;
            default:
                message = "Usage: `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "setspecs [CPU/GPU/RAM] [VALUE]`";
                break;
        }

        event.getChannel().sendMessage(message).queue();

    }

    private void getspecs(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().trim().split(" ");
        User targetUser;

        if (messageArgs.length < 2) {
            myspecs(event);
            return;
        }


        //declare the targetUser
        if (messageArgs[1].startsWith("<@!") && messageArgs[1].endsWith(">")) {
            targetUser = DiscordBot.getJda().getUserById(messageArgs[1].substring(3, messageArgs[1].indexOf(">")));
        } else {
            ArrayList<User> userList = new ArrayList<>(DiscordBot.getJda().getUsersByName(messageArgs[1], true));
            if (userList.isEmpty()) {
                event.getChannel().sendMessage("No users named `" + messageArgs[1] + "` were found on this server, try @mentioning them.").queue();
                return;
            } else {
                targetUser = userList.get(0);
            }
        }

        UserSpecs targetSpecs = Recordkeeper.getSpecsByUserId(targetUser.getId());
        if (targetSpecs.getPrivacy()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle((targetUser.getId().equals("168376512272269313") ? "Kabrir" : targetUser.getName()) + "'s PC specs")
                    .setDescription((targetUser.getId().equals("168376512272269313") ? "Kabrir" : targetUser.getName()) + " has set their privacy settings to private.\nAsk them to send `~myspecs` *or* have them set their\nprivacy to public.")
                    .setThumbnail(targetUser.getAvatarUrl())
                    //.addField("Overall PC Score", "**" + targetSpecs.getPcScore() + "** (" + getPcRank(targetSpecs.getPcScore()) + ")", false)
                    .setFooter("Privacy setting: " + (targetSpecs.getPrivacy() ? "Private" : "Public"), null);

            if (!targetSpecs.getPcDescription().equals("null")) {
                embed.setDescription(targetSpecs.getPcDescription());
              }

            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }

        myspecs(event, targetUser);
    }

    private void setprivacy(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().trim().split(" ");
        UserSpecs user = Recordkeeper.getSpecsByUserId(event.getAuthor().getId());
        String prefix = Recordkeeper.getGuildPrefix(event.getGuild().getId());
        String affirm = "on|true|private", deny = "off|false|public";

        if(messageArgs[1].equalsIgnoreCase("privacy") && messageArgs.length > 2)
            messageArgs[1] = messageArgs[2];

        if (affirm.contains(messageArgs[1].toLowerCase().trim())) {
            //if the argument is found in the string "affirm", the argument is affirmative
            user.setPrivacy(true);
        } else if (deny.contains(messageArgs[1].toLowerCase().trim())) {
            //if the argument is found in the string "deny", the argument is denial
            user.setPrivacy(false);
        } else {
            //if the argument is not found in either, it is unrecognized
            event.getChannel().sendMessage("Unrecognized argument. Please use `" + prefix + "setprivacy public` or `" + prefix + "setprivacy private`.").queue();
            return;
        }

        event.getChannel().sendMessage(event.getAuthor().getAsMention() + "'s privacy setting was updated to `" + (Recordkeeper.getSpecsByUserId(event.getAuthor().getId()).getPrivacy() ? "private" : "public") + "`.").queue();
        Recordkeeper.addUserSpecs(user);
    }

    private void setprefix(GuildMessageReceivedEvent event) {

        //TODO find a way to specify ADMINS ONLY
        if (!authorHasAdminPrivileges(event)) {
            event.getChannel().sendMessage("Sorry, you must have server management permissions to change the prefix!").queue();
            return;
        }
        //*
        String prefix = event.getMessage().getContentRaw();
        prefix = prefix.substring(prefix.indexOf("setprefix") + 9).trim();

        if (prefix.contains("@")) {
            event.getChannel().sendMessage("Sorry, prefixes can't include mentions!").queue();
            return;
        }

        Recordkeeper.setPrefix(event.getGuild().getId(), prefix);

        boolean success = Recordkeeper.getGuildPrefix(event.getGuild().getId()).equals(prefix);

        event.getChannel().sendMessage(success ? "Successfully set " + event.getGuild().getName() + "'s prefix to " + prefix : "An error occured, use `" + Recordkeeper.getGuildPrefix(event.getGuild().getId()) + "feedback` to tell us what happened.").queue(); //*/
    }

    private void feedback(GuildMessageReceivedEvent event) {
        String[] messageArgs = event.getMessage().getContentRaw().split(" ");

        sendToConsole("New feedback, check the #caniplay-feedback channel.");
        DiscordBot.getJda().getTextChannelById(feedbackChannelId).sendMessage(event.getAuthor().getAsTag() + "'s feedback: ```" + getArgsAfter(0, messageArgs, false) + "```").queue();

        event.getChannel().sendMessage("Thank you! Your feedback has been recorded.").queue();
        event.getChannel().sendMessage("If you're reporting a bug, make sure you mention the word \"bug\" in your response, so it can be filed separately.").queue();
    }


    /*****OTHER METHODS *****/

    private String getArgsAfter(int n, String[] array, boolean commas) {
        StringBuilder output = new StringBuilder();

        for (int i = n + 1; i < array.length; i++) {
            output.append(array[i]).append(commas ? ", " : " ");
        }

        return output.toString();
    }

    public void sendToConsole(String message) {
        if (message.contains("<@!") && message.contains(">")) {
            String userId;
            while (message.contains("<@!") && message.contains(">")) {
                userId = message.substring(message.indexOf("<@!") + 3, message.indexOf(">", message.indexOf("<@!")));
                message = message.substring(0, message.indexOf("<@!")) + "@" + DiscordBot.getJda().getUserById(userId).getAsTag() + message.substring(message.indexOf(">", message.indexOf("<@!")) + 1);
            }
        }

        DiscordBot.getJda().getTextChannelById(consoleChannelId).sendMessage("**__" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) + "__:** " + message)
                .queue();
    }

    /*
    private String getPcRank(int num) {
        String pcRank;

        if (num < 2500) {
            pcRank = "POOR";
        } else if (num < 5000) {
            pcRank = "OKAY";
        } else if (num < 10000) {
            pcRank = "GOOD";
        } else if (num < 15000) {
            pcRank = "SOLID";
        } else if (num > 15000) {
            pcRank = "*OVERKILL*";
        } else
            pcRank = "UNKNOWN";

        return pcRank;
    } //*/

    private void canUserPlay(GuildMessageReceivedEvent event, String message) {
        long deltaTime = System.currentTimeMillis();
        event.getChannel().sendMessage("`THIS FEATURE IS STILL IN BETA. PLEASE ALLOW ~5 SECONDS FOR A RESPONSE, SOME REPORTED GAME SPECS PRESENTED MAYBE INCORRECT.`").queue();

        SteamGame game = new SteamGame(Searcher.getSearchResult(message));
        if (DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - Commands] Can User Play: " + game.getTitle());
        }
        UserSpecs user = Recordkeeper.getSpecsByUserId(event.getAuthor().getId()); //gets the user's specs from the database
        boolean[] specsMeetReqs = compareSpecs(game, user); //returns which of the users specs meet the requirements to play the game
        boolean temp = true;
        for (boolean bool : specsMeetReqs) {
            if (!bool) {
                temp = false;
                break;
            }
        } //if all of specsMeetReqs = true, temp = true

        EmbedBuilder embed = new EmbedBuilder()
                .setImage(game.getImageUrl())
                .setColor((temp ? Color.GREEN : Color.RED))
                .setTitle((temp ? "Yes, you *can* play " + game.getTitle().trim() + "!" : "No, you *can't* play " + game.getTitle()), game.getWebsite())
                .setDescription("because...")
                .addField("CPU - Central Processing Unit", "Your CPU " + (specsMeetReqs[CPU_INDEX] ? "meets" : "**does not** meet") + " the minimum requirement **(__" + user.getUserCpu().getName() + "__ vs. __" + game.getCpu().getName() + "__)**", false)
                .addField("GPU - Graphics Processing Unit", "Your GPU " + (specsMeetReqs[GPU_INDEX] ? "meets" : "**does not** meet") + " the minimum requirement **(__" + user.getUserGpu().getName() + "__ vs. __" + game.getGpu().getName() + "__)**", false)
                .addField("RAM - Random Access Memory", "Your RAM " + (specsMeetReqs[RAM_INDEX] ? "meets" : "**does not** meet") + " the minimum requirement (**" + user.getUserRam() + "** GB vs. **" + (game.getRamInGb() == -1 ? "<1" : game.getRamInGb()) + "** GB)", false);

        if (DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - Commands] Message sent: " + (System.currentTimeMillis() - deltaTime) + " MS");
        }
        event.getChannel().sendMessage(embed.build()).queue();
    }

    private boolean authorHasAdminPrivileges(GuildMessageReceivedEvent event) {
        //if the author is the guild owner, then he obviously has admin privilages
        if (event.getGuild().getOwnerId().equals(event.getAuthor().getId()))
            return true;

        return event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER);

        /*
        ArrayList<Role> guildRoles = new ArrayList<Role>(); //an arrayList containing all roles in the guild
        guildRoles.addAll(event.getGuild().getRoles());

        //a loop to cycle through all the roles in guildRoles
        for (Role role : guildRoles) {
            if (!role.getPermissions().contains(Permission.MANAGE_SERVER)) //if a role doesn't have permissions to manage the server, skip it
                break;

            //get a list of all members who have that role on the server
            ArrayList<Member> memberList = new ArrayList<>();
            memberList.addAll(event.getGuild().getMembersWithRoles(role));

            //cycle through memberList to see if it contains the author of the event
            for(Member member : memberList) {
                if (member.getUser().getId().equals(event.getAuthor().getId())) { return true; }
            }

        }

        //if the code reaches this point, the author doesn't have admin privilages
        return false; //*/
    }

    private boolean[] compareSpecs(SteamGame gameInfo, UserSpecs user) {
        boolean[] output = new boolean[3];

        output[CPU_INDEX] = user.getUserCpu().isBetterThan(gameInfo.getCpu());
        output[GPU_INDEX] = user.getUserGpu().isBetterThan(gameInfo.getGpu());
        output[RAM_INDEX] = user.getUserRam() >= gameInfo.getRamInGb();

        //System.out.println(output[0] + ", " + output[1] + ", " + output[2]);

        return output;
    }
}

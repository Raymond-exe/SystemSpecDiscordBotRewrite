package me.raymond.webaccess;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import me.raymond.discordbot.DiscordBot;

import java.util.ArrayList;
import java.util.Arrays;

public class Searcher {

    private static String gamesSearchURL = "https://store.steampowered.com/"; //search?q=
    private static String steamURL = "https://store.steampowered.com/"; //search?q=
    private static String specSearchURL = "https://www.google.com/search?&q=";
    private static String gpuSearch = "+%2Bsite%3Atechpowerup.com%2Fgpu-specs%2F";
    private static String cpuSearch = "+%2Bsite%3Atechpowerup.com%2Fcpu-specs%2F";

    //Used for searching for both GPUs and CPUs
    public static ArrayList<SearchResult> searchSpecs(String spec, String query) {
        ArrayList<SearchResult> output = new ArrayList<>();
        String searchModifier, attributeValue;
        Document doc;
        if (spec.equalsIgnoreCase("cpu")) {
            searchModifier = cpuSearch;
            attributeValue = "https://www.techpowerup.com/cpu-specs/";
        } else if (spec.equalsIgnoreCase("gpu")) {
            attributeValue = "https://www.techpowerup.com/gpu-specs/";
            searchModifier = gpuSearch;
        } else {
            return null;
        }

        query = StringTools.cleanString(query.trim());

        //replaces all spaces with "+"
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == ' ')
                query = query.substring(0, i) + "%20" + query.substring(i + 1);
        }

        doc = WebFetch.fetch(specSearchURL + query + searchModifier);

        if (doc.outerHtml().contains("It looks like there aren't any great matches for your search</div>")) {
            System.out.println("No results for " + query + " found.");
            return new ArrayList<>();
        }
        //System.out.println("Search entries found!");

        Elements titleElements = doc.getElementsByTag("h3");
        Elements linkElements = doc.getElementsByAttributeValueContaining("href", attributeValue);

        String title, link;
        for (int i = 0; i < titleElements.size() && i < linkElements.size(); i++) {
            title = titleElements.get(i).text();

            try {
                title = title.substring(0, title.indexOf("Specs")).trim();
                link = linkElements.get(i).attr("href");
                link = link.substring(link.indexOf("https://"), link.indexOf("&", link.indexOf("https://")));
                output.add(new SearchResult(title, link));
            } catch (Exception e) {
                link = linkElements.get(i).attr("href");
                link = link.substring(link.indexOf("https://"), link.indexOf("&", link.indexOf("https://")));
                System.out.println("[DEBUG - Searcher] Failed to add the following link: " + link);
            }
        }

        //searches for and removes duplicates
        for (int i = 0; i < output.size(); i++) {
            for (int j = i; j < output.size(); j++) {
                if (i != j && output.get(i).getLink().equals(output.get(j).getLink())) {
                    output.remove(j);
                    j--;
                }
            }
        }

        return output;
    }

    public static ArrayList<SearchResult> looseSearch(String spec, String query) {

        //fixing "spec" str
        //will return null if spec is not recognized
        switch (spec.toLowerCase()) {
            case "cpu":
            case "gpu":
                break;
            case "processor":
                spec = "cpu";
                break;
            case "graphic":
            case "graphics":
            case "graphics card":
                spec = "gpu";
                break;
            default:
                System.out.println("Rejected argument: \"" + spec + "\" was not recognized as cpu/gpu.");
                return null;
        }

        ArrayList<String> queryArgs = new ArrayList<>(Arrays.asList(query.split(" "))); //cpuToParse as a ArrayList, seperated by spaces

        String[] cpuIndicators = {"i3", "i5", "i7", "i9", "ryzen", "core", "pentium", "celeron", "fx", "intel", "amd"}; //common brands or GPU types to be used as markers for where a search term begins
        String[] gpuIndicators = {"gtx", "rtx", "rx", "vega", "geforce", "radeon"}; //common brands or GPU types to be used as markers for where a search term begins

        String searchTerm = ""; //the final phrase used for search
        int index = -1; //the index of the start of the searchTerm found in cpuArgs

        //checks to see if query contains any matches with indicators
        if(spec.equalsIgnoreCase("cpu")) {
            for(String cpu : cpuIndicators) {
                if(queryArgs.contains(cpu)) {
                    index = queryArgs.indexOf(cpu);
                    break;
                }
            }
        } else if(spec.equalsIgnoreCase("gpu")) {
            for(String gpu : gpuIndicators) {
                if(queryArgs.contains(gpu)) {
                    index = queryArgs.indexOf(gpu);
                    break;
                }
            }
        }

        //if index was never changed, then cpuArgs DID NOT contain any matches with cpuIndicators
        if (index == -1 && DiscordBot.debugPrintouts) {
            System.out.println("[DEBUG - Searcher] No indicators found in " + query);
            return new ArrayList<>(); //returns an empty arraylist
        }

        //extends searchTerm to the next 4 entries in cpuArgs, or until end of cpuArgs
        for(int i = 0; i < 4; i++) {
            if(index+i < queryArgs.size()) {
                searchTerm += (i == 0 ? "" : " ") + queryArgs.get(index+i);
            }
        }

        searchTerm += " ";

        while(searchTerm.contains(" ")) {
            searchTerm = searchTerm.substring(0, searchTerm.lastIndexOf(" "));
            ArrayList<SearchResult> searchResults = searchSpecs(spec, searchTerm);

            //if no results found, continue
            if(searchResults == null || searchResults.isEmpty()) {
                continue;
            }

            return searchResults;
        }

        System.out.println("Could not find any GPUs for " + query);
        return new ArrayList<>(); //returns an empty arraylist

    }

    public static String getGameSiteLink(String query, String site) {
        query = query.trim();

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == ' ')
                query = query.substring(0, i) + "+" + query.substring(i + 1);
        }

        switch (site.toLowerCase()) {
            case "steam":
                return steamURL + "search?q=" + query;
            default:
                return gamesSearchURL + "search?q=" + query;
        }
    }

    //Used for games search
    public static ArrayList<String> searchFor(String originalQuery) {
        ArrayList<String> output = new ArrayList<>();
        String query = originalQuery.trim();
        Document doc;

        //replaces all spaces with '+'
        while(query.contains(" ")) {
            query = query.substring(0, query.indexOf(" ")) + "+" + query.substring(query.indexOf(" ")).trim();
        }

        try {
            doc = WebFetch.fetch(steamURL + "search/?term=" + query + "&category1=998");
        } catch (Exception ex) {
            System.out.println("Unable to connect to " + steamURL + "search/?term=" + query + "&category1=998");
            ex.printStackTrace();
            return null;
        }

        String title, link;
        for (Element element : doc.getElementsByAttributeValueContaining("class", "search_result_row ds_collapse_flag")) {
            title = element.select("span.title").first().text();
            link = element.attr("href");
            output.add(title + "[!(" + link + ")!]");
        }

        return output;
    }

    public static String getSearchResult(String query, int resultNum) {
        ArrayList<String> searchResults = searchFor(query);

        if (resultNum > searchResults.size())
            resultNum = searchResults.size();

        String requestedResult = searchResults.get(resultNum - 1);
        return requestedResult.substring(requestedResult.lastIndexOf("(") + 1, requestedResult.lastIndexOf(")"));
    }

    public static String getSearchResult(String query) {
        return getSearchResult(query, 1);
    }

    public static void main(String[] args) {

        searchFor("Halo Master Chief Collection");
    }

}

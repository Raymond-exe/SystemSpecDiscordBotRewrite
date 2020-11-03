package me.raymond.webaccess;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import me.raymond.pcparts.Cpu;
import me.raymond.pcparts.Gpu;

import java.util.ArrayList;
import java.util.HashMap;

//A re-write of the GameInfo class to work with the Steam platform
public class SteamGame {

    private String website;
    private Document doc;

    public SteamGame(String site) {
        website = site;
        doc = WebFetch.fetch(site);
    }

    public String getWebsite() { return website; }

    public String getHtml() { return  doc.outerHtml(); }

    public String getTitle() {
        String title;

        title = doc.select("div.apphub_AppName").text();
        title = StringTools.cleanString(title);

        return title;
    }

    public ArrayList<String> getInfo() {
        ArrayList<String> output = new ArrayList<>();

        Element detailsBlock = doc.select("div.details_block").first();

        //System.out.println(detailsBlock.text());

        for(Element element : detailsBlock.getElementsByTag("b")) {
            output.add(element.text());
        }

        output.set(0, output.get(0) + " " + getTitle()); //sets title

        ArrayList<String> hrefElements = new ArrayList<>();
        for(Element element : detailsBlock.getElementsByTag("a")) {
            if(element.text().equalsIgnoreCase("Title: ")) {
                hrefElements.add(element.text() + getTitle());
            } else {
                hrefElements.add("[" + element.text() + "](" + element.attr("href") + ")");
            }
        }

        //sorts and copies text from hrefElements to output
        for (String hyperlink : hrefElements) {
            if(hyperlink.contains("/genre/")) {
                output.set(1, output.get(1) + " " + hyperlink);
            } else if (hyperlink.contains("/developer/")) {
                output.set(2, output.get(2) + " " + hyperlink);
            } else if (hyperlink.contains("/publisher/")) {
                output.set(3, output.get(3) + " " + hyperlink);
            } else if (hyperlink.contains("/franchise/")) {
                output.set(4, output.get(4) + " " + hyperlink);
            }
        }

        String html = getHtml();
        String releaseDate = html.substring(html.indexOf("<b>Release Date:</b>") + 20, html.indexOf("<br>", html.indexOf("<b>Release Date:</b>"))).trim();
        output.set(output.size() - 1, output.get(output.size() - 1) + " " + releaseDate);

        return output;
    }

    public String getImageUrl() {
        String output;

        output = doc.select("img.game_header_image_full").attr("src");

        return output;
    }

    //**********SPECS**********\\


    public ArrayList<String> getSpecs (int requirements) throws NullPointerException {
        HashMap<String, String> specMap = getSpecMap(requirements);
        ArrayList<String> output = new ArrayList<>();

        output.add(specMap.get("CPU")); //CPU
        output.add(specMap.get("GPU")); //GPU
        output.add(specMap.get("RAM")); //RAM
        output.add(specMap.get("OS")); //OS
        output.add(specMap.get("Storage")); //Storage

        return output;
    }

    public HashMap<String, String> getSpecMap (int requirements) throws NullPointerException {
        HashMap<String, String> output = new HashMap<>();

        //Changes the spec table used based on "requirements" int
        String divider = "";
        if(requirements == GameInfo.MIN_SYS_REQS) {
            divider = "div.game_area_sys_req_leftCol"; //min specs
        } else if (requirements == GameInfo.REC_SYS_REQS) {
            divider = "div.game_area_sys_req_rightCol"; //rec specs
        }

        if(doc.select(divider).first() == null) {
            divider = "div.game_area_sys_req.sysreq_content.active";
        }

        String detailsStr = doc.select(divider).first().toString();
        String[] detailsArray = detailsStr.split("\n");
        String hashMapKey;
        for(int i = 0; i < detailsArray.length; i++) {
            hashMapKey = "";
            detailsArray[i] = StringTools.removeHtmlTags(detailsArray[i]).trim();
            //System.out.println(detailsArray[i]);

            if(detailsArray[i].toLowerCase().startsWith("processor")) {
                hashMapKey = "CPU";
            } else if(detailsArray[i].toLowerCase().startsWith("graphics")) {
                hashMapKey = "GPU";
            } else if(detailsArray[i].toLowerCase().startsWith("memory")) {
                hashMapKey = "RAM";
            } else if(detailsArray[i].toLowerCase().startsWith("os")) {
                hashMapKey = "OS";
            } else if(detailsArray[i].toLowerCase().startsWith("storage")
            || detailsArray[i].toLowerCase().startsWith("hard drive")) {
                hashMapKey = "Storage";
            }

            if(!hashMapKey.isEmpty()) {
                output.put(hashMapKey, detailsArray[i].substring(detailsArray[i].indexOf(" ")).trim());
            }

        }

        return output;
    }

    public int getRamInGb(int requirements) {
        String ramToParse = getSpecMap(requirements).get("RAM");

        if(!ramToParse.contains("GB")
                && !ramToParse.contains("gb")
                && !ramToParse.contains("Gigabyte")
                && !ramToParse.contains("gigabyte")) {
            return -1; //less than 1 GB of ram
        }

        int ramToOutput = -1;
        for(String word : ramToParse.split(" ")) {
            try {
                if(word.contains("GB")) {
                    word = word.substring(0, word.indexOf("GB"));
                }
                ramToOutput = Integer.parseInt(word.trim());
                break;
            } catch (Exception ex) {
                System.out.println("Failed to parse " + word + " to Integer.");
            }
        }

        return ramToOutput;

    }

    public int getRamInGb() {
        return getRamInGb(0);
    }

    public Gpu getGpu(int requirements) {
        String gpuSearchTerm = getSpecMap(requirements).get("GPU").toLowerCase(); //grabs the string that will be used to search for a GPU

        ArrayList<SearchResult> gpusFound = Searcher.looseSearch("gpu", gpuSearchTerm);

        if(gpusFound.size() > 0)
            return gpusFound.get(0).getGpu();
        else
            return null;
    }

    public Gpu getGpu() {
        return getGpu(0);
    }

    public Cpu getCpu(int requirements) {
        String cpuSearchTerm = getSpecMap(requirements).get("CPU").toLowerCase(); //grabs the string that will be used to search for a CPU

        ArrayList<SearchResult> cpusFound = Searcher.looseSearch("CPU", cpuSearchTerm);

        if(cpusFound.size() > 0)
            return cpusFound.get(0).getCpu();
        else
            return null;
    }

    public Cpu getCpu() {
        return getCpu(0);
    }



    public static void main(String[] args) {
        SteamGame game = new SteamGame("https://store.steampowered.com/app/271590/Grand_Theft_Auto_V/");

        System.out.println(game.getSpecMap(0));
    }

}

package me.raymond.webaccess;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import me.raymond.pcparts.Cpu;
import me.raymond.pcparts.Gpu;

import java.util.ArrayList;

//NOTE: Deprecated, may be re-implemented in the future
public class GameInfo {

    public static final int MIN_SYS_REQS = 0, REC_SYS_REQS = 1;
    private String website, html;
    private Document doc;

    public GameInfo(String site) {
        website = site;
        doc = WebFetch.fetch(site);
        html = doc.outerHtml();
    }

    public String getWebsite() {
        return website;
    }

    public String getTitle() {
        String title;

        title = html.substring(html.indexOf("<title>") + 7, html.indexOf("System Requirements"));

        title = StringTools.fixString(title);

        return title;
    }

    public ArrayList<String> getInfo() {
        ArrayList<String> output = new ArrayList<>();

        output.add(doc.getElementsByClass("game_head_title").text().trim()); //adds title to output ArrayList

        for(Element element : doc.getElementsByClass("game_head_details_row")) {
            output.add(element.text().trim());
        }

        return output;
    }

    public String getImageUrl() {
        String output;

        output = html.substring(html.indexOf("<img src=", html.indexOf("game_head_cover")) + 10, html.indexOf("alt=", html.indexOf("game_head_cover")) - 2);

        return output;
    }

    /**********SPECS**********/

    public ArrayList<String> getSpecs(int requirements) {
        String[] headers = {"CPU:", "RAM:", "GPU:", "OS:", "Store:"};
        ArrayList<String> output = new ArrayList<>();
        String tempInfo;
        int startIndex, endIndex, lastIndex = 0;

        //*
        //Returns minimum system requirements
        if (requirements == MIN_SYS_REQS) {
            //for loop parsing through html to find CPU, RAM, GPU, OS, Storage, and Network requirements
            for (int i = 0; i < headers.length + (html.contains("<b>Store:</b>") ? 0 : -1); i++) {
                startIndex = html.indexOf("table-cell\">", html.indexOf("<b>" + headers[i] + "</b>", lastIndex) + 12) + 17;
                endIndex = html.indexOf("<", startIndex + 15);
                lastIndex = endIndex;
                tempInfo = html.substring(startIndex, endIndex);
                output.add(tempInfo.substring(tempInfo.indexOf(">") + 1).trim());
            }


        } else if (requirements == REC_SYS_REQS) {


        } else {
            return null;
        } //*/

        return output;
    }

    public int getRamInGb(int requirements) {
        String str = getSpecs(requirements).get(1).toLowerCase();

        if (!str.contains("gb") && !str.contains("gigabyte")) {
            return -1;
        }

        int endPoint = 0;
        if (str.contains("gb") && str.contains("gigabyte")) {
            endPoint = Math.min(str.indexOf("gb"), str.indexOf("gigabyte"));
        } else if (str.contains("gb")) {
            endPoint = str.indexOf("gb");
        } else if (str.contains("gigabyte")) {
            endPoint = str.indexOf("gigabyte");
        }

        str = str.substring(0, endPoint).trim();

        if (str.lastIndexOf(" ") != -1)
            str = str.substring(str.lastIndexOf(" ")).trim();
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }

    public int getRamInGb() {
        return getRamInGb(0);
    }

    public Gpu getGpu(int requirements) {
        String gpu = StringTools.cleanString(getSpecs(requirements).get(2).trim().toLowerCase());
        //ArrayList<Gpu> searchResults;

        //check for any references to directX
        /*
        if(gpu.contains("directx")) {
            int dxVersion = 0;
            if (gpu.contains("directx 9")) {
                dxVersion = 9;
            } else if (gpu.contains("directx 10")) {
                dxVersion = 10;
            } else if (gpu.contains("directx 11")) {
                dxVersion = 11;
            } else if (gpu.contains("directx 12")) {
                dxVersion = 12;
            }

            return new Gpu("DirectX version", dxVersion);
        } //*/

        if (gpu.contains("gtx") && gpu.indexOf(" ", gpu.indexOf("gtx")) != -1) {
            gpu = gpu.substring(gpu.indexOf("gtx"), gpu.indexOf(" ", gpu.indexOf("gtx") + 4));
        } else if (gpu.contains("rtx")) {
            gpu = gpu.substring(gpu.indexOf("rtx"), gpu.indexOf(" ", gpu.indexOf("rtx") + 4));
        } else if (gpu.contains("radeon") && gpu.indexOf(" ", gpu.indexOf("radeon")) != -1) {
            System.out.println(gpu);
            gpu = gpu.substring(gpu.indexOf("radeon"), gpu.indexOf(" ", gpu.indexOf("radeon") + 7));
        }

        //System.out.println("\n gameInfo background: " + gpu);

        gpu = " " + gpu;
        /*
        while (!gpu.isEmpty() && gpu.contains(" ")) {
            searchResults = Searcher.searchGpu(gpu, 1);

            if (searchResults.isEmpty()) {
                gpu = gpu.substring(0, gpu.lastIndexOf(" "));
            } else
                return searchResults.get(0); //(int)Math.round(searchResults.size()/2.0)
        } //*/

        return new Gpu("Unknown", 0, 0, 0);
    }

    public Gpu getGpu() {
        return getGpu(0);
    }

    public Cpu getCpu(int requirements) {
        String str = getSpecs(requirements).get(0).trim().toLowerCase();
        String[] intelCpus = {"i9", "i7", "i5", "i3", "core 2", "pentium", "xeon"};

        /*
        for (String cpus : intelCpus) {
            if (str.contains(cpus)) {
                if (str.charAt(str.indexOf(cpus) + cpus.length()) == '-')
                    return Searcher.searchCpu(str.substring(str.indexOf(cpus), str.indexOf(" ", str.indexOf(cpus))), 1).get(0);


                return Searcher.searchCpu(cpus, 1).get(0);
            } //*/

        //reaching this point means a matching intel cpu could not be found, resorting to amd
        String[] amdCpus = {"ryzen 9", "ryzen 7", "ryzen 5", "ryzen 3"};
        String[] amdIndicators = {"threadripper", "fx"}; // possible implementation? not specific enough BUT is the most specific if modifiers are added

        return new Cpu("Unknown", 0, 0, 0, 0);
    }

    public Cpu getCpu() {
        return getCpu(0);
    } //*/

}

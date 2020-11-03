package me.raymond.pcparts;


public class UserSpecs {

    private String userId;
    private Cpu userCpu;
    private Gpu userGpu;
    private int userRam;
    private boolean specsPrivacy;
    private String pcDescription;

    public UserSpecs(String id, Cpu c, Gpu g, int r) {
        userId = id;
        userCpu = c;
        userGpu = g;
        userRam = r;
        specsPrivacy = true;
    }

    public UserSpecs(String id, Cpu c, Gpu g, int r, boolean priv, String desc) {
        this(id, c, g, r);
        specsPrivacy = priv;
        pcDescription = desc;
    }

    public UserSpecs(String id, Object obj) {
        userId = id;
        if (obj instanceof Cpu) {
            userCpu = (Cpu) obj;
        } else if (obj instanceof Gpu) {
            userGpu = (Gpu) obj;
        } else if (obj instanceof Integer) {
            userRam = (int) obj;
            if (userRam < 2) {
                userRam = 2;
            }
        }
    }

    //for converting info from file back to ArrayList<UserSpecs>
    public UserSpecs(String rawText) {
        //System.out.println(rawText);

        userId = rawText.substring(rawText.indexOf("<user>") + 6, rawText.indexOf("</user>")).trim();

        userCpu = new Cpu(rawText.substring(rawText.indexOf("<cpu>") + 5, rawText.indexOf("</cpu>")).trim());

        userGpu = new Gpu(rawText.substring(rawText.indexOf("<gpu>") + 5, rawText.indexOf("</gpu>")).trim());

        userRam = Integer.parseInt(rawText.substring(rawText.indexOf("<ram>") + 5, rawText.indexOf("</ram>")));

        //0 = public, 1 = private
        if (rawText.contains("<private>")) {
            specsPrivacy = (rawText.charAt(rawText.indexOf("<private>") + 9) != '0');
        } else {
            specsPrivacy = true;
        }

        if (rawText.contains("<description>") && rawText.contains("</description>")) {
            pcDescription = rawText.substring(rawText.indexOf("<description>") + 13, rawText.indexOf("</description>")).trim();
        }
    }

    //"GETTER" METHODS
    public String getUserId() {
        return userId;
    }

    public Cpu getUserCpu() {
        return userCpu;
    }

    //"SETTER" METHODS
    public void setUserCpu(Cpu c) {
        userCpu = c;
    }

    public Gpu getUserGpu() {
        return userGpu;
    }

    public void setUserGpu(Gpu g) {
        userGpu = g;
    }

    public int getUserRam() {
        if (userRam < 2) {
            userRam = 2;
        }
        return userRam;
    }

    public void setUserRam(int ram) {
        if (ram < 2)
            ram = 2;
        userRam = ram;
    }

    public String getPcDescription() {
        return pcDescription;
    }

    public void setPcDescription(String str) {
        pcDescription = str.trim();
    }

    public boolean getPrivacy() {
        return specsPrivacy;
    }

    public void setPrivacy(boolean bool) {
        System.out.println("Privacy updated to " + bool);
        specsPrivacy = bool;
    }

    public Boolean[] isBetterThan(UserSpecs other) {
        //Order is CPU, GPU, RAM
        Boolean[] output = new Boolean[3];

        output[0] = userCpu.isBetterThan(other.getUserCpu());
        output[1] = userGpu.isBetterThan(other.getUserGpu());

        if(userRam != other.getUserRam()) {
            output[2] = userRam > other.getUserRam();
        }

        return output;
    }

    //toString looks like this:
    // <specs><user>USERID</user><cpu>CPU</cpu><gpu>GPU</gpu><ram>RAM</ram></specs>
    public String toString() {
        String output = "<specs>";

        output += "<user>" + getUserId() + "</user>"; //adds userId to output
        output += "<description>" + getPcDescription() + "</description>"; //adds pcName to output
        output += "<cpu>" + getUserCpu() + "</cpu>"; //adds user's CPU to output
        output += "<gpu>" + getUserGpu() + "</gpu>"; //adds user's GPU to output
        output += "<ram>" + getUserRam() + "</ram>"; //adds user's RAM to output
        output += "<privacy>" + (getPrivacy() ? 1 : 0) + "</privacy>"; //defines whether or not these specs are private

        output += "</specs>";

        return output;
    }
}

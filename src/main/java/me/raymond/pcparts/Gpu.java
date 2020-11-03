package me.raymond.pcparts;

public class Gpu {

    private String name;
    private double baseClock;  //MHz
    private double boostClock; //MHz
    private double memClock;   //MHz
    private int dxVersion;

    public static Gpu getGpuDefault() {
        return new Gpu("No GPU", 0, 0, 0);
    }

    public Gpu(String n, double base, double boost, double mem) {
        name = n;
        baseClock = base;
        boostClock = boost;
        memClock = mem;
        dxVersion = 9;
    }

    public Gpu(String n, double base, double boost, double mem, int dx) {
        name = n;
        baseClock = base;
        boostClock = boost;
        memClock = mem;

        if (dx >= 9 && dx <= 12)
            dxVersion = dx;
        else
            dxVersion = 9;

    }

    public Gpu(String rawText) {

        if (rawText.contains("{") && rawText.contains("}")) {
            name = rawText.substring(rawText.indexOf("name=") + 5, rawText.indexOf("}")).trim();
            baseClock = Double.parseDouble(rawText.substring(rawText.indexOf("baseClock=") + 10, rawText.indexOf(", ", rawText.indexOf("baseClock="))).trim());
            boostClock = Double.parseDouble(rawText.substring(rawText.indexOf("boostClock=") + 11, rawText.indexOf(", ", rawText.indexOf("boostClock="))).trim());
            memClock = Double.parseDouble(rawText.substring(rawText.indexOf("memClock=") + 9, rawText.indexOf(", ", rawText.indexOf("memClock="))).trim());
            dxVersion = Integer.parseInt(rawText.substring(rawText.indexOf("dxVersion=") + 10, rawText.indexOf(", ", rawText.indexOf("dxVersion="))).trim());
        } else if (rawText.equalsIgnoreCase("[No Gpu: 0]")) {
            name = "null";
            baseClock = 0;
            boostClock = 0;
            memClock = 0;
            dxVersion = 0;
        } else {
            System.out.println("[DEBUG - Gpu] Unaccepted parameter: " + rawText);
        }
    }

    public String getName() {
        return name;
    }

    public double getBaseClock() {
        return baseClock;
    }

    public double getBoostClock() {
        return boostClock;
    }

    public double getMemClock() {
        return memClock;
    }

    public int getDxVersion() {
        return  dxVersion;
    }


    public boolean isBetterThan(Gpu other) {
        int counter = 0;

        if (baseClock >= other.getBaseClock())
            counter++;
        if (boostClock >= other.getBoostClock())
            counter++;
        if(memClock >= other.getMemClock())
            counter++;

        return counter >= 2;
    }

    public String toString() {
        return "{name=" + name
                + ", baseClock=" + baseClock
                + ", boostClock=" + boostClock
                + ", memClock=" + memClock
                + "}";
    }
}

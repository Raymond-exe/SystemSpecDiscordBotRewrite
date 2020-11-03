package me.raymond.pcparts;

public class Cpu {

    private String name;
    private double freqInGHz;  //GHz
    private double turboClock; //GHz
    private int coreCount;
    private int threadCount;

    public static Cpu getCpuDefault() {
        return new Cpu("No CPU", 0, 0, 0, 0);
    }

    public Cpu(String n, double freq, double turbo, int cores, int threads) {
        name = n;
        freqInGHz = freq;
        turboClock = turbo;
        coreCount = cores;
        threadCount = threads;
    }

    public Cpu(String rawText) {

        if (rawText.contains("{") && rawText.contains("}")) {
            name = rawText.substring(rawText.indexOf("name=") + 5, rawText.indexOf(", ", rawText.indexOf("name="))).trim();
            freqInGHz = Double.parseDouble(rawText.substring(rawText.indexOf("freqInGHz=") + 10, rawText.indexOf(", ", rawText.indexOf("freqInGHz="))).trim());
            turboClock = Double.parseDouble(rawText.substring(rawText.indexOf("turboClock=") + 11, rawText.indexOf(", ", rawText.indexOf("turboClock="))).trim());
            coreCount = Integer.parseInt(rawText.substring(rawText.indexOf("coreCount=") + 10, rawText.indexOf("}", rawText.indexOf("coreCount="))).trim());
            threadCount = Integer.parseInt(rawText.substring(rawText.indexOf("threadCount=") + 12, rawText.indexOf(", ", rawText.indexOf("threadCount="))).trim());
        } else if (rawText.equalsIgnoreCase("[No Cpu: 0]")) {
            name = "null";
            freqInGHz = 0;
            turboClock = 0;
            coreCount = 0;
            threadCount = 0;
        } else {
            System.out.println("[DEBUG - Cpu] Unaccepted parameter: " + rawText);
        }

    }

    public String getName() { return name; }
    public double getFreqInGHz() { return freqInGHz; }
    public double getTurboClock() { return turboClock; }
    public int getCoreCount() { return coreCount; }
    public int getThreadCount() { return threadCount; }

    public boolean isBetterThan(Cpu other) {
        int counter = 0;

        if (freqInGHz >= other.getFreqInGHz())
            counter++;
        if (turboClock >= other.getTurboClock())
            counter++;
        if(coreCount >= other.getCoreCount())
            counter++;
        if(threadCount >= other.getThreadCount())
            counter++;

        return counter >= 3;
    }

    public String toString() {
        return "{name=" + name
                + ", freqInGHz=" + freqInGHz
                + ", turboClock=" + turboClock
                + ", coreCount=" + coreCount
                + ", threadCount=" + threadCount
                + "}";
    }

}

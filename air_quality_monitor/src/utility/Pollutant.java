package utility;

public enum Pollutant {
    PM10, PM2_5, NO2, O3, CO, C6H6;

    public static Pollutant from(String s) {
        s = s.replace("\"", "").toUpperCase();
        if (s.equals("PM2,5") || s.equals("PM2.5")) return PM2_5;
        return valueOf(s);
    }
}
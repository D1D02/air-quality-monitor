package utility;

public enum Unit {
    UG_M3, MG_M3;

    public static Unit from(String s) {
        return s.startsWith("µ") ? UG_M3 : MG_M3;
    }
}

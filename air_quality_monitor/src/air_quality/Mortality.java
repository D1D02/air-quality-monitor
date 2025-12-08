package air_quality;

public class Mortality {
	private final String illness;
    private final int deathNumber;

    public Mortality(String illness, int deathNumber) {
        this.illness = illness;
        this.deathNumber = deathNumber;
    }

    // Getter
    public String getIllness() {
        return illness;
    }

    public int getDeathNumber() {
        return deathNumber;
    }

    @Override
    public String toString() {
        return "Mortality{" +
               "illness='" + illness + '\'' +
               ", deathNumber=" + deathNumber +
               '}';
    }
}

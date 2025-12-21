package air_quality;

public class Mortality {
	private final String illness;
	private final int year;
    private final int deathNumber;

    public Mortality(String illness, int year, int deathNumber) {
        this.illness = illness;
        this.deathNumber = deathNumber;
        this.year = year;
    }

    // Getter
    public String getIllness() {
        return illness;
    }
    
    public int getYear() {
        return year;
    }
    
    public int getDeathNumber() {
        return deathNumber;
    }

    @Override
    public String toString() {
        return "Mortality{" +
               "illness='" + illness + '\'' +
               ", deathNumber=" + deathNumber + 
               ", year=" + year +
               '}';
    }
}

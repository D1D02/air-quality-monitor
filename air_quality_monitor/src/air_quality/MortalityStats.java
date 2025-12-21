package air_quality;

import dao.impl.MortalityDAOImpl;
import java.util.Map;

public class MortalityStats {
    private final MortalityDAOImpl dao = new MortalityDAOImpl();

    public Map<String, Integer> getStatsByYear(int year) {
        try {
            return dao.getDeathsByYear(year);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
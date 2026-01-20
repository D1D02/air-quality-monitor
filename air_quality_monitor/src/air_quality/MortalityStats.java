package air_quality;

import dao.impl.MortalityDAOImpl;
import java.util.Map;

public class MortalityStats {
    private final MortalityDAOImpl dao = new MortalityDAOImpl();

    public Map<String, Integer> getStatsByYear(int year, String region) {
        try {
            return dao.getDeathsByYear(year, region);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
    
    public Map<Integer, Map<String, Integer>> getStatsByYears(int startYear, int endYear, String region) {
        try {
            return dao.getDeathsByYears(startYear, endYear, region);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
package air_quality;

import java.sql.SQLException;
import java.util.Map;
import dao.impl.AirQualityDAOImpl;

public class AirQualityStats {

    private final AirQualityDAOImpl dao = new AirQualityDAOImpl();

    public AirQualityStats() {}

    public Map<String, Double> getAnnualAverageByPollutant(String region) {
        try {
            return dao.getAnnualAverageByPollutant(region);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<String, Double> getAverageByPollutant(String region) {
        try {
            return dao.getAverageByPollutant(region);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<String, Double> getMonthlyStats(int year, int month, String region) {
        try {
            return dao.getAverageByMonth(year, month, region);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }
    
    public Map<String, Double> getStatsByYears(int startYear, int endYear, String region) {
        try {
            return dao.getAnnualAveragesForPeriod(startYear, endYear, region);
        } catch (SQLException e) {
            System.err.println("❌ Errore nel recupero medie pluriennali aria: " + e.getMessage());
            e.printStackTrace();
            return Map.of();
        }
    }
}
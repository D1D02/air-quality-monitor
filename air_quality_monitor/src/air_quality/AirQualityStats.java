package air_quality;

import java.sql.SQLException;
import java.util.Map;
import dao.impl.AirQualityDAOImpl;

public class AirQualityStats {

    private final AirQualityDAOImpl dao = new AirQualityDAOImpl();

    public AirQualityStats() {}

    public Map<String, Double> getAnnualAverageByPollutant() {
        try {
            return dao.getAnnualAverageByPollutant();
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<String, Double> getAverageByPollutant() {
        try {
            return dao.getAverageByPollutant();
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<String, Double> getMonthlyStats(int year, int month) {
        try {
            return dao.getAverageByMonth(year, month);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
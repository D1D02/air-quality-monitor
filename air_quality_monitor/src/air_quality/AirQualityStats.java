package air_quality;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AirQualityStats {

    private final List<AirQuality> data;

    public AirQualityStats(List<AirQuality> data) {
        this.data = data;
    }

    public AirQualityStats filterByMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Il mese deve essere compreso tra 1 e 12.");
        }
        
        List<AirQuality> filteredData = this.data.stream()
            .filter(aq -> aq.getdate().getYear() == year)
            .filter(aq -> aq.getdate().getMonthValue() == month)
            .collect(Collectors.toList());
            
        return new AirQualityStats(filteredData);
    }
    
 
    public Map<String, Double> getAnnualAverageByPollutant() {
        return this.data.stream()
            .collect(Collectors.groupingBy(
                aq -> aq.getYearString() + "|" + aq.getcodPolluting(),
                Collectors.averagingDouble(AirQuality::getvaluePolluting)
            ));
    }
    

    public Map<String, Double> getAverageByPollutant() {
        return this.data.stream()
            .collect(Collectors.groupingBy(
                AirQuality::getcodPolluting,
                Collectors.averagingDouble(AirQuality::getvaluePolluting)
            ));
    }
}
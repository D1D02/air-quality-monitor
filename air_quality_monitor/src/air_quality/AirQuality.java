package air_quality;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AirQuality {

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSSSSSSSS");
    
    private final LocalDateTime date;
    private final String codPolluting;
    private final String measurementUnit;
    private final double valuePolluting;

    public AirQuality(String dateStr, String codPolluting, 
                       String measurementUnit, String valuePollutingStr) {
        
        this.date = LocalDateTime.parse(dateStr, FORMATTER);
        this.codPolluting = codPolluting;
        this.measurementUnit = measurementUnit;
        
        String cleanedValue = valuePollutingStr
                .trim()                      
                .replaceAll("\"", "")        
                .replace(".", "")            
                .replace(",", ".");
                        
        this.valuePolluting = Double.parseDouble(cleanedValue);  
            
    }

    // Getter
    public LocalDateTime getdate() {
        return date;
    }

    public String getDateString() {
        return date.toLocalDate().toString();
    }
    
    public String getYearMonthString() {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }
    
    public String getYearString() { 
        return String.valueOf(date.getYear());
    }

    public String getcodPolluting() {
        return codPolluting;
    }

    public String getmeasurementUnit() {
        return measurementUnit;
    }

    public double getvaluePolluting() {
        return valuePolluting;
    }

    @Override
    public String toString() {
        return "QualitaAria{" +
                "date=" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                ", codPolluting='" + codPolluting + '\'' +
                ", valuePolluting=" + valuePolluting +
                '}';
    }
}
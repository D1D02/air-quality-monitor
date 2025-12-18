package air_quality;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class AirQuality {

	private static final DateTimeFormatter FORMATTER_LONG_NANO = 
	        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSSSSSSSS");
	    
	    // FORMATTORE 2: DD-MMM-YY HH:mm (Schema 2006/2011)
	private static final DateTimeFormatter FORMATTER_SHORT_IT =
		    new DateTimeFormatterBuilder()
		        .parseCaseInsensitive()   // ← QUESTO È IL FIX
		        .appendPattern("dd-MMM-yy HH:mm")
		        .toFormatter(Locale.ITALIAN);

	    
    private final LocalDateTime date;
    private final String codPolluting;
    private final String measurementUnit;
    private final double valuePolluting;


    public AirQuality(String dateStr, String timeStr, String codPolluting, 
            String measurementUnit, String valuePollutingStr) {

		String fullDateStr = (timeStr == null || timeStr.trim().isEmpty()) 
		                  ? dateStr.trim()
		                  : dateStr.trim() + " " + timeStr.trim();
		                  
		LocalDateTime parsedDate;
		try {
		 parsedDate = LocalDateTime.parse(fullDateStr, FORMATTER_LONG_NANO);
		} catch (DateTimeParseException e) {
		 try {
		     parsedDate = LocalDateTime.parse(fullDateStr.toUpperCase(), FORMATTER_SHORT_IT);
		 } catch (DateTimeParseException e2) {
		      throw new IllegalArgumentException("Formato data non riconosciuto: " + fullDateStr, e2);
		 }
		}
		this.date = parsedDate;
		
		this.codPolluting = codPolluting;
		this.measurementUnit = measurementUnit;
		
		String cleanedValue = valuePollutingStr
		        .trim()
		        .replace("\"", "");

		if (cleanedValue.contains(",") && cleanedValue.contains(".")) {
		    // formato 1.234,56
		    cleanedValue = cleanedValue.replace(".", "").replace(",", ".");
		} else if (cleanedValue.contains(",")) {
		    // formato 4,8
		    cleanedValue = cleanedValue.replace(",", ".");
		}
		// altrimenti: 3.9, .63 → OK

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
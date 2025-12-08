package utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import air_quality.AirQuality;
import air_quality.Mortality;



public class CsvReader {

    public List<Mortality> extractMortality(String path) throws IOException {
        
        return Files.lines(Path.of(path))
            .skip(1)
            .map(linea -> linea.split(","))
            .map(campi -> {
                String malattia = campi[0].trim();
                String mortiStr = campi[1].trim().replace(".", ""); 
                
                try {
                    int numeroMorti = Integer.parseInt(mortiStr);
                    return new Mortality(malattia, numeroMorti);
                } catch (NumberFormatException e) {
                    System.err.println("Errore nella conversione del numero di morti: " + mortiStr);
                    return null; 
                }
            })
            .filter(m -> m != null) 
            .collect(Collectors.toList());
        
    }

    public List<AirQuality> extractAirQuality(String path) throws IOException {
        
        return Files.lines(Path.of(path))
            .skip(1)
            .map(linea -> linea.split(",", 6)) 
            .map(campi -> {
                if (campi.length < 6) return null; 
                
                try {
                    return new AirQuality(
                        campi[2].trim(), 
                        campi[3].trim(), 
                        campi[4].trim(), 
                        campi[5].trim() 
                    );
                } catch (Exception e) {
                    return null; 
                }
            })
            .filter(d -> d != null)
            .filter(d -> d.getvaluePolluting() >= 0) 
            .collect(Collectors.toList());
    }
}
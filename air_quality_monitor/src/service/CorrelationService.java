package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CorrelationService {

    private final Map<String, Double> aqData;
    private final Map<Integer, Map<String, Integer>> mortalityData;

    public CorrelationService(
        Map<String, Double> aqData,
        Map<Integer, Map<String, Integer>> mortalityData
    ) {
        this.aqData = aqData;
        this.mortalityData = mortalityData;
    }

    public double getCorrelation(String pollutant, String illness,
                                 int startYear, int endYear) {

        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            String key = year + "|" + pollutant;

            Map<String, Integer> m = mortalityData.get(year);
            if (aqData.containsKey(key) && m != null && m.containsKey(illness)) {
                x.add(aqData.get(key));
                y.add(m.get(illness).doubleValue());
            }
        }
        return computePearson(x, y);
    }
    
    private double computePearson(List<Double> x, List<Double> y) {
        if (x.size() < 2 || x.size() != y.size()) return 0.0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        int n = x.size();

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }

        double numerator = (n * sumXY) - (sumX * sumY);
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return (denominator == 0) ? 0 : numerator / denominator;
    }
}

package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import air_quality.AirQuality;

public interface AirQualityDAO 
{
	public void insertBatch(List<AirQuality> records, String region) throws SQLException;
    public List<AirQuality> getAll(String region) throws SQLException;
	
	public Map<String, Double> getAverageByPollutant(String region) throws SQLException;
    public Map<String, Double> getAnnualAverageByPollutant(String region) throws SQLException;
    public Map<String, Double> getAnnualAveragesForPeriod(int startYear, int endYear, String region) throws SQLException;
    public Map<String, Double> getAverageByMonth(int year, int month, String region) throws SQLException;
    
    public String getMostPollutedMonth(int year, String region) throws SQLException;
}

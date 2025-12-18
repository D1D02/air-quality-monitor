package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import air_quality.AirQuality;

public interface AirQualityDAO 
{
	public void insertBatch(List<AirQuality> records) throws SQLException;
    public List<AirQuality> getAll() throws SQLException;
	
	public Map<String, Double> getAverageByPollutant() throws SQLException;
    public Map<String, Double> getAnnualAverageByPollutant() throws SQLException;
    public Map<String, Double> getAverageByMonth(int year, int month) throws SQLException;

}

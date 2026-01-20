package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import air_quality.Mortality;

public interface MortalityDAO 
{
	public void insertBatch(List<Mortality> list, String region) throws SQLException;
    public List<Mortality> getAll(String region) throws SQLException;
    
	public Map<String, Integer> getDeathsByYear(int year, String region) throws SQLException;
	public Map<Integer, Map<String, Integer>> getDeathsByYears(int startYear, int endYear, String region) 
			throws SQLException ;
	
	public Map<String, Integer> getTotalDeathsByIllness(String region) throws SQLException;
}

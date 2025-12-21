package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import air_quality.Mortality;

public interface MortalityDAO 
{
	public void insertBatch(List<Mortality> list) throws SQLException;
	public Map<String, Integer> getDeathsByYear(int year) throws SQLException;
	public Map<String, Integer> getTotalDeathsByIllness() throws SQLException;
    public List<Mortality> getAll() throws SQLException;
}

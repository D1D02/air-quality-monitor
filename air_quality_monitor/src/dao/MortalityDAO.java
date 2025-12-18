package dao;

import java.sql.SQLException;
import java.util.List;

import air_quality.Mortality;

public interface MortalityDAO 
{
	public void insertBatch(List<Mortality> list) throws SQLException;
    public List<Mortality> getAll() throws SQLException;
}

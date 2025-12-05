package air_quality_monitor;

import gui.AirQualityUI;
import sql.ConnectionMySql;

public class Main {

	public static void main(String[] args) 
	{
		
		System.out.println( "IOT" );
		
		ConnectionMySql.getConnection();
		
		AirQualityUI.start();
		
	}

}

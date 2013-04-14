package dia.server.config;

import com.google.gson.Gson;

public class DiaConfig
{
	public static final String DATABASE_PATH = "./data/storage/neo4j";
	
	public Gson gson;
	
	public DiaConfig()
	{
		// TODO: make useful configuration
		gson = new Gson();
	}
	
	
}

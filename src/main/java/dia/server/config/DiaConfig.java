package dia.server.config;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class DiaConfig
{
	
	private CrawlerConfig crawler;
	
	private StoreConfig storage;
	
	public static DiaConfig load(String configFilename)
	{
		// TODO: make useful configuration
		Gson gson = new Gson();
		
		InputStream stream = DiaConfig.class.getClassLoader().getResourceAsStream(configFilename);
		if(stream == null) {
			throw new IllegalArgumentException("Configuration file [" + configFilename + "] not found.");
		}
		
		DiaConfig config = gson.fromJson(new InputStreamReader(stream), DiaConfig.class);
		
		return config;
	}
	
	private DiaConfig() {}
	
	public CrawlerConfig getCrawlerConf() { return crawler; }
	
	public StoreConfig getStoreConf() { return storage; }
}

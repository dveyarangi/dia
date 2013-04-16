package dia.server;

import org.neo4j.graphdb.Transaction;

import dia.api.CategoryNode;
import dia.server.config.DiaConfig;
import dia.server.crawler.DiaCrawler;
import dia.server.crawler.ILinkConsumer;
import dia.server.crawler.WikiLinkConsumer;
import dia.server.store.DiaStore;
import dia.server.store.neo4j.Neo4JStore;

public class Dia
{
	/**
	 * Test main
	 */
	public static void main( String ... args)
	{
		
		Dia dia = new Dia();
		
		dia.init();
		
		dia.run();
		
		registerShutdownHook(dia);

	}

	/**
	 * Server configuration
	 */
	private final DiaConfig config;
	
	/**
	 * Database
	 */
	private final DiaStore store;
	
	/**
	 * Internet crawler
	 */
	private final DiaCrawler crawler;
	
	private Dia()
	{
		config = new DiaConfig();
		
		store = new Neo4JStore();
		
		crawler = new DiaCrawler();
	}
	
	private void init()
	{
		// read config
		
		crawler.init(config);
		
		store.init(config);
	}
	
	
	private void run()
	{
		///////////////////////////////////////////
		// test setup
		///////////////////////////////////////////
		
		String language = "en";
		
		// processes links generated by crawler:
		ILinkConsumer consumer = new WikiLinkConsumer( store, language );
		
		// starting node for tests:
		String rootUrl = "http://en.wikipedia.org/wiki/Category:Physics";
		CategoryNode rootNode = new CategoryNode( "Physics", rootUrl );
		
		Transaction tx = store.startTransaction();
		store.updateCategoryNode( rootNode );
		tx.success();
		tx.finish();
		
		// do crawl:
		int count = crawler.extractLinks( rootUrl, consumer );
		
		System.out.println("Links processed: " + count);
	}
	
	private void destroy()
	{
		store.destroy();
		crawler.destroy();
	}

	
	private static void registerShutdownHook(final Dia dia)
	{
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run()
			{
				dia.destroy();
				
			}
		});
	}
}

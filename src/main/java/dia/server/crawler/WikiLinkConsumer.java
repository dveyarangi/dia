package dia.server.crawler;

import org.apache.log4j.Logger;

import dia.api.DNode;
import dia.server.store.DiaStore;

public class WikiLinkConsumer implements ILinkConsumer
{
	private final DiaStore store;
	
	private final String baseUrl;
	
	private final String language;
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	public WikiLinkConsumer(DiaStore _store, String _language)
	{
		store = _store;
		language = _language;
		baseUrl = "http://" + language + ".wikipedia.org";
	}

	@Override
	public int consume(DNode homeNode, String url)
	{
		if(!url.startsWith( "/wiki" )) {
			return 0;
		}
		
		if(url.contains( ":" )) {
			return 0;
		}
		
		String name = url.substring( 6 ); // dropping "/wiki"
		
		DNode node = DNode.create( name );
		node.setUrl( baseUrl + url );
		node.setLanguage( language );
		
		if(log.isTraceEnabled()) {
			log.trace( "Consumed link -> " + node + "");
		}
		
		store.updateNode( node );
		
		store.hyperlinkNodes( homeNode, node );
				
		return 1;
	}

}

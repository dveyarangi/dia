package dia.server.crawler;

import org.apache.log4j.Logger;

import dia.api.ArticleNode;
import dia.api.CategoryNode;
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
		if( !url.startsWith( "/wiki" ) ) {
			return 0;
		}
		
		String name = url.substring(6);
		if( name.contains( ":" ) )
		{
			String [] parts = name.split(":");
			if(!parts[0].equals("Category")) {
				return 0;
			}
			CategoryNode node = new CategoryNode(parts[1]);
			store.updateCategoryNode( node );
			
			if(homeNode instanceof CategoryNode) 
				store.addSubcategory((CategoryNode)homeNode, node);
			
			return 1;
		}
		
		ArticleNode node = new ArticleNode( name, baseUrl + url, language );
		store.updateArticleNode( node );
		if(homeNode instanceof ArticleNode) {
			store.addHyperlink( homeNode, node );
		} else {
			store.addToCategory( (CategoryNode)homeNode, node);
		}
		
		if(log.isTraceEnabled()) {
			log.trace( "Consumed link -> " + node + "");
		}
		
				
		return 1;
	}

}

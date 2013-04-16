package dia.server.crawler;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Transaction;

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
	
	private HashSet <String> names = new HashSet <String> ();
	
	private Transaction tx;
	
	public WikiLinkConsumer(DiaStore _store, String _language)
	{
		store = _store;
		language = _language;
		baseUrl = "http://" + language + ".wikipedia.org";
	}
	
	

	@Override
	public String consume(String parentName, String url)
	{
		// preparing parent node:
		DNode parentNode = parentName == null ? null : store.getNode(parentName);
		
		if(!url.startsWith( baseUrl )) // not wiki link
			return null;
		
		String relativeUrl = url.substring(baseUrl.length());
		if( !relativeUrl.startsWith( "/wiki" ) ) {
			return null;
		}
		
		// cutting out node name:
		String name = url.substring(baseUrl.length() + 6);
		
		DNode node;

		boolean isCategory = false;
		if( name.contains( ":" ) ) // special page:
		{
			String parts = name.split("#")[0];
			if(!parts.split( ":" )[0].equals("Category")) {
				return null;
			}
			
			name = parts;
			isCategory = true;
		}
		
		if(names.contains( name ))
			return null;
	
		if(isCategory)
			node = createCategoryNode( name, url, parentNode );
		else 
			node = createArticleNode( name, url, parentNode );

		
		if(node != null)
			log.trace( "Consumed link -> " + node + "");
		else
			log.trace( "Node for url [" + url + "] already exists.");
		
				
		return node != null ? node.getName() : null;
	}
	
	private DNode createCategoryNode(String name, String url, DNode parentNode)
	{
		CategoryNode node = new CategoryNode(name, url);
		boolean isNew = store.updateCategoryNode( node );
		
		if(parentNode != null && parentNode instanceof CategoryNode) 
			store.addSubcategory((CategoryNode)parentNode, node);
		
		return isNew ? node : null;
	}
	
	private DNode createArticleNode(String name, String url, DNode parentNode)
	{
		ArticleNode node = new ArticleNode( name, url, language );
		boolean isNew = store.updateArticleNode( node );
		if(parentNode != null)
		{
			if(parentNode instanceof ArticleNode) 
			{
				store.addHyperlink( parentNode, node );
			} else {
				store.addToCategory( (CategoryNode)parentNode, node);
			}
		}
		
		return isNew ? node : null;

	}

	@Override
	public void start()
	{
		tx = store.startTransaction();
	}

	@Override
	public void finish()
	{
		tx.success();
		tx.finish();
		names.clear();
	}

}

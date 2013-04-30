package dia.server.store.neo4j;

import static dia.server.store.neo4j.Infrastructure.HYPERLINK;
import static dia.server.store.neo4j.Infrastructure.IN_CATEGORY;
import static dia.server.store.neo4j.Infrastructure.NODE_LANG;
import static dia.server.store.neo4j.Infrastructure.NODE_NAME;
import static dia.server.store.neo4j.Infrastructure.NODE_TYPE;
import static dia.server.store.neo4j.Infrastructure.NODE_URL;
import static dia.server.store.neo4j.Infrastructure.SUBCATEGORY_OF;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import com.google.common.base.Preconditions;

import dia.api.ArticleNode;
import dia.api.CategoryNode;
import dia.api.DNode;
import dia.server.config.StoreConfig;
import dia.server.store.DiaStore;

public class Neo4JStore implements DiaStore
{
	
//	public static final String DB_PATH = "./data/storage/neo4j/wiki";
	
	/**
	 * Embedded Neo4j service.
	 */
	private GraphDatabaseService graphDb;
	
	/**
	 * Nodes index by name.
	 */
	private Index <Node> articles;
	
	private Index <Node> categories;
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	
	@Override
	public void init(StoreConfig config)
	{
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( config.getRootPath() );
		
		IndexManager index = graphDb.index();
		
		articles = index.forNodes( "articles" );
		
		categories = index.forNodes( "categories" );
	}
	
	@Override
	public void destroy()
	{
		graphDb.shutdown();
	}


	@Override
	public DNode getNode(String nodeName)
	{
		Preconditions.checkNotNull( nodeName, "Node name must not be null." );
		
		IndexHits <Node> hits = articles.get( NODE_NAME, nodeName );
		Node neonode = hits.getSingle();
		if(neonode == null) {
			return null;
		}
		
		String nodeType = (String)neonode.getProperty( NODE_TYPE );
		
		switch(nodeType)
		{
		case DNode.TYPE_ARTICLE:
			return new ArticleNode( nodeName,
						(String)neonode.getProperty( NODE_URL ),
						(String)neonode.getProperty( NODE_LANG ) );
		case DNode.TYPE_CATEGORY:
			return new CategoryNode( nodeName, (String)neonode.getProperty( NODE_URL ) );
		default:
			log.warn( "Unknown node [" + nodeName + "] type [" + nodeType + "]." );
			return null;
		}
	}

	@Override
	public boolean updateArticleNode(ArticleNode dianode)
	{
		String nodeName = dianode.getName();
		
		boolean added = false;

/*		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		try {*/
			// checking if node already exists:
			IndexHits <Node> hits = articles.get( NODE_NAME, nodeName );
			Node neonode = hits.getSingle();
			
			
			if(neonode == null)
			{
				// creating node:
				neonode = graphDb.createNode();
				
				neonode.setProperty( NODE_NAME, dianode.getName() );
			
				// updating index:
				articles.add( neonode, NODE_NAME, nodeName );
				
				log.trace( "Created new article node [" + neonode.getId() + ":" + dianode.getName() + "]." );
				
				added = true;
			}
			else
			{
				log.trace( "Updated existing article node [" + neonode.getId() + ":" + dianode.getName() + "]." );
				added = false;
			}
			
			// setting values:
			updateNodeProperties( neonode, dianode );

/*			tx.success();
		}
		finally { tx.finish(); }*/
		
		return added;
	}
	
	
	@Override
	public boolean updateCategoryNode(CategoryNode dianode)
	{
		String nodeName = dianode.getName();
		
		boolean added = false;
		
/*		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {*/
			// checking if node already exists:
			IndexHits <Node> hits = categories.get( NODE_NAME, nodeName );
			Node neonode = hits.getSingle();
			
			if(neonode == null)
			{
				// creating node:
				neonode = graphDb.createNode();
				
				neonode.setProperty( NODE_NAME, dianode.getName() );
			
				// updating index:
				categories.add( neonode, NODE_NAME, nodeName );
				
				added = true;
				
				log.trace( "Created new category node [" + neonode.getId() + ":" + dianode.getName() + "]." );
			}
			else
			{
				log.trace( "Updated existing category node [" + neonode.getId() + ":" + dianode.getName() + "]." );
				added = false;
			}
			
			// setting values:
			updateNodeProperties( neonode, dianode );

/*			tx.success();
		}
		finally { tx.finish(); }*/
		
		return added;
	}

	/**
	 * Converts {@link DNode} fields to neo4j properties.
	 * @param neonode
	 * @param dianode
	 */
	private void updateNodeProperties(Node neonode, ArticleNode dianode)
	{
		neonode.setProperty( NODE_TYPE, dianode.getType() );
		neonode.setProperty( NODE_NAME, dianode.getName() );
		neonode.setProperty( NODE_LANG, dianode.getLanguage());
		neonode.setProperty( NODE_URL, dianode.getUrl());
	}
	/**
	 * Converts {@link DNode} fields to neo4j properties.
	 * @param neonode
	 * @param dianode
	 */
	private void updateNodeProperties(Node neonode, CategoryNode dianode)
	{
		neonode.setProperty( NODE_TYPE, dianode.getType() );
		neonode.setProperty( NODE_NAME, dianode.getName() );
//		neonode.setProperty( NODE_LANG, dianode.getLanguage());
		neonode.setProperty( NODE_URL, dianode.getUrl());
	}
	
	@Override
	public boolean addHyperlink(DNode dianodea, DNode dianodeb)
	{
		return linkNodes(dianodea, articles, HYPERLINK, dianodeb, articles, null);
	}
	
	@Override
	public boolean addToCategory(CategoryNode category, ArticleNode dianode)
	{
		return linkNodes(category, categories, null, dianode, articles, IN_CATEGORY);

	}
	
	@Override
	public boolean addSubcategory(CategoryNode category, CategoryNode subcategory)
	{
		return linkNodes(category, categories, null, subcategory, categories, SUBCATEGORY_OF);
	}
	///////////////////////////////////////////////////////////////
	private boolean linkNodes(DNode dianodea, Index indexa, RelationshipType typea, DNode dianodeb, Index indexb, RelationshipType typeb)
	{
		Preconditions.checkArgument( typea != null || typeb != null, "Relationship types are null" );
		
		boolean added = false;

/*		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		try {*/
			// looking for nodes in index:
			IndexHits <Node> hitsa = indexa.get( NODE_NAME, dianodea.getName() );
			Node nodeA = hitsa.getSingle();
			IndexHits <Node> hitsb = indexb.get( NODE_NAME, dianodeb.getName() );
			Node nodeB = hitsb.getSingle();
			
			if(nodeA == null) {
				throw new IllegalArgumentException( "Node [" + dianodea.getName() + "] not found in database.");
			}
			if(nodeB == null) {
				throw new IllegalArgumentException( "Node [" + dianodeb.getName() + "] not found in database.");
			}

			if(typea != null) {
				added |= updateRelationship(nodeA, nodeB, typea);
			}
				
			if(typeb != null) {
				added |= updateRelationship(nodeB, nodeA, typeb);
			}

/*			tx.success();
		}
		finally
		{
			tx.finish();
		}*/
		
		return added;
	}
	
	private boolean updateRelationship(Node nodea, Node nodeb, RelationshipType type)
	{
		
		boolean added = false;
		Relationship relationship = null;
		Iterable <Relationship> relationships = nodea.getRelationships( type, Direction.OUTGOING );
		if(relationships != null) {
			for(Relationship rel : relationships)
			{
				if(rel.getEndNode().equals( nodeb ))
				{
					relationship = rel;
					break;
				}
			}
		}
		
		if(relationship == null)
		{
			relationship = nodea.createRelationshipTo( nodeb, type );
			log.trace( "Created new [" + type + "] relation: [" + nodea.getId() + ":" + nodea.getProperty(NODE_NAME) + "] -> [" + nodeb.getId() + ":" + nodeb.getProperty(NODE_NAME) + "]." );
			added = true;
		}
		else
		{
			log.trace( "Updated existing [" + type + "] relation: [" + nodea.getId() + ":" + nodea.getProperty(NODE_NAME) + "] -> [" + nodeb.getId() + ":" + nodeb.getProperty(NODE_NAME) + "]." );
			added = false;
		}
		
		return added;
		
	}
	
	
	public static void main(String ... args)
	{
		Neo4JStore store = new Neo4JStore();
		store.init( null );
		
//		DNode node = store.retrieveNode( "Test1" );
//		System.out.println(node);
	}

	@Override
	public Transaction startTransaction()
	{
		return graphDb.beginTx();
	}

}

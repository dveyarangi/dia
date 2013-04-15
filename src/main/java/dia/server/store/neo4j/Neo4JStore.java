package dia.server.store.neo4j;

import static dia.server.store.neo4j.Infrastructure.HYPERLINK;
import static dia.server.store.neo4j.Infrastructure.IN_CATEGORY;
import static dia.server.store.neo4j.Infrastructure.NODE_LANG;
import static dia.server.store.neo4j.Infrastructure.NODE_NAME;
import static dia.server.store.neo4j.Infrastructure.NODE_TYPE;
import static dia.server.store.neo4j.Infrastructure.NODE_URL;

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

import dia.api.ArticleNode;
import dia.api.CategoryNode;
import dia.api.DNode;
import dia.server.config.DiaConfig;
import dia.server.store.DiaStore;

public class Neo4JStore implements DiaStore
{
	
	public static final String DB_PATH = "./data/storage/neo4j";
	
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
	public void init(DiaConfig config)
	{
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		
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
	public void updateArticleNode(ArticleNode dianode)
	{
		String nodeName = dianode.getName();
		
		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {
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
				
				log.trace( "Created new node [" + neonode.getId() + ":" + dianode.getName() + "]." );
			} else {
				log.trace( "Updated existing node [" + neonode.getId() + ":" + dianode.getName() + "]." );
			}
			
			// setting values:
			updateNodeProperties( neonode, dianode );

			tx.success();
		}
		finally { tx.finish(); }
	}
	
	
	@Override
	public void updateCategoryNode(CategoryNode dianode)
	{
		String nodeName = dianode.getName();
		
		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {
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
				
				log.trace( "Created new node [" + neonode.getId() + ":" + dianode.getName() + "]." );
			} else {
				log.trace( "Updated existing node [" + neonode.getId() + ":" + dianode.getName() + "]." );
			}
			
			// setting values:
			updateNodeProperties( neonode, dianode );

			tx.success();
		}
		finally { tx.finish(); }
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
//		neonode.setProperty( NODE_URL, dianode.getUrl());
	}
	
	@Override
	public void hyperlinkNodes(DNode dianodea, DNode dianodeb)
	{
		linkNodes(dianodea, articles, HYPERLINK, dianodeb, articles, null);
	}
	@Override
	public void addToCategory(DNode category, ArticleNode dianode)
	{
		linkNodes(category, categories, IN_CATEGORY, dianode, articles, null);

	}
	///////////////////////////////////////////////////////////////
	private void linkNodes(DNode dianodea, Index indexa, RelationshipType typea, DNode dianodeb, Index indexb, RelationshipType typeb)
	{
		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {
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

			getRelationship(nodeA, nodeB, typea);
			if(typeb != null) {
				getRelationship(nodeB, nodeA, typeb);
			}

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private Relationship getRelationship(Node nodea, Node nodeb, RelationshipType type)
	{
		
		Relationship relationship = null;
		for(Relationship rel : nodea.getRelationships( type, Direction.OUTGOING ))
		{
			if(rel.getEndNode().equals( nodeb ))
			{
				relationship = rel;
				break;
			}
		}
		
		if(relationship == null)
		{
			relationship = nodea.createRelationshipTo( nodeb, type );
			log.trace( "Created new " + type + " relation: [" + nodea.getId() + ":" + nodea.getProperty(NODE_NAME) + "] -> [" + nodeb.getId() + ":" + nodeb.getProperty(NODE_NAME) + "]." );
		} else {
			log.trace( "Updated existing " + type + " relation: [" + nodea.getId() + ":" + nodea.getProperty(NODE_NAME) + "] -> [" + nodeb.getId() + ":" + nodeb.getProperty(NODE_NAME) + "]." );
		}
		
		return relationship;
		
	}
	
	
	public static void main(String ... args)
	{
		Neo4JStore store = new Neo4JStore();
		store.init( null );
		
//		DNode node = store.retrieveNode( "Test1" );
//		System.out.println(node);
	}

}

package dia.server.store.neo4j;

import static dia.server.store.neo4j.Infrastructure.HYPERLINK;
import static dia.server.store.neo4j.Infrastructure.NODE_LANG;
import static dia.server.store.neo4j.Infrastructure.NODE_NAME;
import static dia.server.store.neo4j.Infrastructure.NODE_URL;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

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
	private Index<Node> nodes;
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	@Override
	public void init(DiaConfig config)
	{
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		
		IndexManager index = graphDb.index();
		
		nodes = index.forNodes( "names" );
	}
	
	@Override
	public void destroy()
	{
		graphDb.shutdown();
	}


	@Override
	public void updateNode(DNode dianode)
	{
		String nodeName = dianode.getName();
		
		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {
			// checking if node already exists:
			IndexHits <Node> hits = nodes.get( NODE_NAME, nodeName );
			Node neonode = hits.getSingle();
			
			if(neonode == null)
			{
				// creating node:
				neonode = graphDb.createNode();
				
				neonode.setProperty( NODE_NAME, dianode.getName() );
			
				// updating index:
				nodes.add( neonode, NODE_NAME, nodeName );
				
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
	private void updateNodeProperties(Node neonode, DNode dianode)
	{
		neonode.setProperty( NODE_NAME, dianode.getName() );
		neonode.setProperty( NODE_LANG, dianode.getLanguage());
		neonode.setProperty( NODE_URL, dianode.getUrl());
	}

	@Override
	public void hyperlinkNodes(DNode dianodea, DNode dianodeb)
	{
		Transaction tx = graphDb.beginTx(); // why not implements AutoClosable?
		
		try {
			// looking for nodes in index:
			IndexHits <Node> hits = nodes.get( NODE_NAME, dianodea.getName() );
			Node nodeA = hits.getSingle();
			hits = nodes.get( NODE_NAME, dianodeb.getName() );
			Node nodeB = hits.getSingle();
			
			if(nodeA == null) {
				throw new IllegalArgumentException( "Node [" + dianodea.getName() + "] not found in database.");
			}
			if(nodeB == null) {
				throw new IllegalArgumentException( "Node [" + dianodeb.getName() + "] not found in database.");
			}
			
			// looking for existing hyperlink connection data:
			boolean found = false;
			for(Relationship rel : nodeA.getRelationships( HYPERLINK, Direction.OUTGOING ))
			{
				if(rel.getEndNode().equals( nodeB ))
				{
					found = true;
					break;
				}
			}
			
			Relationship hyperlinkRel;
			
			if(!found)
			{
				hyperlinkRel = nodeA.createRelationshipTo( nodeB, HYPERLINK );
				log.trace( "Created new hyperlink: [" + nodeA.getId() + ":" + dianodea.getName() + "] -> [" + nodeB.getId() + ":" + dianodeb.getName() + "]." );
			} else {
				log.trace( "Updated existing hyperlink: [" + nodeA.getId() + ":" + dianodea.getName() + "] -> [" + nodeB.getId() + ":" + dianodeb.getName() + "]." );
			}
			

			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
		
	///////////////////////////////////////////////////////////////

	public static void main(String ... args)
	{
		Neo4JStore store = new Neo4JStore();
		store.init( null );
		
//		DNode node = store.retrieveNode( "Test1" );
//		System.out.println(node);
	}

}

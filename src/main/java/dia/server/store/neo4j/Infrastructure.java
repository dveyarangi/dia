package dia.server.store.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * Graph structural properties and names
 */
public class Infrastructure
{
	public static final String NODE_ID = "id";
	public static final String NODE_TYPE = "type";
	public static final String NODE_NAME = "name";
	public static final String NODE_URL = "url";
	public static final String NODE_LANG = "lang";
	
	public static final RelationshipType HYPERLINK = createRelationshipType("hyperlink"); // internet link
	
	public static final RelationshipType IN_CATEGORY = createRelationshipType("in_category"); // category member
	public static final RelationshipType SUBCATEGORY_OF = createRelationshipType("subcategory_of"); // category member
	
	
	private static RelationshipType createRelationshipType(final String name)
	{
		
		return new RelationshipType() { 
			@Override public String name() { return name; }
			@Override public String toString() { return name; }
		};
	}
}

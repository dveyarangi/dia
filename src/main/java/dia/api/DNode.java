package dia.api;

import com.google.common.base.Preconditions;


public class DNode
{
//	private long id;
	public static final String TYPE_ARTICLE = "ARTICLE";
	public static final String TYPE_CATEGORY = "CATEGORY";
	
	private final String name;
	
	private final String type;

	public DNode(String _name, String _type)
	{
		name = Preconditions.checkNotNull( _name );
		type = Preconditions.checkNotNull( _type );
	}
	
//	public long getId() { return id; }

	public String getName() { return name; }
	
	public String getType() { return type; }

	
	@Override
	public int hashCode() { return name.hashCode(); }

}

package dia.api;

import com.google.common.base.Preconditions;


public class DNode
{
//	private long id;
	public static final String TYPE_ARTICLE = "ARTICLE";
	public static final String TYPE_CATEGORY = "CATEGORY";
	
	private final String name;
	
	private final String type;
	
	private final String url;

	public DNode(String _name, String _type, String _url)
	{
		name = Preconditions.checkNotNull( _name );
		type = Preconditions.checkNotNull( _type );
		url = Preconditions.checkNotNull(_url);
	}
	
		
//	public long getId() { return id; }

	public String getName() { return name; }
	
	public String getType() { return type; }

	public String getUrl() { return url; }
	
	@Override
	public int hashCode() { return name.hashCode(); }


}

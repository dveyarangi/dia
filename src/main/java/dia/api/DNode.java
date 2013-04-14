package dia.api;

import com.google.common.base.Preconditions;


public class DNode
{
//	private long id;
	
	private final String name;
	
	private String url;
	
	private DNode category;
	
	private String language;
	
//	private List <Knowd> children = new LinkedList <Knowd> ();
	
	
	public static DNode create(String name)
	{
		 return new DNode(name);
	}
	
	private DNode(String _name)
	{
		name = Preconditions.checkNotNull( _name );
	}
	
//	public long getId() { return id; }

	public String getName() { return name; }

	public String getUrl() { return url; }
	public void setUrl(String _url)
	{
		url = Preconditions.checkNotNull( _url );
	}
	
	public String getLanguage() { return language; }
	public void setLanguage(String _language)
	{
		language = Preconditions.checkNotNull( _language );
	}

	
	public DNode getCategory() { return category; }
	
	@Override
	public int hashCode() { return name.hashCode(); }
	
	@Override
	public String toString()
	{
		return new StringBuffer() 
			.append("DNODE [" )//.append(id).append(", ")
			.append(name).append(", ")
			.append(url).append("]")
			.toString();
	}
}

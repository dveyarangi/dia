/**
 * 
 */
package dia.api;

import static dia.api.DNode.TYPE_ARTICLE;

import com.google.common.base.Preconditions;

/**
 *
 */
public class ArticleNode extends DNode
{
	

	private final String url;
	
	private DNode category;
	
	private final String language;
	
	/**
	 * @param _name
	 */
	public ArticleNode(String _name, String _url, String _language)
	{
		super( _name, TYPE_ARTICLE );
		
		url = Preconditions.checkNotNull(_url);
		language = Preconditions.checkNotNull(_language);
	}

	
	
	public String getUrl() { return url; }
/*	public void setUrl(String _url)
	{
		url = Preconditions.checkNotNull( _url );
	}*/
	
	public String getLanguage() { return language; }
/*	public void setLanguage(String _language)
	{
		language = Preconditions.checkNotNull( _language );
	}*/

	
	public DNode getCategory() { return category; }
	
	@Override
	public String toString()
	{
		return new StringBuffer()
			.append("DNODE [" )//.append(id).append(", ")
			.append(getName()).append(", ")
			.append(getUrl()).append("]")
			.toString();
	}
}

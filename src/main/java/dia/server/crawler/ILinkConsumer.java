package dia.server.crawler;

import dia.api.DNode;

/**
 * Converts link url to {@link DNode}
 * 
 * @author dveyarangi
 *
 */
public interface ILinkConsumer
{
	public void start();
	
	/**
	 * Consumes a link, return number of used links
	 * @param url
	 * @return
	 */
	public String consume(String parentName, String url);
	
	public void finish();

}

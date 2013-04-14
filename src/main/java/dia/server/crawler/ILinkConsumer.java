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
	/**
	 * Consumes a link, return number of used links
	 * @param url
	 * @return
	 */
	public int consume(DNode homeNode, String url);

}
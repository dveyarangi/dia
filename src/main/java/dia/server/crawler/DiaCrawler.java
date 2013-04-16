package dia.server.crawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dia.api.DNode;
import dia.server.config.DiaConfig;

public class DiaCrawler
{
	private DiaConfig config;
	
	private final HttpClient httpClient;

	private final Logger log = Logger.getLogger( this.getClass() );
	
	private LinkedHashMap <String, String> linksQueue = new LinkedHashMap <String, String> ();

	public DiaCrawler()
	{
		this.httpClient = new DefaultHttpClient();
	}

	/**
	 * @param config
	 */
	public void init(DiaConfig config)
	{
		this.config = config;
	}

	/**
	 * Takes html's children and feeds them to the consumer (Chronus would be proud)
	 * 
	 * @param homeNode
	 * @param consumer
	 * @return number of links processed
	 */
	public int extractLinks(String startingUrl, ILinkConsumer consumer)
	{
		linksQueue.put( startingUrl, null );

		int totalCount = 0;
		
		while(!linksQueue.isEmpty()) {
			
			String url = linksQueue.keySet().iterator().next();
			String parentName = linksQueue.get( url );
			
			linksQueue.remove( url );
			
			// reading html to string:
			Document doc;
			try
			{
				doc = Jsoup.connect(url).get();
			} catch ( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
//			String body = readUrl(url);
			
			// parsing the received html:
//			Jsoup.parse( body );
			
			// walking over links:
			Elements links = doc.select("a[href]");
			
			int count = 0;
			log.debug( "Extracting links from [" + url + "] (" + links.size() + " found): " );
			
			for(int idx = 0; idx < links.size(); idx ++)
			{
				Element link = links.get( idx );
				String ref = link.attr( "abs:href" );
				
				// sending link to the link processor:
				String nodeName = consumer.consume( parentName, ref );
				if(nodeName == null)
					continue;
				System.out.print(".");
				linksQueue.put( ref, nodeName );
				
				count ++;
			}
			System.out.println("");
			
			
			log.debug( "Extracted [" + count + "] links; queue size [" + linksQueue.size() + "]." );
			totalCount += count;
		}
		
		return totalCount;
	}
	
	/**
	 * Read body of the specified URL
	 * @param url
	 * @return
	 */
	private String readUrl(String url)
	{
		//////////////////////////////////////////////////////
		// executing GET request to the specified url:
		HttpGet method = new HttpGet(url);
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try
		{
			HttpResponse response = httpClient.execute(method);
    		
    		// reading URL data:
    		HttpEntity entity = response.getEntity();
    		
			entity.writeTo( buffer );
		}
		catch ( IOException e )	{
			log.error("Failed to read data from URL [" + url + "]", e);
			return null;
		}
		finally {
			method.releaseConnection();
		}
		
		return new String(buffer.toByteArray()); // TODO: specify encoding

	}

	/**
	 * 
	 */
	public void destroy() {
		
	}

}

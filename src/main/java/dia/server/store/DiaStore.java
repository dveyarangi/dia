package dia.server.store;

import dia.api.DNode;
import dia.server.config.DiaConfig;

public interface DiaStore
{
	
	
	public void init(DiaConfig config);
	public void destroy();

	
	public void updateNode(DNode dianode);
	
	public void hyperlinkNodes(DNode dianodea, DNode dianodeb);
}
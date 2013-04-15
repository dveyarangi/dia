package dia.server.store;

import dia.api.ArticleNode;
import dia.api.CategoryNode;
import dia.api.DNode;
import dia.server.config.DiaConfig;

public interface DiaStore
{
	
	
	public void init(DiaConfig config);
	public void destroy();

	
	public void updateArticleNode(ArticleNode dianode);
	public void updateCategoryNode(CategoryNode dianode);
	
	public void hyperlinkNodes(DNode dianodea, DNode dianodeb);
	/**
	 * @param homeNode
	 * @param node
	 */
	public void addToCategory(DNode homeNode, ArticleNode node);
}
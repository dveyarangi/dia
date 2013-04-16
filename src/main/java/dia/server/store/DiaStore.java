package dia.server.store;

import dia.api.ArticleNode;
import dia.api.CategoryNode;
import dia.api.DNode;
import dia.server.config.DiaConfig;

public interface DiaStore
{
	
	
	public void init(DiaConfig config);
	public void destroy();

	public DNode getNode(String parentName);
	
	public boolean updateArticleNode(ArticleNode dianode);
	public boolean updateCategoryNode(CategoryNode dianode);
	
	public boolean addHyperlink(DNode dianodea, DNode dianodeb);
	/**
	 * @param homeNode
	 * @param node
	 */
	public boolean addToCategory(CategoryNode category, ArticleNode article);
	public boolean addSubcategory(CategoryNode category, CategoryNode subcategory);
}
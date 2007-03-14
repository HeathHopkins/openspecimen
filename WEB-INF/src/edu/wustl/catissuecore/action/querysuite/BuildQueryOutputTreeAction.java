package edu.wustl.catissuecore.action.querysuite;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.common.dynamicextensions.domaininterface.AttributeInterface;
import edu.wustl.catissuecore.actionForm.CategorySearchForm;
import edu.wustl.catissuecore.bizlogic.querysuite.QueryOutputTreeBizLogic;
import edu.wustl.catissuecore.util.global.Constants;
import edu.wustl.common.action.BaseAction;
import edu.wustl.common.beans.SessionDataBean;
import edu.wustl.common.querysuite.queryobject.impl.OutputTreeDataNode;

/**
 * This class is invoked when user clicks on a node from the tree. It loads the data required for tree formation.
 * @author deepti_shelar
 *
 */
public class BuildQueryOutputTreeAction extends BaseAction
{
	/**
	 * This method loads the data required for Query Output tree. 
	 * With the help of QueryOutputTreeBizLogic it generates a string which will be then passed to client side and tree is formed accordingly. 
	 * @param mapping mapping
	 * @param form form
	 * @param request request
	 * @param response response
	 * @throws Exception Exception
	 * @return ActionForward actionForward
	 */
	protected ActionForward executeAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	throws Exception
	{
		Map<Long,OutputTreeDataNode> idNodesMap = (Map<Long,OutputTreeDataNode>)request.getSession().getAttribute(Constants.ID_NODES_MAP);
		Map<Long, Map<AttributeInterface, String>>  columnMap = (Map<Long, Map<AttributeInterface, String>> )request.getSession().getAttribute(Constants.ID_COLUMNS_MAP);
		CategorySearchForm actionForm = (CategorySearchForm)form;
		SessionDataBean sessionData = getSessionData(request);
		String outputTreeStr = "";
		String id = actionForm.getNodeId();		
		QueryOutputTreeBizLogic outputTreeBizLogic = new QueryOutputTreeBizLogic();
		String actualParentNodeId = id.substring(id.lastIndexOf(Constants.NODE_SEPARATOR)+2,id.length());
		String[] nodeIds = id.split(Constants.NODE_SEPARATOR);
		if(id.endsWith(Constants.LABEL_TREE_NODE))
		{
			outputTreeStr = outputTreeBizLogic.updateTreeForLabelNode(id,idNodesMap,columnMap,sessionData);
		}
		else
		{
			nodeIds = actualParentNodeId.split(Constants.UNDERSCORE);
			Long idLong = new Long(nodeIds[0]); 
			String parentNodeId = nodeIds[1];
			OutputTreeDataNode parentNode = idNodesMap.get(idLong);
			outputTreeStr = outputTreeBizLogic.updateTree(id,parentNode, columnMap, parentNodeId, sessionData);	
		}
		response.setContentType("text/html");
		response.getWriter().write(outputTreeStr);
		return null;
	}
}

package com.liferay.portal.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.liferay.portal.util.PortalUtil;

public class LogoutHookAction extends LogoutAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	
    	super.execute(mapping, form, request, response);
    	
    	response.sendRedirect(PortalUtil.getPortalURL(request) + "/c/portal/login?redirect=" + request.getParameter("redirect"));
    	
    	return null;
    }
}

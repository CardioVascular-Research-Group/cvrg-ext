package com.liferay.portal.action;
/*
Copyright 2012 Johns Hopkins University Institute for Computational Medicine
This project incorporates tools whose development was funded in part by the NIH 
through the NHLBI grant: The Cardiovascular Research Grid (R24HL085343)
-----------------
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * @author Chris Jurado
 */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.liferay.portal.NewUserMissingFieldException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsAction;
import com.liferay.portal.kernel.struts.StrutsAction;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.struts.ActionConstants;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

import edu.jhu.cvrg.utilities.authentication.GlobusRESTAuthenticator;

public class UpdateNewUserFormAction extends Action{
	
	protected String institution;
	protected String department;
	protected String reason;
	protected String grant;
	
	public UpdateNewUserFormAction(){
		
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)  
			throws Exception{

		long userId = PortalUtil.getUserId(request);
		User newUser = UserLocalServiceUtil.getUser(userId);
		
		institution = ParamUtil.getString(request, "institution", "");
		department = ParamUtil.getString(request, "department", "");
		reason = ParamUtil.getString(request, "reason", "");
		grant = ParamUtil.getString(request, "grant", "");
		
		try{
			if(department.equals("") || reason.equals("")){
				throw new NewUserMissingFieldException();
			}
		}
		catch(NewUserMissingFieldException e){
			SessionErrors.add(request, e.getClass().getName());
			return mapping.findForward("portal.new_user_form");		
		}

		newUser.getExpandoBridge().setAttribute("Institution", institution);
		newUser.getExpandoBridge().setAttribute("Department", department);
		newUser.getExpandoBridge().setAttribute("Reason", reason);
		newUser.getExpandoBridge().setAttribute("Grant Number", grant);
		newUser.getExpandoBridge().setAttribute("NewUserFormComplete", true);
		
		long tableId = getTableId();
		long classNameId = getClassNameId();
		long classPk = newUser.getUserId();
		
		storeValue(getColumnId(tableId, "Institution"), institution, classNameId, tableId, classPk);
		storeValue(getColumnId(tableId, "Department"), department, classNameId, tableId, classPk);
		storeValue(getColumnId(tableId, "Reason"), reason, classNameId, tableId, classPk);
		storeValue(getColumnId(tableId, "Grant Number"), grant, classNameId, tableId, classPk);
		storeValue(getColumnId(tableId, "NewUserFormComplete"), "true", classNameId, tableId, classPk);

		newUser.setPasswordReset(false);
		newUser.setReminderQueryQuestion("generic-question");
		newUser.setReminderQueryAnswer("generic-answer");

		return mapping.findForward(ActionConstants.COMMON_REFERER_JSP);
	}
	
	private void storeValue(long columnId, String value, long classNameId, long tableId, long classPK){
		ExpandoValue expandoValue = null;
		try {
			expandoValue = ExpandoValueLocalServiceUtil.addValue(classNameId, tableId, columnId, classPK, value);
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}
	
	private long getColumnId(long tableId, String columnName){
		long columnId = 0L;
		try {
			ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(tableId, columnName);
			columnId = column.getColumnId();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		
		return columnId;
	}
	
	private long getClassNameId(){
		long companyId = PortalUtil.getDefaultCompanyId();
		long classNameId = 0L;
		ExpandoTable table = null;
		try {
			table = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(), "CUSTOM_FIELDS");
			classNameId = table.getClassNameId();
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return classNameId;
	}
	
	private long getTableId(){
		long companyId = PortalUtil.getDefaultCompanyId();
		long tableId = 0L;
		ExpandoTable table = null;
		try {
			table = ExpandoTableLocalServiceUtil.getTable(companyId, User.class.getName(), "CUSTOM_FIELDS");
			tableId = table.getTableId();
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return tableId;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}
}

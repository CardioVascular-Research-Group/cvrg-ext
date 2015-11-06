<%--
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/portal_settings/init.jsp" %>

<%
String openTsdbHost = 		PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.OPENTSDB_HOST, 			PropsValues.OPENTSDB_HOST);
String openTsdbSshUser = 	PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.OPENTSDB_SSH_USER, 		PropsValues.OPENTSDB_SSH_USER);
String openTsdbSshPassword =PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.OPENTSDB_SSH_PASSWORD, 	PropsValues.OPENTSDB_SSH_PASSWORD);

String openTsdbStrategy =	PrefsPropsUtil.getString(company.getCompanyId(), PropsKeys.OPENTSDB_STRATEGY, 	PropsValues.OPENTSDB_STRATEGY);

if (Validator.isNotNull(openTsdbSshPassword)) {
	openTsdbSshPassword = Portal.TEMP_OBFUSCATION_VALUE;
}
%>

<h3>OpenTSDB</h3>

<aui:fieldset>

	<aui:input cssClass="lfr-input-text-container" label="host" name='<%= "settings--" + PropsKeys.OPENTSDB_HOST + "--" %>' type="text" value="<%= openTsdbHost %>" />

	<aui:input cssClass="lfr-input-text-container" label="user-name" name='<%= "settings--" + PropsKeys.OPENTSDB_SSH_USER + "--" %>' type="text" value="<%= openTsdbSshUser %>" />

	<aui:input cssClass="lfr-input-text-container" label="password" name='<%= "settings--" + PropsKeys.OPENTSDB_SSH_PASSWORD + "--" %>' type="password" value="<%= openTsdbSshPassword %>" />
	
	
	<aui:select label="Strategy" name='<%= "settings--" + PropsKeys.OPENTSDB_STRATEGY + "--" %>'>
		<aui:option selected='<%= "Portlet".equals(openTsdbStrategy) %>' label="Portlet" 	value="Portlet" />
		<aui:option selected='<%= "WebService".equals(openTsdbStrategy) %>' label="WebService" 	value="WebService" />
	</aui:select>
	
</aui:fieldset>
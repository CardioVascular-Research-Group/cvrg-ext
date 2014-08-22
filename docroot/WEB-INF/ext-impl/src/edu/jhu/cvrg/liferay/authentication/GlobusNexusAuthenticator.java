package edu.jhu.cvrg.liferay.authentication;
/*
Copyright 2012 Johns Hopkins University Institute for Computational Medicine

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
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AuthException;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PropsValues;

import edu.jhu.cvrg.utilities.authentication.AuthenticationMethod;
import edu.jhu.cvrg.utilities.authentication.MainAuthenticator;

public class GlobusNexusAuthenticator implements Authenticator{
	
	static org.apache.log4j.Logger logger = Logger.getLogger(GlobusNexusAuthenticator.class);

	public int authenticateByEmailAddress(long companyId, String emailAddress,
			String password, Map<String, String[]> headerMap,
			Map<String, String[]> parameterMap) throws AuthException {
		
		logger.info("Authenticating by email");
			
		try {
			User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
			return authenticateByScreenName(companyId, user.getScreenName(), password, headerMap, null);
		} catch (NoSuchUserException e){
			logger.error("User not found for E-mail address " + emailAddress);
			return FAILURE;
		} catch (PortalException e) {
			e.printStackTrace();
			return DNE;
		} catch (SystemException e) {
			e.printStackTrace();
			return FAILURE;
		}
	}

	public int authenticateByScreenName(long companyId, String screenName, String password, Map<String, 
			String[]> headerMap, Map<String, String[]> parameterMap) throws AuthException {
		
		logger.info("Authenticating by screenName for " + screenName);

		if(isDefaultAdmin(screenName, password)){
			logger.info("Administrator user " + screenName + " identified.");
			return SUCCESS;
		}
		else{
			logger.info("Trying Axis2 user...");
		}
		
		if(isServiceUser(screenName, password)){
			logger.info("Axis2 user " + screenName + " identified.");
			return SUCCESS;
		}
		
		logger.info("User is not Admin or Service.  Proceeding with Globus Online authentication.");

		MainAuthenticator authenticator = new MainAuthenticator();
		
		@SuppressWarnings("unused")
		User user = null;
		String url = "";
		String community = "";

		try {
			url = PropsValues.GLOBUS_LINK;
			logger.info("Using Globus URL " + PropsValues.GLOBUS_LINK);
		} catch (Exception e) {
			logger.warn("Unable to load Globus URL.  Relying on authenticator package default.");
		}
		
		try {
			community = PropsValues.GLOBUS_COMMUNITY;
			logger.info("Using Globus community " + community);
		} catch (Exception e) {
			logger.warn("Unable to load Globus community.  Relying on authenticator package default.");
		}
			
		String[] args = { screenName, password, url, community };
			if (authenticator.authenticate(args, AuthenticationMethod.GLOBUS_REST)) {
			try {
				user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
			} catch (NoSuchUserException e) {
				user = createNewUser(authenticator.getUserEmail(), screenName, authenticator.getUserFullname().split(" "), companyId);
			} catch (PortalException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			logger.info("Authentication successful.");
			return SUCCESS;
		} else {
			logger.info("Authentication failed.");
			return FAILURE;
		}
	}

	public int authenticateByUserId(long companyId, long userId,
			String password, Map<String, String[]> headerMap,
			Map<String, String[]> parameterMap) throws AuthException {

		try {
			User user = UserLocalServiceUtil.getUserById(companyId, userId);
			return authenticateByScreenName(companyId, user.getScreenName(), password, headerMap, null);
		} catch (NoSuchUserException e){
			logger.error("User not found for user ID " + userId);
			return FAILURE;
		} catch (PortalException e) {
			e.printStackTrace();
			return DNE;
		} catch (SystemException e) {
			e.printStackTrace();
			return FAILURE;
		}
	}
	
	private User createNewUser(String userEmail, String screenName, String[] userName, long companyId){

		String creatingUserProperty = null;

        try {
			creatingUserProperty = PropsValues.LIFERAY_ADMIN_USER;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		User creatingUser = null;
		User newUser = null;
		try {
			creatingUser = UserLocalServiceUtil.getUserByEmailAddress(companyId, creatingUserProperty);
				
			newUser = UserLocalServiceUtil.addUser(creatingUser.getUserId(), companyId, false, "test", "test", 
					false, screenName, userEmail, 0L, "", Locale.US, userName[0], "", userName[1], 
					0, 0, false, 0, 1,1970, "User", null, null, null, null, false, new ServiceContext());
			
			Role memberRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.SITE_MEMBER);
			Role userRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.USER);
			
			long[] roles = {memberRole.getRoleId(), userRole.getRoleId()};
			RoleLocalServiceUtil.setUserRoles(newUser.getUserId(), roles);
		
		} catch (PortalException e) {

			e.printStackTrace();
		} catch (SystemException e) { 
			e.printStackTrace();
		}		
		return newUser;
	}
	
	private boolean isServiceUser(String screenName, String password){
		
		try{
			ServiceProperties serviceProperties = ServiceProperties.getInstance();
		
			if(serviceProperties.getProperty("liferay.ws.user").equals(screenName) &&
					serviceProperties.getProperty("liferay.ws.password").equals(password)){
				logger.info("Service User found.");
					return true;
			}
			else{
				logger.info("Service User not found.");
				return false;
			}
		} catch (Exception e){
			logger.info("Service User config not found.");
			return false;
		}
	}
	
	private boolean isDefaultAdmin(String screenName, String password){

		logger.info("Defaults are " + PropsValues.DEFAULT_ADMIN_SCREEN_NAME + " and " + PropsValues.DEFAULT_ADMIN_PASSWORD);
			
		try{
			if(PropsValues.DEFAULT_ADMIN_SCREEN_NAME.equals(screenName) &&
					PropsValues.DEFAULT_ADMIN_PASSWORD.equals(password)){
				logger.info("Default Admin match found.");
				return true;
			}
			else {
				logger.info("Default Admin match not found.");
				return false;
			}
		} catch (Exception e){
			logger.info("Admin User config not found.");
			return false;
		}	
	}
}

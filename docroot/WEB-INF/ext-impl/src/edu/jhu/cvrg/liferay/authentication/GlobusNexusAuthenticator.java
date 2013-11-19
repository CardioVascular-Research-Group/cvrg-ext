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
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AuthException;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PropsValues;

import edu.jhu.cvrg.utilities.authentication.AuthenticationMethod;
import edu.jhu.cvrg.utilities.authentication.MainAuthenticator;

public class GlobusNexusAuthenticator implements Authenticator{
	
	static org.apache.log4j.Logger logger = Logger.getLogger(GlobusNexusAuthenticator.class);

	@Override
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

	@Override
	public int authenticateByScreenName(long companyId, String screenName,
			String password, Map<String, String[]> headerMap,
			Map<String, String[]> parameterMap) throws AuthException {
		
		logger.info("Authenticating by screenName");

		MainAuthenticator authenticator = new MainAuthenticator();
		
		@SuppressWarnings("unused")
		User user = null;
		String url = "";
		String community = "";

		try {
			url = PropsValues.GLOBUS_LINK;
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("No Globus URL found.  Relying on default.");
		}
		
		try {
			community = PropsValues.GLOBUS_COMMUNITY;
			logger.info("Using Globus community " + community);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("No Globus community found.  Relying on default.");
		}
			
		String[] args = { screenName, password, url, community };
		
		if (authenticator.authenticate(args, AuthenticationMethod.GLOBUS_REST)) {
			try {
				user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
			} catch (NoSuchUserException e){
				user = createNewUser(authenticator.getUserEmail(), authenticator.getUserFullname().split(" "), companyId);
			} catch (PortalException e) {
				e.printStackTrace();
			} catch (SystemException e) {
				e.printStackTrace();
			}
			logger.info("Authentication successful.");
			return SUCCESS;
		}
		else{
			logger.info("Authentication failed.");
			return FAILURE;
		}
	}

	@Override
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
	
	private User createNewUser(String userEmail, String[] userName, long companyId){

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
				
			UserLocalServiceUtil.addUser(creatingUser.getUserId(), companyId, false, "test", "test", false, (userName[0] + userName[1]), userEmail, 0L, "", Locale.US,
					userName[0], "", userName[1], 0, 0, false, 0, 1,1970, "User", null, null, null,
					null, false, new ServiceContext());
		
		} catch (PortalException e) {

			e.printStackTrace();
		} catch (SystemException e) { 
			e.printStackTrace();
		}		
		return newUser;
	}
}
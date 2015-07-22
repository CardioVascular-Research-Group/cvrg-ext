package edu.jhu.cvrg.liferay.authentication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eurekaclinical.scribeupext.profile.EurekaAttributesDefinition;
import org.eurekaclinical.scribeupext.provider.GlobusProvider;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.UserProfile;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsValues;

public class GlobusOAuthAutoLogin implements AutoLogin{

	private static final int CLEANER_INTERVAL = 30;
	private static final TimeUnit INTERVAL_UNIT = TimeUnit.MINUTES;
	
	private static Map<String, String> sessionRedirectMap;
	
	static Logger logger = Logger.getLogger(GlobusOAuthAutoLogin.class);
	
	@Override
	public String[] login(HttpServletRequest request, HttpServletResponse response) throws AutoLoginException {
		
		String oauthCode = ParamUtil.get(request, "code", "");
		
		long companyId = PortalUtil.getCompanyId(request);
		
		try {
			if(PrefsPropsUtil.getBoolean(companyId, PropsKeys.GLOBUS_OAUTH_ENABLED,PropsValues.GLOBUS_OAUTH_ENABLED) && oauthCode != null && !oauthCode.isEmpty()){
				String redirectURL = GlobusOAuthAutoLogin.getSessionRedirectMap().get(request.getSession().getId());
				if(redirectURL != null && !redirectURL.isEmpty()){
					redirectURL = redirectURL.split("\\|")[0];
					GlobusOAuthAutoLogin.getSessionRedirectMap().remove(request.getSession().getId());
					
				}
				
				GlobusProvider provider = GlobusOAuthAutoLogin.getGlobusOAuthURL(request);
				
				// Trade the Request Token and Verifier for the Access Token
				logger.info("Get user's OAuth credential...");
				OAuthCredential credential = new OAuthCredential(null, null, oauthCode, provider.getType());
				logger.debug("Credential is " + credential);
				
				// Now, get the user's profile (access token is retrieved behind the scenes)
				UserProfile userProfile = provider.getUserProfile(credential);
				logger.debug("The user's profile is:");
				logger.debug(userProfile.getAttributes());
				
				
				String screenName = userProfile.getAttributes().get(EurekaAttributesDefinition.USERNAME).toString();
				User user = null;
				try {
					user = UserLocalServiceUtil.getUserByScreenName(companyId, userProfile.getAttributes().get(EurekaAttributesDefinition.USERNAME).toString());
				} catch (NoSuchUserException e) {
					logger.info("New user! Creating a new Liferay user...");
					String[] names = userProfile.getAttributes().containsKey(EurekaAttributesDefinition.FULLNAME) ? userProfile.getAttributes().get(EurekaAttributesDefinition.FULLNAME).toString().split(" ") : new String[]{"", ""};
					
					if(userProfile.getAttributes().containsKey(EurekaAttributesDefinition.FIRSTNAME)){
						names[0] = userProfile.getAttributes().get(EurekaAttributesDefinition.FIRSTNAME).toString();
					}
					
					if(userProfile.getAttributes().containsKey(EurekaAttributesDefinition.LASTNAME) ){
						names[1] = userProfile.getAttributes().get(EurekaAttributesDefinition.LASTNAME).toString();
					}
					
					user = createNewUser(userProfile.getAttributes().get(EurekaAttributesDefinition.EMAIL).toString(), 
										  screenName, 
										  names, 
										  companyId);
				} catch (PortalException e) {
					logger.error("Error in liferay user verification. Message = " + e.getMessage());
				} catch (SystemException e) {
					logger.error("Error in liferay user verification. Message = " + e.getMessage());
				}
				
				if(user != null){
					request.setAttribute(AutoLogin.AUTO_LOGIN_REDIRECT_AND_CONTINUE, redirectURL);
					return new String[]{String.valueOf(user.getUserId()), user.getPassword(), String.valueOf(user.isPasswordEncrypted())};
				}
			}
		} catch (SystemException e) {
			logger.error("Error in Liferay constants verification. Message = " + e.getMessage());
		} catch (Exception e) {
			logger.error("General error in Globus OAuth auto login. Message = " + e.getMessage());
		}		
		
		return null;
	}
	
    public static GlobusProvider getGlobusOAuthURL(HttpServletRequest request){
    	
    	GlobusProvider provider = new GlobusProvider();
    	
    	try {
    		
    		long companyId = PortalUtil.getCompanyId(request);
			provider.setKey(PrefsPropsUtil.getString(companyId, PropsKeys.GLOBUS_OAUTH_USER,PropsValues.GLOBUS_OAUTH_USER));
			provider.setSecret(PrefsPropsUtil.getString(companyId, PropsKeys.GLOBUS_OAUTH_PASSWORD,PropsValues.GLOBUS_OAUTH_PASSWORD));
			
			StringBuilder url = new StringBuilder("http"); 
			
			if(request.isSecure()){
				url.append('s');
			}
			
			url.append("://");
			url.append(request.getServerName());
			
			if(request.getServerPort() != 80 && request.getServerPort() != 443){
				url.append(':').append(request.getServerPort());
			}
			
			url.append("/c/portal/login");
					
			provider.setCallbackUrl(url.toString());
			
		} catch (SystemException e) {
			logger.error("Error in Globus OAuth URL generation. Message = " + e.getMessage());
		}
		
		return provider;
    	
    }
    
    private User createNewUser(String userEmail, String screenName, String[] userName, long companyId){

		String creatingUserProperty = null;

        try {
			creatingUserProperty = PropsValues.LIFERAY_ADMIN_USER;
		} catch (Exception e1) {
			logger.error("Error in liferay user creation. Message = " + e1.getMessage());
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
			logger.error("Error in liferay user creation. Message = " + e.getMessage());
		} catch (SystemException e) { 
			logger.error("Error in liferay user creation. Message = " + e.getMessage());
		}		
		return newUser;
	}
    
    public static void addRedirect(String sessionId, String URL){
    	GlobusOAuthAutoLogin.getSessionRedirectMap().put(sessionId, URL+"|"+System.currentTimeMillis());
    }
    
    public static Map<String, String> getSessionRedirectMap(){
    	if(sessionRedirectMap == null){
			sessionRedirectMap = new HashMap<String, String>();
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(new Thread(){public void run(){ GlobusOAuthAutoLogin.sessionCleanUp(); };}, CLEANER_INTERVAL, CLEANER_INTERVAL, INTERVAL_UNIT);
		}
    	return sessionRedirectMap;
    }
    
    public static void sessionCleanUp(){
		if(sessionRedirectMap != null && !sessionRedirectMap.isEmpty()){
			for (String key : sessionRedirectMap.keySet()) {
				String data = sessionRedirectMap.get(key);
				long creationTime = 0;
				try{
					creationTime = Long.parseLong(data.split("\\|")[1]);
				}catch (Exception e){ }
				
				if(data != null && !data.isEmpty() &&  (System.currentTimeMillis() - creationTime >= INTERVAL_UNIT.toMillis(CLEANER_INTERVAL)) ){
					sessionRedirectMap.remove(key);
				}
			}
		}
	}
}

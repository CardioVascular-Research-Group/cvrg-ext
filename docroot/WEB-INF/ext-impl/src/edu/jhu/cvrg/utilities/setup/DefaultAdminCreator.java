package edu.jhu.cvrg.utilities.setup;

import org.apache.log4j.Logger;

import com.liferay.portal.DuplicateUserEmailAddressException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PropsValues;

public class DefaultAdminCreator {
	
	static org.apache.log4j.Logger logger = Logger.getLogger(DefaultAdminCreator.class);

	public static void createAdmin(long companyId){
		
		try{
			String screenName = PropsValues.DEFAULT_ADMIN_SCREEN_NAME;
			String firstName = PropsValues.DEFAULT_ADMIN_FIRST_NAME;
			String lastName = PropsValues.DEFAULT_ADMIN_LAST_NAME;
			String emailAddress = PropsValues.LIFERAY_ADMIN_USER;
			String password = PropsValues.DEFAULT_ADMIN_PASSWORD;
		
			User adminUser = UserLocalServiceUtil.addDefaultAdminUser(companyId, screenName, 
				emailAddress, LocaleUtil.getDefault(), firstName, "", lastName);
			
			UserLocalServiceUtil.updatePassword(adminUser.getUserId(), password, password, false);
		}
		catch(DuplicateUserEmailAddressException e){
			logger.info("Account already exists with this E-mail address.");
		}
		catch(UserPasswordException p){
			int type = ((UserPasswordException)p).getType();
			switch(type){
			case UserPasswordException.PASSWORD_SAME_AS_CURRENT:
				logger.error("New Admin password same as current.");
				break;
			case UserPasswordException.PASSWORD_ALREADY_USED:
				logger.error("New Admin password is an old password.");
				break;
			case UserPasswordException.PASSWORD_INVALID:
				logger.error("New Admin password is an invalid password.");
				break;
			case UserPasswordException.PASSWORD_LENGTH:
				logger.error("New Admin password isn't the right length.");
				break;
			case UserPasswordException.PASSWORD_NOT_CHANGEABLE:
				logger.error("New Admin password can't be changed.");
				break;
			case UserPasswordException.PASSWORD_CONTAINS_TRIVIAL_WORDS:
				logger.error("New Admin password contains trivial words.");
				break;
			case UserPasswordException.PASSWORD_TOO_TRIVIAL:
				logger.error("New Admin password is too trivial.");
				break;
			case UserPasswordException.PASSWORD_TOO_YOUNG:
				logger.error("New Admin password is too young.");
				break;
			case UserPasswordException.PASSWORDS_DO_NOT_MATCH:
				logger.error("New Admin passwords don't match.");
				break;
			default:
				logger.error("Unidentified password error.");
			}
		}

		catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}
}

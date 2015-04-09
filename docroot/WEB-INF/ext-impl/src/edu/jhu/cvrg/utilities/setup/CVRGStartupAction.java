package edu.jhu.cvrg.utilities.setup;

import org.apache.log4j.Logger;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.events.AppStartupAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

import edu.jhu.cvrg.utilities.setup.UserFieldCreator;

public class CVRGStartupAction extends AppStartupAction{
	
	static org.apache.log4j.Logger logger = Logger.getLogger(CVRGStartupAction.class);

	@Override
	public void run(String[] ids) {
		
		String webId = PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID);
		Company company = null;
		long groupId = 0L;
		try {
			company = CompanyLocalServiceUtil.getCompanyByWebId(webId);
			groupId = GroupLocalServiceUtil.getGroup(company.getCompanyId(), "Guest").getGroupId();
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		long companyId = company.getCompanyId();
		
		DefaultAdminCreator.createAdmin(companyId);
		UserFieldCreator.createCustomFields(companyId);
		disableTest(companyId);
		
		DocumentLibraryInitilizer.createWaveformFolder(companyId, groupId);
	}
	
	private void disableTest(long companyId){

		User testUser = null;
		
		try {
			testUser = UserLocalServiceUtil.getUserByEmailAddress(companyId, "test@liferay.com");
			UserLocalServiceUtil.deleteUser(testUser.getUserId());
		} catch (NoSuchUserException e){
			logger.info("test user already deleted for portal with companyId" + companyId);
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		
	}
}

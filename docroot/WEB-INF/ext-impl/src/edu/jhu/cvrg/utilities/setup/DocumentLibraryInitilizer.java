package edu.jhu.cvrg.utilities.setup;

import java.util.List;

import org.apache.log4j.Logger;

import com.liferay.counter.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.model.ResourceAction;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.ResourcePermission;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourceActionLocalServiceUtil;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

public class DocumentLibraryInitilizer {

	static org.apache.log4j.Logger logger = Logger.getLogger(DocumentLibraryInitilizer.class);

	private static long ROOT_FOLDER_ID = 0L;
	
	private static String WAVEFORM_FOLDER_NAME = "waveform";
	
	public static void createWaveformFolder(long companyId, long groupId) {
		
		try {
			User admin = UserLocalServiceUtil.getUserByScreenName(companyId, PropsValues.DEFAULT_ADMIN_SCREEN_NAME);
			
			List<Folder> rootChildrenFolder = DLAppLocalServiceUtil.getFolders(groupId, ROOT_FOLDER_ID);
			
			if(rootChildrenFolder != null){
				Folder waveformFolder = null;
				for (Folder folder : rootChildrenFolder) {
					if(WAVEFORM_FOLDER_NAME.equals(folder.getName())){
						waveformFolder = folder;
						break;
					}
				}
				if(waveformFolder == null){
					ServiceContext service = new ServiceContext();
					
					waveformFolder = DLAppLocalServiceUtil.addFolder(admin.getUserId(), groupId, ROOT_FOLDER_ID, WAVEFORM_FOLDER_NAME, "", service);
					logger.info("Waveform folder created.");
				}else{
					logger.info("Already exists a Waveform folder.");
				}
				
				if(waveformFolder != null) {
					Role userRole = RoleLocalServiceUtil.getRole(companyId, "User");

					ResourcePermission resourcePermission = null;
					resourcePermission = ResourcePermissionLocalServiceUtil.createResourcePermission(CounterLocalServiceUtil.increment());
					resourcePermission.setCompanyId(companyId);
					resourcePermission.setName(DLFolder.class.getName());
					resourcePermission.setScope(ResourceConstants.SCOPE_INDIVIDUAL);
					resourcePermission.setPrimKey(String.valueOf(waveformFolder.getPrimaryKey()));
					resourcePermission.setRoleId(userRole.getRoleId());
					
					ResourceAction resourceActionAccess = ResourceActionLocalServiceUtil.getResourceAction(DLFolder.class.getName(), ActionKeys.ACCESS);
					ResourceAction resourceActionView = ResourceActionLocalServiceUtil.getResourceAction(DLFolder.class.getName(), ActionKeys.VIEW);
					ResourceAction resourceActionAddSubFolder = ResourceActionLocalServiceUtil.getResourceAction(DLFolder.class.getName(), ActionKeys.ADD_SUBFOLDER);
					
					resourcePermission.setActionIds(resourceActionView.getBitwiseValue()+resourceActionAccess.getBitwiseValue()+resourceActionAddSubFolder.getBitwiseValue());
					
					ResourcePermissionLocalServiceUtil.addResourcePermission(resourcePermission);
					
					logger.info("Change the waveform folder permissions.");
				}else {
					logger.error("Error on Document library initialization. Waveform folder not found.");
				}
			}
			
			logger.info("Document library initialization completed.");
			
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}
		
		
	}
	
}

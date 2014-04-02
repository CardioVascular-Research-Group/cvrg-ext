package edu.jhu.cvrg.utilities.setup;

import com.liferay.portal.events.AppStartupAction;

import edu.jhu.cvrg.utilities.setup.UserFieldCreator;

public class CVRGStartupAction extends AppStartupAction{

	@Override
	public void run(String[] ids) {
		
		UserFieldCreator.createCustomFields();
		
	}
	
}

#cvrg-ext
Extension for generic CVRG portal projects

Loads in: /opt/liferay/liferay-plugins-sdk-6.1.1/ext

IMPORTANT: Before installing this Extension plugin you MUST edit the included portal-ext.properties file to provide a default admin e-mail address, username and password. Otherwise the installation will fail to create a new default Admin account, even though it will still delete the default Liferay admin account. This will leave the portal unuseable.

Reccommended installation steps:

- Shut down the Liferay Portal
- Run the Maven build for cvrg-ext
- Open the resulting .war file and edit the portal-ext.properties file to provide the necessary fields
- Start the Liferay Portal

Additional configuration settings added to portal by this ext:

	globus.url for using Globus Nexus authentication
	globus.community for Globus Nexus authentication 
	globus.account.url for Globus Nexus authentication
	opentsdb.url for setting up the OpenTSDB time series database for use with Waveform components
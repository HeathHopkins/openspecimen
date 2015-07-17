package com.krishagni.catissueplus.core.init;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.krishagni.catissueplus.core.common.service.ConfigChangeListener;
import com.krishagni.catissueplus.core.common.service.ConfigurationService;
import com.krishagni.catissueplus.core.common.util.Utility;
import com.krishagni.catissueplus.core.de.UserContextImpl;
import com.krishagni.catissueplus.core.de.ui.StorageContainerControlFactory;
import com.krishagni.catissueplus.core.de.ui.StorageContainerMapper;
import com.krishagni.catissueplus.core.de.ui.UserControlFactory;
import com.krishagni.catissueplus.core.de.ui.UserFieldMapper;

import edu.common.dynamicextensions.domain.nui.factory.ControlManager;
import edu.common.dynamicextensions.nutility.DEApp;
import edu.common.dynamicextensions.query.PathConfig;
import edu.wustl.dynamicextensions.formdesigner.mapper.ControlMapper;
import edu.wustl.dynamicextensions.formdesigner.usercontext.CSDProperties;

public class DeInitializer implements InitializingBean {
	private static final String QUERY_PATH_CFG = "/com/krishagni/catissueplus/core/de/query/paths.xml";
	
	private PlatformTransactionManager transactionManager;
	
	private ConfigurationService cfgSvc;
	
	private DataSource dataSource;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void setCfgSvc(ConfigurationService cfgSvc) {
		this.cfgSvc = cfgSvc;		 
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, Object> localeSettings = cfgSvc.getLocaleSettings();		
		String dateFormat = (String)localeSettings.get("deBeDateFmt");
		String timeFormat = (String)localeSettings.get("timeFmt");
		String dataDir = cfgSvc.getDataDir();
		
		String dir = new StringBuilder(dataDir).append(File.separator)
			.append("de-file-data").append(File.separator)
			.toString();
		
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				throw new RuntimeException("Error couldn't create directory for storing de file data");
			}
		}
		
		CSDProperties.getInstance().setUserContextProvider(new UserContextImpl());
					
		DEApp.init(dataSource, transactionManager, dir, dateFormat, timeFormat);
		ControlManager.getInstance().registerFactory(UserControlFactory.getInstance());			
		ControlMapper.getInstance().registerControlMapper("userField", new UserFieldMapper());
		
		ControlManager.getInstance().registerFactory(StorageContainerControlFactory.getInstance());
		ControlMapper.getInstance().registerControlMapper("storageContainer", new StorageContainerMapper());

		InputStream in = null;
		try {
			in = Utility.getResourceInputStream(QUERY_PATH_CFG);
			PathConfig.initialize(in);			
		} finally {
			IOUtils.closeQuietly(in);
		}
		
		cfgSvc.registerChangeListener("common", new ConfigChangeListener() {			
			@Override
			public void onConfigChange(String name, String value) {
				if (!name.equals("locale")) {
					return;
				}
				
				Map<String, Object> localeSettings = cfgSvc.getLocaleSettings();		
				DEApp.setDateFormat((String)localeSettings.get("deBeDateFmt"));
				DEApp.setTimeFormat((String)localeSettings.get("timeFmt"));				
			}
		});		
	}
}

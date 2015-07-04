package com.krishagni.catissueplus.core.common.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;

public class LabelPrintRule {
	private String labelType;
	
	private IpAddressMatcher ipAddressMatcher;
	
	private String userLogin;
	
	private String printerName;
	
	private List<LabelTmplToken> dataTokens = new ArrayList<LabelTmplToken>();
	
	private MessageSource messageSource;

	public String getLabelType() {
		return labelType;
	}

	public void setLabelType(String labelType) {
		this.labelType = labelType;
	}

	public IpAddressMatcher getIpAddressMatcher() {
		return ipAddressMatcher;
	}

	public void setIpAddressMatcher(IpAddressMatcher ipAddressMatcher) {
		this.ipAddressMatcher = ipAddressMatcher;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public List<LabelTmplToken> getDataTokens() {
		return dataTokens;
	}

	public void setDataTokens(List<LabelTmplToken> dataTokens) {
		this.dataTokens = dataTokens;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public boolean isApplicableFor(User user, String ipAddr) {
		if (!isWildCard(userLogin) && !user.getLoginName().equals(userLogin)) {
			return false;
		}
		
		if (ipAddressMatcher != null && !ipAddressMatcher.matches(ipAddr)) {
			return false;
		}
		
		return true;
	}
	
	public String formatPrintData(Object obj) {
		try {
			Map<String, String> dataItems = new HashMap<String, String>();
			for (LabelTmplToken token : dataTokens) {
				String propName = messageSource.getMessage(token.getName(), null, Locale.getDefault());
				dataItems.put(propName, token.getReplacement(obj));
			}
			
			return new ObjectMapper().writeValueAsString(dataItems);			
		} catch (Exception e) {
			throw OpenSpecimenException.serverError(e);
		}
	}
	
	protected boolean isWildCard(String str) {
		return StringUtils.isNotBlank(str) && str.trim().equals("*");
	}
}
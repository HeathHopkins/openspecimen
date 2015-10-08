
package com.krishagni.catissueplus.core.administrative.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocol;
import com.krishagni.catissueplus.core.administrative.domain.DistributionProtocolDistSite;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.de.events.SavedQuerySummary;

public class DistributionProtocolDetail extends DistributionProtocolSummary {

	private String instituteName;

	private String defReceivingSiteName;
	
	private String irbId;

	private String activityStatus;

	private SavedQuerySummary report;
	
	private Map<String, List<String>> distributingSites = new HashMap<String, List<String>>();

	public String getInstituteName() {
		return instituteName;
	}

	public void setInstituteName(String instituteName) {
		this.instituteName = instituteName;
	}
	
	public String getDefReceivingSiteName() {
		return defReceivingSiteName;
	}
	
	public void setDefReceivingSiteName(String defReceivingSiteName) {
		this.defReceivingSiteName = defReceivingSiteName;
	}

	public String getIrbId() {
		return irbId;
	}

	public void setIrbId(String irbId) {
		this.irbId = irbId;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public SavedQuerySummary getReport() {
		return report;
	}

	public void setReport(SavedQuerySummary report) {
		this.report = report;
	}
	
	public Map<String, List<String>> getDistributingSites() {
		return distributingSites;
	}
	
	public void setDistributingSites(Map<String, List<String>> distributingSites) {
		this.distributingSites = distributingSites;
	}

	public static DistributionProtocolDetail from(DistributionProtocol distributionProtocol) {
		DistributionProtocolDetail detail = new DistributionProtocolDetail();
		
		copy(distributionProtocol, detail);
		detail.setInstituteName(distributionProtocol.getInstitute().getName());
		if (distributionProtocol.getDefReceivingSite() != null) {
			detail.setDefReceivingSiteName(distributionProtocol.getDefReceivingSite().getName());
		}
		
		detail.setIrbId(distributionProtocol.getIrbId());
		detail.setPrincipalInvestigator(UserSummary.from(distributionProtocol.getPrincipalInvestigator()));
		detail.setActivityStatus(distributionProtocol.getActivityStatus());
		if (distributionProtocol.getReport() != null) {
			detail.setReport(SavedQuerySummary.fromSavedQuery(distributionProtocol.getReport()));
		}
		
		setDistributingSites(detail, distributionProtocol);
		
		return detail;
	}

	public static List<DistributionProtocolDetail> from(List<DistributionProtocol> distributionProtocols) {
		List<DistributionProtocolDetail> list = new ArrayList<DistributionProtocolDetail>();
		
		for (DistributionProtocol dp : distributionProtocols) {
			list.add(from(dp));
		}
		
		return list;
	}
	
	private static void setDistributingSites(DistributionProtocolDetail detail, DistributionProtocol dp) {
		for (DistributionProtocolDistSite distSite: dp.getDistributingSites()) {
			String instituteName = distSite.getInstitute().getName();
			List<String> siteNames = detail.getDistributingSites().get(instituteName);
			if (siteNames == null) {
				siteNames = new ArrayList<String>();
				detail.distributingSites.put(instituteName, siteNames);
			}
			
			if (distSite.getSite() != null) {
				siteNames.add(distSite.getSite().getName());
			}
		}
	}
}

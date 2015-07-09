
package com.krishagni.catissueplus.core.administrative.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.krishagni.catissueplus.core.administrative.domain.factory.DistributionProtocolErrorCode;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.util.Status;
import com.krishagni.catissueplus.core.common.util.Utility;
import com.krishagni.catissueplus.core.de.domain.SavedQuery;

public class DistributionProtocol {
	private static final String ENTITY_NAME = "distribution_protocol";

	private Long id;
	
	private Institute institute;

	private User principalInvestigator;

	private String title;

	private String shortTitle;

	private String irbId;

	private Date startDate;
	
	private Date endDate;

	private String activityStatus;
	
	private SavedQuery report;
	
	private Set<DistributionOrder> distributionOrders = new HashSet<DistributionOrder>();
	
	public static String getEntityName() {
		return ENTITY_NAME;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Institute getInstitute() {
		return institute;
	}

	public void setInstitute(Institute institute) {
		this.institute = institute;
	}

	public User getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(User principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public String getIrbId() {
		return irbId;
	}

	public void setIrbId(String irbId) {
		this.irbId = irbId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public SavedQuery getReport() {
		return report;
	}

	public void setReport(SavedQuery report) {
		this.report = report;
	}

	public Set<DistributionOrder> getDistributionOrders() {
		return distributionOrders;
	}

	public void setDistributionOrders(Set<DistributionOrder> distributionOrders) {
		this.distributionOrders = distributionOrders;
	}

	public void update(DistributionProtocol distributionProtocol) {
		if (distributionProtocol.getActivityStatus().equals(Status.ACTIVITY_STATUS_DISABLED.getStatus())) {
			setShortTitle(Utility.getDisabledValue(distributionProtocol.getShortTitle(), 50));
			setTitle(Utility.getDisabledValue(distributionProtocol.getTitle(), 255));
		}
		else {
			setShortTitle(distributionProtocol.getShortTitle());
			setTitle(distributionProtocol.getTitle());
		}
		setIrbId(distributionProtocol.getIrbId());
		setInstitute(distributionProtocol.getInstitute());
		setPrincipalInvestigator(distributionProtocol.getPrincipalInvestigator());
		setStartDate(distributionProtocol.getStartDate());
		setEndDate(distributionProtocol.getEndDate());
		setActivityStatus(distributionProtocol.getActivityStatus());
		setReport(distributionProtocol.getReport());
	}
	
	public List<DependentEntityDetail> getDependentEntities() {
		return DependentEntityDetail
				.singletonList(DistributionOrder.getEntityName(), getDistributionOrders().size());
	}
	
	public void delete() {
		List<DependentEntityDetail> dependentEntities = getDependentEntities();
		if (!dependentEntities.isEmpty()) {
			throw OpenSpecimenException.userError(DistributionProtocolErrorCode.REF_ENTITY_FOUND);
		}
		
		setShortTitle(Utility.getDisabledValue(getShortTitle(), 50));
		setTitle(Utility.getDisabledValue(getTitle(), 255));
		setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());
	}
	
}

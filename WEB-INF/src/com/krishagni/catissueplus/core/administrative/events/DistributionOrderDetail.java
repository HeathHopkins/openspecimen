package com.krishagni.catissueplus.core.administrative.events;

import java.util.ArrayList;
import java.util.List;

import com.krishagni.catissueplus.core.administrative.domain.DistributionOrder;
import com.krishagni.catissueplus.core.common.events.UserSummary;

public class DistributionOrderDetail extends DistributionOrderSummary {
	private UserSummary distributor;
	
	private String trackingUrl;
	
	private String comments;
	
	private List<DistributionOrderItemDetail> orderItems = new ArrayList<DistributionOrderItemDetail>();
	
	private String activityStatus;

	public UserSummary getDistributor() {
		return distributor;
	}

	public void setDistributor(UserSummary distributor) {
		this.distributor = distributor;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public List<DistributionOrderItemDetail> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<DistributionOrderItemDetail> orderItems) {
		this.orderItems = orderItems;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public static DistributionOrderDetail from(DistributionOrder order) {
		DistributionOrderDetail detail = new DistributionOrderDetail();
		detail.setId(order.getId());
		detail.setName(order.getName());
		detail.setDistributionProtocol(DistributionProtocolDetail.from(order.getDistributionProtocol()));
		detail.setInstituteName(order.getInstitute().getName());
		if (order.getSite() != null) {
			detail.setSiteId(order.getSite().getId());
			detail.setSiteName(order.getSite().getName());
		}
		
		detail.setRequester(UserSummary.from(order.getRequester()));
		detail.setCreationDate(order.getCreationDate());
		detail.setExecutionDate(order.getExecutionDate());
		detail.setOrderItems(DistributionOrderItemDetail.from(order.getOrderItems()));		
		detail.setStatus(order.getStatus().toString());
		detail.setActivityStatus(order.getActivityStatus());
		detail.setTrackingUrl(order.getTrackingUrl());
		detail.setComments(order.getComments());
		if (order.getDistributor() != null ) {
			detail.setDistributor(UserSummary.from(order.getDistributor()));
		}
		
		return detail;
	}
	
	public static List<DistributionOrderDetail> from(List<DistributionOrder> orders) {
		List <DistributionOrderDetail> list = new ArrayList<DistributionOrderDetail>();
		
		for (DistributionOrder order : orders) {
			list.add(from(order));
		}
		
		return list;
	}
}
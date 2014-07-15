package com.krishagni.catissueplus.core.services.testdata;

import java.util.HashSet;
import java.util.Set;

import com.krishagni.catissueplus.core.administrative.domain.Department;
import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.events.CreateInstituteEvent;
import com.krishagni.catissueplus.core.administrative.events.DisableInstituteEvent;
import com.krishagni.catissueplus.core.administrative.events.InstituteDetails;
import com.krishagni.catissueplus.core.administrative.events.UpdateInstituteEvent;
import com.krishagni.catissueplus.core.common.util.Status;


public class InstituteTestData {

	public static final Object INSTITUTE_NAME = "institute name";
	
	public static final Object ACTIVITY_STATUS_DISABLED = "Disabled";

	public static CreateInstituteEvent getCreateInstituteEvent() {
		CreateInstituteEvent event = new CreateInstituteEvent();
		InstituteDetails details = new InstituteDetails();
		details.setName("My Inst");
		event.setInstituteDetails(details);
		return event;
	}

	public static Institute getInstitute(long id) {
		Institute institute = new Institute();
		institute.setId(id);
		institute.setName("My Inst");
		return institute;
	}

	public static UpdateInstituteEvent getUpdateInstituteEvent() {
		UpdateInstituteEvent event = new UpdateInstituteEvent();
		InstituteDetails details = new InstituteDetails();
		details.setName("My Inst");
		event.setInstituteDetails(details);
		return event;
	}

	public static CreateInstituteEvent getCreateInstituteEventForEmptyName() {
		CreateInstituteEvent event = getCreateInstituteEvent();
		event.getInstituteDetails().setName(null);
		return event;
	}

	public static UpdateInstituteEvent getUpdateInstituteEventForEmptyName() {
		UpdateInstituteEvent event = getUpdateInstituteEvent();
		event.getInstituteDetails().setName(null);
		return event;
	}

	public static DisableInstituteEvent getDisableInstituteEventForName() {
		DisableInstituteEvent  event = new DisableInstituteEvent();
		event.setName("Abc");
		return event;
	}

	public static DisableInstituteEvent getDisableInstituteEvent() {
		DisableInstituteEvent  event = new DisableInstituteEvent();
		event.setId(1l);
		return event;
	}

	public static Institute getInstituteForDisable(long id) {
		Institute inst = getInstitute(id);
		inst.setDepartmentCollection(getDepartmentCollection());
		inst.setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());
		return inst;
	}

	private static Set<Department> getDepartmentCollection() {
		Set<Department> departments = new HashSet<Department>();
		departments.add(new Department());
		return departments;
	}

}
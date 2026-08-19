package com.studentmanagementsystem.api.daoimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.studentmanagementsystem.api.dao.QuarterlyAttendanceDao;
import com.studentmanagementsystem.api.model.custom.CommonFilterDto;
import com.studentmanagementsystem.api.model.custom.quarterlyreport.QuarterlyAttendanceDto;
import com.studentmanagementsystem.api.model.entity.DailyAttendanceModel;
import com.studentmanagementsystem.api.model.entity.QuarterlyAttendanceModel;
import com.studentmanagementsystem.api.model.entity.StudentModel;
import com.studentmanagementsystem.api.util.WebServiceUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class QuarterlyAttendanceDaoImp implements QuarterlyAttendanceDao {

	@Autowired
	private EntityManager entityManager;

	
	/**
	 * Retrieve the attendance summary report to update the quarterly attendance report.
	 */	 
	//    SELECT 
	//    d.Student_Id AS studentId,
	//    SUM(CASE WHEN d.AT_Status = 'P' THEN 1 ELSE 0 END) AS totalDaysOfPresent,
	//    SUM(CASE WHEN d.AT_Status = 'AB' THEN 1 ELSE 0 END) AS totalDaysOfAbsents,
	//    SUM(CASE WHEN d.AT_Status = 'AB' AND d.AT_Approved_SickLeave = 'Y' THEN 1 ELSE 0 END) AS totalApprovedSickdays,
	//    SUM(CASE WHEN d.AT_Status = 'AB' AND d.AT_Approved_Extra_Cur_Activities = 'Y' THEN 1 ELSE 0 END) AS totalApprovedActivitiesPermissionDays
	//	FROM daily_attendance_registration d
	//	JOIN student s ON d.Student_Id = s.STU_Id
	//	WHERE MONTH(d.AT_Date) IN (10,11,12)
	//	  AND YEAR(d.AT_Date) = 2025
	//	GROUP BY d.Student_Id;
	@Override
	public List<QuarterlyAttendanceDto> getAttendanceSummary(List<Integer> quarterMonth) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<QuarterlyAttendanceDto> cq = cb.createQuery(QuarterlyAttendanceDto.class);

		Root<DailyAttendanceModel> dailyAttendanceRoot = cq.from(DailyAttendanceModel.class);

		Join<DailyAttendanceModel, StudentModel> studentJoin = dailyAttendanceRoot.join("studentModel");

		Predicate monthPredicate = cb.function("MONTH", Integer.class, dailyAttendanceRoot.get("attendanceDate"))
				.in(quarterMonth);

		Predicate yearPredicate = cb.equal(
				cb.function("YEAR", Integer.class, dailyAttendanceRoot.get("attendanceDate")), WebServiceUtil.YEAR);

		Expression<Long> totalPresent = cb.sum(cb.<Long>selectCase()
				.when(cb.equal(dailyAttendanceRoot.get("attendanceStatus"), WebServiceUtil.PRESENT), 1L).otherwise(0L));

		Expression<Long> totalAbsent = cb.sum(cb.<Long>selectCase()
				.when(cb.equal(dailyAttendanceRoot.get("attendanceStatus"), WebServiceUtil.ABSENT), 1L).otherwise(0L));

		Expression<Long> totalSick = cb.sum(cb.<Long>selectCase()
				.when(cb.and(cb.equal(dailyAttendanceRoot.get("attendanceStatus"), WebServiceUtil.ABSENT),
						cb.equal(dailyAttendanceRoot.get("longApprovedSickLeaveFlag"), WebServiceUtil.YES)), 1L)
				.otherwise(0L));

		Expression<Long> totalExtra = cb.sum(cb.<Long>selectCase()
				.when(cb.and(cb.equal(dailyAttendanceRoot.get("attendanceStatus"), WebServiceUtil.PRESENT),
						cb.equal(dailyAttendanceRoot.get("approvedExtraCurricularActivitiesFlag"), WebServiceUtil.YES)),
						1L)
				.otherwise(0L));

		cq.multiselect(
		        studentJoin.get("studentId"),
		        totalPresent,
		        totalAbsent,
		        totalExtra,
		        totalSick
		        
		).where(cb.and(monthPredicate, yearPredicate))
		 .groupBy(studentJoin.get("studentId"));
		
		return entityManager.createQuery(cq).getResultList();
	}


	/**
	 * Retrive list of quarterly attendance list
	 *
	 */
	//	SELECT 
	//    s.student_id,
	//    qar.quarter_and_year,
	//    s.student_first_name,
	//    s.student_middle_name,
	//    s.student_last_name,
	//    qar.attendance_compliance_status
	//   FROM quarterly_attendance_report qar
	//   INNER JOIN student s ON qar.student_id = s.student_id
	//  WHERE qar.quarter_and_year = :quarterAndYear
	//  AND qar.attendance_compliance_status = :complianceStatus;
	@Override
	public Map<String, Object> quarterlyAttendanceList(
			CommonFilterDto filterDto) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<QuarterlyAttendanceDto> cq  =cb.createQuery(QuarterlyAttendanceDto.class);
		Root<QuarterlyAttendanceModel> quarterlyAttendanceRoot = cq.from(QuarterlyAttendanceModel.class);
		
		Map<String,Object> result = new HashMap<>();
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		// quarterAndYear and classOfStudy filter
		predicates.add( cb.equal(quarterlyAttendanceRoot.get("studentModel").get("classOfStudy"),filterDto.getClassOfStudy()));
		predicates.add(cb.equal(quarterlyAttendanceRoot.get("quarterAndYear"), filterDto.getQuarterAndYear()));
		
		
		// Compliance and Non compliance status filter
		if (WebServiceUtil.COMPLIANCE.equalsIgnoreCase(filterDto.getStatus())) {
			predicates.add(cb.equal(quarterlyAttendanceRoot.get("attendanceComplianceStatus").get("code"),
					WebServiceUtil.COMPLIANCE));
		} else if (WebServiceUtil.NON_COMPLIANCE.equalsIgnoreCase(filterDto.getStatus())) {
			predicates.add(cb.equal(quarterlyAttendanceRoot.get("attendanceComplianceStatus").get("code"),
					WebServiceUtil.NON_COMPLIANCE));
		}
		
		
		// Name , email , phone number filter
		if (filterDto.getSearchBy() != null && filterDto.getSearchValue() != null) {
			switch (filterDto.getSearchBy().toLowerCase()) {

			case WebServiceUtil.NAME:
				String fullName = filterDto.getSearchValue().replaceAll(" ", "").toLowerCase();
				predicates.add(cb.like(cb.lower(quarterlyAttendanceRoot.get("studentModel").get("fullNameSearch")), "%" + fullName + "%"));
				break;

			case WebServiceUtil.EMAIL:
				Predicate email = cb.like(cb.lower(quarterlyAttendanceRoot.get("studentModel").get("email")),
						"%" + filterDto.getSearchValue().toLowerCase() + "%");
				predicates.add(email);
				break;

			case WebServiceUtil.PHONE_NUMBER:
				Predicate phoneNumber = cb.like(quarterlyAttendanceRoot.get("studentModel").get("phoneNumber"),
						"%" + filterDto.getSearchValue() + "%");
				predicates.add(phoneNumber);
				break;
			}
		}
		 
		    cq.select(cb.construct(QuarterlyAttendanceDto.class,
				 
					quarterlyAttendanceRoot.get("studentModel").get("studentId"),
					quarterlyAttendanceRoot.get("studentModel").get("fullName")	,
					quarterlyAttendanceRoot.get("studentModel").get("classOfStudy"),
					quarterlyAttendanceRoot.get("studentModel").get("dateOfBirth"),
					quarterlyAttendanceRoot.get("studentModel").get("phoneNumber"),
					quarterlyAttendanceRoot.get("studentModel").get("email"),
					quarterlyAttendanceRoot.get("quarterAndYear"),
					quarterlyAttendanceRoot.get("totalSchoolWorkingDays"),
					quarterlyAttendanceRoot.get("totalDaysOfPresent"),
					quarterlyAttendanceRoot.get("totalDaysOfAbsents"),				
					quarterlyAttendanceRoot.get("totalApprovedActivitiesPermissionDays"),
					quarterlyAttendanceRoot.get("totalApprovedSickdays"),
					quarterlyAttendanceRoot.get("attendanceComplianceStatus").get("code"),
					quarterlyAttendanceRoot.get("attendancePercentage")
					
					));
		
		    	//sorting filter
					List<String> stuOrderColumnList = new ArrayList<>(
							Arrays.asList("firstName", "email", "phoneNumber", "dateOfBirth"));
					if (filterDto.getOrderColumn() != null && filterDto.getOrderType() != null) {
						if (stuOrderColumnList.contains(filterDto.getOrderColumn())) {
							if (WebServiceUtil.ASCENDING_ORDER.equals(filterDto.getOrderType()))
								cq.orderBy(cb.asc(
										quarterlyAttendanceRoot.get("studentModel").get(filterDto.getOrderColumn())));
							else if (WebServiceUtil.DESCENDING_ORDER.equals(filterDto.getOrderType()))
								cq.orderBy(cb.desc(
										quarterlyAttendanceRoot.get("studentModel").get(filterDto.getOrderColumn())));
						} else {
							if (WebServiceUtil.ASCENDING_ORDER.equals(filterDto.getOrderType()))
								cq.orderBy(cb.asc(quarterlyAttendanceRoot.get(filterDto.getOrderColumn())));
							else if (WebServiceUtil.DESCENDING_ORDER.equals(filterDto.getOrderType()))
								cq.orderBy(cb.desc(quarterlyAttendanceRoot.get(filterDto.getOrderColumn())));
						}

					}

		
		if (!predicates.isEmpty()) {
			cq.where(cb.and(predicates.toArray(new Predicate[0])));
		}
		
		List<QuarterlyAttendanceDto> quarterlyAttendanceList=  entityManager.createQuery(cq)
												             .setFirstResult(filterDto.getStart())
												             .setMaxResults(filterDto.getLength())
												             .getResultList();
		
		CriteriaQuery<Long> filterCountQuery = cb.createQuery(Long.class);
		Root<QuarterlyAttendanceModel> filterCountRoot = filterCountQuery.from(QuarterlyAttendanceModel.class);

		filterCountQuery.multiselect(cb.count(filterCountRoot)).where(cb.and(predicates.toArray(new Predicate[0])));

		Long filterCount = entityManager.createQuery(filterCountQuery).getSingleResult();

		result.put("filterCount", filterCount);
		result.put("data", quarterlyAttendanceList);
		
		  return result;
			
	}
	
	/*
	 *send mail to quarterly report 
	 */

	@Override
	public List<QuarterlyAttendanceDto> getQuarterlyAttendanceReport(String quarterAndYear,Integer classOfStudy) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<QuarterlyAttendanceDto> cq = cb.createQuery(QuarterlyAttendanceDto.class);
		Root<QuarterlyAttendanceModel> quarterlyAttendanceRoot = cq.from(QuarterlyAttendanceModel.class);
		Predicate quarterandYearCondition = cb.equal(quarterlyAttendanceRoot.get("quarterAndYear"), quarterAndYear);
		Predicate classCondition = cb.equal(quarterlyAttendanceRoot.get("studentModel").get("classOfStudy"),
				classOfStudy);
		cq.select(cb.construct(QuarterlyAttendanceDto.class,
				 
				quarterlyAttendanceRoot.get("studentModel").get("studentId"),
				quarterlyAttendanceRoot.get("totalSchoolWorkingDays"),
				quarterlyAttendanceRoot.get("totalDaysOfPresent"),
				quarterlyAttendanceRoot.get("totalDaysOfAbsents"),				
				quarterlyAttendanceRoot.get("totalApprovedActivitiesPermissionDays"),
				quarterlyAttendanceRoot.get("totalApprovedSickdays"),
				quarterlyAttendanceRoot.get("attendanceComplianceStatus").get("code"),
				quarterlyAttendanceRoot.get("attendancePercentage")
				
				)).where(quarterandYearCondition,classCondition)	;	
		

		
		return entityManager.createQuery(cq).getResultList();
	}
	
	/**
	 * Find the total working days at particular quarter and year
	 */
	//	SELECT COUNT(DISTINCT da.AT_Date) AS totalWorkingDays
	//	FROM daily_attendance_registration da
	//	WHERE MONTH(da.AT_Date) IN (10,11,12)   -- example: your quarterMonth list
	//	  AND YEAR(da.AT_Date) = 2025; 
	@Override
	public Long getTotalWorkingDays(List<Integer> quarterMonth) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> cq = cb.createQuery(Long.class);

		Root<DailyAttendanceModel> totalWorkingDaysRoot = cq.from(DailyAttendanceModel.class);

		Predicate monthPredicateCondition = cb
				.function("MONTH", Integer.class, totalWorkingDaysRoot.get("attendanceDate")).in(quarterMonth);

		Predicate yearPredicateCondition = cb.equal(
				cb.function("YEAR", Integer.class, totalWorkingDaysRoot.get("attendanceDate")), WebServiceUtil.YEAR);
		cq.select(cb.countDistinct(totalWorkingDaysRoot.get("attendanceDate")))
				.where(cb.and(monthPredicateCondition, yearPredicateCondition));

		return entityManager.createQuery(cq).getSingleResult();

	}
}

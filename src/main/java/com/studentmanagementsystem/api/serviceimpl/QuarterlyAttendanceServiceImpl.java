package com.studentmanagementsystem.api.serviceimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.studentmanagementsystem.api.dao.QuarterlyAttendanceDao;
import com.studentmanagementsystem.api.model.custom.CommonFilterDto;
import com.studentmanagementsystem.api.model.custom.Response;
import com.studentmanagementsystem.api.model.custom.quarterlyreport.QuarterlyAttendanceDto;
import com.studentmanagementsystem.api.model.entity.QuarterlyAttendanceModel;
import com.studentmanagementsystem.api.model.entity.StudentCodeModel;
import com.studentmanagementsystem.api.repository.QuarterlyAttendanceRepository;
import com.studentmanagementsystem.api.repository.StudentCodeRespository;
import com.studentmanagementsystem.api.repository.StudentRepository;
import com.studentmanagementsystem.api.service.QuarterlyAttendanceService;
import com.studentmanagementsystem.api.util.WebServiceUtil;

import jakarta.transaction.Transactional;

@Service
public class QuarterlyAttendanceServiceImpl implements QuarterlyAttendanceService {
	
	private static final Logger logger = LoggerFactory.getLogger(QuarterlyAttendanceServiceImpl.class);


	@Autowired
	private QuarterlyAttendanceDao quartlyAttendanceDao;
	
	@Autowired
	private QuarterlyAttendanceRepository quarterlyRepository;
	
	@Autowired
	private StudentRepository studentModelRepository;
	
	@Autowired
	private StudentCodeRespository studentCodeRespository;

	/**
	 *Updates the quarterly attendance report.
	 * This method is executed automatically by a Cron job 
	 * every day at 5:00 PM.
	 *
	 * Author: Vijayakumar
	 */
	@Override
	public void findQuarterToUpadte(List<Integer> quarterMonth) {
		int march = 0, june = 0, sep = 0, dec = 0;
		for (Integer monthNumber : quarterMonth) {
			if (march == 0 && monthNumber == 1 || monthNumber == 2 || monthNumber == 3) {
				updateQuarterlyAttendanceReport(WebServiceUtil.QUART_MARCH_AND_YEAR, WebServiceUtil.MARCH_END);
				march = 1;
			} else if (june == 0 && monthNumber == 4 || monthNumber == 5 || monthNumber == 6) {
				updateQuarterlyAttendanceReport(WebServiceUtil.QUART_JUNE_AND_YEAR, WebServiceUtil.JUNE_END);
				june = 1;
			} else if (sep == 0 && monthNumber == 7 || monthNumber == 8 || monthNumber == 9) {
				updateQuarterlyAttendanceReport(WebServiceUtil.QUART_SEP_AND_YEAR, WebServiceUtil.SEP_END);
				sep = 1;
			} else if (dec == 0 && monthNumber == 10 || monthNumber == 11 || monthNumber == 12) {
				updateQuarterlyAttendanceReport(WebServiceUtil.QUART_DEC_AND_YEAR, WebServiceUtil.DEC_END);
				dec = 1;
			}
		}
	}


	/**
	 * Scheduled task that updates the quarterly attendance report.
	 * This method is executed automatically by a Cron job 
	 * every day at 5:00 PM.
	 *
	 * Author: Vijayakumar
	 */
	 // @Scheduled(cron = "0 0 17 * * ?") // Every day at 5 PM
		public void runQuarterlyAttendanceUpdate() {

			logger.info("Before runQuarterlyAttendanceUpdate - Update the quarterly Attendance ");

			List<String> quarterAndYear = new ArrayList<>(
					Arrays.asList(WebServiceUtil.QUART_MARCH_AND_YEAR, WebServiceUtil.QUART_JUNE_AND_YEAR,
							WebServiceUtil.QUART_SEP_AND_YEAR, WebServiceUtil.QUART_DEC_AND_YEAR));

			for (String quarter : quarterAndYear) {

				if (quarter == WebServiceUtil.QUART_MARCH_AND_YEAR) {
					updateQuarterlyAttendanceReport(WebServiceUtil.QUART_MARCH_AND_YEAR, WebServiceUtil.MARCH_END);
				} else if (quarter == WebServiceUtil.QUART_JUNE_AND_YEAR) {
					updateQuarterlyAttendanceReport(WebServiceUtil.QUART_JUNE_AND_YEAR, WebServiceUtil.JUNE_END);
				} else if (quarter == WebServiceUtil.QUART_SEP_AND_YEAR) {
					updateQuarterlyAttendanceReport(WebServiceUtil.QUART_SEP_AND_YEAR, WebServiceUtil.SEP_END);
				} else if (quarter == WebServiceUtil.QUART_DEC_AND_YEAR) {
					updateQuarterlyAttendanceReport(WebServiceUtil.QUART_DEC_AND_YEAR, WebServiceUtil.DEC_END);
				}
			}
			logger.info("After runQuarterlyAttendanceUpdate -Successfully updated the quarterly Attendance ");

		}
	  
	  /**
	   * Updates the quarterly attendance report for all students.
	   * <p>
	   * This method aggregates daily attendance data (present, absent, sick leave,
	   * extra-curricular leave) for the given quarter and persists the results 
	   * into the quarterly report table.
	   */
//	@Override
	public void updateQuarterlyAttendanceReport(String quarterAndYear, List<Integer> quarterMonthEnd) {
		
		logger.info("Before updateQuarterlyAttendanceReport - Update the quarterly Attendance , quarter : {} ",quarterAndYear);

		List<QuarterlyAttendanceDto> quarterlyAttendanceCountList = quartlyAttendanceDao
				.getAttendanceSummary(quarterMonthEnd);

		if (quarterlyAttendanceCountList.isEmpty()) {
			logger.info("No attendance records found for quarter : {} , skipping the update", quarterAndYear);
			return;
		}

	//	Long totalWorkingDays = quartlyAttendanceDao.getTotalWorkingDays(quarterMonthEnd);

		Long totalWorkingDays = quarterlyAttendanceCountList.get(0).getTotalDaysOfPresent() + quarterlyAttendanceCountList.get(0).getTotalDaysOfAbsents();
		
		List<QuarterlyAttendanceModel> quarterlyModelList = new ArrayList<>();

		for (QuarterlyAttendanceDto quarterlyAttendanceCount : quarterlyAttendanceCountList) {
			
			QuarterlyAttendanceModel existsQuarterlyAttendanceModel = quarterlyRepository
					.findByStudentAndQuarterAndYear(quarterlyAttendanceCount.getStudentId(), quarterAndYear);
			
			if (existsQuarterlyAttendanceModel == null) {
				
				existsQuarterlyAttendanceModel = new QuarterlyAttendanceModel();

				existsQuarterlyAttendanceModel.setStudentModel(
						studentModelRepository.findByStudentId(quarterlyAttendanceCount.getStudentId()));

				existsQuarterlyAttendanceModel.setQuarterAndYear(quarterAndYear);
			}
						
			existsQuarterlyAttendanceModel.setTotalSchoolWorkingDays(totalWorkingDays);
			existsQuarterlyAttendanceModel.setTotalDaysOfPresent(quarterlyAttendanceCount.getTotalDaysOfPresent());
			existsQuarterlyAttendanceModel.setTotalDaysOfAbsents(quarterlyAttendanceCount.getTotalDaysOfAbsents());
			
			existsQuarterlyAttendanceModel.setTotalApprovedActivitiesPermissionDays(
					quarterlyAttendanceCount.getTotalApprovedActivitiesPermissionDays());
			
			existsQuarterlyAttendanceModel.setTotalApprovedSickdays(quarterlyAttendanceCount.getTotalApprovedSickdays());
			
			Long present = quarterlyAttendanceCount.getTotalDaysOfPresent();
			//Long activities = quarterlyAttendance.getTotalApprovedActivitiesPermissionDays();
			Long sick = quarterlyAttendanceCount.getTotalApprovedSickdays();		

//			int percentageOfPresent = (int) Math.ceil((((present + sick) / totalWorkingDays) * 100));
			int percentageOfPresent =
			        (int) Math.ceil(((double) (present + sick) / totalWorkingDays) * 100);
	       		
			existsQuarterlyAttendanceModel.setAttendancePercentage(percentageOfPresent);
			StudentCodeModel complianceStatus;
			if (percentageOfPresent < 75) {
				complianceStatus = studentCodeRespository
						.findByCode(WebServiceUtil.NON_COMPLIANCE);
				existsQuarterlyAttendanceModel.setComments(WebServiceUtil.NON_COMPLIANCE_COMMENT);
			} else {
			    complianceStatus = studentCodeRespository.findByCode(WebServiceUtil.COMPLIANCE);
				existsQuarterlyAttendanceModel.setComments(WebServiceUtil.COMPLIANCE_COMMENT);
			}
			existsQuarterlyAttendanceModel.setAttendanceComplianceStatus(complianceStatus);
			quarterlyModelList.add(existsQuarterlyAttendanceModel);
		}
		quarterlyRepository.saveAll(quarterlyModelList);
		logger.info("After updateQuarterlyAttendanceReport - SuccessFully updated");
	}
	
	/**
	 * Retrieve the list of compliance or non-compliance students for a given quarter and year.
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Response quarterlyAttendanceList(CommonFilterDto filterDto) {
		logger.info("Before listQuarterlyAttendance - Retrive the quarterly attendance report ");
		
		Map<String, Object> result = quartlyAttendanceDao.quarterlyAttendanceList(filterDto);
		
		List<QuarterlyAttendanceDto> quarterlyAttendanceList = (List<QuarterlyAttendanceDto>) result.get("data");
		
		Long totalCount = quarterlyRepository.findTotalCount(filterDto.getClassOfStudy(),
				filterDto.getQuarterAndYear());
		
		if (quarterlyAttendanceList != null) {

			if (WebServiceUtil.S_NO.equals(filterDto.getOrderColumn())
					&& WebServiceUtil.DESCENDING_ORDER.equals(filterDto.getOrderType())) {
				int sno = (int) ((filterDto.getLength() <= totalCount) ? filterDto.getLength() : totalCount);
				for (QuarterlyAttendanceDto attendance : quarterlyAttendanceList) {
					attendance.setSno(sno--);
				}
			} else {
				int sno = (filterDto.getStart() > 0) ? filterDto.getStart() : 1;
				for (QuarterlyAttendanceDto attendance : quarterlyAttendanceList) {
					attendance.setSno(sno++);
				}
			}
		}

		Response response = new Response();
		response.setStatus(WebServiceUtil.SUCCESS);
		response.setDraw(filterDto.getDraw());
		response.setTotalCount(totalCount);
		response.setFilterCount((Long) result.get("filterCount"));
		response.setData(quarterlyAttendanceList);
		logger.info("After listQuarterlyAttendance - Successfully retrive the quarterly attendance report ");
		return response;
	}

}

package com.studentmanagementsystem.api.serviceimpl;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.studentmanagementsystem.api.dao.DailyAttendanceDao;
import com.studentmanagementsystem.api.model.custom.MessageResponse;
import com.studentmanagementsystem.api.model.custom.Response;
import com.studentmanagementsystem.api.model.custom.dailyattendance.DailyAttendanceDto;
import com.studentmanagementsystem.api.model.custom.dailyattendance.DailyAttendanceFilterDto;
import com.studentmanagementsystem.api.model.custom.dailyattendance.MonthlyAbsenceDto;
import com.studentmanagementsystem.api.model.entity.DailyAttendanceModel;
import com.studentmanagementsystem.api.model.entity.HolidayModel;
import com.studentmanagementsystem.api.model.entity.StudentCodeModel;
import com.studentmanagementsystem.api.model.entity.StudentModel;
import com.studentmanagementsystem.api.model.entity.TeacherModel;
import com.studentmanagementsystem.api.repository.DailyAttendanceRepository;
import com.studentmanagementsystem.api.repository.HolidayRepository;
import com.studentmanagementsystem.api.repository.StudentCodeRespository;
import com.studentmanagementsystem.api.repository.StudentRepository;
import com.studentmanagementsystem.api.repository.TeacherRepository;
import com.studentmanagementsystem.api.service.DailyAttendanceService;
import com.studentmanagementsystem.api.service.EmailSentService;
import com.studentmanagementsystem.api.service.QuarterlyAttendanceService;
import com.studentmanagementsystem.api.util.WebServiceUtil;
import com.studentmanagementsystem.api.validation.FieldValidation;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

@Service
public class DailyAttendanceServiceImpl implements DailyAttendanceService {
	
	private static final Logger logger = LoggerFactory.getLogger(DailyAttendanceServiceImpl.class);


	@Autowired
	private DailyAttendanceDao dailyAttendanceDao;
	 
	@Autowired
	private FieldValidation fieldValidation;
	
	@Autowired
	private DailyAttendanceRepository dailyAttendanceRepository;
	
	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private StudentRepository studentModelRepository;
	
	@Autowired
	private StudentCodeRespository studentCodeRespository;
	
	@Autowired
	private HolidayRepository schoolHolidaysRepository;
	
	@Autowired
	private EmailSentService emailSentService;

	@Autowired
	private  QuarterlyAttendanceService quarterlyAttendanceService;



	/**
	 * Mark attendance for students.
	 */

	@Override
	@Transactional
	public MessageResponse saveDailyAttendance(List<DailyAttendanceDto> dailyAttendanceDtoList) {

		MessageResponse response = new MessageResponse();
		LocalDateTime currentDateTime = LocalDateTime.now();

		LocalDate currentDate = LocalDate.now();

		// List to save attendance
		List<DailyAttendanceModel> dailyAttendanceModelList = new ArrayList<>();

		List<Long> mailSendStudentIdList = new ArrayList<>();

		// List the students who have not been marked attendance
		List<Long> attendanceMarekedStudentIdList = new ArrayList<>();

		// List to collect month to update the quarterly attendance
		List<Integer> quarterMonthList = new ArrayList<>();

		LocalDate attendanceDate = null;
		DailyAttendanceModel dailyAttendanceModel;

		for (DailyAttendanceDto dailyAttendance : dailyAttendanceDtoList) {

			logger.info(
					"Before markStudentAttendance - Attempting to mark attendance StudentId : {}  for TeacherId: {}",
					dailyAttendance.getStudentId(), dailyAttendance.getTeacherId());

			// Field Validation
			List<String> requestFieldList = fieldValidation.attendanceFieldValidation(dailyAttendance);
			if (!requestFieldList.isEmpty()) {
				response.setStatus(WebServiceUtil.WARNING);
				response.setData(requestFieldList);
				return response;
			}
			// Verify if the teacher ID exists
			TeacherModel teacher = teacherRepository.findByTeacherId(dailyAttendance.getTeacherId());
			if (teacher == null) {
				response.setStatus(WebServiceUtil.WARNING);
				response.setData(WebServiceUtil.TEACHER_ID_ERROR);
				return response;
			}

			// Verify if studentId and attendanceDate already exist

			dailyAttendanceModel = dailyAttendanceRepository.findStudentIdAndAttendanceDate(dailyAttendance.getStudentId(),
					dailyAttendance.getAttendanceDate());

			// save the daily attendance
			if (dailyAttendanceModel == null) {

				// Verify if attendanceDate is holiday
				HolidayModel schoolHolidayModel = schoolHolidaysRepository
						.getByHolidayDate(dailyAttendance.getAttendanceDate());
				if (schoolHolidayModel != null && Boolean.FALSE.equals(schoolHolidayModel.getIsHolidayCancelled())) {
					response.setStatus(WebServiceUtil.WARNING);
					response.setData(dailyAttendance.getAttendanceDate() + WebServiceUtil.DO_NOT_MARK_ATTENDANCE);
					return response;
				}

				// Verify if attendanceDate is Sunday

				if (dailyAttendance.getAttendanceDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
					response.setStatus(WebServiceUtil.WARNING);
					response.setData(currentDate + WebServiceUtil.SUNDAY_ERROR);
					return response;
				}

				dailyAttendanceModel = new DailyAttendanceModel();

				Long id = dailyAttendance.getStudentId();
				StudentModel student = studentModelRepository.findByStudentId(dailyAttendance.getStudentId());
				dailyAttendanceModel.setStudentModel(student);
				dailyAttendanceModel.setCreateTeacher(teacher);
				dailyAttendanceModel.setCreateDate(currentDateTime);
				dailyAttendanceModel.setAttendanceDate(dailyAttendance.getAttendanceDate());

				attendanceMarekedStudentIdList.add(dailyAttendance.getStudentId());

//				attendanceDate = attendance.getAttendanceDate();

				// collect absent and ECA student id
				if (currentDate.equals(dailyAttendance.getAttendanceDate())
						&& (WebServiceUtil.ABSENT.equals(dailyAttendance.getAttendanceStatus())
								|| WebServiceUtil.YES.equals(dailyAttendance.getApprovedExtraCurricularActivitiesFlag()))) {
					mailSendStudentIdList.add(dailyAttendance.getStudentId());
				}

				response.setData(WebServiceUtil.MARK_ATTENDANCE);
				logger.info("After markStudentAttendance - Attendace Marked successfully");

			} else {

				dailyAttendanceModel.setUpdateTeacher(teacher);
				dailyAttendanceModel.setUpdateTime(currentDateTime);
				response.setData(WebServiceUtil.UPDATE_ATTENDANCE);
				logger.info("After markStudentAttendance - Attendace Updated successfully");
			}

			dailyAttendanceModel
					.setAttendanceStatus(studentCodeRespository.findByCode(dailyAttendance.getAttendanceStatus()));

			dailyAttendanceModel
					.setApprovedExtraCurricularActivitiesFlag(dailyAttendance.getApprovedExtraCurricularActivitiesFlag());

			dailyAttendanceModel.setLongApprovedSickLeaveFlag(dailyAttendance.getLongApprovedSickLeaveFlag());

			int monthValue = dailyAttendance.getAttendanceDate().getMonthValue();
			if (!quarterMonthList.contains(monthValue)) {
				quarterMonthList.add(monthValue);
			}
			dailyAttendanceModelList.add(dailyAttendanceModel);
		}

		Integer classOfStudy = dailyAttendanceDtoList.get(0).getClassOfStudy();
		Long teacherId = dailyAttendanceDtoList.get(0).getTeacherId();
		System.out.println("Teacher Id" + teacherId);

		// students who have not been marked attendance ,set default absent
		if (!attendanceMarekedStudentIdList.isEmpty()) {
			attendanceDate = dailyAttendanceDtoList.get(0).getAttendanceDate();
			List<Long> existsStudentId = studentModelRepository.findStudentIdByClassOfStudyAndStatus(classOfStudy,WebServiceUtil.ACTIVE);
			List<Long> notTakenList = new ArrayList<>(existsStudentId);
			notTakenList.removeAll(attendanceMarekedStudentIdList);

			if (!notTakenList.isEmpty()) {
				for (Long studentId : notTakenList) {
					dailyAttendanceModel = new DailyAttendanceModel();
					StudentModel student = studentModelRepository.findByStudentId(studentId);

					dailyAttendanceModel.setStudentModel(student);
					TeacherModel teacher = teacherRepository.findByTeacherId(teacherId);
					dailyAttendanceModel.setCreateTeacher(teacher);
					dailyAttendanceModel.setCreateDate(currentDateTime);
					dailyAttendanceModel.setAttendanceDate(attendanceDate);

					StudentCodeModel attendanceStatus = studentCodeRespository.findByCode(WebServiceUtil.ABSENT);
					dailyAttendanceModel.setAttendanceStatus(attendanceStatus);
					dailyAttendanceModel.setLongApprovedSickLeaveFlag(WebServiceUtil.NO);
					dailyAttendanceModel.setApprovedExtraCurricularActivitiesFlag(WebServiceUtil.NO);
					dailyAttendanceModelList.add(dailyAttendanceModel);

				}
			}
		}

		// save the student attendance
		dailyAttendanceRepository.saveAll(dailyAttendanceModelList);
		dailyAttendanceRepository.flush();

		// update the quarterly attendance report
		quarterlyAttendanceService.findQuarterToUpadte(quarterMonthList);
		// send mail to absent students
		if (!mailSendStudentIdList.isEmpty()) {

			try {
				emailSentService.attendanceStatusAlertEmail(mailSendStudentIdList, teacherId);
			} catch (MessagingException e) {

				e.printStackTrace();
			}
		}
		response.setStatus(WebServiceUtil.SUCCESS);
		return response;
	}

	/**
	 * Retrieve student attendance for a particular date to check whether given filter.
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Response dailyAttendanceList(DailyAttendanceFilterDto filterDto) {
		logger.info("Before attendanceList - Attempting to retrive the student attendance");

		Map<String, Object> result = dailyAttendanceDao.dailyAttendanceList(filterDto);

		List<DailyAttendanceDto> dailyAttendanceList = (List<DailyAttendanceDto>) result.get("data");
		Long totalCount = dailyAttendanceRepository.findTotalCount(filterDto.getClassOfStudy());

		if (dailyAttendanceList != null) {

			if (WebServiceUtil.S_NO.equals(filterDto.getOrderColumn())
					&& WebServiceUtil.DESCENDING_ORDER.equals(filterDto.getOrderType())) {
				int sno = (int) ((filterDto.getLength() <= totalCount) ? filterDto.getLength() : totalCount);
				for (DailyAttendanceDto dailyAttendance : dailyAttendanceList) {
					dailyAttendance.setSno(sno--);
				}
			} else {
				int sno = (filterDto.getStart() > 0) ? filterDto.getStart() : 1;
				for (DailyAttendanceDto dailyAttendance : dailyAttendanceList) {
					dailyAttendance.setSno(sno++);
				}
			}
		}
		Response response = new Response();
		response.setStatus(WebServiceUtil.SUCCESS);
		response.setDraw(filterDto.getDraw());
		response.setTotalCount(totalCount);
		response.setFilterCount((Long) result.get("filterCount"));
		response.setData(dailyAttendanceList);
		logger.info("After getStudentAttendanceByDate - Successfully retrived student attendance");

		return response;
	}


	/**
	 * Retrieve students'monthly attendance list for a given filter
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Response monthlyDailyAttendanceList(DailyAttendanceFilterDto filterDto) {
		logger.info("Before monthlyAttendanceList - Attempting to retrive the monthly absence studenet list");

		Map<String, Object> result = dailyAttendanceDao.monthlyDailyAttendanceList(filterDto);

		List<MonthlyAbsenceDto> monthlyDailyAttendanceList = (List<MonthlyAbsenceDto>) result.get("data");

		Long totalCount = dailyAttendanceRepository.findTotalCount(filterDto.getClassOfStudy());

		if (monthlyDailyAttendanceList != null) {

			if (WebServiceUtil.S_NO.equals(filterDto.getOrderColumn())
					&& WebServiceUtil.DESCENDING_ORDER.equals(filterDto.getOrderType())) {
				int sno = (int) ((filterDto.getLength() <= totalCount) ? filterDto.getLength() : totalCount);
				for (MonthlyAbsenceDto attendance : monthlyDailyAttendanceList) {
					attendance.setSno(sno--);
				}
			} else {
				int sno = (filterDto.getStart() > 0) ? filterDto.getStart() : 1;
				for (MonthlyAbsenceDto attendance : monthlyDailyAttendanceList) {
					attendance.setSno(sno++);
				}
			}
		}
		Response response = new Response();
		response.setStatus(WebServiceUtil.SUCCESS);
		response.setDraw(filterDto.getDraw());
		response.setTotalCount(totalCount);
		response.setFilterCount((Long) result.get("filterCount"));
		response.setData(monthlyDailyAttendanceList);
		logger.info("After getMonthlyAbsenceReport - Successfully retrived monthly absence list");

		return response;
	}
}

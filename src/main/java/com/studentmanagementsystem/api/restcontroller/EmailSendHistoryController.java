package com.studentmanagementsystem.api.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.studentmanagementsystem.api.service.EmailSentService;
import jakarta.mail.MessagingException;

@RestController
@CrossOrigin
@RequestMapping(value = "mail")
public class EmailSendHistoryController {
	
	@Autowired
	private EmailSentService emailSentService;

	
	/**
	 * Send the quarterly attendance report to student parents via email.
	 *
	 * @param quarterAndYear The quarter and year for which the reports are to be sent (e.g., Q1-2025)
	 * @return Confirmation message indicating that the reports were sent successfully
	 * @author Vijiyakumar
	 * @throws MessagingException 
	 */
	
	@PostMapping("/quarter-report")
	ResponseEntity<?> sendQuarterlyResultReport(@RequestParam String quarterAndYear,@RequestParam Integer classOfStudy,@RequestParam Long teacherId) throws MessagingException  {
		return new ResponseEntity<>(emailSentService.sendQuarterlyAttendanceReport(quarterAndYear,classOfStudy,teacherId),HttpStatus.OK);
	}
	
	/**
	 * Send the quarterly mark report  to student parents via email.
	 *
	 * @param quarterAndYear The quarter and year for which the reports are to be sent (e.g., 03/2025)
	 * @param classOfStudy  The class of study for which the report are to sent 
	 * @return Confirmation message indicating that the reports were sent successfully
	 * @author Vijiyakumar
	 * @throws MessagingException 
	 */
	
	@PostMapping("/mark-result")
	ResponseEntity<?> sendMarkReport(@RequestParam String quarterAndYear,@RequestParam Integer classOfStudy,@RequestParam Long teacherId) throws MessagingException {
		return new ResponseEntity<>(emailSentService.sendQuarterlyMarkResult(quarterAndYear,classOfStudy,teacherId),HttpStatus.OK);
	}
	
//	/**
//	 * Send the absent alert to student parents via email.
//	 *
//	 * @return Confirmation message indicating that the alert were sent successfully
//	 * @author Vijiyakumar
//	 */
//	@GetMapping("/absentAlert")
//	ResponseEntity<?> runAbsentAlertMessage(@RequestParam Long teacherId) throws MessagingException{
//		return new ResponseEntity<>(emailSentService.runAbsentAlertMessage(teacherId),HttpStatus.OK);
//	}
	
}

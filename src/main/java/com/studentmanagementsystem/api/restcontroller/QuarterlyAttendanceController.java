package com.studentmanagementsystem.api.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagementsystem.api.model.custom.CommonFilterDto;
import com.studentmanagementsystem.api.service.QuarterlyAttendanceService;

@RestController
@RequestMapping("quarterly")
@CrossOrigin
public class QuarterlyAttendanceController {
	
	@Autowired
	private QuarterlyAttendanceService quarterlyAttendanceReportService;


	/**
	 * Retrieve the list of quarter attendnace report.
	 *
	 * @param filterDto
	 * @return Return list of quarterli attendance report
	 * @author Vijiyakumar
	 */	
	@GetMapping("/list")
	ResponseEntity<?> quarterlyAttendanceList(@RequestBody CommonFilterDto filterDto){
		return new ResponseEntity<>(quarterlyAttendanceReportService.quarterlyAttendanceList(filterDto),HttpStatus.OK);
	}
	

//	@GetMapping("/save")
//	String updateQuarterlyReport(){
//		 quarterlyAttendanceReportService.runQuarterlyAttendanceUpdate();
//		 return "saved";
//	}

}

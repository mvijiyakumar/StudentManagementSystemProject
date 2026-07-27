package com.studentmanagementsystem.api.restcontroller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagementsystem.api.model.custom.CommonFilterDto;
import com.studentmanagementsystem.api.model.custom.dailyattendance.DailyAttendanceFilterDto;
import com.studentmanagementsystem.api.model.custom.studentmarks.MarkFilterDto;
import com.studentmanagementsystem.api.service.ExcelService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "report")
@CrossOrigin
public class ExcelController {
	
	@Autowired
	private ExcelService excelService;
	
	/*
	 * Download the monthy attendance report
	 * 
	 * @param filterDto Filter criteria for retrieving monthly attendance.
	 * @author Vijiyakumar
	 */
	
	@PostMapping("attendance/monthly-report")
    public void downloadMonthlyAttendanceReport(@RequestBody DailyAttendanceFilterDto filterDto,HttpServletResponse response) throws IOException {  
    		  excelService.downloadMonthlyAttendanceReport(filterDto,response);
    
    }
	
	/*
	 * Download the mark detail report
	 * 
	 * @param filterDto Filter criteria for retrieving report.
	 * @author Vijiyakumar
	 */
	@PostMapping("mark/detail-report")
    public void downloadMarkDetailReport(@RequestBody MarkFilterDto filterDto,HttpServletResponse response) throws IOException {  
    		  excelService.downloadMarkDetailReport(filterDto,response);
    
    }
	
//	/*
//	 * Download the mark summary report
//	 * 
//	 * @param filterDto Filter criteria for retrieving report.
//	 * @author Vijiyakumar
//	 */
//	@PostMapping("mark/summary-report")
//    public void downloadMarkSummaryReport(@RequestBody CommonFilterDto filterDto,HttpServletResponse response) throws IOException {  
//    		  excelService.downloadMarkSummaryReport(filterDto,response);
//    
//    }



}

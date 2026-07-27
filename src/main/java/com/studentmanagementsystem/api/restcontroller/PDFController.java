package com.studentmanagementsystem.api.restcontroller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.studentmanagementsystem.api.model.custom.studentmarks.MarkFilterDto;
import com.studentmanagementsystem.api.service.PDFService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "report")
@CrossOrigin
public class PDFController {
	
	@Autowired
	private PDFService pdfService;
	
	/*
	 * Download the mark summary report
	 * 
	 * @param filterDto Filter criteria for retrieving report.
	 * @author Vijiyakumar
	 */
	@PostMapping("mark/summary-report")
    public void downloadMarkSummaryReport(@RequestBody MarkFilterDto filterDto,HttpServletResponse response) throws IOException {  
		pdfService.downloadMarkSummaryReport(filterDto,response);
    
    }

}

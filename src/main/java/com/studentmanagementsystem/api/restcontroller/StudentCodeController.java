package com.studentmanagementsystem.api.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studentmanagementsystem.api.model.custom.studentcode.StudentCodeDto;
import com.studentmanagementsystem.api.service.StudentCodeService;



@RestController
@RequestMapping(value = "code")
@CrossOrigin
public class StudentCodeController {
	
	
@Autowired
private StudentCodeService studentCodeService;

/**
 * Declare code.
 *
 * @param studentCodeDto The list of code to be declared( from request body)
 * @return  Confirmation message 
 * @author Vijiyakumar
 */
@PostMapping
public ResponseEntity<?> addStudentCode(@RequestBody List<StudentCodeDto> studentCodeDto){
	return new ResponseEntity<>(studentCodeService.addStudentCode(studentCodeDto),HttpStatus.OK);
}

@GetMapping
public ResponseEntity<?> stuCodeList(@RequestParam(required = false) String groupCode){
	return new ResponseEntity<>(studentCodeService.stuCodeList(groupCode),HttpStatus.OK);
}
}

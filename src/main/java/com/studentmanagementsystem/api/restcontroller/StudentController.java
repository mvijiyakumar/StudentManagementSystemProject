package com.studentmanagementsystem.api.restcontroller;
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

import com.studentmanagementsystem.api.model.custom.student.StudentDto;
import com.studentmanagementsystem.api.model.custom.student.StudentFilterDto;
import com.studentmanagementsystem.api.service.StudentService;

@RestController
@RequestMapping(value = "student")
@CrossOrigin
public class StudentController {
	
	@Autowired
	private StudentService studentService;
	
	/**
	 * check
	 * Save or update student details.
	 *
	 * @param studentSaveRequestDto All content of the student (from request body)
	 * @return Confirmation message indicating saved or updated student details
	 * @author Vijiyakumar
	 */	
	@PostMapping("/save")
	public ResponseEntity<?> saveStudent(@RequestBody StudentDto studentDto){
		return new ResponseEntity<>(studentService.saveStudent(studentDto),HttpStatus.OK);
	}
	
	/**
	 * Retrieves the student list.
	 *
	 * @param searchBy       (optional) The field to search by: name, email, or phoneNumber.
	 * @param searchValue    (optional) The value to search for, based on the selected searchBy field.
	 * @param residingStatus (optional) The residential status of the student 
	 *                       (HOSTEL -> Hostel, DAYSCHOLAR -> Day Scholar).
	 * @param status         (optional) The status of the student 
	 *                       (ACTIVE -> Active, DEACTIVE -> Deactive).
	 * @param classOfStudy   (Require)  The class of study of the student (6–10).
	 * @param sortingBy      (optional) The column name to sort by.
	 * @param sortingOrder   (optional) The sorting order: asc → ascending, desc → descending.
	 * @return Return list of students that match the given filter criteria.
	 * @author Vijiyakumar
	 */

	@GetMapping("/list")
	public ResponseEntity<?> listStudentDetails(@RequestBody StudentFilterDto filterDto){
		return new ResponseEntity<>( studentService.listStudentDetails(filterDto),HttpStatus.OK);
	}
	
	/**
	 * Activate or deactivate a student by ID.
	 *
	 * @param studentId The ID of the student to activate/deactivate 
	 * @param studentActiveStatus The status object containing activation details)
	 * @param TeacherId The tescher Id Who is create.
	 * @return Updated student details with active/deactive status
	 * @author Vijiyakumar
	 */	
	@PostMapping("/status-change")
	public ResponseEntity<?> activeOrDeactiveByStudentId(@RequestParam String status,@RequestParam Long studentId,@RequestParam Long TeacherId){
		return new ResponseEntity<>(studentService.activeOrDeactiveByStudentId(status,studentId,TeacherId),HttpStatus.OK);
	}
	
}

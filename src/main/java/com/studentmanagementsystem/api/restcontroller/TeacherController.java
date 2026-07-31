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
import com.studentmanagementsystem.api.model.custom.CommonFilterDto;
import com.studentmanagementsystem.api.model.custom.teacher.PasswordChangeDto;
import com.studentmanagementsystem.api.model.custom.teacher.TeacherDto;
import com.studentmanagementsystem.api.service.TeacherService;

@RestController
@RequestMapping(value = "teacher")
public class TeacherController {
	
	@Autowired
	private TeacherService teacherService;
	

	/**
	 * Save or update teacher details.
	 *
	 * @param teacherModelListDto The teacher details to be saved or updated (from request body)
	 * @return Confirmation message indicating that the teacher details were successfully saved or updated
	 * @author Vijiyakumar
	*/
	@CrossOrigin
	@PostMapping("/save")
	public ResponseEntity<?>  saveTeacher(@RequestBody TeacherDto teacherDto) {
		return new ResponseEntity<>( teacherService.saveTeacher(teacherDto),HttpStatus.OK);
	}
	
	
	/**
	 * Retrieve the list of all teacher details.
	 *
	 * @param filterDto (from request body)
	 * @return Retrun list of all teachers
	 * @author Vijiyakumar
	 */	
	@CrossOrigin
	@PostMapping("/list")
	public ResponseEntity<?>  teacherList(@RequestBody CommonFilterDto filterDto){
		return new ResponseEntity<>( teacherService.teacherList(filterDto),HttpStatus.OK);
	}
	
	/**
	 * Login to teacher Module.
	 *
	 * @param email
	 * @param Password
	 * @return Retrun list of all teachers
	 * @author Vijiyakumar
	 */	
	@CrossOrigin
	@PostMapping("/login")
	public ResponseEntity<?>  teacherLogin(@RequestParam String email,@RequestParam String password){
		return new ResponseEntity<>( teacherService.teacherLogin(email,password),HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping("/password-change")
	public ResponseEntity<?>  passwordChange(@RequestBody PasswordChangeDto passwordChangeDto){
		return new ResponseEntity<>( teacherService.passwordChange(passwordChangeDto),HttpStatus.OK);
	}

}

package com.capgemini.springbootbasic;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // only returns data
//@Contoller will search for html it is not there it will give data (returns view)
public class DemoController {
	
	@GetMapping("/a") // end point 
//	@ResponseBody //-->repeating data present in body
	public List<String> hello() {
		return List.of("Dingaaa","Dingiii","Tingaaa","Tingiiii");
	}
}

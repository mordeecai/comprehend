package com.esther.comprehend.poc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.esther.comprehend.poc.exception.TextSizeLimitException;
import com.esther.comprehend.poc.util.AwsComprehendService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/comprehend-poc")
public class AwsComprehendDetectKeyPhrasesController {
	
	@Autowired
	AwsComprehendService service;
	
	@RequestMapping("/extract-key-phrases/**")
	public List<KeyPhrase> analyzeWebPage(HttpServletRequest request) {
	    String fullUrl = request.getRequestURL().toString();
	    String url = fullUrl.split("/extract-key-phrases/")[1];
	    log.info(url);
	    
	    List<KeyPhrase> result = new ArrayList<KeyPhrase>();
		
		try {
			result = service.extractKeyPhrases(url);
		} catch(TextSizeLimitException e) {
			e.printStackTrace();
		}
	    
	    return result;
	}

}

package com.esther.comprehend.poc.util;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesRequest;
import com.amazonaws.services.comprehend.model.DetectKeyPhrasesResult;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectSyntaxRequest;
import software.amazon.awssdk.services.comprehend.model.DetectSyntaxResponse;
import software.amazon.awssdk.services.comprehend.model.SyntaxToken;
import com.amazonaws.services.comprehend.model.KeyPhrase;
import com.esther.comprehend.poc.exception.TextSizeLimitException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AwsComprehendService {
	public static final int TEXT_SIZE_LIMIT = 5000;
	
	public List<KeyPhrase> extractKeyPhrases(String url) throws TextSizeLimitException {
		List<KeyPhrase> result = new ArrayList<KeyPhrase>();
		String html = "";
		try {
			html = getText(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String text = extractText(html);
		
		log.info("Web page's ({}) text:\n\n{}\n\n", url, text);
		
		int textSize = text.getBytes().length;
//		if(textSize > 5000) {
//			throw new TextSizeLimitException(String.format("Text size exceeded limit %d; Expected %d", textSize, TEXT_SIZE_LIMIT));
//		}
		
        AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
 
        AmazonComprehend comprehendClient =
            AmazonComprehendClientBuilder.standard()
                                         .withCredentials(awsCreds)
                                         .withRegion("ap-southeast-1")
                                         .build();
                                         
        // Call detectKeyPhrases API
        log.info("Calling DetectKeyPhrases");
        
        
        
        for(int i = 0; i < text.length(); i += 4900) {
        	int endIndex = ((i + 4900) > text.length()) ? text.length() - 1 : i + 4900;
        	log.info("Start Index: {}; End Index: {}", i, endIndex);
            DetectKeyPhrasesRequest detectKeyPhrasesRequest = new DetectKeyPhrasesRequest().withText(text.substring(i, endIndex))
                                                                                           .withLanguageCode("en");
            DetectKeyPhrasesResult detectKeyPhrasesResult = comprehendClient.detectKeyPhrases(detectKeyPhrasesRequest);
            detectKeyPhrasesResult.getKeyPhrases()
            .stream()
            .filter(phrase -> phrase.getText().split(" ").length >= 5)
            .forEach(result::add);
        }
        
        log.info("End of DetectKeyPhrases\n");
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonInString2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

            log.info("JSON KeyPhrases:\n\n{}", jsonInString2);
	
        } catch (JsonProcessingException jpe) {
        	jpe.printStackTrace();
        }        
        return result;
	}
	
	//public String 
	
	/*
	 * Implement utility to extract text content of an HTML page
	 * scraping ONLY the content 
	 * */
	public static String getText(String url ) throws Exception {
		String result = "";
		URL oracle = new URL(("".equals(url)) ? "http://www.oracle.com/": url);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            result += inputLine;
        in.close();
        
        return result;
	}
	
	public static String extractText(String html) {
		Document doc = Jsoup.parse(html);
		String text = doc.text();
		return text;
	}

}

package com.mantuliu.retry.demo;


import java.io.IOException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MantuRetryLeveldb {

	public static void main(String [] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "LeveldbRetry.xml" });
		context.start();
		MantuRetryService bpApi = (MantuRetryService) context.getBean(MantuRetryService.class);
		bpApi.sendMessage("one","two","hello world");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

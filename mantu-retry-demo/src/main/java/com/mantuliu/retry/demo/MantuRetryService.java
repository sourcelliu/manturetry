package com.mantuliu.retry.demo;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.mantuliu.retry.core.common.RetryIntercept;
import com.mantuliu.retry.core.common.RetryStrategy;
import com.mantuliu.retry.core.receive.AspectReceiver;

@Controller
public class MantuRetryService {
	@Autowired
    AspectReceiver register;
	private static Logger logger = LoggerFactory.getLogger(MantuRetryService.class);
	int count =0 ;
	@RetryIntercept(retryFor = { IllegalArgumentException.class,NumberFormatException.class,RuntimeException.class}, 
			retryStrategy = { @RetryStrategy(unit=TimeUnit.SECONDS,interval=0,counts=3),@RetryStrategy(unit=TimeUnit.SECONDS,interval=1,counts=2) })
	public void sendMessage(String from,String to,String message) {
	    if(count<5) {
	        count++;
	        throw new ClassCastException("ddd");
	    }
        else {
            logger.info("somebody {} talk to {} : {}",from,to,message);
        }
	}
}

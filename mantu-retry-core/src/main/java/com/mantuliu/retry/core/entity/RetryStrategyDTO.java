package com.mantuliu.retry.core.entity;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RetryStrategyDTO implements Serializable{
    
	private Integer counts ;//重试次数
    private Integer interval ;//间隔时间
    private TimeUnit unit ; //间隔的时间单位
    private AtomicInteger hasRetryCounts = new AtomicInteger(0) ;//已经重试次数
    
    public Integer getCounts() {
		return counts;
	}
	public void setCounts(Integer counts) {
		this.counts = counts;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	public TimeUnit getUnit() {
		return unit;
	}
	public void setUnit(TimeUnit unit) {
		this.unit = unit;
	}
	public AtomicInteger getHasRetryCounts() {
		return hasRetryCounts;
	}
	public void setHasRetryCounts(AtomicInteger hasRetryCounts) {
		this.hasRetryCounts = hasRetryCounts;
	}
}


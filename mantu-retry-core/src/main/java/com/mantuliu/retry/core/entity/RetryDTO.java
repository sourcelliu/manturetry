package com.mantuliu.retry.core.entity;

import java.io.Serializable;
import java.util.List;


public class RetryDTO implements Serializable {

	private String serviceName;//服务名称，此名称至关重要，后面的重试校验机制都依赖于此名称
    private String className;//类的全称，例如：com.fcbox.retry.entity.SpringRetryDTO
    private String methodName;//对象的方法名
    private List<Object> parameters;//参数列表，只支持基本类型、spring和实现Serializable接口的pojo类
    private List<RetryParameter> paras;
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public List<Object> getParameters() {
        return parameters;
    }
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
	public List<RetryParameter> getParas() {
		return paras;
	}
	public void setParas(List<RetryParameter> paras) {
		this.paras = paras;
	}
}

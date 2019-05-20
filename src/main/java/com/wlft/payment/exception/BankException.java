package com.wlft.payment.exception;

import com.wlft.payment.common.TaskLog;

public class BankException extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TaskLog taskLog;

    public BankException(String message){
        super(message);
    }

    public BankException(String message, TaskLog taskLog){
        this(message);
        this.taskLog = taskLog;
    }
    public TaskLog getTaskLog(){
        return taskLog;
    }
}

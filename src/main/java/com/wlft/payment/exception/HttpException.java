package com.wlft.payment.exception;

public class HttpException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 11L;
	private int status = 0;

    public HttpException(String message, int status){
        super(message);
        this.setStatus(status);
    }

    protected void setStatus(int status){
        this.status = status;
    }

    public int getStatus(){
        return this.status;
    }
}

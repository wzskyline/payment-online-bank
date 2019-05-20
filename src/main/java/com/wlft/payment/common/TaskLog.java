package com.wlft.payment.common;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskLog {

    public static final String SUCCESS = "success";

    public static final String FAIL = "fail";
    //  任務id
    private BigDecimal id = new BigDecimal(0);
    //  銀行卡代碼
    private String accountCode = "";
    //  時間
    private Date time = new Date();
    //  狀態
    private String status = "";
    // 回传payment 狀態
    private String apiStatus = "";
    //  截圖
    private String img = "";
    //  金額
    private BigDecimal amount = new BigDecimal(0);
    //  charge
    private String charge = "";
    //  kind
    private String kind = "";
    public TaskLog(){
        status = "success";
    }

    public TaskLog(BigDecimal id, String accountCode, Date time, BigDecimal amount){
        setId(id);
        setAccountCode(accountCode);
        setTime(time);
        setStatus(SUCCESS);
        setAmount(amount);
    }

    public String getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(String apiStatus) {
        this.apiStatus = apiStatus;
    }

    public TaskLog(BigDecimal id, String accountCode, Date time, String state, String img, BigDecimal amount){
        setId(id);
        setAccountCode(accountCode);
        setTime(time);
        setStatus(state);
        setImg(img);
        setAmount(amount);
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public void setCharge( String charge) {
        this.charge = charge;
    }
    public String getCharge( ) {
        return charge;
    }

    public void setKind( String kind) {
        this.kind = kind;
    }
    public String getKind( ) {
        return kind;
    }
    public String toString(){
    	SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] data = new String[]{getId().toString(), getAccountCode(), "" + sdf.format(getTime()), getStatus(), getImg(), getAmount().toString()};
        return StringUtils.join(data, ",");
    }
}

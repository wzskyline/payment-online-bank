package com.wlft.payment.common;

import com.wlft.payment.exception.HttpException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Operator {

    //  帳號
    private String username;
    //  密碼
    private String password;
    //  銀行卡代號
    private String accountCode;
    //  銀行卡餘額
    private BigDecimal balance = new BigDecimal(0);
    //  Cookie
    private String cookie;

    private JSONObject data;

    private List<TaskLog> taskLogList;

    public Operator(){

    }

    public Operator(String username, String password){
        this.setUsername(username);
        this.setPassword(password);
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return this.username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    public void setAccountCode(String accountCode){
        this.accountCode = accountCode;
    }

    public String getAccountCode(){
        return this.accountCode;
    }

    public void setBalance(BigDecimal balance){
        this.balance = balance == null ? new BigDecimal(0) : balance;
    }

    public BigDecimal getBalance(){
        return this.balance;
    }

    public void setCookie(String cookie){
        this.cookie = cookie;
    }

    public String getCookie(){
        return this.cookie;
    }

    public void setData(JSONObject data){
        this.data = data;
    }

    public JSONObject getData(){
        return this.data;
    }

    public String toString(){
        return "username:" + this.getUsername() + "\tpassword:" + this.getPassword() + "\tcookie:" + this.getCookie();
    }

    public void login(String username, String password) throws HttpException {
        this.setUsername(username);
        this.setPassword(password);

        //  發送登錄請求
        SXApi.login(this);

        //  載入歷史任務
        this.loadTaskLog();
    }

    public void logout(){
        this.setUsername(null);
        this.setPassword(null);
        this.setCookie(null);
        this.setData(null);
        this.cleanAccount();
        this.saveTaskLog();
    }

    public void cleanAccount(){
        this.setAccountCode(null);
        this.setBalance(new BigDecimal(0));
    }

    public void addTaskLog(TaskLog taskLog){
        this.taskLogList.add(taskLog);
    }

    public TaskLog getTaskLogByIndex(int index){
        return this.taskLogList.get(index);
    }

    public List<TaskLog> getTaskLogList(){
        return this.taskLogList;
    }

    private void loadTaskLog(){
        this.taskLogList = new ArrayList<TaskLog>();
        String data;
        String[] splitData;
        String[] line;

        //  Load data
        data = PcUtils.readFile(Config.get("output.path") + "\\task-" + FormatUtils.DATE_FORMATTER.format(new Date()) + ".csv");

        //  If no data, then return;
        if(data == null){
            return;
        }
        splitData = data.split("\n");
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
        	 for (int i = 0; i < splitData.length; i++){
                 if(i == 0){
                     continue;
                 }else if(StringUtils.isNotEmpty(splitData[i].trim())){
                     line = splitData[i].split(",");
                     this.taskLogList.add(new TaskLog(
                             new BigDecimal(line[0]),    //  id
                             line[1],    //  accountCode
                             //new Date(Long.parseLong(line[2])),  //  time
                             sdf.parse(line[2]),
                             line[3],    //  state
                             line[4],    //  img
                             new BigDecimal(line[5]) // amount
                     ));
                 }
             }
        } catch (Exception e) {
        	
        }
    }

    private void saveTaskLog(){
        StringBuilder sb = new StringBuilder();
        Iterator<TaskLog> it;

        if(this.taskLogList == null){
            return;
        }else if(this.taskLogList.size() == 0){
            this.taskLogList = null;
            return;
        }

        sb.append("Task Id,Account Code,Time,Status,Image,Amount\n");

        //  Generate string
        it = this.taskLogList.iterator();
        while(it.hasNext()){
            sb.append(it.next().toString() + "\n");
        }

        //  Write to file
        PcUtils.writeFile(Config.get("output.path") + "\\task-" + FormatUtils.DATE_FORMATTER.format(new Date()) + ".csv", sb.toString());

        //  close
        this.taskLogList.clear();
        this.taskLogList = null;
    }
}

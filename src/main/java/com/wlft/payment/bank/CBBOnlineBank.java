package com.wlft.payment.bank;

import com.wlft.payment.common.Config;
import com.wlft.payment.common.PcUtils;
import com.wlft.payment.common.SXApi;

import org.apache.commons.io.FileUtils;
import com.wlft.payment.common.TaskLog;
import com.wlft.payment.exception.BankException;
import org.apache.commons.lang3.StringUtils; 
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement; 
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.seimicrawler.xpath.JXDocument;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static com.wlft.payment.common.FormatUtils.DATE_TIME_FORMATTER2;
import static io.github.seleniumquery.SeleniumQuery.$;
import static java.lang.Thread.sleep;

import java.awt.image.BufferedImage;
import java.io.File;

public class CBBOnlineBank extends OnlineBank {
 

    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
    	 BANK_MAPPING.put("工商银行", "中国工商银行");  
    	 BANK_MAPPING.put("邮政储汇", "邮政");  
    }
    public CBBOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("CBB", id, accountCode, hostname, port);
    }

    @Override
    public void login(String username, String password, String queryPassword, String usbPassword) throws Exception {
        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        
        
        loginByPassword();
 

    }

    private void loginByPassword() throws Exception{
    	org.openqa.selenium.Point position = null;

        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        press(new String[]{"F5"}, 500, 50); 
        //  初始化訊息框
        initMessageBox();
        //  focus window
        //  clearPage(driver); 
        this.setMessage("Start 现代风格 登陆");
         
         
       
        int waitTime = 30;
        try {
        	while(waitTime>0) {
        		sleep(1000);
        		waitTime -- ;
        		if(position == null) {
        			 position = driver.findElement(By.id("LoginName")).getLocation();    	
        	    } else {
        	       	 break;
        	    }
        		
        	}	
        	
        }catch(Throwable x) {
        	
        }
        
        
        this.click( position.getX()+190  ,position.getY() - 80  );
        
        this.click( position.getX()  ,position.getY() - 30  );
         
        driver.executeScript( "$('#LoginName').val('"+username+"').focus().click()");
        press(new String[]{"Tab"}, 100, 50); 
        this.setMessage("Type password");
        
        press(new String[]{"a","Backspace"}, 1000, 50);
        sleep(3000);
 	    if( PcUtils.capsLock()){ 
      	   press(new String[]{"CapsLock"}, 10, 50);
      	   
         }
 	    
        for(int i = 0;i < password.length(); i++) { 
        	
        	press(password.substring(i,i+1).split(""), 100 + (int)(Math.random() * 100), 60);
        	
        } 
        this.setMessage("Start  登陆");
        this.click( position.getX() -90 ,position.getY() + 30  );
        
         // driver.executeScript( "$('.v-pristine').click()");
    	  
         driver.executeScript( "$('.bt1').click()");
         
         
         // NEED WAIT QUESTION 
         // 
         // NEED  WAIT PAGE 
          
         sleep(10000);
          
    }

  
    @Override
    public void logout() throws Exception {

    }

    @Override
    public void checkBalance(BigDecimal balance) throws Exception {
        sleep(5000);
        initMessageBox();
        setMessage("Fetch balance");
        driver.switchTo().window(windowHandle); 
        // CHECK BLANCE 
        String bal = driver.executeScript(" return $('.yb_balance:eq(0)').text()" ).toString(); 
        bal = bal.replaceAll("元","");
        bal = bal.replaceAll(" ","");
        bal = bal.replaceAll(",",""); 
        if(bal.length()>0) {
        	 this.balance = new BigDecimal(bal);
        } 
        if(this.balance.compareTo(balance) != 0){
            setMessage("Balance:" + bal + ". <span style=\"color:red;\">Different with payment 1.0. Please check</span>");
        }else{
            setMessage("Balance:" + bal);
        }  
    }
     
  
    
    @Override
    public BigDecimal getPageBalance( ) throws  Exception{
    	 
        setMessage("Fetch balance");
        
        driver.switchTo().window(windowHandle);
    	
        driver.executeScript("$('#menu>ul').children('li:eq(2)').find('a').click();" );
        sleep(5000);
    	 
        // CHECK BLANCE
        
        String bal = driver.executeScript(" return $('.yb_balance:eq(0)').text()" ).toString(); 
        bal = bal.replaceAll("元","");
        bal = bal.replaceAll(" ","");
        bal = bal.replaceAll(",","");
        
        if(bal.length()>0) {
        	 this.balance = new BigDecimal(bal);
        }
        
        if(this.balance.compareTo(balance) != 0){
            setMessage("Balance:" + bal + ". <span style=\"color:red;\">Different with payment 1.0. Please check</span>");
        }else{
            setMessage("Balance:" + bal);
        }
          
        return this.balance;
    };
     
    public TaskLog doTransfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch,String type ) throws Exception {
    	int step = 0;        
    	org.openqa.selenium.Point position;

    	TaskLog taskLog = new TaskLog(id, this.accountCode, new Date(), amount);
        String imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + ".png";
        driver.switchTo().window(windowHandle);
       try { 
        //  初始化pending
        this.initPending();
        this.setMessage("Start transaction");

        //  跳轉至轉帳頁面 
        
    	
        driver.executeScript("$('#menu>ul').children('li:eq(2)').find('a').click();" );
        sleep(5000); 
         
        //  waiting
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

        if(type.equals("other")) {
        	driver.executeScript("$('.cont_box').find('li:eq(1)>a').click()" );
            sleep(5000); 
            driver.executeScript("$('#winsTips>li:eq(1)>img').click()" ); // 关闭行内转账不然影响到后面jquery
        }
        
        //  初始化pending
        this.initMessageBox();
        this.initPending(); 
        this.setMessage("Mask fields");
 
      
        driver.executeScript("$('#PayeeAcName').after('<div class=\"payment-masker\" style=\"position:absolute; left:920px; top:170px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#PayeeAcNo').after('<div class=\"payment-masker\" style=\"position:absolute; left:990px; top:190px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#Amount').after('<div class=\"payment-masker\" style=\"position:absolute; left:465px; top:390px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        //  加入移除遮照的事件
        driver.executeScript("$('.payment-masker').on('click', function(){ var ans = prompt('Please enter password', ''); if(ans == 5201314){$('.payment-masker').remove();}})");

        driver.executeScript("$('#Amount').val('"+ amount + "').focus().click()");
        driver.executeScript("$('#PayeeAcNo').val('"+ accountNumber + "').focus().click()");
        driver.executeScript("$('#PayeeAcName').val('"+ accountName + "').focus().click()");
        this.setMessage("Start click");
        // 触发金额
        position = driver.findElement(By.id("Amount")).getLocation();
        this.click(position.getX(), position.getY()+18);
   
        // 触发帐户名
        position = driver.findElement(By.id("PayeeAcName")).getLocation();
        int x = position.getX() ;
        int y = position.getY() ;
        this.click(x - 150,y + 67 );
        press(new String[]{"a","Backspace"}, 1000, 50);
        press(new String[]{"Enter"}, 1000, 50);
        // 触发帐号
        this.click(x -120, y + 78);
        press(new String[]{"1","Backspace"}, 1000, 50);
        press(new String[]{"Enter"}, 1000, 50);
       
        if(type.equals("other")) {
        	// 触发银行选择列表 
        	this.click(x-30, y + 108);
        	sleep(1000);
        	driver.executeScript("$('.select2-input').val('"+(BANK_MAPPING.get(bankName) != null ? BANK_MAPPING.get(bankName) : bankName) +"').focus().click()" );
        	press(new String[]{"0","Backspace"}, 1000, 50);
        	press(new String[]{"Enter"}, 1000, 50);
        	this.click(x-60, y + 145); 
        }
        this.setMessage("Click 下一步");  
        driver.executeScript("$('.yb_an1').click()");
         
        
        
        // check page error
        sleep(2000);
      
        

         //取得 费用 charge  本行不需要费用  
         Object charge = null;    
           try { 
                charge = driver.executeScript("return $('#ConfirmForm>.box:eq(1)>.shoukuan>.rece-item>li:eq(1)').text() ");
                
             } catch (Exception e){
            	
           }
          
         
          if(charge == null ){
            
        	   taskLog.setCharge("0.0");
        	   
          } else {  
            
        	  String tmp = charge.toString(); //  String tmp = "手续费： 0.00  元"
            
        	  if(tmp.length()>1) {
        		  Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(tmp);
                  if (matcher.find()) {
                     tmp = matcher.group(1);
                     taskLog.setCharge(tmp);
                  }    
              } else {
            	  taskLog.setCharge("0.0");
              }
        	  System.out.println("手续 ===" + tmp);
         }
          
          
          //点击数字证书  
          this.click(x-105 , y +217 );
          // 点击验证确认信息的 下一步 
          sleep(1300);
          this.click(x-50 , y +303 );
          
         
          step = 1;
       
          
        } catch(Throwable x) {
        	    		
        } 	
        
       if(step == 1) {
    	   // 睡眠2秒后开始输入USBKey 密码
    	   System.out.println("睡眠2秒后开始输入USBKey 密码");
    	   sleep(2000);    
    	   press(new String[]{"a","Backspace"}, 1000, 50);
    	   sleep(1000);
           if( PcUtils.capsLock()){
        	   press(new String[]{"CapsLock"}, 200, 50);
           } 
           
          press(password.split(""), 1000, 50);  // at lest 100ms and  rund val
          press(new String[]{"Enter"}, 1000, 50);
         
          //呼叫: 开启 声音提示操作人员按下 OK
          new Thread(new Runnable() {
              public void run() {
                  PcUtils.open("a.wav");
              }
          }).start();
         
  		
  	   }

 
        // 循环等待   30秒内 操作人员按下
       String successInfo = "";
       int waitTime = 30;
        
        while(true) {
        	try {
        		sleep(1000);
        		waitTime --;
        		if(waitTime==0) {
        			break;
        		}
        		driver.switchTo().window(windowHandle);  
        		successInfo = driver.executeScript("return  $('.res_suc').find('H5').html() ").toString();
                if(successInfo.length()>0) {
                	break;
                }
        		
        	}catch(Throwable e) {
        		
        	}
        }
  
      
     if(successInfo.length() > 0 ) {
        	 
        	imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + "_success_.png";
            PcUtils.saveScreen(driver, imagePath);
            taskLog.setStatus(TaskLog.SUCCESS);
            taskLog.setImg(imagePath);   
            
        }else {  
            imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_usbPassword_error_.png";
            
            taskLog.setStatus(TaskLog.FAIL);
            taskLog.setImg(imagePath);
            new Thread(new Runnable() {
                public void run() {
                    PcUtils.open("a.wav");
                }
            }).start();
            PcUtils.captureScreen(this.code, id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_usbPassword_error_.png");
            throw new BankException("usbPassword_error", taskLog);
        }
       
         
        
        return taskLog;
    }
    public boolean checkTransfer(  BigDecimal amount   ) throws Exception {
   	 BigDecimal  oldBalance = balance;
   	 getPageBalance(); 
   	 
   	 if(balance.equals(oldBalance.subtract(amount))) {
   		    return true;	
    	 } else {
    		return false;
    	 }
   	
   }
   
   @Override 
   public TaskLog transfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
   
   	TaskLog tasklog = null ;
    // 1. 执行转账
   	try {
   		  if(accountNumber.substring(0,6).equals("621268")
   		    ||accountNumber.substring(0,6).equals("622684")
   		    ||accountNumber.substring(0,6).equals("621453")
   		    ||accountNumber.substring(0,6).equals("622884")) {
   			  
   			     tasklog =  doTransfer( id, bankName,accountNumber, accountName,  amount, memberBankProvince, memberBankCity, memberBankBranch,"same" );    	 
   		   
   		  } else {
   			   
   		         tasklog =  doTransfer( id, bankName,accountNumber, accountName,  amount, memberBankProvince, memberBankCity, memberBankBranch ,"other");
   		    		
   		   }
   	} catch(Exception x) {
   		x.printStackTrace();
   		
    } 	
    // 2. 验证转账     	
   	try {
   		
   		if(checkTransfer(amount)) {
           	 tasklog.setStatus(TaskLog.SUCCESS);	
        }
   		driver.executeScript(" $('#winsTips>li>img').click() " ); // 关闭所有分页
 
   		
       } catch(Throwable x) {
       
    }
   	
   	return tasklog;
   }

    @Override
    public void queryTransaction() throws Exception { 
        driver.switchTo().window(windowHandle);
    }

 
    private TaskLog fillHandle(BigDecimal id,TaskLog taskLog, String imageName) {
        String  imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_" + imageName + "_.png";
        PcUtils.saveScreen(driver, imagePath);
        taskLog.setStatus(TaskLog.FAIL);
        taskLog.setImg(imagePath);
        new Thread(new Runnable() {
            public void run() {
                PcUtils.open("a.wav");
            }
        }).start();
        return taskLog;
    }
 
    
     
}
 
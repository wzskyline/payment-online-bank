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

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static com.wlft.payment.common.FormatUtils.DATE_TIME_FORMATTER2;
import static io.github.seleniumquery.SeleniumQuery.$;
import static java.lang.Thread.sleep;

import java.awt.image.BufferedImage;
import java.io.File;

public class ICBCOnlineBank extends OnlineBank {
 

    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
    	 BANK_MAPPING.put("工商银行", "中国工商银行");  
    	 BANK_MAPPING.put("邮政储蓄", "邮政储汇");  
    }
    public ICBCOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("ICBC", id, accountCode, hostname, port);
    }

    @Override
    public void login(String username, String password, String queryPassword, String usbPassword) throws Exception {
        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        //  初始化訊息框
        initMessageBox();
        loginByPassword();
 

    }

    private void loginByPassword() throws Exception{


        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        //  初始化訊息框
        initMessageBox();

        //  focus window
        clearPage(driver); 
        this.setMessage("Start 用户名登陆");
        
        
        int x=0,y=0,w=0,h=0;

        WebElement ICBC_login_right = driver.findElement(By.className("ICBC_login_right"));
        
        x += ICBC_login_right.getLocation().x;
        y += ICBC_login_right.getLocation().y;
        
        driver.switchTo().frame("ICBC_login_frame");
        
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
        ((JavascriptExecutor) wd).executeScript("return $('#logonCardNum').val()").equals(""));
        
        String js = "$('#logonCardNum').val('"+username+"').focus().click()";	
        driver.executeScript(js );
        
        press(new String[]{"Tab"}, 100, 50);
        this.setMessage("Type password");
        
        press(new String[]{"a","Backspace"}, 1000, 50);
 	    sleep(500);
        if( PcUtils.capsLock()){
     	   press(new String[]{"CapsLock"}, 200, 50);
        } 
        press(password.split(""), 100, 50);
        
         
        driver.switchTo().defaultContent();
        driver.switchTo().frame("ICBC_login_frame");
        
        WebElement vcode_img_wrapper = driver.findElement(By.className("vcode-img-wrapper"));
        
        x += vcode_img_wrapper.getLocation().x - 210;
        y += vcode_img_wrapper.getLocation().y - 12;
        
        w = vcode_img_wrapper.getSize().width - 15;
        h = vcode_img_wrapper.getSize().height;
        
        File screen = PcUtils.captureScreen("ICBC","login.png");
        String codePath = Config.get("output.path")+"\\ICBC\\code.png";
        try {
            BufferedImage screenImg = ImageIO.read(screen);
            BufferedImage elementImg = screenImg.getSubimage(x,y,w,h);
            ImageIO.write(elementImg, "png", screen);
            FileUtils.copyFile(screen,new File(codePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.setMessage("Wait code value");
        
        String codeBase64 = PcUtils.encodeImgageToBase64(codePath);
        String codeId = SXApi.sendBase64To2captcha(codeBase64);
        String code = SXApi.getCodeFrom2captcha(codeId);
       
        driver.switchTo().window(windowHandle);
        driver.switchTo().defaultContent();
        driver.switchTo().frame("ICBC_login_frame");  
        js = "$('#verifyCodeCn').val('"+code+"').focus().click()";	
        driver.executeScript(js ); 
       
        
         js = "$('#loginBtn').click()";	
         driver.executeScript(js ); 
         
        driver.switchTo().window(windowHandle);
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

        sleep(300);

        clearPage2(driver);
        
        driver.switchTo().window(windowHandle);
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

        driver.switchTo().window(windowHandle);
        
        clearPageAdv(driver); 

        sleep(3000);
        driver.switchTo().window(windowHandle);

        // 为查询余额做准备
        driver.executeScript("$('#PBL201786r').click()");
        
        sleep(2000);
        driver.switchTo().frame("perbank-content-frame");

        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

    }

  
    @Override
    public void logout() throws Exception {

    }

    @Override
    public void checkBalance(BigDecimal balance) throws Exception {
 
        setMessage("Fetch balance");
        
        driver.switchTo().window(windowHandle);
        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");

        String  html = $("body").html();
        JXDocument body =  JXDocument.create(html);//kabao-main

        String xpath = "//table/tbody/tr/td[2]/table/tbody/tr/td/div/div[@class='kabao-block']/div/div[@class='kabao-list']/div[@class='kabao-main']/div[@id='li_1']/div[@class='kabao-main-item-box']/div[@class='kabao-main-item-center']/div/div/table/tbody/tr/td[3]/span/html()";
        List<Object> res =  body.sel(xpath);
        String bal = StringUtils.join(res, ",");
       
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
     
    public boolean isRequireMobile( ) throws  Exception{
    	  org.openqa.selenium.Point position;
    	  
    	  driver.switchTo().window(windowHandle);
          driver.switchTo().frame("perbank-content-frame");
          driver.switchTo().frame("content-frame");
          String mobile = "";
          
          try {
        	
        	  mobile = driver.findElement(By.id("getSMSCode")).getText();
          
          } catch(Throwable x) {
        	  
        	  
          }
          if(mobile.length()>1) {
        	  position = driver.findElement(By.id("otherMedium")).getLocation();
              // 点击元素 
              int  x = position.getX() + 20;
              int  y =position.getY() + 235;
              this.click(x+50,y-10);
              sleep(1000);
              this.click(x+160,y-10);
              return true;
          } else {
        	  return false;
          }
        		   
    };
    
    @Override
    public BigDecimal getPageBalance( ) throws  Exception{
    	
    	org.openqa.selenium.Point position;
        
    	setMessage("Fetch balance");
        
        driver.switchTo().window(windowHandle);
        driver.executeScript(  "$('#PBL200811r').click()" );
        driver.switchTo().window(windowHandle);
        driver.switchTo().frame("perbank-content-frame");
        
        //  waiting
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        
        //  初始化pending
        this.initMessageBox();
        this.initPending();
        this.setMessage("Fetch balance");
        
        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");
        position = driver.findElement(By.id("span_balance1")).getLocation();
        
        // 点击元素 
        int  x = position.getX() + 20;
        int  y =position.getY() + 235;
        this.click(x,y);
        
        // 获取值
        sleep(1500); // 必须睡眠 必须切回来 ....
        driver.switchTo().window(windowHandle);
        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");
        
        String bal  = driver.findElement(By.id("span_balance2")).getText();
        bal = bal.replaceAll(",","");
         
        if(bal.length()>0) {
       	  this.balance = new BigDecimal(bal);
       }
        return this.balance;
    };
     
    public TaskLog doTransfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
    	int step = 0;        
    	org.openqa.selenium.Point position;

    	TaskLog taskLog = new TaskLog(id, this.accountCode, new Date(), amount);
        String imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + ".png";

       try { 
        //  初始化pending
        this.initPending();
        this.setMessage("Start transaction");

        //  跳轉至轉帳頁面 
        driver.executeScript(  "$('#PBL200811r').click()" );
        driver.switchTo().window(windowHandle);
        driver.switchTo().frame("perbank-content-frame");
        //  waiting
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        //  初始化pending
        this.initMessageBox();
        this.initPending(); 
        this.setMessage("Mask fields");
 

        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");
        
        position = driver.findElement(By.id("remitAmtInput")).getLocation();
        
        driver.executeScript("$('#recNameShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:25px; top:10px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#recAcctShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:0px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#bankListShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:-14px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        
        //  加入移除遮照的事件
        // driver.executeScript("$('.payment-masker').on('click', function(){var ans = prompt('Please enter password', ''); if(ans == 5201314){$('.payment-masker').remove();}})");

        driver.executeScript("$('#recNameShow').val('"+ accountName + "')");
        driver.executeScript("$('#recAcctShow').val('"+ accountNumber + "')");
        driver.executeScript("$('#bankListShow').val('"+ (BANK_MAPPING.get(bankName) != null ? BANK_MAPPING.get(bankName) : bankName) + "')");
        driver.executeScript("$('#remitAmtInput').val('"+ amount + "').click()");
       
        this.setMessage("Set transaction fields end");
         
        int  x = position.getX() + 90;
        int  y = position.getY() + 250;
        this.click(x,y);
        press(new String[]{"Enter"}, 100, 50);
         
        // 点击银行
        this.click(x,y-20);
        press(new String[]{"Enter"}, 100, 50);
        sleep(5000);
        //  点击左侧空白(用于跳出)
        this.click(x-200,y);

        this.setMessage("Click 下一步");
        
        try{  
            driver.switchTo().frame("perbank-content-frame");
            driver.switchTo().frame("content-frame");
            driver.executeScript("$('#tijiao').trigger('click')");
            
            String tijiao = driver.executeScript("return $('#tijiao').html()").toString();
            position = driver.findElement(By.id("tijiao")).getLocation();
            
            this.click(position.getX() + 115, position.getY() + 210);
            this.click(x+100,y+200);
  
            tijiao = driver.executeScript("return $('#tijiao').trigger('click')").toString();
  
            driver.switchTo().frame("perbank-content-frame");
            driver.switchTo().frame("content-frame");
             
            new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                    ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

         } catch (Exception e){
            
            taskLog =  fillHandle(id,taskLog,"value_error");
            throw new BankException("赋值页面错误", taskLog);
         }

         //取得 费用 charge
         Object charge = null;     // 没有取到就是 0 因为空指针异常 不要抛出 bank 异常
           try { 
                charge = driver.executeScript("return $('#Fee-Money').text() ");
                
            } catch (Exception e){
            	
           }
        
        
 
          if(charge == null ){
            
        	   taskLog.setCharge("0.0");
        	   
          } else {  
            
        	  String tmp = charge.toString(); //  String tmp = "（手续费：7.50元）"
            
        	  if(tmp.length()>1) {
            	tmp = tmp.substring(5);
           	    tmp = tmp.replaceAll("元）","");
                taskLog.setCharge(tmp);
              } else {
            	  taskLog.setCharge("0.0");
              }           
         }
         // 是否显示手机验证  
         if(isRequireMobile()) {
        	 sleep(1500);
         }
         
         step = 1;
         this.setMessage("Click  确定 ");
         this.click(x+100+90,y+80); 
        
        } catch(Throwable x) {
        	    		
        } 	
        
       if(step == 1) {
          
    	   sleep(8000);  // 睡眠时间越长 越安全
          
    	   press(new String[]{"a","Backspace"}, 1000, 50);
    	   sleep(500);
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
        String tag = "";
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
                driver.switchTo().frame("perbank-content-frame");
                driver.switchTo().frame("content-frame"); 
                tag = driver.findElement(By.className("middleFontSize")).getText();
                successInfo  = driver.findElement(By.id("snapDiv")).getText();
                if(tag.length()>0) {
                	break;
                }
        		
        	}catch(Throwable e) {
        		
        	}
        }
 
          
        
     if(tag.length() > 0 && successInfo.length() > 0) {
        	 
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
       
   		tasklog =  doTransfer( id, bankName,accountNumber, accountName,  amount, memberBankProvince, memberBankCity, memberBankBranch );
   		
   	} catch(Throwable x) {
   		
    } 	
    // 2. 验证转账     	
   	try {
   		
   		if(checkTransfer(amount)) {
           	 tasklog.setStatus(TaskLog.SUCCESS);	
        }
   		
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


    public   void clearPage(RemoteWebDriver driver) throws Exception {
        boolean run = true;
        int     runCount = 0;
        while(run){
            try{
                sleep(700);
                runCount++;
                WebElement element  =driver.findElement(By.id("onemap"));
                if(element != null){
                    driver.executeScript( "oneclick();");
                    driver.executeScript( "twoclick();");
                    driver.executeScript( "$('#threemap area').click()");
                    sleep(2000);

                    run = false;
                }else{
                    run = false;
                }
                if(runCount ==1){
                    run = false;
                    break;
                }
            }catch (Exception e){
                run = false;
                break;
            }

        }
    }
    
    public   void clearPage2(RemoteWebDriver driver) throws Exception {
        boolean run = true;
        int     runCount = 0;
        while(run){
            try{
                sleep(700);
                runCount++;
                WebElement element  =driver.findElement(By.id("onemap"));
                if(element != null){
                    driver.executeScript( "$('#threemap area:eq(1)').click()");
                    sleep(2000);

                    run = false;
                }else{
                    run = false;
                }
                if(runCount ==1){
                    run = false;
                    break;
                }
            }catch (Exception e){
                run = false;
                break;
            }

        }
    }
   
    public   void clearPageAdv(RemoteWebDriver driver) throws Exception {
    	// 有界面才点击
        int     runCount = 0;
        while(true){
            try{
                sleep(700);
                runCount++;
                 
                 click(870, 80);
                   
                if(runCount ==1){
                    break;
                }
            }catch (Throwable e){
                break;
            }

        }
    }
}
 
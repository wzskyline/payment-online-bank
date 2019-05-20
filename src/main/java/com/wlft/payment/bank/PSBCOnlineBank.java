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

public class PSBCOnlineBank extends OnlineBank {
 

    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
    	 BANK_MAPPING.put("工商银行", "中国工商银行");  
    	 BANK_MAPPING.put("邮政储汇", "邮政");  
    }
    public PSBCOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("PSBC", id, accountCode, hostname, port);
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
        //  clearPage(driver); 
        //  this.setMessage("Start 用户名登陆");
        String js = "";
        js = "$('#logType').click()";	
        driver.executeScript(js );
        //username = "hwxnlmsr";
        //password = "kk686262";
        
        js = "$('#logonId').val('"+username+"').focus().click()";	
        driver.executeScript(js );
        press(new String[]{"Tab"}, 100, 50);
        this.setMessage("Type password");
        

        for(int i = 0;i < password.length(); i++) {
        	
        	press(password.substring(i,i+1).split(""), 100 + (int)(Math.random() * 100), 60);
        	
        }
        
         
         
        
         
       
        int x=0,y=0,w=0,h=0;

        WebElement verifyImg = driver.findElement(By.id("verifyImg"));
        
        x += verifyImg.getLocation().x-215;
        y += verifyImg.getLocation().y+10;

        w = verifyImg.getSize().width - 20;
        h = verifyImg.getSize().height;
        
        
        
        
     
        File screen = PcUtils.captureScreen("PSBC","login.png");
        String codePath = Config.get("output.path")+"\\PSBC\\code.png";
        try {
            BufferedImage screenImg = ImageIO.read(screen);
            BufferedImage elementImg = screenImg.getSubimage(x,y,w,h);
            ImageIO.write(elementImg, "png", screen);
            FileUtils.copyFile(screen,new File(codePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.setMessage("Wait code value");
        Object[] options = {"Yes,是的", "No,取消¦"};
        JOptionPane.showOptionDialog(null, " again?", "询问 ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,null);
        sleep(3000);
      
    }

  
    @Override
    public void logout() throws Exception {

    }

    @Override
    public void checkBalance(BigDecimal balance) throws Exception {
 
        setMessage("Fetch balance");
        
        driver.switchTo().window(windowHandle);
    	
        driver.executeScript("$('#firsrMenu').children('li:eq(2)').click();" );
        sleep(3000);
        // CHECK BLANCE
        WebElement balanceElement = driver.findElement(By.id("balance"));
        String bal = balanceElement.getText();
        // String bal = driver.executeScript(" return $('#balance').html()" ).toString();
      
  
       
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
    	
    	org.openqa.selenium.Point position;
        
    	setMessage("Fetch balance");
      
        return this.balance;
    };
     
    public TaskLog doTransfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
    	int step = 0;        
    	org.openqa.selenium.Point position;

    	TaskLog taskLog = new TaskLog(id, this.accountCode, new Date(), amount);
        String imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + ".png";
        Object charge = null;
        try {
        //  初始化pending
        this.initPending();
        this.setMessage("Start transaction");

        //  跳轉至轉帳頁面 
        driver.executeScript("$('#firsrMenu').children('li:eq(2)').click();" );
        driver.switchTo().window(windowHandle);
         
        //  waiting
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        //  初始化pending
        this.initMessageBox();
        this.initPending(); 
        this.setMessage("Mask fields");
 
  
        
        driver.executeScript("$('#recAccountshow').after('<div class=\"payment-masker\" style=\"position:absolute; left:255px; top:20px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#recAccountName').after('<div class=\"payment-masker\" style=\"position:absolute; left:255px; top:20px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#recAccountOpenBank').after('<div class=\"payment-masker\" style=\"position:absolute; left:255px; top:14px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        
        //  加入移除遮照的事件
        // driver.executeScript("$('.payment-masker').on('click', function(){var ans = prompt('Please enter password', ''); if(ans == 5201314){$('.payment-masker').remove();}})");

        driver.executeScript("$('#transferSum').val('"+ amount + "').focus().click()");
        //driver.executeScript("$('#recAccountshow').val('"+ accountNumber + "').focus().click()");
        driver.executeScript("$('#recAccountName').val('"+ accountName + "').focus().click()");

        WebElement transAmt = driver.findElement(By.id("recAccountshow"));
        transAmt.sendKeys(accountNumber.toString());
        press(new String[]{"Tab"}, 100, 50);

        //driver.executeScript(" $('#recAccountOpenBank').click();");
          
        //sleep(3000);
        
        //driver.executeScript("$('#queryBank').val('"+ (BANK_MAPPING.get(bankName) != null ? BANK_MAPPING.get(bankName) : bankName) + "')");
        //driver.executeScript("$('#query').click()");
        
        //position = driver.findElement(By.id("reList")).getLocation();
        
        //this.click(position.getX()-45, position.getY()-273);

        sleep(2000);

        driver.executeScript("$('#nextBut').click()");

        this.setMessage("sleep 120s");
        // 等待时间
        sleep(60000);
        //
        this.setMessage("sleep end and 确认");
        driver.executeScript("$(\"button[data-toggle='submit']\").click()");
        sleep(2999);
        // 没有 iframe 的 PSBC


           System.out.println("get charge");
           try {
               charge = driver.executeScript("return $('#showFee').text() ");
            } catch (Exception e){
           }
        
        
 
          if(charge == null ){
        	   taskLog.setCharge("0.0");
          } else {
        	  String tmp = charge.toString();
              taskLog.setCharge(tmp);
         }

           System.out.println(charge);
        } catch(Throwable x) {
        	    		
        }

        charge = driver.executeScript("return $('#recAccount').text() ");

     if(((String) charge).length()>0) {
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
 
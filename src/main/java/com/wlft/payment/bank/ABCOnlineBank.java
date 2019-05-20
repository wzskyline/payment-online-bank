package com.wlft.payment.bank;


import com.wlft.payment.common.PcUtils;
import com.wlft.payment.common.TaskLog;
import com.wlft.payment.exception.BankException; 
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal; 
import java.util.Date;
import java.util.HashMap; 
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.supermoonie.winio.VirtualKeyBoard.press; 
import static java.lang.Thread.sleep;
import static com.wlft.payment.common.FormatUtils.*;

public class ABCOnlineBank extends OnlineBank {

    private static Logger logger = Logger.getLogger(ABCOnlineBank.class);
    
    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
        BANK_MAPPING.put("工商银行", "中国工商银行");
        BANK_MAPPING.put("上海浦东发展银行", "浦东发展银行");
        BANK_MAPPING.put("邮政储蓄", "中国邮政储蓄银行");
        
        
    }
    public ABCOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("ABC", id, accountCode, hostname, port);
    }

    @Override
    public void login(String username, String password, String queryPassword, String usbPassword) throws Exception {
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
        // 测试数据 写值
//        username = "nhbvfgtr";
//        password = "zz790890";
//        queryPassword = "790890";
//        usbPassword = "zz790890";

        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        //  初始化訊息框
        initMessageBox();
        
        // 有usbPassword，但是沒有U盾時
        if(StringUtils.isNotEmpty(usbPassword) && !checkKbao()){
            this.close();
            throw new BankException("Please insert USB device");
        }

        //  1.有U盾則使用U盾登入
        //  2.沒有U盾則使用帳密登入
        if(StringUtils.isNotEmpty(usbPassword)){

            loginByKbao();
        }else{
            loginByPassword();
        }

        //  初始化訊息框
        initMessageBox();

        //   clear Introduce
        driver.executeScript(" $('.intro-close').click(); ");
    }

    private void loginByPassword() throws Exception{

        setMessage("Type username");
        driver.executeScript(" $('[name=\"username\"]').focus()");
        press(this.username.trim().split(""), 1000, 200);

        setMessage("Type password");
        press(new String[]{"Tab"}, 1000, 200);
        press(password.trim().split(""), 1000, 200);

        setMessage("Type Captcha");
        press(new String[]{"Tab"}, 1000, 200);
        press("capc".trim().split(""), 1000, 200);

        setMessage("Click  登录");
        driver.executeScript(" $('input.m-uersbtn').click()");

        waiting();

        //  Check
        if(driver.getCurrentUrl().indexOf("login.do") == -1){
            Object msg = "";

            msg = driver.executeScript("return $('#username-error').text();");
            if(StringUtils.isNotEmpty(msg.toString())){
                throw new BankException(msg.toString());
            }

            msg = driver.executeScript("return $('#powerpass_ie_dyn_Msg').text();");
            if(StringUtils.isNotEmpty(msg.toString())){
                throw new BankException(msg.toString());
            }

            msg = driver.executeScript(" return $('.logon-error').text();");
            if(StringUtils.isNotEmpty(msg.toString())){
                throw new BankException(msg.toString());
            }

            throw new BankException("Unknown error");
        }
    }

    private void loginByKbao() throws Exception{
        logger.info("Open K宝登录");
        setMessage("Open K宝登录");
        driver.executeScript("$('.m-login-kbaoLink').trigger('click');");

        setMessage("Type password");
        driver.executeScript(" $('#m-kbbtn').click()");

        //org.openqa.selenium.Point kBaoLoginPoint = driver.findElement(By.id("PowerEnterDiv_powerpass_2")).getLocation();
        //this.click(kBaoLoginPoint.getX() - 50, kBaoLoginPoint.getY() - 50);
//        press(new String[]{"Tab"}, 1000, 200);
//        press(new String[]{"a","Backspace"}, 1000, 50);
 	    sleep(500);
//        if( PcUtils.capsLock()){
//     	   press(new String[]{"CapsLock"}, 200, 50);
//        }
        press(usbPassword.trim().split(""), 1000, 200);

        setMessage("Click  登录");
        driver.executeScript(" $('#m-kbbtn').click()");

       
        //waiting();
        
        while(true) {
        	 sleep(1000);
        	 if(driver.getCurrentUrl().indexOf("login.do") > -1){
        		 break;
        	 }
        }
        	
         

        //  Check
        if(driver.getCurrentUrl().indexOf("login.do") == -1){
            Object msg = "";

            msg = driver.executeScript(" return $('#PowerEnterDiv_powerpass_2_Msg').text();");

            PcUtils.saveScreen(driver, "\\" + DATE_TIME_FORMATTER2.format(System.currentTimeMillis()) + "_fail_.png");

            if(StringUtils.isNotEmpty(msg.toString())){
                throw new BankException(msg.toString());
            }

            msg = driver.executeScript(" return $('.logon-error').text();");
            if(StringUtils.isNotEmpty(msg.toString())){
                throw new BankException(msg.toString());
            }



            throw new BankException("Unknown error");
        }
    }

    @Override
    public void logout() throws Exception {
        driver.executeScript(" $('#logout_a').click()");
        driver.switchTo().alert().accept();
        this.close();
    }

    @Override
    public void checkBalance(BigDecimal balance) throws Exception {
        //  跳回最外層，以保證之後的操作是正確的
        driver.switchTo().window(windowHandle);

        setMessage("Fetch balance");
        String bal = driver.switchTo().frame(0).findElement(By.id("dnormal")).getText();

        if(StringUtils.isEmpty(bal)){
            setMessage("Not fetch Balance");
            return;
        }
        bal = bal.replaceAll(",","");
        this.balance = new BigDecimal(bal);

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
        driver.executeScript("$('#menuNav').find('ul').children('li:eq(3)>ul:eq(0)>li:eq(0)').click();");
        driver.switchTo().frame("contentFrame");
        
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        
        sleep(3000);  
        String bal = driver.findElement(By.id("fromAcctBalance")).getText();
        bal = bal.replaceAll(",",""); 
        
        if(bal.length()>0) {
        	  balance = new BigDecimal(bal);
        	  System.out.println(" balance= "+balance);
        	  return balance;
        } else {
        	 return this.getBalance();
        }
      
    };
    

    void clearVerifyPopup() {
        String idName = "popupbox-confirm-sure-0";
        By id = By.id(idName);
        try{
            new WebDriverWait(driver, 2).until(ExpectedConditions.visibilityOfElementLocated(id));
//            driver.executeAsyncScript(String.format("$('#%s').focus()", idName));
            driver.executeScript(String.format("$('#%s').focus()", idName));
            press(new String[]{"Enter"}, 1000, 200);
//            driver.findElement(id).click();
        } catch (ScriptTimeoutException e) {
            logger.info("wait ScriptTimeoutException ", e);
        }
        catch (TimeoutException e) {
            logger.info("wait verify popup timeout ", e);
        }
        catch (NoSuchElementException e) {
            logger.info("wait verify popup NoSuchElementException", e);
        }
        catch (Exception e) {

        }
    }
    public TaskLog doTransfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
    	org.openqa.selenium.Point position;
        TaskLog taskLog = new TaskLog(id, this.accountCode, new Date(), amount);
        String imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + ".png";

        //  初始化pending
        this.initPending();

        this.setMessage("Start transaction");

        //  跳轉至轉帳頁面
        driver.executeScript("$('#menuNav').find('ul').children('li:eq(3)>ul:eq(0)>li:eq(0)').click();");

        //  waiting
        driver.switchTo().frame("contentFrame");
        //  剩下错误里面出现比较少可以改写的一个错误 bug 隐患bug
        new WebDriverWait(driver, 7).until((ExpectedCondition<Boolean>) wd ->
                 ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        int   waitTime = 30;
     
        
        //  加入遮照
        this.setMessage("Mask fields");
        driver.switchTo().frame("contentFrame");
        driver.executeScript("$('.right').css('position','relative');");
        driver.executeScript("$('.selectric-trn_fromAcctNo').after('<div class=\"payment-masker\" style=\"position:absolute; left:65px; top:14px; width:200px; height:32px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#toAcctNameKey').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px;  width:100px !important; height:32px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#toAcctNo').after('<div class=\"payment-masker\" style=\"position:absolute; left:50px; width:235px; height:32px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        //  加入移除遮照的事件
        driver.executeScript("$('.payment-masker').on('click', function(){var ans = prompt('Please enter password', ''); if(ans == 5201314){$('.payment-masker').remove();}})");

        //  設定轉帳參數
        this.setMessage("Set transaction fields");
        driver.switchTo().frame("contentFrame");
        //  收款方,收款銀行
        driver.executeScript("$('#toAcctNameKey').val('"+ accountName + "')");
        driver.executeScript("$('#bankKey').val('"+ (BANK_MAPPING.get(bankName) != null ? BANK_MAPPING.get(bankName) : bankName) + "')");
        driver.executeScript("$('#toAcctNo').val('"+ accountNumber + "')");
        driver.executeScript("$('#transAmt').trigger('click').click()");
        WebElement transAmt = driver.findElement(By.id("transAmt"));
        transAmt.sendKeys(amount.toString());
        press(new String[]{"Tab"}, 1000, 200);


          
        this.setMessage("Click 下一步");
        driver.switchTo().frame("contentFrame");
        driver.executeScript("$('#transferNext').click()");

         logger.info("点击下一步后进入等待:");

        try{
            // driver.switchTo().frame("contentFrame");
            new WebDriverWait(driver, 7).until(new ExpectedCondition<Boolean>() {
                @NullableDecl
                @Override
                public Boolean apply(@NullableDecl WebDriver webDriver) {
                    assert webDriver != null;
                    return webDriver.findElements(By.id("PowerEnterDiv")).size() > 0;
                }
            });

//            position = driver.findElement(By.id("PowerEnterDiv")).getLocation();
        }catch (Exception e){
            logger.info("内层错误描述"+e.getMessage());

            taskLog =  fillHandle(id,taskLog,"value_error");
            throw new BankException("value_page_error", taskLog);
        }


        //取得 费用 charge
        try{
            String  charge =   driver.executeScript("return $('.trn_confirmBalance:eq(1)').html() ").toString();
            charge = charge.substring(0,charge.indexOf(".")+3);
            taskLog.setCharge(charge);
        }catch (Exception e){
        	
            taskLog =  fillHandle(id,taskLog,"while_charge_error");
            throw new BankException("while_charge_error", taskLog);
        }

        //  輸入轉帳密碼
        //this.click(position.getX() + 130, position.getY() + 115);

        driver.executeScript("$('#agreeBtn').click()");
        for(int i = 0;i < queryPassword.length(); i++) {  
        	press(queryPassword.substring(i,i+1).split(""), 100 + (int)(Math.random() * 100), 60);
        	
        }
        logger.info("已完成密码输入");

        //driver.executeScript("$('#agreeBtn').click()");
        //driver.findElement(By.id("agreeBtn")).click();
        //System.out.println("已完成js 触发确认按钮");
        press(new String[]{"Tab"}, 1000, 200);
        press(new String[]{"Enter"}, 1000, 200);
        logger.info("已完成Tab 与 Enter 键盘");
        this.clearVerifyPopup();

        // 稍微等待一下 驱动密码自动输入并提交
        sleep(1500);
        if( PcUtils.capsLock()){
     	   press(new String[]{"CapsLock"}, 200, 50);
        } 
        press(usbPassword.split(""), 700, 50);
        press(new String[]{"Enter"}, 200, 50);
        logger.info("输入完毕USB密码");
        new Thread(new Runnable() {
            public void run() {
                PcUtils.open("a.wav");
            }
        }).start();
        
        // 循环等待 操作人员按下
        String tag = "";
        String successInfo = "";
         waitTime = 30;
        
        WebDriverWait wait = new WebDriverWait(driver, 60);
        boolean ok = false;
        int waitCount = 5;
        while(waitCount > 0) {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("m-serialnumberleft")));
                logger.info("本行代码已经执行,获取到成功标识");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trnTips")));
                ok = true;
                break;
            } catch (TimeoutException e) {
                waitCount -= 1;
                logger.info("wait element timeout exception:" + e.getMessage());
                e.printStackTrace();
                break;
            }
            catch (Exception e) {
                logger.info("wait exception:" + e.getMessage());
            }

        }
        if(ok) {
        	imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + "_success_.png";
            PcUtils.saveScreen(driver, imagePath);
            taskLog.setStatus(TaskLog.SUCCESS);
            taskLog.setImg(imagePath);

         } else {

            imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_usbPassword_error_.png";
            taskLog.setStatus(TaskLog.FAIL);
            taskLog.setImg(imagePath);

            new Thread(new Runnable() {
                public void run() {
                    PcUtils.open("a.wav");
                }
            }).start();

            PcUtils.captureScreen(this.code, id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_usbPassword_error_.png");
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
            logger.info("外层错误描述" + x.getMessage());
    		x.printStackTrace();
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

        //  跳回最外層，以保證之後的操作是正確的
        driver.switchTo().window(windowHandle);
    }

    private boolean checkKbao() {
        return driver.findElement(By.id("cspName")).getText().equals("K宝已插入");
    }
    
    private TaskLog fillHandle(BigDecimal id,TaskLog taskLog, String imageName) {
        try {
        	String  imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime())      + "_" + imageName + "_.png";
            PcUtils.saveScreen(driver, imagePath);
            taskLog.setStatus(TaskLog.FAIL);
            taskLog.setImg(imagePath);
            new Thread(new Runnable() {
                public void run() {
                    PcUtils.open("a.wav");
                }
            }).start();
        }catch(Throwable x) {
        	 logger.error("  fillHandle  Exception === ",x);
        }
        return taskLog;
    }
}

package com.wlft.payment.bank;

import com.wlft.payment.common.PcUtils;
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
 
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static com.wlft.payment.common.FormatUtils.DATE_TIME_FORMATTER2;
import static io.github.seleniumquery.SeleniumQuery.$;
import static java.lang.Thread.sleep;

public class CMBOnlineBank extends OnlineBank {
 

    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
        BANK_MAPPING.put("工商银行", "中国工商银行");
    }
    public CMBOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("CCB", id, accountCode, hostname, port);
    }

    @Override
    public void login(String username, String password, String queryPassword, String usbPassword) throws Exception {
        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        //  初始化訊息框
        initMessageBox();



        loginByPassword();

        //  初始化訊息框
        initMessageBox();

    }

    private void loginByPassword() throws Exception{


        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        org.openqa.selenium.Point position;


        clearPage(driver);


        this.setMessage("Start 用户名登陆");

        driver.switchTo().frame("fQRLGIN");
        String html = $("body").html();
        PcUtils.writeFile("src/main/resources/1.txt",html);


        position = driver.findElement(By.id("USERID")).getLocation();
        int  x = position.getX() + 90;
        int  y =position.getY() + 100;
        this.click(x,y);
        press("dcuijkei".split(""), 100, 100);
        press(new String[]{"Tab"}, 100, 50);
        setMessage("Type password");
        sleep(3000);
        //  监测键盘大小写
        if( PcUtils.capsLock()){
            press(new String[]{"CapsLock"}, 200, 50);
        }


        press("kk933699".split(""), 1000, 500);
        // press(new String[]{"Enter"}, 100, 50);

         //
        driver.switchTo().window(windowHandle);
        new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

        driver.executeScript("$('body').contents().find('#mainfrm').contents().find('#SafeTypeU').click().trigger('click')");
        driver.executeScript("$('body').contents().find('#mainfrm').contents().find('#btnNext').click().trigger('click')");

        // press("kk933699".split(""), 1000, 500);
        // press(new String[]{"Enter"}, 100, 50);
        // 开启 声音提示操作人员按下 OK
        new Thread(new Runnable() {
            public void run() {
                PcUtils.open("a.wav");
            }
        }).start();



        new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));

    }
 
    @Override
    public void logout() throws Exception {

    }

    @Override
    public void checkBalance(BigDecimal balance) throws Exception {
        // driver.executeScript(   "  $('#PBL201786r').click()  " );  wait page onload
        setMessage("Fetch balance");
        //  跳回最外層，以保證之後的操作是正確的
        driver.switchTo().window(windowHandle);
        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");

        String  html = $("body").html();
        JXDocument body =  JXDocument.create(html);//kabao-main

        String xpath = "//table/tbody/tr/td[2]/table/tbody/tr/td/div/div[@class='kabao-block']/div/div[@class='kabao-list']/div[@class='kabao-main']/div[@id='li_1']/div[@class='kabao-main-item-box']/div[@class='kabao-main-item-center']/div/div/table/tbody/tr/td[3]/span/html()";
        List<Object> res =  body.sel(xpath);
        String bal = StringUtils.join(res, ",");

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

        BigDecimal banlance = new BigDecimal("0");


        return banlance;
    };
    @Override
    public TaskLog transfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
        org.openqa.selenium.Point position;
 
        TaskLog taskLog = new TaskLog(id, this.accountCode, new Date(), amount);
        String imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + ".png";

        //  初始化pending
        this.initPending();

        this.setMessage("Start transaction");

        //  跳轉至轉帳頁面
        System.out.println("js://  跳轉至轉帳頁面");
        driver.executeScript(  "$('#PBL200811r').click()" );
        driver.switchTo().window(windowHandle);
        //  waiting
        driver.switchTo().frame("perbank-content-frame");
        System.out.println("driver://  perbank -frame");
        new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        //  初始化pending
        this.initMessageBox();
        this.initPending();
        System.out.println("//  初始化pending END ");
        this.setMessage("Mask fields");
        //  加入遮照
        System.out.println("//  this.setMessage(\"Mask fields\"); END ");


        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");
        System.out.println("driver://  content -frame");
        position = driver.findElement(By.id("remitAmtInput")).getLocation();
        driver.executeScript("$('#recNameShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:25px; top:10px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#recAcctShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:0px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#bankListShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:-14px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");


        driver.executeScript("$('#je10').val('"+ accountName + "')");
        driver.executeScript("$('#killa55').val('"+ accountNumber + "')");
        driver.executeScript("$('#RECVBRANCH1').val('"+ bankName + "')");
        driver.executeScript("$('#txtTranAmt').val('"+ amount + "').click()");


        this.setMessage("Set transaction fields end");
        //  設定轉帳參數 end
        // amount need  click  must  amount first  必须使用一个元素的坐标


        int  x = position.getX() + 90;
        int  y =position.getY() + 250;
        this.click(x,y);
        press(new String[]{"Enter"}, 100, 50);

        // bank
        this.click(x,y-20);
        press(new String[]{"Enter"}, 100, 50);
        sleep(5000);
        //  mid click one blank area
        this.click(x-200,y);


        System.out.println("//  設定轉帳參數 end ");
        try{  // 下一步

            // 因为页面刷新 driver 目前在 最外层  bank frame
            driver.switchTo().frame("perbank-content-frame");
            System.out.println(" 1. driver.switchTo().frame(\"perbank-content-frame\"); ");
            driver.switchTo().frame("content-frame");
            System.out.println(" 2. driver.switchTo().frame(\"content-frame\");");
            driver.executeScript("$('#tijiao').trigger('click')");
            String tijiao = driver.executeScript("return $('#tijiao').html()").toString();
            position = driver.findElement(By.id("tijiao")).getLocation();
            this.click(position.getX() + 115, position.getY() + 210);
            this.click(x+100,y+200);
            System.out.println("tijiao="+tijiao);
              tijiao = driver.executeScript("return $('#tijiao').trigger('click')").toString();
              //tijiao = driver.executeScript("  transferSubmit() ").toString();


            // Actions action = new Actions(driver);
            // action.moveToElement(driver.findElement(By.id("tijiao"))).click().build().perform();


            System.out.println("// 下一步 end ");
            driver.switchTo().frame("perbank-content-frame");
            System.out.println(" 1. driver.switchTo().frame(\"perbank-content-frame\"); ");
            driver.switchTo().frame("content-frame");
            System.out.println(" 2. driver.switchTo().frame(\"content-frame\");");
            new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                    ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
             // position = driver.findElement(By.id("Fee-Money")).getLocation();

        }catch (Exception e){
            // 获取赋值页面的错误信息
            taskLog =  fillHandle(id,taskLog,"value_error");
            throw new BankException("赋值页面错误", taskLog);
        }

        //取得 费用 charge
        Object charge = null;     // 没有取到就是 0 因为空指针异常 不要抛出 bank 异常
        try{
            System.out.println("//取得 费用 charge ");
             charge  =   driver.executeScript("return $('#Fee-Money').text() ");
            System.out.println("取得零钱"+charge);

        }catch (Exception e){


        }
 
        if(charge == null ){
            System.out.println("真的是空呀");
            taskLog.setCharge("0.0");
        } else {
            String tmp = charge.toString().substring(5);
            tmp = tmp.replaceAll("元）","");
            System.out.println(tmp);
            taskLog.setCharge(tmp);
        }



        // 按下确认键盘  this.click(x+100+100,y);
        driver.executeScript( " return $ ('#queren').trigger('click') ");

        // may be 盾出错 暂不处理
        // 输入密码
        System.out.println("开始睡眠");
        sleep(3000);
        System.out.println("开始输入");
        press("aa412452".split(""), 1000, 50);
        press(new String[]{"Enter"}, 1000, 50);
        System.out.println("输入结束");
        //呼叫: 开启 声音提示操作人员按下 OK
        new Thread(new Runnable() {
            public void run() {
                PcUtils.open("a.wav");
            }
        }).start();


        // 等待
        sleep(5000);
        try{
            driver.switchTo().window(windowHandle); // 因为有驱动层的问题 需要这个
            driver.switchTo().frame("perbank-content-frame");
            driver.switchTo().frame("content-frame"); 
            new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                    ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
            // System.out.println(driver.findElement(By.id("trnTips")).getText()); // 目前发现两类提示  1. 大额成功受理 //2. 您已成功转账1.00元给姚本春(6217993610018374594)
            imagePath =  "\\" + this.code + "\\" + id +"_" + DATE_TIME_FORMATTER2.format(taskLog.getTime()) + "_success_.png";
            PcUtils.saveScreen(driver, imagePath);

            taskLog.setStatus(TaskLog.SUCCESS);
            taskLog.setImg(imagePath);
        }catch (Exception ee){
            // System.out.println(driver.findElement(By.id("popup_k_verifySigned_content")).getText());  // 签名失败! ²Ù×÷È¡Ïû!
            taskLog =  fillHandle(id,taskLog,"usbPassword_error");
            throw new BankException("签名失败错误", taskLog);
        }

        return taskLog;
    }

    @Override
    public void queryTransaction() throws Exception {

        //  跳回最外層，以保證之後的操作是正確的
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
    // 为什么用这种 蛋疼的方式   因为 并不一定能确信  元素出现
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
    // 为什么用这种 蛋疼的方式   因为 并不一定能确信  元素出现
    public   void clearPageAdv(RemoteWebDriver driver) throws Exception {
        boolean run = true;
        int     runCount = 0;
        while(run){
            try{
                sleep(700);
                runCount++;
                WebElement element  =driver.findElement(By.id("emall_closebtn"));
                if(element != null){
                    driver.executeScript( "$('#emall_closebtn').click()");
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
}

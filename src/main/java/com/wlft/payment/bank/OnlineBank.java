package com.wlft.payment.bank;

import com.wlft.payment.common.PcUtils;
import com.wlft.payment.common.TaskLog;
import com.wlft.payment.swing.ErrorDialog;

import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static io.github.seleniumquery.SeleniumQuery.$;

public abstract class OnlineBank {

    //  銀行代碼
    protected String code = "";
    //  銀行卡ID
    protected Integer id;
    //  銀行卡代碼
    protected String accountCode;
    //  餘額
    protected BigDecimal balance;
    //  帳號
    protected String username;
    //  登入密碼
    protected String password;
    //  轉帳密碼
    protected String queryPassword;
    //  U盾密碼
    protected String usbPassword;
    //  Robot
    protected Robot robot;
    //  selenium
    protected RemoteWebDriver driver;
    //  main window handle
    protected String windowHandle;
    //  Default browser
    protected String browser = IE;

    public static final String IE = "IEDriverServer.exe";

    public static final String FIRE_FOX = "";

    public  static final String CHROME = "";

    public OnlineBank(String code, Integer id, String accountCode, String hostname, Integer port){
        this.code = code;
        this.setId(id);
        this.setAccountCode(accountCode);
        this.createDriver(hostname, port);

        //  產生機器人
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
         * 網銀登入
         */
    public abstract void login(String username, String password, String queryPassword, String usbPassword) throws Exception;

    /**
         * 網銀登出
         */
    public abstract void logout() throws Exception;
    /**
         * 確認餘額
         */
    public abstract void checkBalance(BigDecimal balance) throws Exception;

    /**
     * 查询页面餘額
     */
    public abstract BigDecimal getPageBalance() throws  Exception;

    /**
         * 轉帳
         */
    public abstract TaskLog transfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount, String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception;

    /**
         * 查詢交易紀錄
         */
    public abstract void queryTransaction() throws Exception;

    public void setId(Integer id){
        this.id = id;
    }

    public Integer getId(){
        return this.id;
    }

    public void setAccountCode(String accountCode){
        this.accountCode = accountCode;
    }

    public String getAccountCode(){
        return this.accountCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setLoginInfo(String username, String password, String queryPassword, String usbPassword){
        this.username = username;
        this.password = password;
        this.queryPassword = queryPassword;
        this.usbPassword = usbPassword;
    }

    public void open(String url) throws Exception{

        //  開啟網銀頁面
        driver.get(url);

        //  监测设备是否大写锁定
        if(PcUtils.capsLock()){
            press(new String[]{"CapsLock"}, 200, 50);
        }

        //  設定螢幕座標
        driver.manage().window().setPosition(new Point(0, 0));
        //  設定螢幕大小
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1125, 830));

        //  focus window
        String currentWindow = driver.getWindowHandle();
        driver.switchTo().window(currentWindow);
        // add 
    }

    public void initMessageBox(){
        String currentHandle = driver.getWindowHandle();

        ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript("" +
                "$(document).ready(function(){if(!$('#payment-online-bank-message').html()){" +
                "   $('body').append('<div id=\"payment-online-bank-message\" style=\"position:fixed; " +
                "top: 300px; " +
                "left:10px; " +
                "width:300px; " +
                "height:60px; " +
                "padding:10px; " +
                "background:#000000; " +
                "z-index:999;"+
                "line-height:20px; " +
                "font-size:18px; " +
                "color:#FFFFFF; " +
                "\"></div>');}" +
                "});");

        driver.switchTo().window(currentHandle);
    }

    public void initPending(){
        String currentHandle = driver.getWindowHandle();

        ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript(
                "window.tcgPendingFlag = false;");
        ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript(
                "   $('body').append('<div id=\"payment-online-bank-status\" style=\"position:fixed; " +
                "top: 400px; " +
                "left:10px; " +
                "width:50px; " +
                "height:50px; " +
                "border-radius: 25px; " +
                "text-align:center; " +
                "background:#FF0000; " +
                "line-height:50px; " +
                "font-size:16px; " +
                "cursor: pointer; " +
                 "z-index:999;"+
                "color:#FFFFFF; \">Run</div>');");
        ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript(
                "$(window).on(\"keypress\",function(e){if(e.keyCode == 27){window.tcgPendingFlag = !window.tcgPendingFlag; $(\"#payment-online-bank-status\").text(window.tcgPendingFlag ? 'Pause': 'Run');};});");

        driver.switchTo().window(currentHandle);
    }

    /**
         *  顯示訊息
         * @param message 訊息
         */
    public void setMessage(String message){
        ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript("$('#payment-online-bank-message').html('" + message + "');");
    }

    protected void createDriver(String hostname, Integer port){

        if(browser == IE){
            String driverPath = System.getProperty("user.home") + File.separator + browser;
            System.setProperty("webdriver.ie.driver", driverPath);

            InternetExplorerOptions options = new InternetExplorerOptions();
            options.ignoreZoomSettings();
            options.requireWindowFocus();
            setProxy(options, hostname, port);
            driver = new InternetExplorerDriver(options);
            driver.manage().timeouts().pageLoadTimeout(91,TimeUnit.SECONDS);
            $.driver().use(driver);
        }else if(browser == CHROME){
           // ChromeDriver options = new ChromeDriver();
        }else if(browser == FIRE_FOX){
           // FirefoxDriver options = new FirefoxDriver();

        }
        //  記錄該driver的windowHandle，供後續使用
        windowHandle = driver.getWindowHandle();
    }

    /**
         * 等待頁面載入完成
         */
    protected void waiting(){
        new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState;").equals("complete"));
    }

    /**
         *
         */
    protected void checkPending(){
        boolean flag = true;

        while (flag){
            Object ret = ((RemoteWebDriver)driver.switchTo().window(windowHandle)).executeScript("return window.tcgPendingFlag;");
            flag = ret.equals("true");
        }
    }

    /**
     *  模擬滑鼠移動
     * @param x - X軸
     * @param y - Y軸
     */
    public void move(Integer x, Integer y){ 
        //  切回主畫面
        driver.switchTo().window(windowHandle); 
        org.openqa.selenium.Point windowPosition = driver.manage().window().getPosition(); 
        Long outerHeight = (Long)driver.executeScript("return window.outerHeight;");  // window.innerWidth; 不一定有定义
        Long outerWidth = (Long)driver.executeScript("return window.outerWidth;");
        Long innerHeight = (Long)driver.executeScript("return window.innerHeight;");
        Long innerWidth = (Long)driver.executeScript("return window.innerWidth;");
        outerHeight =  outerHeight == null ? (long) 0 : outerHeight;
         outerWidth =  outerWidth == null ? (long) 0 : outerWidth;
        innerHeight =  innerHeight == null ? (long) 0 : innerHeight;
         innerWidth =  innerWidth == null ? (long) 0 : innerWidth;
         
        Integer offsetX = windowPosition.getX() + (outerWidth.intValue() - innerWidth.intValue()) - 10;
        Integer offsetY = windowPosition.getY() + (outerHeight.intValue() - innerHeight.intValue()) - 28;
        System.out.println((offsetX + x )+","+ (offsetY + y));
        robot.mouseMove(offsetX + x, offsetY + y); 
    }

    /**
         *  模擬滑鼠點擊
         * @param x - X軸
         * @param y - Y軸
         */
    public void click(Integer x, Integer y){
        this.move(x, y);
        robot.mousePress(InputEvent.BUTTON1_MASK);  //模拟鼠标按下左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);    //模拟鼠标松开左键
    }

    /**
         * 關閉驅動
         */
    public void close(){
        if(driver != null){
            driver.quit();
        }
    }

    /**
         * 設定代理
         * @param hostname
         * @param port
         */
    protected void setProxy(MutableCapabilities options, String hostname, int port){
        Proxy proxy = new Proxy();
        String[] noProxy = new String[]{
                "www.tcgpayment.com",
                "www.google.com",
                "translate.google.com",
                "jira.tc-gaming.co",
                "bsp.top1bsp.com",
                "windows10.microdone.cn",
                "https://ops.jeepayment.com",
                "https://e.bank.ecitic.com",
                "translate.google.com",
                "telegram",
                "www.google.com.hk",
                "http://10.8.95.22:7001"
        };

        if(options instanceof InternetExplorerOptions){
            InternetExplorerOptions o = (InternetExplorerOptions) options;
            o.ignoreZoomSettings();
            o.requireWindowFocus();
            proxy.setSslProxy(hostname + ":" + port);
            proxy.setNoProxy(String.join(";", noProxy));
            o.setProxy(proxy);
        }
    }
    
  
}

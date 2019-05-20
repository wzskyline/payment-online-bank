package com.wlft.payment;

import com.wlft.payment.common.Config;
import com.wlft.payment.common.HttpUtils;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.PcUtils;
import com.wlft.payment.common.SXApi;
import com.wlft.payment.exception.BankException;
import com.wlft.payment.swing.TaskSelectPanel;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static com.wlft.payment.common.FormatUtils.DATE_TIME_FORMATTER2;
import static java.lang.Thread.sleep;
public class PG1 {

    public static void test(String[] args) {
        String a = "" +
                "$(document).ready(function(){" +
                "   $('body').append('<div id=\"payment-online-bank-button\" style=\"position:fixed; " +
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
                "color:#FFFFFF; \">Stop</div>');" +
                "});";
        System.out.println(a);
    }
    public static void bobiTest(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        String  out = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(runtime.exec("sc query winio").getInputStream()));
            String line=null;
            StringBuffer b=new StringBuffer();
            while ((line=br.readLine())!=null) {
                b.append(line+"\n");
            }
            out = b.toString();
            out = out.substring(out.indexOf("STATE              :")+21,out.indexOf("STATE              :")+22);
            if(Integer.parseInt(out) == 4){
                //initFrame();
            } else {
                Robot r=new Robot();
                String[] winio = {"WinIo/WinIoInstall.exe"};
                Runtime.getRuntime().exec(winio);
                sleep(5000);
                r.keyPress(KeyEvent.VK_UP );
                r.keyPress(KeyEvent.VK_UP );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void xxxx(String[] args) throws Exception {


        String str = "{additionalInfo1: \"LRM\" ,additionalInfo2: \"B2B Production\" ,asignee: \"\" ,createdAt: \"2019-03-19 12:46:17\" ,createdBy: 1025574 ,customer: \"2000cai@welove\" ,fcAsignee: \"\" ,field1: \"2000cai@welove\" ,field2: \"Processed By wpm_memo\" ,field3: \"中国建设银行\" ,field4: \"5.ABC.B006\" ,field5: \"100\" ,field7: \"100\" ,field8: \"0.5\" ,id: 13325157 ,merchantId: \"1009\" ,merchantName: \"TCG 2KC\" ,param1: \"100\" ,param3: \"100\" ,pendingTime: 2903 ,priority: 70 ,ref: \"4330539\" ,remarks: \"Processed By wpm_memo\" ,requestor: \"benson\" ,state: {id: -510} ,task: \"WP-A\" ,time: \"2019-03-19 16:50:21\" ,toAcct: \"0.5\" ,updatedAt: \"2019-03-19 16:50:21\" ,updatedBy: 1094202 ,version: 3 ,workflow: \"Partial Withdraw\"}";

        JSONObject  item =  new JSONObject(str);
        System.out.println(item);

        String value = "（手续费：7.50元）";//"1223.00&nbsp;元&nbsp;&nbsp;";
                value = value.substring(5);
        value = value.replaceAll("元）","");
        System.out.println(value);
        value = "1223.00&nbsp;元&nbsp;&nbsp;";
        System.out.println(value.substring(0,value.indexOf(".")+2));

    }
    public static void main(String[] args) throws Exception {
    	sleep(5000);
    	//6217993610018374594
    	Robot robot = null;
        try {
        	 robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }     
        int x = 317;
        int y = 172; 
    	robot.mouseMove(x,y);
    	robot.mousePress(InputEvent.BUTTON1_MASK);  //模拟鼠标按下左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);    //模拟鼠标松开左键
    	sleep(2000);
    	x -= 20;
    	y += 25;
    	robot.mouseMove(x,y);
    	robot.mousePress(InputEvent.BUTTON1_MASK);  //模拟鼠标按下左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);    //模拟鼠标松开左键
    	sleep(2000);
    	x += 20;
    	y += 25;
    	robot.mouseMove(x,y);
    	robot.mousePress(InputEvent.BUTTON1_MASK);  //模拟鼠标按下左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);    //模拟鼠标松开左键
    	sleep(2000);
  	  }
    public static void main11(String[] args) throws Exception {
    	boolean x= false;
    	//初始化robot
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        sleep(3000);
        robot.mouseMove(870, 129);
        //模拟鼠标按下左键
        robot.mousePress(InputEvent.BUTTON1_MASK);
       //模拟鼠标松开左键 
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    	  PcUtils.encodeImgageToBase64("src/main/resources/T.png");
    	
    	 BigDecimal banlance = new BigDecimal("11.00");
      	 BigDecimal amount = new BigDecimal("11.00");
      	 if(banlance.equals(  amount)) {
      		System.out.println("//  equals "+banlance);
         } else {
        	 banlance = banlance.subtract(amount);
        	 System.out.println("//  新余额"+banlance);
         }
    	///
    	 
    	
    	
     // TEST EXE
    	 String driverPath = System.getProperty("user.home") + File.separator + "IEDriverServer.exe";
         System.setProperty("webdriver.ie.driver", driverPath);

         InternetExplorerOptions options = new InternetExplorerOptions();
         options.ignoreZoomSettings();
         options.requireWindowFocus();
          
         RemoteWebDriver driver = new InternetExplorerDriver(options);
         driver.manage().timeouts().pageLoadTimeout(91,TimeUnit.SECONDS);
         driver.get("https://perbank.abchina.com/EbankSite/startup.do");
         press("abcd1234".split(""), 1000, 200); 
         
         JFrame window = new JFrame("测试");
         window.setVisible(true);
         Object[] op = {"Yes,是的", "No,取消¦"};
         JOptionPane.showOptionDialog(null, " again?", "询问 ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, op,null);
 		
 		
         press("abcd1234".split(""), 1000, 200); 




    }
    public static void testNameValuePair (String[] args) throws Exception {
        NameValuePair[] data = {

                new NameValuePair("messageSending", "false"),
        };
        System.out.println("//////////////////////API updateTask2() //////////////////");
        System.out.println(Arrays.toString(data));
    }
    public static void main2(String[] args) throws Exception {
        Robot robot = new Robot();
        String exe;
        exe = "IEDriverServer.exe";
        exe = "chromedriver.exe";
        exe = "chromedriver2.36.exe";
        exe = "IEDriverServer.exe";
        String driverPath = System.getProperty("user.home") + File.separator + exe;
        System.setProperty("webdriver.ie.driver", driverPath);
        // declaration and instantiation of objects/variables

        InternetExplorerOptions options = new InternetExplorerOptions();

        options.ignoreZoomSettings();

        options.requireWindowFocus();
        Proxy proxy = new Proxy();
        proxy.setSslProxy("10.221.3.18:8800");
        //options.setProxy(proxy);
        RemoteWebDriver driver = new InternetExplorerDriver(options);
        //comment the above 2 lines and uncomment below 2 lines to use Chrome
        //System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
        //WebDriver driver = new ChromeDriver();

        //String baseUrl = "https://perbank.abchina.com/EbankSite/startup.do";
        String baseUrl = "https://perbank.abchina.com/EbankSite/startup.do";
        String expectedTitle = "Welcome: Mercury Tours";
        String actualTitle = "";

        // launch Fire fox and direct it to the Base URL


        driver.get(baseUrl);

        //  設定螢幕大小
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1125, 730));

        //  將畫面至於右上角
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        org.openqa.selenium.Dimension browserSize = driver.manage().window().getSize();
        driver.manage().window().setPosition(new org.openqa.selenium.Point(0, 0));
        // get the actual value of the title
        Thread.sleep(3000);
        //org.openqa.selenium.Point point = driver.findElement(By.id("kBaoLogin")).getLocation();
        //System.out.println("kBaoLogin:" + point);
        Dimension windowDimension = driver.manage().window().getSize();

        System.out.println("window:" + windowDimension);
        System.out.println(123);
        org.openqa.selenium.Point windowPosition = driver.manage().window().getPosition();

        Long outerHeight = (Long)driver.executeScript("return window.outerHeight;");
        Long outerWidth = (Long)driver.executeScript("return window.outerWidth;");
        Long innerHeight = (Long)driver.executeScript("return window.innerHeight;");
        Long innerWidth = (Long)driver.executeScript("return window.innerWidth;");
        Integer offsetX = windowPosition.getX() + (outerWidth.intValue() - innerWidth.intValue()) - 10;
        Integer offsetY = windowPosition.getY() + (outerHeight.intValue() - innerHeight.intValue()) - 28;
        Integer x = 0; Integer y = 0;

        robot.mouseMove(offsetX + x, offsetY + y);
        robot.mousePress(InputEvent.BUTTON1_MASK);  //模拟鼠标按下左键
        robot.mouseRelease(InputEvent.BUTTON1_MASK);    //模拟鼠标松开左键

        org.openqa.selenium.Point searchForm = driver.findElement(By.id("PowerEnterDiv_powerpass_2")).getLocation();
        System.out.println(searchForm.getX() + "," + searchForm.getY());

        if (actualTitle.contentEquals(expectedTitle)){
            System.out.println("Test Passed!");
        } else {
            System.out.println("Test Failed");
        }

        //close Fire fox
        File Screen = null;
        Screen = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(Screen, new File(Config.get("output.path") + "\\123.png"));

    }

}
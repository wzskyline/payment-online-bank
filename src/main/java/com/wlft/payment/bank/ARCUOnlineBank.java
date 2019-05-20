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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static com.wlft.payment.common.FormatUtils.DATE_TIME_FORMATTER2;
import static io.github.seleniumquery.SeleniumQuery.$;
import static java.lang.Thread.sleep;

import java.awt.image.BufferedImage;
import java.io.File;

public class ARCUOnlineBank extends OnlineBank {
 

    private static final Map<String, String> BANK_MAPPING = new HashMap<String, String>();

    static {
    	 
        BANK_MAPPING.put("工商银行","102100099996");
        BANK_MAPPING.put("中国农业银行","103100000026");
        BANK_MAPPING.put("中国银行","104100000004");
        BANK_MAPPING.put("中国建设银行","105100000017");
        BANK_MAPPING.put("交通银行","301290000007");
        BANK_MAPPING.put("中信银行","302100011000");
        BANK_MAPPING.put("中国光大银行","303100000006");
        BANK_MAPPING.put("华夏银行","304100040000");
        BANK_MAPPING.put("中国民生银行","305100000013");
        BANK_MAPPING.put("广发银行","306581000003");
        BANK_MAPPING.put("深圳发展银行","307584007998");
        BANK_MAPPING.put("招商银行","308584000013");
        BANK_MAPPING.put("兴业银行总行","309391000011");
        BANK_MAPPING.put("上海浦东发展银行","310290000013");
        BANK_MAPPING.put("北京银行","313100000013");
        BANK_MAPPING.put("天津银行股份有限公司","313110000017");
        BANK_MAPPING.put("河北银行股份有限公司","313121006888");
        BANK_MAPPING.put("邯郸市商业银行股份有限公司","313127000013");
        BANK_MAPPING.put("邢台银行股份有限公司","313131000016");
        BANK_MAPPING.put("张家口市商业银行股份有限公司","313138000019");
        BANK_MAPPING.put("承德银行股份有限公司","313141052422");
        BANK_MAPPING.put("沧州银行","313143005157");
        BANK_MAPPING.put("晋商银行股份有限公司","313161000017");
        BANK_MAPPING.put("晋城市商业银行","313168000003");
        BANK_MAPPING.put("内蒙古银行","313191000011");
        BANK_MAPPING.put("包商银行股份有限公司","313192000013");
        BANK_MAPPING.put("鄂尔多斯银行股份有限公司","313205057830");
        BANK_MAPPING.put("大连银行","313222080002");
        BANK_MAPPING.put("鞍山市商业银行","313223007007");
        BANK_MAPPING.put("锦州银行","313227000012");
        BANK_MAPPING.put("葫芦岛银行股份有限公司","313227600018");
        BANK_MAPPING.put("营口银行股份有限公司资金清算中心","313228000276");
        BANK_MAPPING.put("阜新银行结算中心","313229000008");
        BANK_MAPPING.put("吉林银行","313241066661");
        BANK_MAPPING.put("哈尔滨银行结算中心","313261000018");
        BANK_MAPPING.put("龙江银行股份有限公司","313261099913");
        BANK_MAPPING.put("上海银行","313290000017");
        BANK_MAPPING.put("南京银行股份有限公司","313301008887");
        BANK_MAPPING.put("江苏银行股份有限公司","313301099999");
        BANK_MAPPING.put("杭州银行股份有限公司","313331000014");
        BANK_MAPPING.put("宁波银行股份有限公司","313332082914");
        BANK_MAPPING.put("温州银行股份有限公司","313333007331");
        BANK_MAPPING.put("湖州银行股份有限公司","313336071575");
        BANK_MAPPING.put("绍兴银行股份有限公司营业部","313337009004");
        BANK_MAPPING.put("浙江稠州商业银行","313338707013");
        BANK_MAPPING.put("台州银行股份有限公司","313345001665");
        BANK_MAPPING.put("浙江泰隆商业银行","313345010019");
        BANK_MAPPING.put("浙江民泰商业银行","313345400010");
        BANK_MAPPING.put("福建海峡银行股份有限公司","313391080007");
        BANK_MAPPING.put("厦门银行股份有限公司","313393080005");
        BANK_MAPPING.put("南昌银行","313421087506");
        BANK_MAPPING.put("赣州银行股份有限公司","313428076517");
        BANK_MAPPING.put("上饶银行","313433076801");
        BANK_MAPPING.put("青岛银行","313452060150");
        BANK_MAPPING.put("齐商银行","313453001017");
        BANK_MAPPING.put("东营市商业银行","313455000018");
        BANK_MAPPING.put("烟台银行股份有限公司","313456000108");
        BANK_MAPPING.put("潍坊银行","313458000013");
        BANK_MAPPING.put("济宁银行股份有限公司","313461000012");
        BANK_MAPPING.put("泰安市商业银行","313463000993");
        BANK_MAPPING.put("莱商银行","313463400019");
        BANK_MAPPING.put("威海市商业银行","313465000010");
        BANK_MAPPING.put("德州银行股份有限公司","313468000015");
        BANK_MAPPING.put("临商银行股份有限公司","313473070018");
        BANK_MAPPING.put("日照银行股份有限公司","313473200011");
        BANK_MAPPING.put("郑州银行","313491000232");
        BANK_MAPPING.put("开封市商业银行","313492070005");
        BANK_MAPPING.put("洛阳银行","313493080539");
        BANK_MAPPING.put("漯河市商业银行","313504000010");
        BANK_MAPPING.put("商丘市商业银行股份有限公司","313506082510");
        BANK_MAPPING.put("南阳市商业银行","313513080408");
        BANK_MAPPING.put("汉口银行资金清算中心","313521000011");
        BANK_MAPPING.put("长沙银行股份有限公司","313551088886");
        BANK_MAPPING.put("广州银行","313581003284");
        BANK_MAPPING.put("平安银行","313584099990");
        BANK_MAPPING.put("东莞银行股份有限公司","313602088017");
        BANK_MAPPING.put("广西北部湾银行","313611001018");
        BANK_MAPPING.put("柳州银行股份有限公司清算中心","313614000012");
        BANK_MAPPING.put("重庆银行","313653000013");
        BANK_MAPPING.put("攀枝花市商业银行","313656000019");
        BANK_MAPPING.put("德阳银行股份有限公司","313658000014");
        BANK_MAPPING.put("绵阳市商业银行","313659000016");
        BANK_MAPPING.put("贵阳市商业银行","313701098010");
        BANK_MAPPING.put("富滇银行股份有限公司运营管理部","313731010015");
        BANK_MAPPING.put("兰州银行股份有限公司","313821001016");
        BANK_MAPPING.put("青海银行股份有限公司营业部","313851000018");
        BANK_MAPPING.put("宁夏银行总行清算中心","313871000007");
        BANK_MAPPING.put("乌鲁木齐市商业银行清算中心","313881000002");
        BANK_MAPPING.put("昆仑银行股份有限公司","313882000012");
        BANK_MAPPING.put("苏州银行股份有限公司","314305006665");
        BANK_MAPPING.put("昆山农村商业银行","314305206650");
        BANK_MAPPING.put("吴江农村商业银行清算中心","314305400015");
        BANK_MAPPING.put("江苏常熟农村商业银行股份有限公司清算中心","314305506621");
        BANK_MAPPING.put("张家港农村商业银行","314305670002");
        BANK_MAPPING.put("广州农村商业银行股份有限公司","314581000011");
        BANK_MAPPING.put("佛山顺德农村商业银行股份有限公司","314588000016");
        BANK_MAPPING.put("重庆农村商业银行股份有限公司","314653000011");
        BANK_MAPPING.put("恒丰银行","315456000105");
        BANK_MAPPING.put("浙商银行","316331000018");
        BANK_MAPPING.put("天津农村合作银行","317110010019");
        BANK_MAPPING.put("渤海银行股份有限公司","318110000014");
        BANK_MAPPING.put("徽商银行股份有限公司","319361000013");
        BANK_MAPPING.put("北京农村商业银行股份有限公司","402100000018");
        BANK_MAPPING.put("上海农村商业银行","402290000011");
        BANK_MAPPING.put("江苏省农村信用社联合社信息结算中心","402301099998");
        BANK_MAPPING.put("宁波鄞州农村合作银行","402332010004");
        BANK_MAPPING.put("福建省农村信用社联合社","402391000068");
        BANK_MAPPING.put("湖北省农村信用社联合社结算中心","402521000032");
        BANK_MAPPING.put("深圳农村商业银行","402584009991");
        BANK_MAPPING.put("东莞农村商业银行股份有限公司","402602000018");
        BANK_MAPPING.put("广西壮族自治区农村信用社联合社","402611099974");
        BANK_MAPPING.put("海南省农村信用社联合社资金清算中心","402641000014");
        BANK_MAPPING.put("云南省农村信用社联合社","402731057238");
        BANK_MAPPING.put("宁夏黄河农村商业银行股份有限公司","402871099996");
        BANK_MAPPING.put("邮政储蓄","403100000004");
        BANK_MAPPING.put("外换银行（中国）有限公司","591110000016");
        BANK_MAPPING.put("新韩银行（中国）有限公司","595100000007");
        BANK_MAPPING.put("企业银行（中国）有限公司","596110000013");
        BANK_MAPPING.put("韩亚银行（中国）有限公司","597100000014"); 
    }
    public ARCUOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("ARCU", id, accountCode, hostname, port);
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
        
        
        // 用户名 
        driver.executeScript(" document.getElementById('UserId').value = '"+username+"';  ");
        // 密码
        press(new String[]{"Tab"}, 100, 50);  
        
       
        for(int i = 0;i < password.length(); i++) {  
        	press(password.substring(i,i+1).split(""), 100 + (int)(Math.random() * 100), 60);
        	
        } 
        // 截图获取验证码
        int x=0,y=0,w=0,h=0;

        WebElement _tokenImg = driver.findElement(By.id("_tokenImg"));
        x += _tokenImg.getLocation().x -90;
        y += _tokenImg.getLocation().y+20;  
        w = _tokenImg.getSize().width-10;
        h = _tokenImg.getSize().height-5;
        File screen = PcUtils.captureScreen("ARCU","login.png");
        String codePath = Config.get("output.path")+"\\ARCU\\code.png";
        try {
            BufferedImage screenImg = ImageIO.read(screen);
            BufferedImage elementImg = screenImg.getSubimage(x,y,w,h);
            ImageIO.write(elementImg, "png", screen);
            FileUtils.copyFile(screen,new File(codePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
         
        String codeBase64 = PcUtils.encodeImgageToBase64(codePath);
        String codeId = SXApi.sendBase64To2captcha(codeBase64);
        String code = SXApi.getCodeFrom2captcha(codeId);
        
      
        
        // 输入验证码
        driver.executeScript("var list = document.getElementsByTagName('input'); for(i=0;i<list.length;i++){ if(list[i].name == '_vTokenName'){ list[i].value =11; } } ");
        // 登陆
        driver.executeScript(" document.getElementById('submitButton').click()  ");
        
         
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
        String bal = driver.executeScript("return $('body').contents().find('#mainframe').contents().find('#SelfBankAcListDiv>table>tbody>tr>td:eq(3)').html()" ).toString(); 
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
    	
        org.openqa.selenium.Point position;

        //  跳轉至轉帳頁面
 	    position = driver.findElement(By.id("welcomediv")).getLocation();
 	     
        int x = position.getX();
        int y = position.getY(); 
      
        this.click(x+85, y);
        sleep(200); 
        this.click(x+40, y+25);
        sleep(5000); 
    
        String bal = driver.executeScript(" return  $('body').contents().find('#mainframe').contents().find('.tdValue:eq(3)').text().trim()" ).toString(); 
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
        //  初始化pending
        this.initPending();

        this.setMessage("Start transaction");
        try {  
        //  跳轉至轉帳頁面
    	   position = driver.findElement(By.id("welcomediv")).getLocation();
    	     
    	   int x = position.getX();
    	   int y = position.getY(); 
         
    	   this.click(x+85, y);
    	   sleep(1000);
        if(type.equals("same")) {
        	this.click(x+40, y+25);
        } else {
        	this.click(x+110, y+25);
        }
        sleep(5000); 
       
        
        
        
        
        
        //  加入遮照
        this.setMessage("Mask fields");
         
        
        // driver.switchTo().frame("mainframe"); 内层没有jquery 故不跳转 
         
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('#PayeeAcNoAlias').after('<div class=\"payment-masker\" style=\"position:absolute; left:500px;  width:45px !important; height:20px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('#PayeeAcNameAlias').after('<div class=\"payment-masker\" style=\"position:absolute; left:500px;  width:45px !important; height:20px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        //  付款金额（元）  
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('.tdValue:eq(4)>input').val('" + amount + "').focus()");
        // 收款人账号 
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('.tdValue:eq(5)>input:eq(0)').val('"+accountNumber+"').focus()");
        
        
       // 加入移除遮照的事件
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('.payment-masker').on('click', function(){var ans = prompt('Please enter password', ''); if(ans == 5201314){$('.payment-masker').remove();}})");
        if(type.equals("other")) {
        	// 确认收款账号
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('#ConPayeeAcNoAlias').after('<div class=\"payment-masker\" style=\"position:absolute; left:470px; width:45px; height:20px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('#ConPayeeAcNoAlias').val('"+accountNumber+"').focus()");
        	// 收款人名称 
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('#PayeeAcNameAlias').val('"+accountName+"').focus()");
        	// 收款人银行 
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('#IBPSPayeeBankIdId').find(\"option[value='"+ BANK_MAPPING.get(bankName) +"']\").attr('selected',true);");
        } else {
        // 收款人名称 	
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('.tdValue:eq(6)>input:eq(0)').val('"+accountName+"').focus().click()");
        	this.click(x+315, y+360);
        //  付款用途方式  手动录入 
        	driver.executeScript("$('body').contents().find('#mainframe').contents().find('.tdValue:eq(7)>input:eq(1)').click()");
        } 
        
        this.setMessage("Start click");
       
         
        this.setMessage("Start 提交");
        driver.executeScript("$('body').contents().find('#mainframe').contents().find('#button').click();");
        //取得 费用 charge  
        Object charge = null;  
        int waitTime = 10; 
        try {
        	while(waitTime>0) {
        		sleep(1000);
        		waitTime -- ;
        		if(waitTime == 0) {
        			break;
        		}
        		if(charge == null) {
        			 charge = driver.executeScript("return $('body').contents().find('#mainframe').contents().find('.tdTitle:eq(3)').text().trim() ");    	
        	    } else {
        	       	 break;
        	    }
        		
        	}	
        	
        }catch(Throwable ee) {
        	
        } 
        if(charge.toString().indexOf("手续费")<0) {
	    	  taskLog =  fillHandle(id,taskLog,"value_error");
	          throw new BankException("value_page_error", taskLog);
	    }
	   
       charge = driver.executeScript("return $('body').contents().find('#mainframe').contents().find('.tdValue:eq(3)').text().trim() ");
       
       if(charge == null ){
            
    	   taskLog.setCharge("0.0");
        	   
       } else {  
            
    	   String tmp = charge.toString(); //  String tmp = "0.00"
            
    	   if(tmp.length()>1) {
        		  Matcher matcher = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(tmp);
                  if (matcher.find()) {
                     tmp = matcher.group(1);
                     taskLog.setCharge(tmp);
                  }    
              } else {
            	  taskLog.setCharge("0.0");
              }
         }
          
       //  交易密码  
       if(type.equals("other")) {
    	   this.click(x+315, y+430);
       } else {
    	   this.click(x+315, y+380);
       }	   
	   if( PcUtils.capsLock()){
       	    press(new String[]{"CapsLock"}, 200, 50);
       } 
       press(queryPassword.split(""), 1000, 50);
       this.setMessage("Start 确定");
       driver.executeScript("$('body').contents().find('#mainframe').contents().find('#button').click();");
            
       sleep(2000);    
       if( PcUtils.capsLock()){
       	   press(new String[]{"CapsLock"}, 200, 50);
        } 
          
       press(usbPassword.split(""), 1000, 50);  
       press(new String[]{"Enter"}, 1000, 50);
       //呼叫: 开启 声音提示操作人员按下 OK
       new Thread(new Runnable() {
             public void run() {
                 PcUtils.open("a.wav");
             }
       }).start();
         
          
       // 循环等待   30秒内 操作人员按下
       String successInfo = "";
        waitTime = 30;
          
          while(true) {
          	try {
          		sleep(1000);
          		waitTime --;
          		if(waitTime==0) {
          			break;
          		}
          		driver.switchTo().window(windowHandle);  
          		successInfo = driver.executeScript("return  $('body').contents().find('#mainframe').contents().find('#detailInfo>tbody>tr:eq(1)>td:eq(2)').html()").toString();
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
         
           
        } catch(Throwable x) {
        	    		
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
   		  if(accountNumber.substring(0,6).equals("623220")
   			||accountNumber.substring(0,6).equals("621550")) {
   			  
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
 
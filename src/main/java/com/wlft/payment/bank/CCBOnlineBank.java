package com.wlft.payment.bank;

import com.wlft.payment.common.TaskLog;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor; 
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.seimicrawler.xpath.JXDocument;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;

import static com.github.supermoonie.winio.VirtualKeyBoard.press;
import static io.github.seleniumquery.SeleniumQuery.$;
import static java.lang.Thread.sleep;

public class CCBOnlineBank extends OnlineBank {
 

    public CCBOnlineBank(Integer id, String accountCode, String hostname, Integer port){
        super("CCB", id, accountCode, hostname, port);
    }

    @Override
    public void login(String username, String password, String queryPassword, String usbPassword) throws Exception {
        //  設定登錄帳號、密碼、交易密碼、U盾密碼
        this.setLoginInfo(username, password, queryPassword, usbPassword);

        //  初始化訊息框
        initMessageBox();

        //  focus window
        clearPage(driver);
        setMessage("Open 用户名登陆");
        this.setMessage("Start 用户名登陆");

        driver.switchTo().frame("ICBC_login_frame");
        new WebDriverWait(driver, 30).until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return $('#logonCardNum').val()").equals(""));
        String js = "$('#logonCardNum').val('errtyyyr').focus().click()";
        driver.executeScript(js );
        press(new String[]{"Tab"}, 100, 50);
        setMessage("Type password");
        press("aa412452".split(""), 100, 50);

        JFrame window = new JFrame("测试");
         window.setVisible(true);
        Object[] options = {"Yes,是的", "No,取消¦"};
        JOptionPane.showOptionDialog(null, " again?", "询问 ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,null);
    }

    @Override
    public void logout() throws Exception {
        driver.executeScript(" $('#logout_a').click()");
        driver.switchTo().alert().accept();
        this.close();
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
    public BigDecimal getPageBalance( )  throws  Exception{
        BigDecimal banlance = new BigDecimal("0.0");
        sleep(2000);
        return banlance;
    }

    @Override
    public TaskLog transfer(BigDecimal id, String bankName, String accountNumber, String accountName, BigDecimal amount,String memberBankProvince,String memberBankCity,String memberBankBranch ) throws Exception {
        this.setMessage("Start transaction");
        driver.switchTo().frame("perbank-content-frame");
        driver.switchTo().frame("content-frame");
        driver.executeScript("$('#recNameShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:25px; top:10px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#recAcctShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:0px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");
        driver.executeScript("$('#bankListShow').after('<div class=\"payment-masker\" style=\"position:absolute; left:30px; top:-14px; width:100px; height:22px; display: inline-block; z-index: 100; background:#000; color:#FFF; padding:5px; line-height:22px; cursor:pointer;\">Click to remove masker</div>');");


        driver.executeScript("$('#recNameShow').val('"+ accountName + "')");
        driver.executeScript("$('#recAcctShow').val('"+ accountNumber + "')");
        driver.executeScript("$('#bankListShow').val('"+ bankName + "')");
        driver.executeScript("$('#remitAmtInput').val('"+ amount + "')");
        //  設定轉帳參數
        this.setMessage("Set transaction fields");

        JFrame window = new JFrame("测试");
        window.setVisible(true);
        Object[] options = {"Yes,是的", "No,取消¦"};
        JOptionPane.showOptionDialog(null, " again?", "询问 ", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,null);
        //  下一步
        driver.executeScript("$('#tijiao').click()");

       return new TaskLog();
    }

    @Override
    public void queryTransaction() throws Exception {

        //  跳回最外層，以保證之後的操作是正確的
        driver.switchTo().window(windowHandle);
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
}

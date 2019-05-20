package com.wlft.payment.swing;

import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.SXApi;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class LoginPanel extends SubPanel {

    private static final Logger logger = Logger.getLogger(LoginPanel.class);

    public static String nickname = "login.panel";

    //  帳號
    private JLabel usernameLabel;
    private JTextField usernameText;

    //  密碼
    private JLabel passwordLabel;
    private JPasswordField passwordText;

    //  登入按鈕
    private JButton okButton;
    //  取消按鈕
    private JButton cancelButton;

    public LoginPanel(Operator operator, PaymentFrame frame, int width, int height){
        super(operator, frame, width, height);
        String username = Config.get("username") != null ? Config.get("username") : "";
        String password = Config.get("password") != null ? Config.get("password") : "";
        //  Declare
        int offsetX = 120;  int offsetY = 50;

        this.setLayout(null);

        //  帳號
        this.usernameLabel = new JLabel("Username");
        this.usernameLabel.setBounds(offsetX, offsetY,80,25);
        this.add(this.usernameLabel);

        this.usernameText = new JTextField(username, 15);
        this.usernameText.setBounds(offsetX + 80, offsetY, 150, 25);
        this.add(this.usernameText);

        //  密碼
        this.passwordLabel = new JLabel("Password");
        this.passwordLabel.setBounds(offsetX,offsetY + 40,80,25);
        this.add(this.passwordLabel);
// Config.get("password") != null ? Config.get("password"):
        this.passwordText = new JPasswordField(password, 15);
        this.passwordText.setBounds(offsetX + 80, offsetY + 40, 150, 25);
        this.add(this.passwordText);

        //  登入
        this.okButton = new JButton("Login");
        this.okButton.setBounds(offsetX + 25, offsetY + 40 + 40, 80, 25);
        this.add(this.okButton);

        //  取消
        this.cancelButton = new JButton("Cancel");
        this.cancelButton.setBounds(offsetX + 25 + 20 + 80, offsetY + 40 + 40, 80, 25);
        this.add(this.cancelButton);

        //  增加監聽事件
        this.setListener();
    }

    public void setListener(){
        final LoginPanel panel = this;
        //  Login
        this.okButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //  操作人員登入
                            operator.login( panel.usernameText.getText(),
                                    new String(panel.passwordText.getPassword()));
                            //  登錄成功
                            parent.setMessage("Login success.");
                            //  Delay
                            Thread.sleep(200);

                            //  跳轉頁面至選卡頁
                            parent.show(BankSelectPanel.nickname);
                            parent.getBankSelectPanel().init();
                        } catch (Exception e){
                            logger.error(e);
                            parent.setErrorMessage("Login fail. Cause:" + e.getMessage(), e.getCause());
                        }
                    }
                }).start();
            }
        });

        //  Cancel
        this.cancelButton.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                  parent.dispatchEvent(new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
            }
        });
    }
}

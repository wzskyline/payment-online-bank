package com.wlft.payment.swing;

import com.wlft.payment.common.Operator;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.wlft.payment.common.PcUtils;
import org.apache.log4j.Logger;

public class PaymentFrame extends JFrame implements WindowListener {
    private static String version = "version = 0.1.0.0515";
    private static final Logger logger = Logger.getLogger(PaymentFrame.class);
    //  會記錄起來的日誌記錄器
    private static final Logger fileLogger = Logger.getLogger("file");
    //  file的日誌
    private static final String INFO = "info";
    //  file的錯誤日誌
    private static final String ERROR = "error";
    //  標題
    private static String TITLE = "Online Bank Helper(0515)";
    //  寬
    public static final int WIDTH = 500;
    //  高
    public static final int HEIGHT = 550;
    //  用戶資訊
    private Operator operator;
    //  主要的panel
    private JPanel mainPanel;
    //  訊息
    private JTextPane messagePane;
    //  登入頁
    private LoginPanel loginPanel;
    // 銀行卡選擇頁
    private BankSelectPanel bankSelectPanel;
    // 任務領取頁
    private TaskSelectPanel taskSelectPanel;
    //  日志页面
    private TaskLogPanel taskLogPanel;

    public PaymentFrame() {

        //  設定標題
        super(TITLE);

        //  設定預設關閉的方式
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //  設定寬高
        setSize(new Dimension(WIDTH, HEIGHT));
        //  不可變形
        setResizable(false);
        //  永遠置頂
        setAlwaysOnTop(true);
        //  顯示在最左上角
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - WIDTH, 0);
        //  綁定監聽器
        this.addWindowListener(this);

        //  運營人員
        this.operator = new Operator();

        //  mainPanel
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBounds(0, 0, WIDTH, HEIGHT);
        // 登入頁
        this.loginPanel = new LoginPanel(this.operator, this, WIDTH, HEIGHT);

        mainPanel.add(this.loginPanel, LoginPanel.nickname);
        // 銀行卡選擇頁
        this.bankSelectPanel = new BankSelectPanel(this.operator, this, WIDTH, HEIGHT);
        mainPanel.add(this.bankSelectPanel,BankSelectPanel.nickname);

        // 任務領取頁
        this.taskSelectPanel = new TaskSelectPanel(this.operator, this, WIDTH, HEIGHT);
        mainPanel.add(this.taskSelectPanel, TaskSelectPanel.nickname);
        // log
        this.taskLogPanel = new TaskLogPanel(this.operator, this, WIDTH, HEIGHT);
        mainPanel.add(this.taskLogPanel, TaskLogPanel.nickname);

        add(mainPanel, BorderLayout.CENTER);

        //  messagePanel
        this.messagePane = new JTextPane();
        this.messagePane.setBorder(BorderFactory.createLoweredBevelBorder());
        this.messagePane.setBounds(0, 0, WIDTH, 70);
//        ((DefaultCaret)this.messagePane.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(this.messagePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel panel = new JPanel();
        scrollPane.setPreferredSize(new Dimension(WIDTH - 13, 70));
        panel.setPreferredSize(new Dimension(WIDTH, 70));
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
        this.setMessage(version);
    }

    public LoginPanel getLoginPanel(){
        return this.loginPanel;
    }

    public BankSelectPanel getBankSelectPanel(){
        return this.bankSelectPanel;
    }

    public TaskSelectPanel getTaskSelectPanel(){
        return this.taskSelectPanel;
    }

    public TaskLogPanel getTaskLogPanel(){
        return this.taskLogPanel;
    }

    public void show(String name){
        CardLayout c1 = (CardLayout) mainPanel.getLayout();
        c1.show(mainPanel, name);
    }

    public void windowOpened(WindowEvent e) {
        System.out.println("windowOpened");
    }

    public void windowClosing(WindowEvent e) {
        // 关闭线程
        getTaskSelectPanel().stopRunner();
        //  操作人員登出
        operator.logout();
        PcUtils.taskkill();
    }

    public void windowClosed(WindowEvent e) {
        System.out.println("windowClosed");
    }

    public void windowIconified(WindowEvent e) {
        System.out.println("windowIconified");
    }

    public void windowDeiconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {
        System.out.println("windowActivated");
    }

    public void windowDeactivated(WindowEvent e) {
        System.out.println("windowDeactivated");
    }

    /**
     *  顯示錯誤訊息
     * @param message - 訊息
     */
    public void setErrorMessage(String message){
        this.setMessage(ERROR, message, Color.RED, null);
    }


    /**
     *  顯示錯誤訊息
     * @param message - 訊息
     */
    public void setErrorMessage(String message, Throwable throwable){
        this.setMessage(ERROR, message, Color.RED, throwable);
    }

    /**
     * 顯示訊息
     * @param message - 訊息
     */
    public void setMessage(String message){
        this.setMessage(INFO, message, Color.BLACK, null);
    }

    /**
     * 顯示訊息
     * @param message - 訊息
     */
    private void setMessage(String level, String message, Color color, Throwable throwable){
        StyledDocument doc = this.messagePane.getStyledDocument();
        Style style = this.messagePane.addStyle("Color Style", null);
        StyleConstants.setForeground(style,color);
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
            if(this.isVisible()) {
                System.out.println("message panel visible");
                this.messagePane.setCaretPosition(doc.getLength());
            } else {
                System.out.println("message panel invisible");
            }
            if(level.equals(INFO)){
                fileLogger.info(message);
            }else if(level.equals(ERROR) && throwable != null){
                fileLogger.error(message);
            }else{
                fileLogger.error(message);
            }
        }catch (BadLocationException e) {
            System.out.println("bad location exception:" + e.toString());
            e.printStackTrace();
        }
    }
}
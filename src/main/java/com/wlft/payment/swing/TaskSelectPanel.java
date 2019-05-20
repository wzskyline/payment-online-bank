package com.wlft.payment.swing;

import com.wlft.payment.bank.OnlineBank;
import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.SXApi;
import com.wlft.payment.common.TaskLog;
import com.wlft.payment.exception.BankException;
import com.wlft.payment.exception.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import static com.wlft.payment.common.FormatUtils.*;

public class TaskSelectPanel extends SubPanel {

    private static final Logger logger = Logger.getLogger(TaskSelectPanel.class);

    public static String nickname = "task.select.panel";

    //  調用的銀行
    private OnlineBank bank;
    //  帳號
    private JLabel operatorLabel;
    //  log
    private JButton logButton;
    //  登出按鈕
    private JButton logoutButton;
    //  換卡按鈕
    private JButton changeButton;
    //  自动/手动按钮
    private JButton autoButton;
    //  重新加載按钮
    private JButton reloadButton;
    //  銀行卡代碼
    private JLabel accountLabel;
    //  銀行卡餘額--tcg
    private JLabel balanceLabel;
    
    //  銀行卡餘額--bank blank
    private JLabel bankBalanceLabel;
    //  刷新時間
    private JLabel refreshLabel;
    //  刷新時間
    private Integer refresh = Config.getInteger("task.refresh.time");
    //  是否刷新
    private Boolean refreshFlag = true;
    //  當前時間
    private JLabel nowLabel;
    //  數據
    private JTable table;
    //  是否自动
    private boolean autoFlag = false;
    //  是否執行中
    private boolean execFlag = false;

    public boolean ieFlag = false;
    // status label
    private JLabel statusLabel;
    private JLabel statusLabel1;
    private JLabel statusLabel2;
    private JLabel statusLabel3;
    private JLabel statusLabel4;
    
    public TaskSelectPanel(Operator operator, PaymentFrame frame, int width, int height){
        super(operator, frame, width, height);  
        //  Declare
        JPanel panel; JLabel label;
        JScrollPane scrollPane;

        panel = new JPanel();
        panel.setLayout(new GridLayout(4, 5, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(panel, BorderLayout.NORTH);
        //  Username
        panel.add(new JLabel("Username: ", SwingConstants.RIGHT));
        panel.add(this.operatorLabel = new JLabel(operator.getUsername()));
        this.operatorLabel.setForeground(Config.WORD_COLOR);
        //  Button "log"
        panel.add(this.logButton = new JButton("Log"));
        //  Button "Change"
        panel.add(this.changeButton = new JButton("Change"));
        //  Button "logout"
        panel.add(this.logoutButton = new JButton("Logout"));
        //  Account CODE
        panel.add(new JLabel("Account Code:", SwingConstants.RIGHT));
        panel.add(this.accountLabel = new JLabel(operator.getAccountCode()));
        this.accountLabel.setForeground(Config.WORD_COLOR);
        // BO Balance
        panel.add(new JLabel("BO Balance:", SwingConstants.RIGHT));
        panel.add(this.balanceLabel = new JLabel(AMOUNT_FORMATTER.format(operator.getBalance())));
        this.balanceLabel.setForeground(Config.WORD_COLOR);
        // start button
        panel.add(this.autoButton = new JButton("Auto"));
        
        //  Refresh Time
        panel.add(new JLabel("Refresh Time:", SwingConstants.RIGHT));
        panel.add(this.refreshLabel = new JLabel("0s"));
        this.refreshLabel.setForeground(Config.WORD_COLOR);
        //  Bank Balance
        panel.add(new JLabel("Bank Balance:", SwingConstants.RIGHT));
        panel.add(this.bankBalanceLabel = new JLabel(AMOUNT_FORMATTER.format(operator.getBalance())));
        this.bankBalanceLabel.setForeground(Config.WORD_COLOR);
        // panel.add(this.nowLabel = new JLabel(DATE_TIME_FORMATTER.format(new Date())));
        // this.nowLabel.setForeground(Config.WORD_COLOR);
        //  Button "logout"
        panel.add(this.reloadButton = new JButton("Reload"));
        
        panel.add(this.statusLabel = new JLabel(" Status:",SwingConstants.RIGHT));  
         
        panel.add(this.statusLabel1 = new JLabel(" "));this.statusLabel1.setOpaque(true);
        panel.add(this.statusLabel2 = new JLabel(" "));this.statusLabel2.setOpaque(true);
        panel.add(this.statusLabel3 = new JLabel(" "));this.statusLabel3.setOpaque(true);
        panel.add(this.statusLabel4 = new JLabel(" "));this.statusLabel4.setOpaque(true); 
        updateLable(Color.GRAY);
        
        //this.statusLabel.setBackground(Color.red);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        add(panel, BorderLayout.CENTER);
        //  Table
        this.initTable();
        scrollPane = new JScrollPane(this.table);
        panel.add(scrollPane);

        //  增加監聽事件
        this.setListener();
    }

    @Override
    public void setListener(){

        //  Change
        this.changeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //  停止自動抓取
                stopRunner();

                //  跳轉頁面至選卡頁
                parent.show(BankSelectPanel.nickname);
                parent.getBankSelectPanel().init();
            }
        });

        //  Log
        this.logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getTaskLogPanel().from = nickname;
                parent.show(TaskLogPanel.nickname);
                parent.getTaskLogPanel().init();
            }
        });

        //  autoFlag button
        this.autoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoFlag = !autoFlag;
                updateButton();
                
            }
        });

        //  reload button
        this.reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadData();
            }
        });

        //  Logout
        this.logoutButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshFlag = false;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //  停止自動抓取
                            stopRunner();
                            //  發送登錄請求
                            SXApi.logout(operator);
                            //  登出
                            operator.logout();

                            //  跳轉頁面至登入頁
                            parent.show(LoginPanel.nickname);
                            parent.setMessage("Logout Success!!");
                        } catch (HttpException he) {
                            logger.error(he, he.getCause());
                        }
                    }
                }).start();
            }
        });

        //  Click row
        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                //  1.keypress時
                //  2.選取有時候會是-1時
                //  3.自動時
                if(e.getValueIsAdjusting()){return;}
                else if(table.getSelectedRow() == -1){return;}
                else if(autoFlag){return;}

                //  取得被選取的row資料
                JSONObject row = ((TaskSelectPanel.TableModel)table.getModel()).getRow(table.getSelectedRow());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fetchTask(row);
                    }
                }).start();
              }
        });

    }


    public void init(OnlineBank bank){
        //  設定使用的銀行
        this.bank = bank;

        // 設定登入人員名稱
        this.operatorLabel.setText(operator.getUsername());
        //  設定銀行卡代碼
        this.accountLabel.setText(bank.getAccountCode());
        //  設定銀行卡餘額
        this.updateBalance();
        //  重新加載銀行卡數據
        this.reloadData();
        //  開始自動更新抓取任務
        this.startRunner();
        this.autoFlag = false;
    }
    /*
     * UPDATE COLOR OF LABLE
     * */
    public void updateLable(Color color) {
    	this.statusLabel1.setBackground(color);
        this.statusLabel2.setBackground(color);
        this.statusLabel3.setBackground(color);
        this.statusLabel4.setBackground(color);
    }
    /*
     * UPDATE updateButton
     * */
    public void updateButton() {
    	autoButton.setText(autoFlag ? "Manual" : "Auto");
    	autoButton.setBackground(autoFlag ? Color.BLUE :  Color.GREEN);
    }
    /**
         * 更新銀行卡餘額
         */
    public void updateBalance(){
        try {
            JSONObject detailData = SXApi.getCardDetail(operator, bank.getId());
 
            BigDecimal banlance = detailData != null ? detailData.getBigDecimal("balance") : new BigDecimal(0);
             
            this.balanceLabel.setText(AMOUNT_FORMATTER.format(banlance));

            this.bankBalanceLabel.setText(AMOUNT_FORMATTER.format(bank.getBalance()));
             
            
            if( ! this.balanceLabel.getText().equals(this.bankBalanceLabel.getText())){
            	this.balanceLabel.setForeground(Color.RED);
            	this.bankBalanceLabel.setForeground(Color.RED);
            }
                  
        }catch (HttpException e){
            parent.setErrorMessage("Fetch balance of " + bank.getAccountCode() + " fail. Cause:", e.getCause());
        }
    }

    public void startRunner(){
        TaskSelectPanel panel = this;

        //  設定刷新頻率
        refresh = Config.getInteger("task.refresh.time");
         
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(panel.refreshFlag){
                    
                    try {
                        refreshLabel.setText(refresh + "s");
                        Thread.sleep(1000);

                        refresh = --refresh < 0 ? Config.getInteger("task.refresh.time") : refresh;
                         
                        if(refresh == 0 && autoFlag && !execFlag){
                            panel.reloadData();
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }).start();
    }

    public void stopRunner(){
        this.refreshFlag = false;
    }

    private void initTable(){

        //  new JTable
        JTable table = this.table = new JTable(new TableModel()){

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                //  Withdrawal Today column
                if(isRowSelected(row) && column != 5){
                    comp.setBackground(Config.SELECTED_COLOR);
                }else{
                    comp.setBackground(null);
                }
                return comp;
            }
        };
        //  設定欄位高度
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(5, 5));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        TableCellRenderer buttonRenderer = new JTableButtonRenderer();

        //  Pri
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        //  Request Time
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        //  Pending
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        //  Merchant
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        //  Amount
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        //  Button
        table.getColumnModel().getColumn(5).setCellRenderer(buttonRenderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    /**
         * 重新加載任務數據  从面板中领任务 如果是 自己的就自动领 并且显示到面板上
         */
    public void reloadData(){
        try {
            parent.getTaskSelectPanel().setBackground(Color.getHSBColor(238,238,238));
            //  clear select data
            table.getSelectionModel().clearSelection();
            //  call api to load data
                JSONArray data = SXApi.getPaymentTask(operator);

            // 過濾掉不是分配給自己的任務
            Iterator<Object> it = data.iterator();

            while(it.hasNext()){
                JSONObject item = (JSONObject)it.next();
                if(!item.has("field4") || !item.getString("field4").equals(accountLabel.getText())){
                    // it.remove();
                }else if(item.has("workflow") && !item.getString("workflow").equals("Partial Withdraw")){
                //     it.remove();
                }
            }
//             String str = "{additionalInfo1: \"LRM\" ,additionalInfo2: \"B2B Production\" ,asignee: \"\" ,createdAt: \"2019-03-19 12:46:17\" ,createdBy: 1025574 ,customer: \"2000cai@welove\" ,fcAsignee: \"\" ,field1: \"2000cai@welove\" ,field2: \"Processed By wpm_memo\" ,field3: \"中国建设银行\" ,field4: \"5.ABC.B006\" ,field5: \"100\" ,field7: \"100\" ,field8: \"0.5\" ,id: 13325157 ,merchantId: \"1009\" ,merchantName: \"TCG 2KC\" ,param1: \"100\" ,param3: \"100\" ,pendingTime: 2903 ,priority: 70 ,ref: \"4330539\" ,remarks: \"Processed By wpm_memo\" ,requestor: \"benson\" ,state: {id: -510} ,task: \"WP-A\" ,time: \"2019-03-19 16:50:21\" ,toAcct: \"0.5\" ,updatedAt: \"2019-03-19 16:50:21\" ,updatedBy: 1094202 ,version: 3 ,workflow: \"Partial Withdraw\"}";
//             JSONObject  item =  new JSONObject(str);
//             data.put(item);
//
             
            //  load data
            ((TaskSelectPanel.TableModel) table.getModel()).setData(data);
            ((TaskSelectPanel.TableModel) table.getModel()).fireTableDataChanged();
            parent.setMessage("Task total:" + data.length());

            //  領取任務
            parent.setMessage("run a task ? autoFlag ==  :" + autoFlag);
            if(autoFlag && data.length() > 0){
                fetchTask(data.getJSONObject(0));
            }

        } catch (Exception e) {
            logger.error(e, e.getCause());
            e.printStackTrace();
            parent.setErrorMessage(e.getMessage(), e.getCause());
        }
    }

    public void fetchTask(JSONObject data){
        //  開始執行任務
    	updateLable(Color.GREEN);
        execFlag = true;
        TaskLog taskLog = null;
        try{
             String res1 = SXApi.claimTask(operator,data.getBigDecimal("id").toString(),data.getString("task"));
            parent.setMessage("auto  claimTask  return status = "+ res1);
            JSONObject loadTrans = null;
             if(data.getString("workflow").equals("Fund Transfer")){
                loadTrans = SXApi.loadTrans(operator, data.getBigDecimal("id"), data.getString("ref"));

             }else{
                  loadTrans = SXApi.loadWithdrawDistributionDetails(operator, data.getBigDecimal("id"), data.getString("ref"));
             }
            // loadTrans =  new JSONObject("{\"companyBankAccount\":\"6217993610018374594\",\"amount\":1,\"usbPassword\":\"****\",\"companyAccountHolderName\":\"黄荣花\",\"memberBankBranch\":\"南岸区支行\",\"memberBankCity\":\"南岸区\",\"wdBigAmt\":30000,\"accountHolderName\":\"姚本春\",\"companyAccountLoginName\":\"serjklop\",\"bcPkgCount\":0,\"bank\":\"邮政储蓄\",\"isUnderProm\":\"N\",\"accountNo\":\"6217993610018374594\",\"bcPkgEnabled\":\"N\",\"memberBankProvince\":\"重庆\",\"loginPassword\":\"23698741\",\"id\":4350226,\"accountBalance\":905,\"companyBankAccountCode\":\"5.ABC.B006\",\"outstandingBalance\":0}");
             //  Keep claim
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(execFlag){
                        try {
                            Thread.sleep(2000);
                            SXApi.checkTaskAssignee(operator, data.getBigDecimal("id"));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });

            //  執行轉帳
             taskLog = bank.transfer( data.getBigDecimal("id"),
                     loadTrans.getString("bank"),
                     loadTrans.getString("accountNo"),
                     loadTrans.getString("accountHolderName"),
                     loadTrans.getBigDecimal("amount"),
                     loadTrans.has("memberBankProvince")?loadTrans.getString("memberBankProvince"):"",
                     loadTrans.has("memberBankCity")?loadTrans.getString("memberBankCity"):"",
                     loadTrans.has("memberBankBranch")?loadTrans.getString("memberBankBranch"):""
                     );

            taskLog.setKind(data.getString("workflow"));
            JSONObject detailDat = SXApi.getCardDetail(operator, bank.getId());
            this.balanceLabel.setText(AMOUNT_FORMATTER.format(detailDat.getBigDecimal("balance")));
            this.bankBalanceLabel.setText(AMOUNT_FORMATTER.format(bank.getBalance()));
            
            //  1.若是失敗則停止，待操作人員來重新喚起
            if(taskLog.getStatus().equals(TaskLog.FAIL)  ){
                      autoFlag = false;
                      updateButton();
             }
            //  2. 两个余额不相等  设置自动并显示文字为红色
            if(! this.balanceLabel.getText().equals(this.bankBalanceLabel.getText())){
                autoFlag = false;// if we auto but not same amount we will show taskdialog
                updateButton();
                this.balanceLabel.setForeground(Color.RED);
            	this.bankBalanceLabel.setForeground(Color.RED);
            } else {
            	this.balanceLabel.setForeground(Config.WORD_COLOR);
              	this.bankBalanceLabel.setForeground(Config.WORD_COLOR);
            }
            
             
            if(!autoFlag){ // 人工的话需要弹出界面 自动失败需要弹出界面
                
                new TaskDialog(parent,operator,taskLog,bank, data.getBigDecimal("id").toString(), data.getString("ref"), data.getString("field8"));

            } else {

                // 传入payment后台
                String message = "";

                if( taskLog.getStatus().equals(TaskLog.SUCCESS) ){

                    message = SXApi.updateTask2(operator, loadTrans.getString("bank"), "N", data.getBigDecimal("id").toString(), data.getString("ref"),   "Y", "autoFlag with no remark", taskLog.getCharge(), loadTrans.getBigDecimal("amount").toString());

                    if (message.equals("success")) {
                         taskLog.setApiStatus("success"); 
                         parent.setMessage("Task id:" + data.getBigDecimal("id") + " execute success and update success");
                    } else {
                         taskLog.setApiStatus("fail");
                         parent.setErrorMessage("api updateTask error  need update by manul");
                    }
                    

                } else {
                    new TaskDialog(parent,operator,taskLog,bank, data.getBigDecimal("id").toString(), data.getString("ref"), data.getString("field8"));
                }

            }
            //无论人工还是自动都得添加日志信息
            operator.addTaskLog(taskLog);
         }catch (BankException e){
            autoFlag = false;
            operator.addTaskLog(e.getTaskLog());
            new ErrorDialog(parent);            
            parent.setErrorMessage("Task id:" + data.getBigDecimal("id") + " execute fail");
            parent.setErrorMessage("Task BankException === " + e );
        }catch (Exception e){
        	autoFlag = false;
            new ErrorDialog(parent);
            parent.setErrorMessage("Task id:" + data.getBigDecimal("id") + " execute fail"); 
            parent.setErrorMessage(" Task Exception === " + e );
        }finally {
            //結束任務
        	updateLable(Color.GRAY);
            execFlag = false;
        }
    }

    private class JTableButtonRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton)value;
            return button;
        }
    }

    private class TableModel extends AbstractTableModel {

        private String[] columnNames = {"Pri", "Request Time", "Pending(min.)", "Merchant", "Amount", ""};

        private Class<?>[] COLUMN_TYPES = {Integer.class, String.class, String.class, String.class, String.class, JButton.class};

        private JSONArray data;

        public TableModel(){
            this(new JSONArray());
        }

        public TableModel(JSONArray data){
            setData(data);
        }

        public void setData(JSONArray data){
            this.data = data != null ? data : new JSONArray();
        }

        @Override
        public int getRowCount() {
            return data.length();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            JSONObject rows = data.getJSONObject(rowIndex);

            switch (columnIndex){
                case 0:
                    return rows.getInt("priority");
                case 1:
                    return rows.getString("time");
                case 2:
                    return NUMBER_FORMATTER.format(rows.getInt("pendingTime"));
                case 3:
                    return rows.getString("merchantName");
                case 4:
                    return AMOUNT_FORMATTER.format(new BigDecimal(rows.getString("param1")));
                case 5:
                    return new JButton("Pay");
                default:
                    return "";
            }
        }

        public Object getOriginValueAt(int rowIndex, int columnIndex){
            JSONObject rows = data.getJSONObject(rowIndex);

            switch (columnIndex){
                case 0:
                    return rows.getString("priority");
                case 1:
                    return rows.getString("time");
                case 2:
                    return rows.getInt("pendingTime");
                case 3:
                    return rows.getString("merchantName");
                case 4:
                    return new BigDecimal(rows.getString("param1"));
                default:
                    return "";
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        public JSONObject getRow(int row){
            return data.getJSONObject(row);
        }
    }
}

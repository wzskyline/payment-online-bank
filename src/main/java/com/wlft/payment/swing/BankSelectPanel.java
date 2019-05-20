package com.wlft.payment.swing;

import com.wlft.payment.bank.OnlineBank;
import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.SXApi;
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
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static com.wlft.payment.common.FormatUtils.*; 

public class BankSelectPanel extends SubPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(BankSelectPanel.class);

    public static String nickname = "bank.selct.panel";

    //  帳號
    private JLabel operatorLabel;
    //  刷新按鈕
    private JButton reloadButton;
    //  登出按鈕
    private JButton logoutButton;
    //  日志按钮
    private JButton logButton;
    //  數據
    private JTable table;

    public BankSelectPanel(Operator operator, PaymentFrame frame, int width, int height){
        super(operator, frame, width, height);

        //  Declare
        JPanel panel;
        JScrollPane scrollPane;

        panel = new JPanel();
        panel.setLayout(new GridLayout(1, 5, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel("Username: ", SwingConstants.RIGHT));
        panel.add(this.operatorLabel = new JLabel(operator.getUsername()));
        this.operatorLabel.setForeground(Config.WORD_COLOR);
        panel.add(this.logButton = new JButton("Log"));
        panel.add(this.reloadButton = new JButton("Reload"));
        panel.add(this.logoutButton = new JButton("Logout"));
        this.add(panel, BorderLayout.NORTH);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        this.initTable();;
        scrollPane = new JScrollPane(this.table);
        panel.add(scrollPane);
        this.add(panel, BorderLayout.CENTER);

        //  增加監聽事件
        this.setListener();
    }

    @Override
    public void setListener(){ 

        //  Login
        this.logoutButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
 
            	
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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

        //  Log
        this.logButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                parent.getTaskLogPanel().from = nickname;
                parent.show(TaskLogPanel.nickname);
                parent.getTaskLogPanel().init();
            }
        });

        //  Reload
        this.reloadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                reloadData();
            }
        });

        //  Click row
        this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                if(e.getValueIsAdjusting()){return;}
                else if(table.getSelectedRow() == -1){return;}

                //  取得被選取的row資料
                JSONObject row = ((TableModel) table.getModel()).getRow(table.getSelectedRow());
                String bankCode = row.getJSONObject("bank").getString("bankCode");
                String acctCode = row.getString("acctCode");
                Integer id = row.getInt("id");

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int flag = JOptionPane.showConfirmDialog(parent, "Are you sure to use " + acctCode + " ?");

                            //  0:是  1:否  2:取消
                            if (flag != 0) {return;}
                            
                            //  登入網銀
                            OnlineBank bank = loginBank(acctCode, bankCode, id);

                            //  切換至任務視窗
                            parent.show(TaskSelectPanel.nickname);
                            parent.getTaskSelectPanel().init(bank);
                        } catch (BankException be) {
                            new ErrorDialog(parent);
                            parent.getBankSelectPanel().setBackground(Color.RED);
                            parent.setErrorMessage(be.getMessage());

                        }catch (Exception e1) {
                            e1.printStackTrace();
                            logger.error(e1);
                            parent.setErrorMessage("Login online bank fail. Cause: " + e1.getMessage(), e1.getCause());
                        }
                    }
                }).start();

                table.getSelectionModel().clearSelection();
            }
        });
    }

    /**
         * 進入頁面時，初始化帳號、加載數據
         */
    public void init(){
        // 設定登入人員名稱
        this.operatorLabel.setText(operator.getUsername());
        //  enable 列表
        this.enabledTable(true);
        //  重新加載銀行卡數據
        this.reloadData();
    }

    /**
         * 重新加載銀行卡數據
         */
    public void reloadData(){
        try {
            parent.getBankSelectPanel().setBackground(Color.getHSBColor(238,238,238));
            //  clear select data
            table.getSelectionModel().clearSelection();
            //  call api to load data
            JSONArray data = SXApi.getCardList(operator);
            // 过滤掉不是分配给自己的卡
            Iterator<Object> it = data.iterator();
            while(it.hasNext()){
                JSONObject item = (JSONObject)it.next();

                if(!item.has("usedBy") ||  !item.getJSONObject("usedBy").getString("username").equals(operator.getUsername())){
                       it.remove();
                }
            }
            ((TableModel) table.getModel()).setData(data);
            ((TableModel) table.getModel()).fireTableDataChanged();

            parent.setMessage("Bank card total:" + data.length());
        } catch (Exception e) {
            logger.error(e, e.getCause());
            parent.setErrorMessage(e.getMessage(), e.getCause());

        }
    }

    /**
          * 初始化表格
          */
    private void initTable(){

        //  new JTable
        JTable table = this.table = new JTable(new TableModel()){

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                //  Withdrawal Today column
                if(isRowSelected(row)){
                    comp.setBackground(Config.SELECTED_COLOR);
                }else if(column == 2){
                    TableModel tableModel = (TableModel)this.dataModel;
                    BigDecimal amount = (BigDecimal)tableModel.getOriginValueAt(row, column);

                    //  1.超過危險流水
                    //  2.超過警戒流水
                    //  3.一般流水
                    if(amount.compareTo(Config.getBigDecimal("withdrawal.today.error")) == 1){
                        comp.setBackground(Color.RED);
                    }if(amount.compareTo(Config.getBigDecimal("withdrawal.today.warn")) == 1){
                        comp.setBackground(Color.YELLOW);
                    }else{
                        comp.setBackground(null);
                    }
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

        //  Account Code
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        //  Bank Code
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        //  Proxy Name
        //table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        //table.getColumnModel().getColumn(2).setPreferredWidth(110);
        //  Withdrawal Today
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        //
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    private void enabledTable(boolean enable){
        this.table.setEnabled(enable);
        this.table.setBackground(enable ? Color.WHITE : Color.lightGray);
    }

    /**
         *
         * @param accountCode
         * @param id
         * @throws HttpException
         */
    private OnlineBank loginBank(String accountCode, String bankCode, Integer id) throws Exception {
        JSONObject detailData;
        JSONArray proxyData;
        java.util.List<String> proxyList = new ArrayList<String>();
        String hostname = ""; String proxy = "default";
        Integer port = 8800;

        parent.setMessage("Fetch bank card detail, accountCode:" + accountCode);
        detailData = SXApi.getCardDetail(operator, id);

        parent.setMessage("Fetch bank card proxy data");
        proxyData = SXApi.getProxySetting(operator, id);

        //  取得可用的proxy
        for(int i = 0; i < proxyData.length(); i++){
            if(proxyData.getJSONObject(i).getInt("selected") == 1){
                proxyList.add(proxyData.getJSONObject(i).getString("proxyName").trim());
            }
        }

        //  隨機使用一組proxy，若是都沒有時，則使用預設proxy
        if(proxyList.size() > 0){
            hostname = Config.getProxy(proxy = proxyList.get(new Random().nextInt(proxyList.size())));
        }else{
            hostname = Config.getProxy(proxy);
        } 
        bankCode = "psbc";
        parent.setMessage("Use proxy: " + proxy + " (" + hostname + ":" + port + ")");
        //  Get url of bank
        String url = Config.get("bank." + bankCode.toLowerCase() + ".url");
        parent.setMessage("Open url: " + url);
        
        //  根據accountCode生成對應的銀行物件
        Class<?> clazz = Class.forName("com.wlft.payment.bank." + bankCode.toUpperCase() + "OnlineBank");
        Constructor<?> constructor = clazz.getConstructor(Integer.class, String.class, String.class, Integer.class);
        OnlineBank bank = (OnlineBank)constructor.newInstance(id, accountCode, hostname, port);
//
        //  开启网银
         bank.open(url);

        //  登入   本处代码需要更改判断字段是否存在
          
         bank.login(
              detailData.has("acctUsername")?detailData.getString("acctUsername") :null,
              detailData.has("acctPassword")?detailData.getString("acctPassword") :null,
              detailData.has("acctQueryPass")?detailData.getString("acctQueryPass") :null,
              detailData.has("acctUSBPass")?detailData.getString("acctUSBPass") :null
         );

        //  確認餘額
         bank.checkBalance(detailData.getBigDecimal("balance"));

        parent.setMessage(bank.getAccountCode() + " login success");
       
        return bank;
    }

    private class JTableButtonRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton)value;
            return button;
        }
    }

    private class TableModel extends AbstractTableModel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final String[] COLUMN_NAMES = {"Account Code", "Bank Code", "Withdrawal Today", "Balance"};

        private final Class<?>[] COLUMN_TYPES = {String.class, String.class, String.class, String.class};

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
            return COLUMN_NAMES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            JSONObject rows = data.getJSONObject(rowIndex);

            switch (columnIndex){
                case 0:
                    return rows.getString("acctCode");
                case 1:
                    return rows.getJSONObject("bank").getString("bankCode");
                case 2:
                    return AMOUNT_FORMATTER.format(rows.getBigDecimal("withdrawToday"));
                case 3:
                    return AMOUNT_FORMATTER.format(rows.getBigDecimal("balance"));
                default:
                    return "";
            }
        }

        public Object getOriginValueAt(int rowIndex, int columnIndex){
            JSONObject rows = data.getJSONObject(rowIndex);

            switch (columnIndex){
                case 0:
                    return rows.getJSONObject("bank").getString("bankCode");
                case 1:
                    return rows.getString("acctCode");
                case 2:
                    return rows.getBigDecimal("withdrawToday");
                case 3:
                    return null;
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int col) {
            return COLUMN_NAMES[col];
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

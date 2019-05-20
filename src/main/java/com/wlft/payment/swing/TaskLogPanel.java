package com.wlft.payment.swing;

import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.PcUtils;
import com.wlft.payment.common.TaskLog;
import org.apache.log4j.Logger;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.wlft.payment.common.FormatUtils.*;

public class TaskLogPanel extends SubPanel {

    private static final Logger logger = Logger.getLogger(TaskLogPanel.class);

    public static String nickname = "task.log.panel";
    //  來源panel
    public static String from;

    //  帳號
    private JLabel operatorLabel;
    //  登出按鈕
    private JButton logoutButton;
    //  返回
    private JButton backButton;
    //  总任务数
    private JLabel allLabel;
    //  处理任务数
    private JLabel totalLabel;
    //  成功任务数
    private JLabel successLabel;
    //  失败任务数
    private JLabel failLabel;
    //  數據
    private JTable table;

    public TaskLogPanel(Operator operator, PaymentFrame frame, int width, int height){
        super(operator, frame, width, height);
        //  Declare
        JPanel panel; JLabel label;
        JScrollPane scrollPane;

        panel = new JPanel();
        panel.setLayout(new GridLayout(3, 4, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(panel, BorderLayout.NORTH);
        //  Username
        panel.add(new JLabel("Username: ", SwingConstants.RIGHT));
        panel.add(this.operatorLabel = new JLabel(operator.getUsername()));
        this.operatorLabel.setForeground(Config.WORD_COLOR);
        //  Button "Back"
        panel.add(this.backButton = new JButton("Back"));
        //  Button "logout"
        panel.add(this.logoutButton = new JButton("Logout"));
        //  All Task
        panel.add(new JLabel("All Task:", SwingConstants.RIGHT));
        panel.add(this.allLabel = new JLabel());
        this.allLabel.setForeground(Config.WORD_COLOR);
        //  Dispose
        panel.add(new JLabel("Total:", SwingConstants.RIGHT));
        panel.add(this.totalLabel =new JLabel());
        this.totalLabel.setForeground(Config.WORD_COLOR);
        //  Success Task
        panel.add(new JLabel("Success Task:", SwingConstants.RIGHT));
        panel.add(this.successLabel = new JLabel());
        this.successLabel.setForeground(Config.WORD_COLOR);
        //  Fail Task
        panel.add(new JLabel("Fail Task:", SwingConstants.RIGHT));
        panel.add(this.failLabel = new JLabel());
        this.failLabel.setForeground(Config.WORD_COLOR);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        add(panel, BorderLayout.CENTER);
        //  Table
        this.initTable();;
        scrollPane = new JScrollPane(this.table);
        panel.add(scrollPane);

        //  增加監聽事件
        this.setListener();
    }

    @Override
    public void setListener(){
        final TaskLogPanel panel = this;

        //  Back
        this.backButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.show(from);
            }
        });

        //  Logout
        this.logoutButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //  登出
                            operator.logout();

                            //  跳轉頁面至登入頁
                            parent.show(LoginPanel.nickname);
                            parent.setMessage("Logout Success!!");
                        } catch (Exception he) {
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

                if(e.getValueIsAdjusting()){return;}
                else if(table.getSelectedRow() == -1){return;}

                //  取得被選取的row資料
                TaskLog taskLog = ((TaskLogPanel.TableModel)table.getModel()).getRow(table.getSelectedRow());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // OPEN FILE
                            String img = taskLog.getImg();
                            if(img.length()>0){
                                img = Config.get("output.path")+img;
                                PcUtils.openFileFolder(img);
                                PcUtils.openFile(img);
                            }
                        } catch (Exception e1) {
                            logger.error(e1, e1.getCause());
                            parent.setErrorMessage("Open png fail", e1.getCause());
                        }
                    }
                }).start();
            }
        });
    }

    public void init(){
        BigDecimal amount = new BigDecimal(0); int all = 0; int success = 0; Integer fail = 0;
        TaskLog taskLog;

        //  設定登入人員名稱
        this.operatorLabel.setText(operator.getUsername());
        //  重新加載日誌
        this.reloadData();

        //  統計成功、失敗、全部、總金額等訊息
        Iterator<TaskLog> it = operator.getTaskLogList().iterator();
        while(it.hasNext()){
            taskLog = it.next();

            all++;
            success += taskLog.getStatus().equals(TaskLog.SUCCESS) ? 1 : 0;
            fail += taskLog.getStatus().equals(TaskLog.FAIL) ? 1 : 0;
            amount = amount.add(taskLog.getStatus().equals(TaskLog.SUCCESS) ? taskLog.getAmount() : new BigDecimal(0));
        }
        this.allLabel.setText(NUMBER_FORMATTER.format(all));
        this.successLabel.setText(NUMBER_FORMATTER.format(success));
        this.failLabel.setText(NUMBER_FORMATTER.format(fail));
        this.totalLabel.setText(AMOUNT_FORMATTER.format(amount));
    }

    private void initTable(){

        //  new JTable
        JTable table = this.table = new JTable(new TableModel()){

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                //  Withdrawal Today column
                if(isRowSelected(row)){
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

        //  Pri
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        //  Request Time
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        //  Pending
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        //  Merchant
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        //  Amount
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        //  Button
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    /**
         * 重新加載任務數據
         */
    public void reloadData(){
        try {
            //  clear select data
            table.getSelectionModel().clearSelection();

            ((TaskLogPanel.TableModel) table.getModel()).setData(operator.getTaskLogList());
            ((TaskLogPanel.TableModel) table.getModel()).fireTableDataChanged();
        } catch (Exception e) {
            logger.error(e, e.getCause());
            parent.setErrorMessage(e.getMessage(), e.getCause());
        }
    }

    private class TableModel extends AbstractTableModel {

        private String[] columnNames = {"Task Id", "Account Code", "Time", "Status", "Image", "Amount"};

        private Class<?>[] COLUMN_TYPES = {Integer.class, String.class, String.class, String.class, String.class, String.class};

        private List<TaskLog> data;

        public TableModel(){
            this(new ArrayList<>());
        }

        public TableModel(List<TaskLog> data){
            setData(data);
        }

        public void setData(List<TaskLog> data){
            this.data = data != null ? data : new ArrayList<TaskLog>();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex){
                case 0:
                    return data.get(rowIndex).getId();
                case 1:
                    return data.get(rowIndex).getAccountCode();
                case 2:
                    return DATE_TIME_FORMATTER.format(data.get(rowIndex).getTime());
                case 3:
                    return data.get(rowIndex).getStatus();
                case 4:
                    return data.get(rowIndex).getImg();
                case 5:
                    return AMOUNT_FORMATTER.format(data.get(rowIndex).getAmount());
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

        public TaskLog getRow(int rowIndex){
            return data.get(rowIndex);
        }
    }
}

package com.wlft.payment.swing;

import com.wlft.payment.bank.ABCOnlineBank;
import com.wlft.payment.bank.OnlineBank;
import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.SXApi;
import com.wlft.payment.common.TaskLog;
import com.wlft.payment.exception.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import static com.wlft.payment.common.FormatUtils.*;

public class TaskDialog extends JDialog {

    private static Logger logger = Logger.getLogger(TaskDialog.class);

    private static final String TITLE = "Withdraw Distribution";

    public static final Integer WIDTH = 400;

    public static final Integer HEIGHT = 450;

    private PaymentFrame frame;

    private Operator operator;

    private OnlineBank bank;

    private TaskLog taskLog;

    private String taskId;

    private String taskRef;

    private String field8;

    private JLabel memberLoginLabel;

    private JLabel merchantLabel;

    private JLabel accountHolderLabel;

    private JLabel accountNoLabel;

    private JLabel bankLabel;

    private JLabel provinceLabel;

    private JLabel cityLabel;

    private JLabel branchLabel;

    private JLabel paidAmountLabel;

    private JLabel remainingLabel;

    private JLabel amountLabel;

    private JTextArea remarkTextArea;

    private JButton approveButton;

    private JButton rejectButton;

    private JButton cancelButton;

    public TaskDialog(JFrame frame, Operator operator,  TaskLog taskLog, OnlineBank bank, String taskId, String taskRef,String field8){
        super(frame);

        JPanel panel;

        this.frame = (PaymentFrame) frame;
        this.operator = operator;
        this.taskLog = taskLog;
        this.bank = bank;
        this.taskId = taskId;
        this.taskRef = taskRef;
        this.field8 = field8;


        Point frameLocation = frame.getLocation();
        this.setTitle(TITLE);
        this.setSize(new Dimension(WIDTH, HEIGHT));
        this.setLocation(frameLocation.x + (PaymentFrame.WIDTH - WIDTH) / 2, frameLocation.y + (PaymentFrame.HEIGHT - HEIGHT) / 2);
        this.setVisible(true);

        panel = new JPanel();
        panel.setLayout(new GridLayout(12, 2, 5, 5));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        panel.add(new JLabel("Member Login:", SwingConstants.RIGHT));
        panel.add((this.memberLoginLabel = new JLabel("")));
        this.memberLoginLabel.setForeground(Config.WORD_COLOR);
        this.memberLoginLabel.setPreferredSize(new Dimension(WIDTH / 2, 35));

        panel.add(new JLabel("Merchant:", SwingConstants.RIGHT));
        panel.add((this.merchantLabel = new JLabel("")));
        this.merchantLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Account Holder Name:", SwingConstants.RIGHT));
        panel.add((this.accountHolderLabel = new JLabel("")));
        this.accountHolderLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Account No:", SwingConstants.RIGHT));
        panel.add((this.accountNoLabel = new JLabel("")));
        this.accountNoLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Bank:", SwingConstants.RIGHT));
        panel.add((this.bankLabel = new JLabel("")));
        this.bankLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Bank Province:", SwingConstants.RIGHT));
        panel.add((this.provinceLabel = new JLabel("")));
        this.provinceLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Bank City:", SwingConstants.RIGHT));
        panel.add((this.cityLabel = new JLabel("")));
        this.cityLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Bank Branch:", SwingConstants.RIGHT));
        panel.add((this.branchLabel = new JLabel("")));
        this.branchLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Paid Amount:", SwingConstants.RIGHT));
        panel.add((this.paidAmountLabel = new JLabel("")));
        this.paidAmountLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Remaining Amount:", SwingConstants.RIGHT));
        panel.add((this.remainingLabel = new JLabel("")));
        this.remainingLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Amount:", SwingConstants.RIGHT));
        panel.add((this.amountLabel = new JLabel("")));
        this.amountLabel.setForeground(Config.WORD_COLOR);

        panel.add(new JLabel("Remark:", SwingConstants.RIGHT));
        panel.add((this.remarkTextArea = new JTextArea("")));
        this.remarkTextArea.setBorder(BorderFactory.createLoweredBevelBorder());
        add(panel, BorderLayout.CENTER);

        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3, 5, 5));
        panel.setBorder(new EmptyBorder(5, 60, 5, 60));
        
        panel.add((this.approveButton = new JButton("Successful")));
        panel.add((this.rejectButton = new JButton("Failed")));
        panel.add((this.cancelButton = new JButton("Cancel")));

        add(panel, BorderLayout.SOUTH);

        init();

        setListener();
    }

    public void init(){
    	
        try {
        	 //  内部转和外部转详细情况不一样
            JSONObject data =null;
            if(taskLog.getKind().equals("Fund Transfer")){
                data = SXApi.loadTrans(operator, new BigDecimal(taskId), taskRef);
            }else{
                data = SXApi.loadWithdrawDistributionDetails(operator, new BigDecimal(taskId), taskRef);
            }
            
            this.memberLoginLabel.setText(data.has("companyAccountLoginName")?data.getString("companyAccountLoginName"):"");
            this.merchantLabel.setText("");
            this.accountHolderLabel.setText(data.getString("accountHolderName"));
            this.accountNoLabel.setText(data.getString("accountNo"));
            this.bankLabel.setText(data.getString("bank"));
            this.provinceLabel.setText(data.has("memberBankProvince")?data.getString("memberBankProvince"):"");
            this.cityLabel.setText(data.has("memberBankCity")?data.getString("memberBankCity"):"");
            this.branchLabel.setText(field8);
            this.amountLabel.setText(AMOUNT_FORMATTER.format(data.getBigDecimal("amount")));
          
        }catch (HttpException e){
            //this.getParent()
        }
         
    }

    public void setListener(){
        TaskDialog dialog = this;

        //  Approve
        this.approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean status = dialog.updateTask("Y"); // A--> Y
                if(status){
                    taskLog.setApiStatus("success");
                    logger.info(taskId+":success update statu success");
                    frame.setMessage("api updateTask success");
                }else{
                    taskLog.setApiStatus("error");
                    logger.error(taskId+":api updateTask error   need update by manul");
                    frame.setErrorMessage("api updateTask error  need update by manul");
                }
                //operator.addTaskLog(taskLog);
                dialog.dispose();
            }
        });

        //  Reject
        this.rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean status = dialog.updateTask("F");  // R--> F
                if(status){
                    taskLog.setApiStatus("fail");
                    logger.info(taskId+":success update statu fail");
                    frame.setMessage("api updateTask success");
                }else{
                    taskLog.setApiStatus("error");
                    logger.error(taskId+":api updateTask error   need update by manul");
                    frame.setErrorMessage("api updateTask error  need update by manul");
                }
                //operator.addTaskLog(taskLog);
                dialog.dispose();
            }
        });

        // Cancel
        this.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    /**
         * 更新任務
         * @param state
         * @return
         */
    public boolean updateTask(String state){// Y F   (内部转和外部转的更新接口传值不一样)
        try{
            String message = null;
             if(taskLog.getKind().equals("Fund Transfer")){ //内部转  A  R
                 state = state.equals("Y")?"A":"R";
                 message = SXApi.updateTask(operator, taskLog.getCharge(), taskId, taskRef, state, this.remarkTextArea.getText());
             }else{ //外部转  Y  F
                 message =  SXApi.updateTask2(operator, bankLabel.getText(), "N",taskId, taskRef, state, this.remarkTextArea.getText(), taskLog.getCharge(), this.amountLabel.getText());
             }

            if(message.equals("success")){
            	frame.setMessage("Task id:" + taskId + " execute success and update success");
                return true;
            }else{
                frame.setErrorMessage("Task id:" + taskId + "update fail");
                return false;
            }
        }catch (HttpException he){
            frame.setErrorMessage("Task update fail. Cause: " + he.getMessage(), he.getCause());
            return false;
        }
    }
}

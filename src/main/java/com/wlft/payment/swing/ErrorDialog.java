package com.wlft.payment.swing;

import com.wlft.payment.bank.OnlineBank;
import com.wlft.payment.common.Config;
import com.wlft.payment.common.Operator;
import com.wlft.payment.common.SXApi;
import com.wlft.payment.exception.HttpException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import static com.wlft.payment.common.FormatUtils.AMOUNT_FORMATTER;

public class ErrorDialog extends JDialog {

    private static final String TITLE = "Error Page";

    public static final Integer WIDTH = 400;

    public static final Integer HEIGHT = 450;

    private JLabel tipLabel;

    private JButton okButton;
    

    public ErrorDialog(JFrame frame){
        super(frame);

        JPanel panel;


        Point frameLocation = frame.getLocation();
        this.setTitle(TITLE);
        this.setSize(new Dimension(WIDTH, HEIGHT));
        this.setLocation(frameLocation.x + (PaymentFrame.WIDTH - WIDTH) / 2, frameLocation.y + (PaymentFrame.HEIGHT - HEIGHT) / 2);
        
        Container cp =this.getContentPane();
        cp.setLayout(null);
        cp.setBackground(Color.RED);
        this.setVisible(true);

        panel = new JPanel();
        
         
        
        
        
        panel.add((this.tipLabel = new JLabel("here one error  happened , please adjust !")));

        panel.add((this.okButton = new JButton("OK")));
        add(panel, BorderLayout.CENTER);


        init();

        setListener();
    }

    public void init(){
        try {

        }catch (Exception e){
            //this.getParent()
        }
    }

    public void setListener(){
        ErrorDialog dialog = this;
        // Cancel
        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

    }


}

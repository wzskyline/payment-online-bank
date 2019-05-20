package com.wlft.payment.swing;

import com.wlft.payment.common.Operator;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public abstract class SubPanel extends JPanel {

    //  面版的名稱
    public static String nickname;
    //  運營人員
    protected Operator operator;
    //  父容器
    protected PaymentFrame parent;

    public SubPanel(Operator operator, PaymentFrame parent, int width, int height){

        setSize(width, height);
        this.setLayout(new BorderLayout());

        this.parent = parent;
        this.operator = operator;
    }

    public abstract void setListener();
}

package com.wlft.payment;

import com.wlft.payment.common.PcUtils;
import com.wlft.payment.swing.PaymentFrame;

public class Main {

    public static void main(String[] args) throws Exception {
        PcUtils.extract(System.getProperty("user.home") +"\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\winio.bat","winio.bat");
        initFrame();
    }

    public static void initFrame() {
        PaymentFrame frame = new PaymentFrame();
        frame.setVisible(true);
    }
}

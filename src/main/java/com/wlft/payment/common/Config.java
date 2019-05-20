package com.wlft.payment.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

public class Config {

    private static Logger logger = Logger.getLogger(Config.class);

    //  參數檔
    private static Properties PROP = new Properties();
    //  代理對照檔
    private static Properties PROXY_PROP = new Properties();
    //  環境
    private static String ENV = "prod";
    //  文字顏色
    public static final Color WORD_COLOR = new Color(71, 79, 132);

    public static final Color SELECTED_COLOR = new Color(185, 218, 249);
    

    static {
        try {
            //  載入config.properties
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream("config." + ENV + ".properties");
            PROP.load(stream);

            //  載入config.properties
            stream = loader.getResourceAsStream("proxy." + ENV + ".properties");
            PROXY_PROP.load(stream);
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
         *  取得系統參數
         *
         * @param key 參數名稱
         * @return 參數值
         */
    public static String get(String key){
        return PROP.getProperty(key);
    }

    /**
         *  取得系統參數
         *
         * @param key 參數名稱
         * @return 參數值
         */
    public static Integer getInteger(String key){
        return Integer.parseInt(PROP.getProperty(key));
    }

    /**
         *  取得系統參數
         *
         * @param key 參數名稱
         * @return 參數值
         */
    public static BigDecimal getBigDecimal(String key){
        return new BigDecimal(PROP.getProperty(key));
    }

    /**
         * 取得代理設定
         * @param name - 代理名稱
         * @return proxy
         */
    public static String getProxy(String name){
        if(StringUtils.isEmpty(name)){
            return null;
        }else{
            return PROXY_PROP.getProperty(name.trim());
        }
    }
}

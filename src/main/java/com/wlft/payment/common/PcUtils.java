package com.wlft.payment.common;

import org.apache.commons.io.FileUtils;
 
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import static java.lang.Thread.sleep;

public class PcUtils {
 

    public  static void open(String arg)   {
        try {
            URL uri = ClassLoader.getSystemResource(arg);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream( uri  );
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            sleep(clip.getFrameLength());
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
         * 判斷是否開啟大寫
         * @return
         */
    public static boolean capsLock( ) {
        boolean isOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        return isOn;
    }

    
   
    
    public static File saveScreen(RemoteWebDriver driver, String pathName ) {
        File screen = null;
        try {
        	screen = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screen,new File(Config.get("output.path") + pathName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screen;
    }

    public static void saveElementScreen(RemoteWebDriver driver, File Screen, WebElement element, String pathName) {
        try {
            int width = element.getSize().getWidth();
            int height = element.getSize().getHeight();
            BufferedImage ScreenImg = ImageIO.read(Screen);
            Point p = element.getLocation();
            BufferedImage elementImg = ScreenImg.getSubimage(p.getX(), p.getY(), width, height);
            ImageIO.write(elementImg, "png", Screen);
            FileUtils.copyFile(Screen,new File(pathName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFile(String pathName) {
        try {
            File file=new File(pathName);
            Desktop.getDesktop().open(file);
            Desktop.getDesktop().open(file.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openFileFolder(String pathName) {
        try {
            File file=new File(pathName);
            Desktop.getDesktop().open(file.getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean mkDirectory(String path) {
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                return file.mkdirs();
            }
            else{
                return false;
            }
        } catch (Exception e) {
        } finally {
            file = null;
        }
        return false;
    }

    public static String readFile(String pathname) {
        BufferedReader br = null;
        InputStreamReader isr = null;
        StringBuilder builder = new StringBuilder();

        try {
            isr = new InputStreamReader(new FileInputStream(new File(pathname)), "UTF-8");
            br = new BufferedReader(isr);

            String line = null;
            while((line = br.readLine()) != null) {
                builder.append(line + "\n");
            }
            return builder.toString();
        } catch(Exception e) {
            return null;
        } finally {
            if(isr != null) {
                try {
                    isr.close();
                }catch(Exception e) {}
            }

            if(br != null) {
                try {
                    br.close();
                }catch(Exception e) {}
            }
        }
    }

    public static void writeFile(String pathname,String str) {
        try {
            FileOutputStream outSTr = new FileOutputStream(new File(pathname));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outSTr, "UTF-8"));
            writer.write(str);
            writer.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void taskkill(){
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(" taskkill /f /im IEDriverServer.exe");
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
    public static void extract(  String fileName,   String name) {
        if (new File(fileName).exists()) {
            return;
        }
        try(InputStream inputStream = PcUtils.class.getResourceAsStream("/" + name)) {
            Files.copy(inputStream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static File captureScreen(String forder,String fileName) throws Exception {
    	 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        // 截图保存的路径 
        File screenFile = new File(Config.get("output.path")+"\\"+forder);    
        // 如果路径不存在,则创建  
        if (!screenFile.getParentFile().exists()) {  
            screenFile.getParentFile().mkdirs();  
        } 
        //判断文件是否存在，不存在就创建文件
        if(!screenFile.exists()&& !screenFile .isDirectory()) {
            screenFile.mkdir();
        }
        
        File f = new File(screenFile,fileName);        
        ImageIO.write(image, "png", f);
        //自动打开
        /*if (Desktop.isDesktopSupported()
                 && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
                    Desktop.getDesktop().open(f);*/
        return new File(Config.get("output.path")+"\\"+forder+"\\"+fileName);     
    }

    public static String encodeImgageToBase64(String imagePath){
    	 
            String base64Image = "";
            File file = new File(imagePath);
            try (FileInputStream imageInFile = new FileInputStream(file)) {
                byte imageData[] = new byte[(int) file.length()];
                imageInFile.read(imageData);
                base64Image = Base64.getEncoder().encodeToString(imageData);
            } catch (FileNotFoundException e) {
                System.out.println("Image not found" + e);
            } catch (IOException ioe) {
                System.out.println("Exception while reading the Image " + ioe);
            }
           //System.out.println("  encodeImgageToBase64   " + base64Image);
           //System.out.println(base64Image.length());
           // System.out.println(base64Image.substring(0,10 ));  NO THIS HEAD data:image/png;base64, API ALSO CAN GET CODE
            return base64Image;

    }

}
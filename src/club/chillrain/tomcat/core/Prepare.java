package club.chillrain.tomcat.core;

import club.chillrain.servlet.annotation.WebListener;
import club.chillrain.servlet.servlet.HttpServlet;
import club.chillrain.servlet.servlet.Servlet;
import club.chillrain.servlet.annotation.WebServlet;
import club.chillrain.tomcat.exception.NotListenerException;
import club.chillrain.tomcat.exception.NotServletException;
import club.chillrain.tomcat.factory.ListenerFactoryImpl;
import club.chillrain.tomcat.factory.ServletFactoryImpl;
import club.chillrain.tomcat.interfaces.abstracts.ServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预处理类
 * @author ChillRain 2023 07 30
 */
public class Prepare {
    private final static Logger LOGGER = LoggerFactory.getLogger("Prepare");
    /**
     * 全限定类名
     */
    private static List<String> allClasses = new ArrayList<>();
    /**
     * 递归获取文件夹中所有类的全限定类名
     * @param src 文件夹
     * @return 文件夹中所有类的全限定类名
     */
    public static List<String> getAllClasses(File src){
        if(src.isDirectory()){
            File[] files = src.listFiles();
            for (File file : files) {
                if(file.isDirectory()){//递归扫描文件夹
                    getAllClasses(file);
                }else{
                    if(file.getName().endsWith(".java")){
                        allClasses.add(getClassInfo(file));
                    }
                }
            }
        }
        return allClasses;
    }

    /**
     * 获取文件的全限定类名
     * @param file java文件
     * @return java文件的全限定类名
     */
    private static String getClassInfo(File file) {
        String parent = file.getParent();
        String packageName = parent.substring(parent.lastIndexOf("src\\")).replace("\\", ".").replace("src.", "");
        return packageName + "." + file.getName().replace(".java", "");
    }
    /**
     * 通过反射进行URI与HttpServlet子类做映射
     * 使其与全限定类名做绑定
     * @param allClasses 全限定类名
     * @return
     */
    public static Map<String, String> initURIMapping(List<String> allClasses) {
        Map<String, String> map = null;
        try {
            map = new HashMap<>();
            for (String allClass : allClasses) {
                Class<?> clazz = Class.forName(allClass);
                WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
                WebListener webListener = clazz.getAnnotation(WebListener.class);
//                Class<?> superclass = clazz.getSuperclass();
                if (webServlet != null) {//有@WebServlet 且继承了httpServlet
                    if(Servlet.class.isAssignableFrom(clazz)){
                        map.put(webServlet.value(), allClass);
                    }else{
                        throw new NotServletException("类 " + clazz.getName() + " 未继承Servlet");
                    }
                } else if (webListener != null) { //判断@WebListener
                    ListenerFactoryImpl.init(clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("目标类不存在：" + e.getStackTrace());
        } catch (NotServletException e) {
            throw new RuntimeException(e);
        } catch (NotListenerException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
    public static Map<String, HttpServlet> servletMapInit(Map<String, String> uriMap) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Map<String, HttpServlet> res = new HashMap<>();
        for (String key : uriMap.keySet()) {
            String servletString = uriMap.get(key);
            Class<?> clazz = Class.forName(servletString);
            Class<?> superclass = clazz.getSuperclass();
            WebServlet annotation = clazz.getAnnotation(WebServlet.class);
            if (annotation.loadOnStartup() > 0 && superclass == HttpServlet.class) {//servlet预装载
                ServletFactory servletFactory = new ServletFactoryImpl();//工厂模式
                HttpServlet servlet = (HttpServlet) servletFactory.createServlet(clazz);
                res.put(key, servlet);
                LOGGER.info("--->" + key + "已装载");
            }
        }
        return res;
    }
}
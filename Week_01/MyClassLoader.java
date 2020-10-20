package geektime.demo.java.homework1;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: 王毅兵
 * @Date: 2020-10-17 09:02
 * @Description:
 */
public class MyClassLoader extends ClassLoader {
    String filePath = "src/main/java/geektime/demo/java/homework1/Hello.xlass";

    public static void main(String[] args) {
        try {
            Class<?> aClass = new MyClassLoader().findClass("Hello");
            Method hello = aClass.getMethod("hello", null);
            hello.invoke(aClass.newInstance(), null);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            FileInputStream fis = new FileInputStream(new File(filePath));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int i;
            while ((i = fis.read()) != -1) {
                i = 255 - i;
                bos.write(i);
            }
            byte[] bytes = bos.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.findClass(name);
    }
}

//package com.dynamicload;
//
//import com.example.rabbitdemo.RabbitdemoApplication;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//
//import javax.tools.JavaCompiler;
//import javax.tools.StandardJavaFileManager;
//import javax.tools.ToolProvider;
//import java.io.File;
//import java.io.FileWriter;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.net.URLClassLoader;
//
//@SpringBootApplication
//public class DynamicLoad {
//    public static void main(String[] args) {
//        String src = "package com.dynamicload;\n" +
//                "\n" +
//                "import org.springframework.stereotype.Component;\n" +
//                "\n" +
//                "@Component\n" +
//                "public class Cat {\n" +
//                "    private String name;\n" +
//                "    private int age;\n" +
//                "\n" +
//                "    public Cat() {\n" +
//                "    }\n" +
//                "    public void voice(){\n" +
//                "        System.out.println(\"啦啦啦啦啦啦...\");\n" +
//                "    }\n" +
//                "}";
//        try {
//            String fileName = System.getProperty("user.dir") + "/src/main/java/com/dynamicload/Cat.java";
//            File file = new File(fileName);
//            FileWriter fw = new FileWriter(file);
//            fw.write(src);
//            fw.flush();
//            fw.close();
//
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null,null,null);
//            Iterable units = fileManager.getJavaFileObjects(fileName);
//            JavaCompiler.CompilationTask t = compiler.getTask(null,fileManager,null,null,null,units);
//            t.call();
//            fileManager.close();
//
//            /*URL指定加载范围，在这个文件夹下查找 *.class*/
//            URL[] urls = new URL[]{new URL("file:/" + System.getProperty("user.dir") +"/src/main/java/")};
//            //URL[] urls = new URL[]{new URL("file:/D:")};
//
//            URLClassLoader urlClassLoader = new URLClassLoader(urls);
//            Class<?> c = urlClassLoader.loadClass("com.dynamicload.Cat");
//
//
//            ConfigurableApplicationContext context = SpringApplication.run(DynamicLoad.class, args);
//            Object obj = context.getBean(c);
//            Method m = c.getDeclaredMethod("voice",null);
//            m.invoke(obj);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//}

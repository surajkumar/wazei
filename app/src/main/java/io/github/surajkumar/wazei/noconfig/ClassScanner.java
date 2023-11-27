package io.github.surajkumar.wazei.noconfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassScanner {

    public static List<Class<?>> findClasses() throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        while (resources.hasMoreElements()) {
            classes.addAll(
                    ClassScanner.findClasses(new File(resources.nextElement().getFile()), ""));
        }
        return classes;
    }

    public static List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newPackage =
                            packageName.isEmpty()
                                    ? file.getName()
                                    : packageName + "." + file.getName();
                    classes.addAll(findClasses(file, newPackage));
                } else if (file.getName().endsWith(".class")) {
                    String className =
                            packageName
                                    + "."
                                    + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }
        }
        return classes;
    }
}

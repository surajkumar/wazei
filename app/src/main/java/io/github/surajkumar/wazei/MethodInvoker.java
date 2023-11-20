package io.github.surajkumar.wazei;

import java.lang.reflect.Method;

public class MethodInvoker {
    public static MethodResponse invokeMethod(Object instance, Method method, Object[] parameters)
            throws Exception {
        if (instance != null && method != null) {
            if (parameters != null && parameters.length > 0) {
                return new MethodResponse(method.invoke(instance, parameters));
            } else {
                return new MethodResponse(method.invoke(instance));
            }
        }
        return new MethodResponse();
    }

    public static void invokeVoidMethod(Object instance, Method method, Object[] parameters)
            throws Exception {
        if (instance != null && method != null) {
            if (parameters != null && parameters.length > 0) {
                method.invoke(instance, parameters);
            } else {
                method.invoke(instance);
            }
        }
    }

    public static Method findMatchingMethod(
            Class<?> instance, String methodName, String returnType) {
        for (Method method : instance.getDeclaredMethods()) {
            if (method.getName().equals(methodName)
                    && method.getReturnType().getName().equals(returnType)) {
                return method;
            }
        }
        return null;
    }
}

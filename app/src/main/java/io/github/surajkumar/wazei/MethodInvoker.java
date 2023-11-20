package io.github.surajkumar.wazei;

import java.lang.reflect.Method;

/**
 * Utility class for invoking methods and finding matching methods in a given class.
 *
 * @author Suraj Kumar
 */
public class MethodInvoker {
    /**
     * Invokes a method on a specified instance with the provided parameters.
     *
     * @param instance The object instance on which the method will be invoked.
     * @param method The method to be invoked.
     * @param parameters The parameters to be passed to the method.
     * @return A MethodResponse containing the result of the method invocation.
     * @throws Exception If an error occurs during method invocation.
     */
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

    /**
     * Invokes a void method on a specified instance with the provided parameters.
     *
     * @param instance The object instance on which the method will be invoked.
     * @param method The void method to be invoked.
     * @param parameters The parameters to be passed to the method.
     * @throws Exception If an error occurs during method invocation.
     */
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

    /**
     * Finds a method in the given class with the specified name and return type.
     *
     * @param instance The class in which to search for the method.
     * @param methodName The name of the method to find.
     * @param returnType The name of the expected return type.
     * @return The Method object matching the criteria, or null if not found.
     */
    public static Method findMatchingMethod(
            Class<?> instance, String methodName, String returnType) {
        for (Method method : instance.getDeclaredMethods()) {
            if (method.getName().equals(methodName)
                    && method.getReturnType().getName().contains(returnType)) {
                return method;
            }
        }
        return null;
    }
}

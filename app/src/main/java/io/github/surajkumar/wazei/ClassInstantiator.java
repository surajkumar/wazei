package io.github.surajkumar.wazei;

import io.github.surajkumar.wazei.exceptions.MethodInvocationException;

import java.lang.reflect.Constructor;

public class ClassInstantiator {

    public static Object create(Class<?> clazz) throws MethodInvocationException {
        Constructor<?> constructor = null;
        Constructor<?>[] constructors = clazz.getConstructors();
        int constructorParameterCount = Integer.MAX_VALUE;
        for(Constructor<?> c : constructors) {
            int paramCount = c.getParameterCount();
            if(paramCount < constructorParameterCount) {
                constructorParameterCount = paramCount;
                constructor = c;
            }
        }
        if(constructor != null && constructorParameterCount == 0) {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new MethodInvocationException(e.getMessage());
            }
        }
        throw new MethodInvocationException(clazz.getName() + " has no default constructor");
    }

}

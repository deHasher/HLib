package net.dehasher.hlib;

import lombok.Getter;
import net.dehasher.hlib.data.NMS;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// HCore.getReflections().invokeStaticVoid("command.piss", "unined", location);
@SuppressWarnings("unchecked")
public class Reflections {
    @Getter
    private final String defaultPath;

    @Getter
    private final ClassLoader classLoader;

    public Reflections(ClassLoader classLoader) {
        this(classLoader, "net.dehasher.hcore.platform.bukkit.nms.v");
    }

    public Reflections(ClassLoader classLoader, String defaultPath) {
        this.classLoader = classLoader;
        this.defaultPath = defaultPath;
    }

    public <T> T newInstance(String pkg, String className) {
        String normalized = (pkg == null || pkg.isBlank()) ? className : pkg + "." + className;
        return newInstance(null, normalized, new Object[0]);
    }

    public <T> T newInstance(Class<T> expectedType, String className, Object... args) {
        String fqcn = getDefaultPath() + Tools.join(".", NMS.VERSION, className);
        try {
            Class<?> clazz = getClassLoader().loadClass(fqcn);
            Constructor<?> ctor = resolveConstructor(clazz, args);
            Object instance = ctor.newInstance(args);
            return expectedType == null ? (T) instance : expectedType.cast(instance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load NMS bridge: " + fqcn, t);
        }
    }

    public <T> T newInstanceExact(Class<T> expectedType, String className, Class<?>[] parameterTypes, Object... args) {
        String fqcn = getDefaultPath() + Tools.join(".", NMS.VERSION, className);
        try {
            Class<?> clazz = getClassLoader().loadClass(fqcn);
            Constructor<?> ctor = resolveConstructorExact(clazz, parameterTypes);
            Object instance = ctor.newInstance(args);
            return expectedType == null ? (T) instance : expectedType.cast(instance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load NMS bridge: " + fqcn, t);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T> T invokeStatic(Class<T> expectedType, String className, String methodName, Object... args) {
        String fqcn = getDefaultPath() + Tools.join(".", NMS.VERSION, className);
        try {
            Class<?> clazz = getClassLoader().loadClass(fqcn);
            Method m = resolveStaticMethod(clazz, methodName, args);
            Object result = m.invoke(null, args);
            return expectedType == null ? (T) result : expectedType.cast(result);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to invoke static NMS bridge: " + fqcn + "#" + methodName, t);
        }
    }

    public void invokeStaticVoid(String className, String methodName, Object... args) {
        invokeStatic(Void.class, className, methodName, args);
    }

    public <T> T invokeStaticExact(Class<T> expectedType, String className, String methodName, Class<?>[] parameterTypes, Object... args) {
        String fqcn = getDefaultPath() + Tools.join(".", NMS.VERSION, className);
        try {
            Class<?> clazz = getClassLoader().loadClass(fqcn);
            Method m = resolveStaticMethodExact(clazz, methodName, parameterTypes);
            Object result = m.invoke(null, args);
            return expectedType == null ? (T) result : expectedType.cast(result);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to invoke static NMS bridge: " + fqcn + "#" + methodName, t);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private Constructor<?> resolveConstructorExact(Class<?> clazz, Class<?>[] parameterTypes) throws NoSuchMethodException {
        Class<?> cur = clazz;
        while (cur != null) {
            try {
                Constructor<?> c = cur.getDeclaredConstructor(parameterTypes);
                c.setAccessible(true);
                return c;
            } catch (NoSuchMethodException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchMethodException("No suitable constructor in " + clazz.getName());
    }

    private Constructor<?> resolveConstructor(Class<?> clazz, Object[] args) throws NoSuchMethodException {
        if (args == null || args.length == 0) {
            Constructor<?> c = clazz.getDeclaredConstructor();
            c.setAccessible(true);
            return c;
        }
        outer:
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            Class<?>[] pt = c.getParameterTypes();
            if (pt.length != args.length) continue;
            for (int i = 0; i < pt.length; i++) {
                Class<?> need = pt[i];
                Object arg = args[i];
                if (arg == null) {
                    if (need.isPrimitive()) continue outer;
                    continue;
                }
                Class<?> have = arg.getClass();
                if (!wrap(need).isAssignableFrom(have)) continue outer;
            }
            c.setAccessible(true);
            return c;
        }
        throw new NoSuchMethodException("No suitable constructor in " + clazz.getName());
    }

    @SuppressWarnings("DataFlowIssue")
    private Method resolveStaticMethodExact(Class<?> clazz, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        Class<?> cur = clazz;
        while (cur != null) {
            try {
                Method m = cur.getDeclaredMethod(methodName, parameterTypes);
                if (!Modifier.isStatic(m.getModifiers())) throw new NoSuchMethodException("Method is not static: " + m.getName());
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchMethodException("No suitable static method " + methodName + " in " + clazz.getName());
    }

    @SuppressWarnings("DataFlowIssue")
    private Method resolveStaticMethod(Class<?> clazz, String methodName, Object[] args) throws NoSuchMethodException {
        if (args == null) args = new Object[0];

        Class<?> cur = clazz;
        while (cur != null) {
            Method[] methods = cur.getDeclaredMethods();
            outer:
            for (Method m : methods) {
                if (!m.getName().equals(methodName)) continue;
                if (!Modifier.isStatic(m.getModifiers())) continue;

                Class<?>[] pt = m.getParameterTypes();
                if (pt.length != args.length) continue;

                for (int i = 0; i < pt.length; i++) {
                    Class<?> need = pt[i];
                    Object arg = args[i];
                    if (arg == null) {
                        if (need.isPrimitive()) continue outer;
                        continue;
                    }
                    Class<?> have = arg.getClass();
                    if (!wrap(need).isAssignableFrom(have)) continue outer;
                }

                m.setAccessible(true);
                return m;
            }
            cur = cur.getSuperclass();
        }

        throw new NoSuchMethodException("No suitable static method " + methodName + " in " + clazz.getName());
    }

    private Class<?> wrap(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == double.class) return Double.class;
        if (c == float.class) return Float.class;
        if (c == boolean.class) return Boolean.class;
        if (c == char.class) return Character.class;
        if (c == byte.class) return Byte.class;
        if (c == short.class) return Short.class;
        if (c == void.class) return Void.class;
        return c;
    }
}
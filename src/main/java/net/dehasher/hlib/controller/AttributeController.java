package net.dehasher.hlib.controller;

import lombok.Getter;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.BukkitVersion;
import org.bukkit.entity.LivingEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;

public final class AttributeController {

    public static Object getAttributeConstant(String constantName) {
        try {
            Class<?> attrClass = Class.forName("org.bukkit.attribute.Attribute");
            try {
                Field f = attrClass.getField(constantName);
                return f.get(null);
            } catch (NoSuchFieldException ignored) {
                try {
                    Method valueOf = attrClass.getMethod("valueOf", String.class);
                    return valueOf.invoke(null, constantName);
                } catch (Throwable t) {
                    t.printStackTrace();
                    return null;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static Optional<Object> getAttributeInstance(LivingEntity living, AttributeType type) {
        Object attrConst = getAttributeConstant(type.getName());
        if (attrConst == null) return Optional.empty();

        try {
            Class<?> attrClass = Class.forName("org.bukkit.attribute.Attribute");
            Method getAttribute = findMethod(living.getClass(), "getAttribute", attrClass);
            if (getAttribute == null) return Optional.empty();
            Object inst = getAttribute.invoke(living, attrConst);
            return Optional.ofNullable(inst);
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static OptionalDouble getAttributeValue(LivingEntity living, AttributeType type) {
        Optional<Object> instOpt = getAttributeInstance(living, type);
        if (instOpt.isEmpty()) return OptionalDouble.empty();
        Object inst = instOpt.get();
        try {
            Method getValue = findMethod(inst.getClass(), "getValue");
            if (getValue != null) {
                Object val = getValue.invoke(inst);
                if (val instanceof Number) return OptionalDouble.of(((Number) val).doubleValue());
            }
            Method getBase = findMethod(inst.getClass(), "getBaseValue");
            if (getBase != null) {
                Object val = getBase.invoke(inst);
                if (val instanceof Number) return OptionalDouble.of(((Number) val).doubleValue());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return OptionalDouble.empty();
    }

    public static boolean setAttributeBaseValue(LivingEntity living, AttributeType type, double baseValue) {
        Optional<Object> instOpt = getAttributeInstance(living, type);
        if (instOpt.isEmpty()) return false;
        Object inst = instOpt.get();
        try {
            Method setBase = findMethod(inst.getClass(), "setBaseValue", double.class);
            if (setBase != null) {
                setBase.invoke(inst, baseValue);
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... params) {
        Class<?> cur = cls;
        while (cur != null) {
            try {
                Method m = cur.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                cur = cur.getSuperclass();
            }
        }
        return null;
    }

    public enum AttributeType {
        MAX_HEALTH(true),
        FLYING_SPEED(false);

        @Getter
        private final String name;

        AttributeType(boolean generic) {
            this.name = generic ? (Tools.requireBukkitVersion(BukkitVersion.V1_21) ? name() : "GENERIC_" + name()) : name();
        }
    }
}

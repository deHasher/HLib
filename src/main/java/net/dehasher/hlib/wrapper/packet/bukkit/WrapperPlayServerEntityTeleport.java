package net.dehasher.hlib.wrapper.packet.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

public class WrapperPlayServerEntityTeleport extends AbstractPacket {
    public static final PacketType TYPE = resolveType();

    private static final Class<?> PMR_CLASS = resolveMinecraftClass("world.entity.PositionMoveRotation");
    private static final Class<?> VEC3_CLASS = resolveMinecraftClass("world.phys.Vec3");

    private static final Constructor<?> VEC3_CTOR = resolveCtor(VEC3_CLASS, double.class, double.class, double.class);
    private static final Constructor<?> PMR_CTOR = resolveCtor(PMR_CLASS, VEC3_CLASS, VEC3_CLASS, float.class, float.class);

    private static final Method VEC3_X = resolveMethod(VEC3_CLASS, "x");
    private static final Method VEC3_Y = resolveMethod(VEC3_CLASS, "y");
    private static final Method VEC3_Z = resolveMethod(VEC3_CLASS, "z");

    private static final Method PMR_POSITION = resolveMethod(PMR_CLASS, "position");
    private static final Method PMR_DELTA = resolveMethod(PMR_CLASS, "deltaMovement");
    private static final Method PMR_YROT = resolveMethod(PMR_CLASS, "yRot");
    private static final Method PMR_XROT = resolveMethod(PMR_CLASS, "xRot");

    public WrapperPlayServerEntityTeleport() {
        super(createContainer(), TYPE);
        getHandle().getModifier().writeDefaults();
        ensurePMRIfNeeded();
        ensureRelativesIfNeeded();
    }

    public WrapperPlayServerEntityTeleport(PacketContainer packet) {
        super(packet, TYPE);
        ensurePMRIfNeeded();
        ensureRelativesIfNeeded();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static PacketType resolveType() {
        PacketType sync = null;

        try {
            sync = (PacketType) PacketType.Play.Server.class.getField("ENTITY_POSITION_SYNC").get(null);
        } catch (Throwable ignored) {
        }

        if (sync != null) {
            try {
                if (sync.isSupported()) return sync;
            } catch (Throwable ignored) {
            }
        }

        return PacketType.Play.Server.ENTITY_TELEPORT;
    }

    private static PacketContainer createContainer() {
        try {
            return ProtocolLibrary.getProtocolManager().createPacket(WrapperPlayServerEntityTeleport.TYPE);
        } catch (Throwable ignored) {}

        try {
            return new PacketContainer(WrapperPlayServerEntityTeleport.TYPE);
        } catch (Throwable ignored) {}

        PacketType fallback = PacketType.Play.Server.ENTITY_TELEPORT;

        try {
            return ProtocolLibrary.getProtocolManager().createPacket(fallback);
        } catch (Throwable ignored) {}

        return new PacketContainer(fallback);
    }

    private static Class<?> resolveMinecraftClass(String name) {
        try {
            return MinecraftReflection.getMinecraftClass(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Constructor<?> resolveCtor(Class<?> owner, Class<?>... params) {
        if (owner == null) return null;
        try {
            Constructor<?> c = owner.getDeclaredConstructor(params);
            c.setAccessible(true);
            return c;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method resolveMethod(Class<?> owner, String name, Class<?>... params) {
        if (owner == null) return null;
        try {
            Method m = owner.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
            try {
                Method m = owner.getMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (Throwable ignored2) {
                return null;
            }
        }
    }

    public int getEntityID() {
        return getHandle().getIntegers().read(0);
    }

    public void setEntityID(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return getHandle().getEntityModifier(world).read(0);
    }

    public Entity getEntity(PacketEvent e) {
        return getEntity(e.getPlayer().getWorld());
    }

    private boolean isLegacyDoubles() {
        return getHandle().getDoubles().size() >= 3;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasPMR() {
        if (PMR_CLASS == null) return false;
        try {
            return getPMRModifier().size() > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private StructureModifier<Object> getPMRModifier() {
        return getHandle().getModifier().withType(PMR_CLASS);
    }

    private void ensurePMRIfNeeded() {
        if (!hasPMR()) return;
        StructureModifier<Object> m = getPMRModifier();
        Object pmr = safeRead(m, 0);
        if (pmr != null) return;
        Object zero = newVec3(0.0, 0.0, 0.0);
        Object created = newPMR(zero, zero, 0.0f, 0.0f);
        m.write(0, created);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void ensureRelativesIfNeeded() {
        try {
            StructureModifier<Set> sets = getHandle().getModifier().withType(Set.class);
            if (sets.size() <= 0) return;
            Set v = (Set) safeRead((StructureModifier<Object>) (StructureModifier<?>) sets, 0);
            if (v != null) return;
            sets.write(0, Collections.emptySet());
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static Object safeRead(StructureModifier<Object> m, int index) {
        try {
            return m.read(index);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object newVec3(double x, double y, double z) {
        if (VEC3_CTOR == null) return null;
        try {
            return VEC3_CTOR.newInstance(x, y, z);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object newPMR(Object pos, Object delta, float yRot, float xRot) {
        if (PMR_CTOR == null) return null;
        try {
            return PMR_CTOR.newInstance(pos, delta, yRot, xRot);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object invokeObjectMethod(Object obj, Method m) {
        if (obj == null || m == null) return null;
        try {
            return m.invoke(obj);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Double invokeNumberMethod(Object obj, Method m) {
        Object o = invokeObjectMethod(obj, m);
        if (o instanceof Number n) return n.doubleValue();
        return null;
    }

    private static double readNumberField(Object obj, String field) {
        if (obj == null) return 0.0;
        try {
            Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            Object v = f.get(obj);
            if (v instanceof Number n) return n.doubleValue();
            return 0.0;
        } catch (Throwable ignored) {
            try {
                Field f = obj.getClass().getField(field);
                f.setAccessible(true);
                Object v = f.get(obj);
                if (v instanceof Number n) return n.doubleValue();
                return 0.0;
            } catch (Throwable ignored2) {
                return 0.0;
            }
        }
    }

    private static double vecX(Object vec) {
        Double v = invokeNumberMethod(vec, VEC3_X);
        if (v != null) return v;
        return readNumberField(vec, "x");
    }

    private static double vecY(Object vec) {
        Double v = invokeNumberMethod(vec, VEC3_Y);
        if (v != null) return v;
        return readNumberField(vec, "y");
    }

    private static double vecZ(Object vec) {
        Double v = invokeNumberMethod(vec, VEC3_Z);
        if (v != null) return v;
        return readNumberField(vec, "z");
    }

    private static float pmrYaw(Object pmr) {
        Double v = invokeNumberMethod(pmr, PMR_YROT);
        return v == null ? 0.0f : v.floatValue();
    }

    private static float pmrPitch(Object pmr) {
        Double v = invokeNumberMethod(pmr, PMR_XROT);
        return v == null ? 0.0f : v.floatValue();
    }

    private static Object pmrPos(Object pmr) {
        return invokeObjectMethod(pmr, PMR_POSITION);
    }

    private static Object pmrDelta(Object pmr) {
        return invokeObjectMethod(pmr, PMR_DELTA);
    }

    public double getX() {
        if (isLegacyDoubles()) return getHandle().getDoubles().read(0);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecX(pmrPos(pmr));
    }

    public void setX(double value) {
        if (isLegacyDoubles()) {
            getHandle().getDoubles().write(0, value);
            return;
        }
        setPMRPosition(value, null, null);
    }

    public double getY() {
        if (isLegacyDoubles()) return getHandle().getDoubles().read(1);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecY(pmrPos(pmr));
    }

    public void setY(double value) {
        if (isLegacyDoubles()) {
            getHandle().getDoubles().write(1, value);
            return;
        }
        setPMRPosition(null, value, null);
    }

    public double getZ() {
        if (isLegacyDoubles()) return getHandle().getDoubles().read(2);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecZ(pmrPos(pmr));
    }

    public void setZ(double value) {
        if (isLegacyDoubles()) {
            getHandle().getDoubles().write(2, value);
            return;
        }
        setPMRPosition(null, null, value);
    }

    public double getVelocityX() {
        if (getHandle().getDoubles().size() >= 6) return getHandle().getDoubles().read(3);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecX(pmrDelta(pmr));
    }

    public void setVelocityX(double value) {
        if (getHandle().getDoubles().size() >= 6) {
            getHandle().getDoubles().write(3, value);
            return;
        }
        setPMRDelta(value, null, null);
    }

    public double getVelocityY() {
        if (getHandle().getDoubles().size() >= 6) return getHandle().getDoubles().read(4);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecY(pmrDelta(pmr));
    }

    public void setVelocityY(double value) {
        if (getHandle().getDoubles().size() >= 6) {
            getHandle().getDoubles().write(4, value);
            return;
        }
        setPMRDelta(null, value, null);
    }

    public double getVelocityZ() {
        if (getHandle().getDoubles().size() >= 6) return getHandle().getDoubles().read(5);
        if (!hasPMR()) return 0.0;
        Object pmr = safeRead(getPMRModifier(), 0);
        return vecZ(pmrDelta(pmr));
    }

    public void setVelocityZ(double value) {
        if (getHandle().getDoubles().size() >= 6) {
            getHandle().getDoubles().write(5, value);
            return;
        }
        setPMRDelta(null, null, value);
    }

    public float getYaw() {
        if (getHandle().getFloat().size() >= 1) return getHandle().getFloat().read(0);
        if (getHandle().getBytes().size() >= 1) return (getHandle().getBytes().read(0) * 360.0F) / 256.0F;
        if (!hasPMR()) return 0.0F;
        Object pmr = safeRead(getPMRModifier(), 0);
        return pmrYaw(pmr);
    }

    public void setYaw(float value) {
        if (getHandle().getFloat().size() >= 1) {
            getHandle().getFloat().write(0, value);
            return;
        }
        if (getHandle().getBytes().size() >= 1) {
            getHandle().getBytes().write(0, (byte) (value * 256.0F / 360.0F));
            return;
        }
        setPMRRotation(value, null);
    }

    public float getPitch() {
        if (getHandle().getFloat().size() >= 2) return getHandle().getFloat().read(1);
        if (getHandle().getBytes().size() >= 2) return (getHandle().getBytes().read(1) * 360.0F) / 256.0F;
        if (!hasPMR()) return 0.0F;
        Object pmr = safeRead(getPMRModifier(), 0);
        return pmrPitch(pmr);
    }

    public void setPitch(float value) {
        if (getHandle().getFloat().size() >= 2) {
            getHandle().getFloat().write(1, value);
            return;
        }
        if (getHandle().getBytes().size() >= 2) {
            getHandle().getBytes().write(1, (byte) (value * 256.0F / 360.0F));
            return;
        }
        setPMRRotation(null, value);
    }

    public boolean getOnGround() {
        return getHandle().getBooleans().read(0);
    }

    public void setOnGround(boolean value) {
        getHandle().getBooleans().write(0, value);
    }

    private void setPMRPosition(Double x, Double y, Double z) {
        if (!hasPMR()) return;
        StructureModifier<Object> m = getPMRModifier();
        Object pmr = safeRead(m, 0);
        if (pmr == null) {
            ensurePMRIfNeeded();
            pmr = safeRead(m, 0);
        }
        Object pos = pmrPos(pmr);
        Object delta = pmrDelta(pmr);
        if (pos == null) pos = newVec3(0.0, 0.0, 0.0);
        if (delta == null) delta = newVec3(0.0, 0.0, 0.0);

        double nx = x != null ? x : vecX(pos);
        double ny = y != null ? y : vecY(pos);
        double nz = z != null ? z : vecZ(pos);

        Object newPos = newVec3(nx, ny, nz);
        Object newPmr = newPMR(newPos, delta, pmrYaw(pmr), pmrPitch(pmr));
        m.write(0, newPmr);
    }

    private void setPMRDelta(Double x, Double y, Double z) {
        if (!hasPMR()) return;
        StructureModifier<Object> m = getPMRModifier();
        Object pmr = safeRead(m, 0);
        if (pmr == null) {
            ensurePMRIfNeeded();
            pmr = safeRead(m, 0);
        }
        Object pos = pmrPos(pmr);
        Object delta = pmrDelta(pmr);
        if (pos == null) pos = newVec3(0.0, 0.0, 0.0);
        if (delta == null) delta = newVec3(0.0, 0.0, 0.0);

        double nx = x != null ? x : vecX(delta);
        double ny = y != null ? y : vecY(delta);
        double nz = z != null ? z : vecZ(delta);

        Object newDelta = newVec3(nx, ny, nz);
        Object newPmr = newPMR(pos, newDelta, pmrYaw(pmr), pmrPitch(pmr));
        m.write(0, newPmr);
    }

    private void setPMRRotation(Float yaw, Float pitch) {
        if (!hasPMR()) return;
        StructureModifier<Object> m = getPMRModifier();
        Object pmr = safeRead(m, 0);
        if (pmr == null) {
            ensurePMRIfNeeded();
            pmr = safeRead(m, 0);
        }
        Object pos = pmrPos(pmr);
        Object delta = pmrDelta(pmr);
        if (pos == null) pos = newVec3(0.0, 0.0, 0.0);
        if (delta == null) delta = newVec3(0.0, 0.0, 0.0);

        float nyaw = yaw != null ? yaw : pmrYaw(pmr);
        float npitch = pitch != null ? pitch : pmrPitch(pmr);

        Object newPmr = newPMR(pos, delta, nyaw, npitch);
        m.write(0, newPmr);
    }
}
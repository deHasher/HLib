package net.dehasher.hlib;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Vector3D {
    public final double x;
    public final double y;
    public final double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Location location) {
        this(location.toVector());
    }

    public Vector3D(Vector vector) {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
    }

    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    public Vector3D subtract(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D multiply(int factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D multiply(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D divide(int divisor) {
        return new Vector3D(x / divisor, y / divisor, z / divisor);
    }

    public Vector3D divide(double divisor) {
        return new Vector3D(x / divisor, y / divisor, z / divisor);
    }

    public Vector3D abs() {
        return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    public String toString() {
        return String.format("[x: %s, y: %s, z: %s]", x, y, z);
    }
}
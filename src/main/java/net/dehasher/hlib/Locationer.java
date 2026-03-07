package net.dehasher.hlib;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.NumberConversions;
import static com.google.common.base.Preconditions.checkArgument;

public final class Locationer {
    private static final BlockFace[] LEFT_ROTATION, RIGHT_ROTATION;
    private static final BlockFace[] CARDINAL = {
            BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST
    };

    private static final BlockFace[] DIAGONAL = {
            BlockFace.SOUTH, BlockFace.SOUTH_WEST,
            BlockFace.WEST, BlockFace.NORTH_WEST,
            BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST
    };

    static {
        BlockFace[] values = BlockFace.values();
        LEFT_ROTATION = new BlockFace[values.length];
        LEFT_ROTATION[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        LEFT_ROTATION[BlockFace.UP.ordinal()] = BlockFace.UP;
        LEFT_ROTATION[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        LEFT_ROTATION[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        LEFT_ROTATION[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        LEFT_ROTATION[BlockFace.NORTH_WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        LEFT_ROTATION[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.WEST;
        LEFT_ROTATION[BlockFace.WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        LEFT_ROTATION[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        LEFT_ROTATION[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        LEFT_ROTATION[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH;
        LEFT_ROTATION[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        LEFT_ROTATION[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        LEFT_ROTATION[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        LEFT_ROTATION[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.EAST;
        LEFT_ROTATION[BlockFace.EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        LEFT_ROTATION[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        LEFT_ROTATION[BlockFace.NORTH_EAST.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        LEFT_ROTATION[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH;

        RIGHT_ROTATION = new BlockFace[values.length];
        RIGHT_ROTATION[BlockFace.SELF.ordinal()] = BlockFace.SELF;
        RIGHT_ROTATION[BlockFace.UP.ordinal()] = BlockFace.UP;
        RIGHT_ROTATION[BlockFace.DOWN.ordinal()] = BlockFace.DOWN;
        RIGHT_ROTATION[BlockFace.NORTH.ordinal()] = BlockFace.NORTH_NORTH_EAST;
        RIGHT_ROTATION[BlockFace.NORTH_NORTH_EAST.ordinal()] = BlockFace.NORTH_EAST;
        RIGHT_ROTATION[BlockFace.NORTH_EAST.ordinal()] = BlockFace.EAST_NORTH_EAST;
        RIGHT_ROTATION[BlockFace.EAST_NORTH_EAST.ordinal()] = BlockFace.EAST;
        RIGHT_ROTATION[BlockFace.EAST.ordinal()] = BlockFace.EAST_SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.EAST_SOUTH_EAST.ordinal()] = BlockFace.SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.SOUTH_EAST.ordinal()] = BlockFace.SOUTH_SOUTH_EAST;
        RIGHT_ROTATION[BlockFace.SOUTH_SOUTH_EAST.ordinal()] = BlockFace.SOUTH;
        RIGHT_ROTATION[BlockFace.SOUTH.ordinal()] = BlockFace.SOUTH_SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.SOUTH_SOUTH_WEST.ordinal()] = BlockFace.SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.SOUTH_WEST.ordinal()] = BlockFace.WEST_SOUTH_WEST;
        RIGHT_ROTATION[BlockFace.WEST_SOUTH_WEST.ordinal()] = BlockFace.WEST;
        RIGHT_ROTATION[BlockFace.WEST.ordinal()] = BlockFace.WEST_NORTH_WEST;
        RIGHT_ROTATION[BlockFace.WEST_NORTH_WEST.ordinal()] = BlockFace.NORTH_WEST;
        RIGHT_ROTATION[BlockFace.NORTH_WEST.ordinal()] = BlockFace.NORTH_NORTH_WEST;
        RIGHT_ROTATION[BlockFace.NORTH_NORTH_WEST.ordinal()] = BlockFace.NORTH;
    }

    public static Location center(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }

    public static boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) && loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean isChunkLoaded(Location loc) {
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static double distanceSquared2D(Location loc1, Location loc2) {
        return NumberConversions.square(loc1.getX() - loc2.getX()) + NumberConversions.square(loc1.getZ() - loc2.getZ());
    }

    public static BlockFace getDirection(Location loc) {
        return getDirection(loc, true, false);
    }

    public static BlockFace getCardinalDirection(Location loc) {
        return getDirection(loc, false, false);
    }

    public static BlockFace getVerticalDirection(Location loc) {
        return getDirection(loc, false, true);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static BlockFace getDirection(Location loc, boolean diagonal, boolean vertical) {
        if (vertical) {
            float pitch = loc.getPitch();
            if (pitch < -70) return BlockFace.UP;
            if (pitch > 70) return BlockFace.DOWN;
        }

        BlockFace[] directions = diagonal ? DIAGONAL : CARDINAL;
        return directions[Math.round(loc.getYaw() / (360 / directions.length)) & directions.length - 1];
    }

    public static float getDifference(BlockFace from, BlockFace to) {
        if (from == to) return 0F;
        if (from.getOppositeFace() == to) return 180F;

        float degrees = 22.5F;
        while ((from = rotateRight(from)) != to) degrees += 22.5F;

        return degrees;
    }

    public static BlockFace rotate(BlockFace face, float degrees) {
        return rotate(face, degrees, false, false);
    }

    public static BlockFace rotate(BlockFace face, float degrees, boolean xAxis, boolean zAxis) {
        if (face == BlockFace.SELF || degrees == 0) return face;

        float dRotations = degrees / 22.5F;
        int rotations = (int) dRotations;
        if (dRotations > rotations) {
            rotations++;
        } else if (dRotations < rotations) {
            rotations--;
        }

        rotations %= 16;
        if (xAxis || zAxis) {

            rotations /= 4;
            if (rotations == 2) return face.getOppositeFace();
            if (rotations == 3 || rotations == -3) rotations /= -3;

            boolean positive = rotations == 1;
            switch (face) {
                case UP:
                    return xAxis ? positive ? BlockFace.NORTH : BlockFace.SOUTH : positive ? BlockFace.EAST : BlockFace.WEST;
                case NORTH:
                case EAST:
                    return positive ? BlockFace.DOWN : BlockFace.UP;
                case DOWN:
                    return xAxis ? positive ? BlockFace.SOUTH : BlockFace.NORTH : positive ? BlockFace.WEST : BlockFace.EAST;
                case SOUTH:
                case WEST:
                    return positive ? BlockFace.UP : BlockFace.DOWN;
            }
        }

        if (degrees > 0) return rotateRight(face, rotations);
        if (degrees < 0) return rotateLeft(face, rotations * -1);

        return face;
    }

    public static BlockFace rotateRight(BlockFace face) {
        return RIGHT_ROTATION[face.ordinal()];
    }

    public static BlockFace rotateRight(BlockFace face, int amount) {
        return rotate(RIGHT_ROTATION, face, amount);
    }

    public static BlockFace rotateLeft(BlockFace face) {
        return LEFT_ROTATION[face.ordinal()];
    }

    public static BlockFace rotateLeft(BlockFace face, int amount) {
        return rotate(LEFT_ROTATION, face, amount);
    }

    private static BlockFace rotate(BlockFace[] rotations, BlockFace face, int amount) {
        amount %= 16;
        if (amount == 0 || face == BlockFace.SELF || face == BlockFace.UP || face == BlockFace.DOWN) return face;
        if (amount == 1) return rotations[face.ordinal()];
        if (amount == 8) return face.getOppositeFace();

        checkArgument(amount > 1, "cannot have negative rotation: %s", amount);
        BlockFace rotated = face;
        for (int i = 0; i < amount; i++) rotated = rotations[rotated.ordinal()];

        return rotated;
    }
}
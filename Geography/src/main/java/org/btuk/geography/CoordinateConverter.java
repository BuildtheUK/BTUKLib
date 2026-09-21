package org.btuk.geography;

/**
 * Placeholder for bi-directional conversion between Minecraft coordinates and real-life coordinates.
 */
public class CoordinateConverter {

    private CoordinateConverter() {}

    public static Coordinate toRealLife(MinecraftCoordinate mc) {
        // Placeholder conversion logic
        return new Coordinate(mc.z() / 100000.0, mc.x() / 100000.0);
    }

    public static MinecraftCoordinate toMinecraft(Coordinate real) {
        // Placeholder conversion logic
        return new MinecraftCoordinate(real.longitude() * 100000.0, real.latitude() * 100000.0);
    }
}

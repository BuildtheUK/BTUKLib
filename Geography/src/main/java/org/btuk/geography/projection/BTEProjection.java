package org.btuk.geography.projection;

/**
 * Standard BuildTheEarth projection configured with default settings
 * (BTE conformal Dymaxion projection with vertical flip and scale factor 7318261.522857145).
 *
 * <p>Provides convenient methods to convert between Minecraft coordinates (X, Z)
 * and real-world geographic coordinates (longitude, latitude in degrees).</p>
 */
public final class BTEProjection implements GeographicProjection {

    /**
     * Default BTE scale factor along X and Y axes.
     */
    public static final double SCALE = 7318261.522857145;

    private static final BTEProjection INSTANCE = new BTEProjection();

    private final GeographicProjection projection;

    /**
     * Returns the singleton instance of the default BTE projection.
     */
    public static BTEProjection get() {
        return INSTANCE;
    }

    /**
     * Converts Minecraft coordinates (X, Z) to real-world geographic coordinates [longitude, latitude] in degrees.
     *
     * @param x Minecraft X coordinate
     * @param z Minecraft Z coordinate
     * @return array of [longitude, latitude] in degrees
     * @throws OutOfProjectionBoundsException if the coordinate is outside projection bounds
     */
    public static double[] toGeoCoordinates(double x, double z) throws OutOfProjectionBoundsException {
        return INSTANCE.toGeo(x, z);
    }

    /**
     * Converts real-world geographic coordinates (longitude, latitude in degrees) to Minecraft coordinates [x, z].
     *
     * @param longitude longitude in degrees (-180 to 180)
     * @param latitude  latitude in degrees (-90 to 90)
     * @return array of [x, z] in Minecraft block coordinates
     * @throws OutOfProjectionBoundsException if the coordinate is outside projection bounds
     */
    public static double[] fromGeoCoordinates(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return INSTANCE.fromGeo(longitude, latitude);
    }

    public BTEProjection() {
        this(new BTEDymaxionProjection());
    }

    public BTEProjection(GeographicProjection base) {
        this.projection = new ScaleProjectionTransform(
                new FlipVerticalProjectionTransform(base),
                SCALE,
                SCALE
        );
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.projection.toGeo(x, y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        return this.projection.fromGeo(longitude, latitude);
    }

    @Override
    public double[] bounds() {
        return this.projection.bounds();
    }

    @Override
    public boolean upright() {
        return this.projection.upright();
    }

    @Override
    public double metersPerUnit() {
        return this.projection.metersPerUnit();
    }

    @Override
    public String toString() {
        return "BTE Default Projection (" + this.projection + ")";
    }
}

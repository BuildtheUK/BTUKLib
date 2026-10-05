package org.btuk.geography.projection;

/**
 * Scales the projection's projected space up or down.
 * Multiplies x and y by their respective scale factors.
 */
public class ScaleProjectionTransform extends ProjectionTransform {
    private final double x;
    private final double y;

    public ScaleProjectionTransform(GeographicProjection delegate, double x, double y) {
        super(delegate);
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Projection scales should be finite");
        }
        if (x == 0 || y == 0) {
            throw new IllegalArgumentException("Projection scale cannot be 0!");
        }
        this.x = x;
        this.y = y;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(x / this.x, y / this.y);
    }

    @Override
    public double[] fromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(lon, lat);
        p[0] *= this.x;
        p[1] *= this.y;
        return p;
    }

    @Override
    public boolean upright() {
        return (this.y < 0) ^ this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        b[0] *= this.x;
        b[1] *= this.y;
        b[2] *= this.x;
        b[3] *= this.y;
        return b;
    }

    @Override
    public double metersPerUnit() {
        return this.delegate.metersPerUnit() / Math.sqrt((this.x * this.x + this.y * this.y) / 2);
    }

    @Override
    public String toString() {
        return "Scale (" + this.delegate + ") by " + this.x + ", " + this.y;
    }
}

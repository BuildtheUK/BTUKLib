package org.btuk.geography.projection;

/**
 * Mirrors the projection vertically: x' = x, y' = -y.
 */
public class FlipVerticalProjectionTransform extends ProjectionTransform {

    public FlipVerticalProjectionTransform(GeographicProjection delegate) {
        super(delegate);
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        return this.delegate.toGeo(x, -y);
    }

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] p = this.delegate.fromGeo(longitude, latitude);
        p[1] = -p[1];
        return p;
    }

    @Override
    public boolean upright() {
        return !this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        double[] b = this.delegate.bounds();
        return new double[]{ b[0], -b[3], b[2], -b[1] };
    }

    @Override
    public String toString() {
        return "Vertical Flip (" + this.delegate + ')';
    }
}

package org.btuk.geography.projection;

/**
 * Abstract decorator for geographic projection transforms.
 */
public abstract class ProjectionTransform implements GeographicProjection {
    protected final GeographicProjection delegate;

    public ProjectionTransform(GeographicProjection delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate cannot be null");
        }
        this.delegate = delegate;
    }

    public GeographicProjection delegate() {
        return this.delegate;
    }

    @Override
    public boolean upright() {
        return this.delegate.upright();
    }

    @Override
    public double[] bounds() {
        return this.delegate.bounds();
    }

    @Override
    public double metersPerUnit() {
        return this.delegate.metersPerUnit();
    }
}

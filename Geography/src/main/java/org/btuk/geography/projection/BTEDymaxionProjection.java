package org.btuk.geography.projection;

/**
 * Implementation of the BTE modified Dymaxion projection.
 */
public class BTEDymaxionProjection extends ConformalDymaxionProjection {

    protected static final double THETA = Math.toRadians(-150);
    protected static final double SIN_THETA = Math.sin(THETA);
    protected static final double COS_THETA = Math.cos(THETA);
    protected static final double BERING_X = -0.3420420960118339;
    protected static final double BERING_Y = -0.322211064085279;
    protected static final double ARCTIC_Y = -0.2;
    protected static final double ARCTIC_M = (ARCTIC_Y - MathUtils.ROOT3 * ARC / 4) / (BERING_X - -0.5 * ARC);
    protected static final double ARCTIC_B = ARCTIC_Y - ARCTIC_M * BERING_X;
    protected static final double ALEUTIAN_Y = -0.5000446805492526;
    protected static final double ALEUTIAN_XL = -0.5149231279757507;
    protected static final double ALEUTIAN_XR = -0.45;
    protected static final double ALEUTIAN_M = (BERING_Y - ALEUTIAN_Y) / (BERING_X - ALEUTIAN_XR);
    protected static final double ALEUTIAN_B = BERING_Y - ALEUTIAN_M * BERING_X;

    @Override
    public double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException {
        double[] c = super.fromGeo(longitude, latitude);
        double x = c[0];
        double y = c[1];

        boolean easia = this.isEurasianPart(x, y);

        y -= 0.75 * ARC * MathUtils.ROOT3;

        if (easia) {
            x += ARC;

            double t = x;
            x = COS_THETA * x - SIN_THETA * y;
            y = SIN_THETA * t + COS_THETA * y;
        } else {
            x -= ARC;
        }

        c[0] = y;
        c[1] = -x;
        return c;
    }

    @Override
    public double[] toGeo(double x, double y) throws OutOfProjectionBoundsException {
        boolean easia;
        if (y < 0) {
            easia = x > 0;
        } else if (y > ARC / 2) {
            easia = x > -MathUtils.ROOT3 * ARC / 2;
        } else {
            easia = y * -MathUtils.ROOT3 < x;
        }

        double t = x;
        x = -y;
        y = t;

        if (easia) {
            t = x;
            x = COS_THETA * x + SIN_THETA * y;
            y = COS_THETA * y - SIN_THETA * t;
            x -= ARC;
        } else {
            x += ARC;
        }

        y += 0.75 * ARC * MathUtils.ROOT3;

        if (easia != this.isEurasianPart(x, y)) {
            throw OutOfProjectionBoundsException.get();
        }

        return super.toGeo(x, y);
    }

    protected boolean isEurasianPart(double x, double y) {
        if (x > 0) {
            return false;
        }
        if (x < -0.5 * ARC) {
            return true;
        }

        if (y > MathUtils.ROOT3 * ARC / 4) {
            return x < 0;
        }

        if (y < ALEUTIAN_Y) {
            return y < (ALEUTIAN_Y + ALEUTIAN_XL) - x;
        }

        if (y > BERING_Y) {
            if (y < ARCTIC_Y) {
                return x < BERING_X;
            }
            return y < ARCTIC_M * x + ARCTIC_B;
        }

        return y > ALEUTIAN_M * x + ALEUTIAN_B;
    }

    @Override
    public double[] bounds() {
        return new double[]{ -1.5 * ARC * MathUtils.ROOT3, -1.5 * ARC, 3 * ARC, MathUtils.ROOT3 * ARC };
    }

    @Override
    public String toString() {
        return "BuildTheEarth Conformal Dymaxion";
    }
}

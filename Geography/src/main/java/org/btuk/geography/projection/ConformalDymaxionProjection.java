package org.btuk.geography.projection;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Implementation of the Dymaxion-like conformal projection.
 * Slightly modifies the Dymaxion projection to make it (almost) conformal.
 */
public class ConformalDymaxionProjection extends DymaxionProjection {
    protected static final double VECTOR_SCALE_FACTOR = 1.0d / 1.1473979730192934d;
    protected static final int SIDE_LENGTH = 256;

    private static final String RESOURCE_PATH = "conformal.gz";

    private static volatile InvertableVectorField inverseField;

    private static InvertableVectorField getInverseField() {
        if (inverseField == null) {
            synchronized (ConformalDymaxionProjection.class) {
                if (inverseField == null) {
                    inverseField = loadInverseField();
                }
            }
        }
        return inverseField;
    }

    private static InvertableVectorField loadInverseField() {
        double[][] vx = new double[SIDE_LENGTH + 1][];
        double[][] vy = new double[SIDE_LENGTH + 1][];
        for (int i = 0; i < SIDE_LENGTH + 1; i++) {
            vx[i] = new double[SIDE_LENGTH + 1 - i];
            vy[i] = new double[SIDE_LENGTH + 1 - i];
        }

        try (InputStream compressedStream = ConformalDymaxionProjection.class.getResourceAsStream(RESOURCE_PATH)) {
            if (compressedStream == null) {
                // Fallback to absolute resource path from root if needed
                InputStream fallback = ConformalDymaxionProjection.class.getResourceAsStream("/bte/projection/" + RESOURCE_PATH);
                if (fallback == null) {
                    throw new IllegalStateException("Missing builtin resource: " + RESOURCE_PATH);
                }
                loadFromStream(fallback, vx, vy);
            } else {
                loadFromStream(compressedStream, vx, vy);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load conformal projection data", e);
        }

        return new InvertableVectorField(vx, vy);
    }

    private static void loadFromStream(InputStream compressedStream, double[][] vx, double[][] vy) throws Exception {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(compressedStream))) {
            for (int v = 0; v < SIDE_LENGTH + 1; v++) {
                for (int u = 0; u < SIDE_LENGTH + 1 - v; u++) {
                    vx[u][v] = in.readDouble() * VECTOR_SCALE_FACTOR;
                    vy[u][v] = in.readDouble() * VECTOR_SCALE_FACTOR;
                }
            }
        }
    }

    protected final InvertableVectorField inverse = getInverseField();

    @Override
    protected double[] triangleTransform(double[] vec) {
        double[] c = super.triangleTransform(vec);

        double x = c[0];
        double y = c[1];

        c[0] /= ARC;
        c[1] /= ARC;

        c[0] += 0.5;
        c[1] += MathUtils.ROOT3 / 6;

        c = this.inverse.applyNewtonsMethod(x, y, c[0], c[1], 5);

        c[0] -= 0.5;
        c[1] -= MathUtils.ROOT3 / 6;

        c[0] *= ARC;
        c[1] *= ARC;

        return c;
    }

    @Override
    protected double[] inverseTriangleTransform(double x, double y) {
        x /= ARC;
        y /= ARC;

        x += 0.5;
        y += MathUtils.ROOT3 / 6;

        double[] c = this.inverse.getInterpolatedVector(x, y);
        return super.inverseTriangleTransform(c[0], c[1]);
    }

    @Override
    public double metersPerUnit() {
        return (40075017.0d / (2.0d * Math.PI)) / VECTOR_SCALE_FACTOR;
    }

    @Override
    public String toString() {
        return "Conformal Dymaxion";
    }

    protected static class InvertableVectorField {
        private final double[][] vx;
        private final double[][] vy;

        public InvertableVectorField(double[][] vx, double[][] vy) {
            this.vx = vx;
            this.vy = vy;
        }

        public double[] getInterpolatedVector(double x, double y) {
            x *= SIDE_LENGTH;
            y *= SIDE_LENGTH;

            double v = 2 * y / MathUtils.ROOT3;
            double u = x - v * 0.5;

            int u1 = (int) u;
            int v1 = (int) v;

            if (u1 < 0) {
                u1 = 0;
            } else if (u1 >= SIDE_LENGTH) {
                u1 = SIDE_LENGTH - 1;
            }

            if (v1 < 0) {
                v1 = 0;
            } else if (v1 >= SIDE_LENGTH - u1) {
                v1 = SIDE_LENGTH - u1 - 1;
            }

            double valx1;
            double valy1;
            double valx2;
            double valy2;
            double valx3;
            double valy3;
            double y3;
            double x3;

            double flip = 1;

            if (y < -MathUtils.ROOT3 * (x - u1 - v1 - 1) || v1 == SIDE_LENGTH - u1 - 1) {
                valx1 = this.vx[u1][v1];
                valy1 = this.vy[u1][v1];
                valx2 = this.vx[u1][v1 + 1];
                valy2 = this.vy[u1][v1 + 1];
                valx3 = this.vx[u1 + 1][v1];
                valy3 = this.vy[u1 + 1][v1];

                y3 = 0.5 * MathUtils.ROOT3 * v1;
                x3 = (u1 + 1) + 0.5 * v1;
            } else {
                valx1 = this.vx[u1][v1 + 1];
                valy1 = this.vy[u1][v1 + 1];
                valx2 = this.vx[u1 + 1][v1];
                valy2 = this.vy[u1 + 1][v1];
                valx3 = this.vx[u1 + 1][v1 + 1];
                valy3 = this.vy[u1 + 1][v1 + 1];

                flip = -1;
                y = -y;

                y3 = -(0.5 * MathUtils.ROOT3 * (v1 + 1));
                x3 = (u1 + 1) + 0.5 * (v1 + 1);
            }

            double w1 = -(y - y3) / MathUtils.ROOT3 - (x - x3);
            double w2 = 2 * (y - y3) / MathUtils.ROOT3;
            double w3 = 1 - w1 - w2;

            return new double[]{
                    valx1 * w1 + valx2 * w2 + valx3 * w3,
                    valy1 * w1 + valy2 * w2 + valy3 * w3,
                    (valx3 - valx1) * SIDE_LENGTH,
                    SIDE_LENGTH * flip * (2 * valx2 - valx1 - valx3) / MathUtils.ROOT3,
                    (valy3 - valy1) * SIDE_LENGTH,
                    SIDE_LENGTH * flip * (2 * valy2 - valy1 - valy3) / MathUtils.ROOT3
            };
        }

        public double[] applyNewtonsMethod(double expectedf, double expectedg, double xest, double yest, int iter) {
            for (int i = 0; i < iter; i++) {
                double[] c = this.getInterpolatedVector(xest, yest);

                double f = c[0] - expectedf;
                double g = c[1] - expectedg;
                double dfdx = c[2];
                double dfdy = c[3];
                double dgdx = c[4];
                double dgdy = c[5];

                double determinant = 1 / (dfdx * dgdy - dfdy * dgdx);

                xest -= determinant * (dgdy * f - dfdy * g);
                yest -= determinant * (-dgdx * f + dfdx * g);
            }

            return new double[]{ xest, yest };
        }
    }
}

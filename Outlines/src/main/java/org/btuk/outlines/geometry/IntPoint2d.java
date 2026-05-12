package org.btuk.outlines.geometry;

public record IntPoint2d(int x, int z) {
    public double distanceTo(IntPoint2d point2d) {
        long dx = (long) x - point2d.x;
        long dz = (long) z - point2d.z;

        return Math.sqrt(dx * dx + dz * dz);
    }
}

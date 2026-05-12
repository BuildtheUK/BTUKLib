package org.btuk.outlines.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import org.btuk.outlines.geometry.Outline;
import org.btuk.outlines.geometry.IntPoint2d;

public final class DrawParticles {

    private static final int PARTICLE_DRAW_RADIUS_SQUARED = 50 * 50;

    private static final float ESTIMATED_DISTANCE_BETWEEN_PARTICLES = 0.5f;

    private static final Particle PARTICLE_TYPE = Particle.DUST;

    public static void drawOutline(Player player, Outline outline) {
        var points = outline.points();
        int pointCount = points.size();

        if (pointCount == 0) {
            return;
        }

        World world = player.getWorld();
        Location location = player.getLocation();
        double playerX = location.getX();
        double playerZ = location.getZ();

        IntPoint2d point = outline.points().getFirst();

        if (pointCount == 1) {
            drawPoint(player, world, playerX, playerZ, point.x(), point.z());
            return;
        }

        if (outline.points().size() == 2) {
            drawLine(player, world, playerX, playerZ, point, points.get(1), true);
            return;
        }

        for (int i = 1; i < outline.points().size(); i++) {
            drawLine(player, world, playerX, playerZ, point, points.get(i), false);
            point = outline.points().get(i);
        }

        drawLine(player, world, playerX, playerZ, point, points.getFirst(), false);
    }

    public static void drawLine(Player player, World world, double playerX, double playerZ, IntPoint2d start, IntPoint2d end, boolean includeEnd) {

        double startX = start.x();
        double startZ = start.z();
        double endX = end.x();
        double endZ = end.z();

        double distance = start.distanceTo(end);

        // Always draw the starting point.
        drawPoint(player, world, playerX, playerZ, startX, startZ);

        if (distance == 0) {
            return;
        }

        if (distance < ESTIMATED_DISTANCE_BETWEEN_PARTICLES) {
            if (includeEnd) {
                drawPoint(player, world, playerX, playerZ, endX, endZ);
            }
            return;
        }

        int steps = (int) (distance / ESTIMATED_DISTANCE_BETWEEN_PARTICLES);

        double stepX = (endX - startX) / steps;
        double stepZ = (endZ - startZ) / steps;

        double currentX = startX + stepX;
        double currentZ = startZ + stepZ;

        for (int i = 1; i < steps; i++) {
            drawPoint(player, world, playerX, playerZ, currentX, currentZ);

            currentX += stepX;
            currentZ += stepZ;
        }

        if (includeEnd) {
            drawPoint(player, world, playerX, playerZ, end.x(), end.z());
        }
    }

    public static void drawPoint(Player player,  World world, double playerX, double playerZ, double particleX, double particleZ) {
        double dx = playerX - particleX;
        double dz = playerZ - particleZ;

        if ((dx * dx) + (dz * dz) <= PARTICLE_DRAW_RADIUS_SQUARED) {
            int y = 1 + world.getHighestBlockYAt((int) particleX, (int) particleZ);

            player.spawnParticle(PARTICLE_TYPE, particleX, y, particleZ, 1);
        }
    }
}

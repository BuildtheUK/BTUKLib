package org.btuk.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.btuk.geography.MinecraftCoordinate;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public final class WorldGuardUtils {

    private WorldGuardUtils() {}

    public static boolean exists(World world, String name) {
        RegionManager manager = getRegionManager(world);
        return manager != null && manager.hasRegion(name);
    }

    public static void createPolygonalRegion(World world, String name, List<MinecraftCoordinate> points, int minY, int maxY) {
        List<BlockVector2> bvPoints = points.stream()
                .map(p -> BlockVector2.at(p.x(), p.z()))
                .toList();

        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            throw new WorldGuardException("Could not get RegionManager for world: " + world.getName());
        }

        if (manager.hasRegion(name)) {
            throw new WorldGuardException("Region already exists: " + name);
        }

        ProtectedRegion region = new ProtectedPolygonalRegion(name, bvPoints, minY, maxY);
        manager.addRegion(region);
        save(manager);
    }

    public static void removeRegion(World world, String regionName) {
        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            throw new WorldGuardException("Could not get RegionManager for world: " + world.getName());
        }

        if (!manager.hasRegion(regionName)) {
            return;
        }

        manager.removeRegion(regionName);
        save(manager);
    }

    public static void addOwner(World world, String regionName, Player player) {
        ProtectedRegion region = getRegion(world, regionName);
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(player.getUniqueId());
        save(getRegionManager(world));
    }

    public static void addMember(World world, String regionName, Player player) {
        ProtectedRegion region = getRegion(world, regionName);
        DefaultDomain members = region.getMembers();
        members.addPlayer(player.getUniqueId());
        save(getRegionManager(world));
    }

    public static void removeOwner(World world, String regionName, Player player) {
        ProtectedRegion region = getRegion(world, regionName);
        DefaultDomain owners = region.getOwners();
        owners.removePlayer(player.getUniqueId());
        save(getRegionManager(world));
    }

    public static void removeMember(World world, String regionName, Player player) {
        ProtectedRegion region = getRegion(world, regionName);
        DefaultDomain members = region.getMembers();
        members.removePlayer(player.getUniqueId());
        save(getRegionManager(world));
    }


    private static ProtectedRegion getRegion(World world, String name) {
        RegionManager manager = getRegionManager(world);
        if (manager == null) {
            throw new WorldGuardException("Could not get RegionManager for world: " + world.getName());
        }

        ProtectedRegion region = manager.getRegion(name);
        if (region == null) {
            throw new WorldGuardException("Region not found: " + name);
        }
        return region;
    }

    private static RegionManager getRegionManager(World world) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    private static void save(RegionManager manager) {
        if (manager == null) {
            return;
        }

        try {
            manager.save();
        } catch (StorageException e) {
            throw new WorldGuardException("Failed to save WorldGuard regions", e);
        }
    }
}

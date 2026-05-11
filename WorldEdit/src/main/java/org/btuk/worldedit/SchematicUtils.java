package org.btuk.worldedit;

import com.fastasyncworldedit.core.extent.clipboard.DiskOptimizedClipboard;
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicReaderV3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.extern.java.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Utility for creating and pasting schematics.
 */
@Log
public final class SchematicUtils {

    private SchematicUtils() {
        // Private constructor.
    }

    /**
     * Async method to create a schematic.
     *
     * @param world  the world to create the schematic in
     * @param points the bounds of the schematic
     * @param minY   the minimum Y coordinate of the schematic
     * @param maxY   the maximum Y coordinate of the schematic
     * @return the schematic in {@link BuiltInClipboardFormat#FAST_V3} format as a CompletableFuture
     */
    public CompletableFuture<Schematic> createSchematic(World world, List<BlockVector2> points, int minY, int maxY) {
        return CompletableFuture.supplyAsync(() -> createSchematic(BuiltInClipboardFormat.FAST_V3, world, points, minY, maxY));
    }

    /**
     * Paste a schematic in a world.
     *
     * @param schematic the schematic to paste
     * @param world     the world to paste the schematic in
     * @param targetY   the Y coordinate of the position to paste the schematic at
     * @param minY      the minimum Y coordinate to include in the paste
     * @param maxY      the maximum Y coordinate to include in the paste
     * @return a CompletableFuture to paste the schematic
     */
    public CompletableFuture<Boolean> pasteSchematic(Schematic schematic, World world, int targetY, int minY, int maxY) {
        if (schematic.format() != BuiltInClipboardFormat.FAST_V3) {
            throw new IllegalArgumentException("Unsupported schematic format: " + schematic.format());
        }
        return CompletableFuture.supplyAsync(() -> pasteSchematicV3(schematic, world, targetY, minY, maxY));
    }

    private boolean pasteSchematicV3(Schematic schematic, World world, int targetY, int minY, int maxY) {
        Clipboard clipboard;

        UUID randomUUID = UUID.randomUUID();
        Polygonal2DRegion region = new Polygonal2DRegion(world, schematic.points(), schematic.minY(), schematic.maxY());

        try (InputStream inputStream = new ByteArrayInputStream(schematic.schematicData())) {

            FastSchematicReaderV3 reader = new FastSchematicReaderV3(inputStream);
            clipboard = reader.read(randomUUID, dimensions -> new DiskOptimizedClipboard(region, randomUUID));

        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not read schematic data, unable to paste schematic: " + e);
            return false;
        }

        int targetMinY = targetY + (minY - schematic.minY());
        int targetMaxY = targetY + (maxY - schematic.minY());

        Polygonal2DRegion allowedPasteRegion = new Polygonal2DRegion(world, schematic.points(), targetMinY, targetMaxY);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            editSession.setMask(new RegionMask(allowedPasteRegion));

            Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(region.getMinimumPoint())
                .build();
            Operations.complete(operation);
        }

        return true;
    }

    private Schematic createSchematic(BuiltInClipboardFormat schematicFormat, World world, List<BlockVector2> points, int minY, int maxY) {
        Schematic schematic;

        UUID randomUUID = UUID.randomUUID();

        Polygonal2DRegion region = new Polygonal2DRegion(world, points, minY, maxY);

        try (DiskOptimizedClipboard clipboard = new DiskOptimizedClipboard(region, randomUUID)) {

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                world, region, clipboard, region.getMinimumPoint()
            );

            forwardExtentCopy.setCopyingBiomes(true);
            forwardExtentCopy.setCopyingEntities(true);

            Operations.complete(forwardExtentCopy);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (ClipboardWriter writer = schematicFormat.getWriter(outputStream)) {
                writer.write(clipboard);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Could not write clipboard to file, unable to create schematic: ", e);
                return null;
            }

            schematic = new Schematic(outputStream.toByteArray(), schematicFormat, points, minY, maxY);
        }

        return schematic;
    }
}

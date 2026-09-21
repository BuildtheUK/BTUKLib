package org.btuk.worldedit;

import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import org.btuk.geography.MinecraftCoordinate;

import java.util.List;

public record Schematic(byte[] schematicData, BuiltInClipboardFormat format, List<MinecraftCoordinate> points, int minY, int maxY){

}
package org.btuk.worldedit;

import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.math.BlockVector2;

import java.util.List;


public record Schematic(byte[] schematicData, BuiltInClipboardFormat format, List<BlockVector2> points, int minY, int maxY){

}
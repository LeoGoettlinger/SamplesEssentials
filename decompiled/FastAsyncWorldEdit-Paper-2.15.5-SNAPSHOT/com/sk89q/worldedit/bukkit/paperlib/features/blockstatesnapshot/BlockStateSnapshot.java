package com.sk89q.worldedit.bukkit.paperlib.features.blockstatesnapshot;

import org.bukkit.block.Block;

public interface BlockStateSnapshot {
   BlockStateSnapshotResult getBlockState(Block var1, boolean var2);
}

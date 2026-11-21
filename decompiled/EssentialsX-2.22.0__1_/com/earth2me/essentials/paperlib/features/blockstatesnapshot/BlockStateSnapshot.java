package com.earth2me.essentials.paperlib.features.blockstatesnapshot;

import org.bukkit.block.Block;

public interface BlockStateSnapshot {
   BlockStateSnapshotResult getBlockState(Block var1, boolean var2);
}

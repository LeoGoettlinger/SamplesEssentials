package com.artillexstudios.axrankmenu.libs.axapi.selection;

import com.artillexstudios.axrankmenu.libs.axapi.libs.math3.distribution.EnumeratedDistribution;
import java.util.function.IntConsumer;
import org.bukkit.block.data.BlockData;

public interface ParallelBlockSetter {
   void fill(Cuboid cuboid, EnumeratedDistribution<BlockData> distribution, IntConsumer consumer);
}

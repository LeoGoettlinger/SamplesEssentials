package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector2;

public interface Mask2D {
   boolean test(BlockVector2 vector);

   Mask2D copy2D();
}

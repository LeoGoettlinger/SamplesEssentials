package com.magmaguy.elitemobs.magmacore.instance;

import org.bukkit.block.Block;

@FunctionalInterface
public interface ContainerAllowlist {
   boolean isAllowed(Block var1);
}

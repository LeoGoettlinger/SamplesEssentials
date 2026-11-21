package com.skyblockexp.ezrtp.platform;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;

public interface ChunkLoadStrategy {
   CompletableFuture<Chunk> loadChunk(World var1, int var2, int var3);
}

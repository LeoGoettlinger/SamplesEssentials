package com.lishid.openinv.internal.paper26_1.player;

import java.util.logging.Logger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerManager extends com.lishid.openinv.internal.paper26_2.player.PlayerManager {
   public PlayerManager(Logger logger) {
      super(logger);
   }

   @Override
   protected void removeAdvancementListeners(ServerPlayer entity) {
      entity.getAdvancements().stopListening();
   }

   @Override
   protected RespawnData getRespawnData(ServerLevel level) {
      return level.levelData.getRespawnData();
   }

   @Override
   protected Vec3 getAdjustedSpawnLocation(ServerPlayer player, ServerLevel level, RespawnData respawnData) {
      return player.adjustSpawnLocation(level, respawnData.pos()).getBottomCenter();
   }

   @Override
   protected void setServerLevel(ServerPlayer player, ServerLevel level) {
      player.spawnIn(level);
   }
}

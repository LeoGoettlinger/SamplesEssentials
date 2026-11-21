package com.sk89q.worldedit.world.gamemode;

import javax.annotation.Nullable;

public final class GameModes {
   public static final GameMode SURVIVAL = register("survival");
   public static final GameMode CREATIVE = register("creative");
   public static final GameMode ADVENTURE = register("adventure");
   public static final GameMode SPECTATOR = register("spectator");

   private GameModes() {
   }

   private static GameMode register(final String id) {
      return register(new GameMode(id));
   }

   public static GameMode register(final GameMode gameMode) {
      return GameMode.REGISTRY.register(gameMode.id(), gameMode);
   }

   @Nullable
   public static GameMode get(final String id) {
      return GameMode.REGISTRY.get(id);
   }
}

package com.viaversion.viabackwards.protocol.v1_13to1_12_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_13 extends ProtocolStorablesBase {
   private final BackwardsBlockStorage backwardsBlockStorage = new BackwardsBlockStorage();
   private final TabCompleteStorage tabCompleteStorage = new TabCompleteStorage();
   private final NoteBlockStorage noteBlockStorage = new NoteBlockStorage();
   private PlayerPositionStorage1_13 playerPositionStorage;

   public BackwardsBlockStorage backwardsBlockStorage() {
      return this.backwardsBlockStorage;
   }

   public TabCompleteStorage tabCompleteStorage() {
      return this.tabCompleteStorage;
   }

   public NoteBlockStorage noteBlockStorage() {
      return this.noteBlockStorage;
   }

   @Nullable
   public PlayerPositionStorage1_13 playerPositionStorage() {
      return this.playerPositionStorage;
   }

   public void setPlayerPositionStorage(PlayerPositionStorage1_13 playerPositionStorage) {
      this.playerPositionStorage = playerPositionStorage;
   }
}

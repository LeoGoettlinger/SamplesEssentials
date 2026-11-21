package com.viaversion.viabackwards.protocol.v1_16to1_15_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_16 extends ProtocolStorablesBase {
   private final PlayerSneakStorage playerSneakStorage = new PlayerSneakStorage();
   private final WorldNameTracker worldNameTracker = new WorldNameTracker();
   private final PlayerAttributesStorage playerAttributesStorage = new PlayerAttributesStorage();

   public PlayerSneakStorage playerSneakStorage() {
      return this.playerSneakStorage;
   }

   public WorldNameTracker worldNameTracker() {
      return this.worldNameTracker;
   }

   public PlayerAttributesStorage playerAttributesStorage() {
      return this.playerAttributesStorage;
   }
}

package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ClientVehicleStorage;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BackwardsStorables1_21_2 extends ProtocolStorablesBase {
   private final InventoryStateIdStorage inventoryStateIdStorage = new InventoryStateIdStorage();
   private final ItemTagStorage itemTagStorage = new ItemTagStorage();
   private final PlayerStorage playerStorage = new PlayerStorage();
   private SignStorage signStorage = new SignStorage();
   private RecipeStorage recipeStorage;
   private ClientVehicleStorage clientVehicleStorage;

   public InventoryStateIdStorage inventoryStateIdStorage() {
      return this.inventoryStateIdStorage;
   }

   public ItemTagStorage itemTagStorage() {
      return this.itemTagStorage;
   }

   public PlayerStorage playerStorage() {
      return this.playerStorage;
   }

   public SignStorage signStorage() {
      return this.signStorage;
   }

   public void setSignStorage(SignStorage signStorage) {
      this.signStorage = signStorage;
   }

   public RecipeStorage recipeStorage() {
      return this.recipeStorage;
   }

   public void setRecipeStorage(RecipeStorage recipeStorage) {
      this.recipeStorage = recipeStorage;
   }

   @Nullable
   public ClientVehicleStorage clientVehicleStorage() {
      return this.clientVehicleStorage;
   }

   public void setClientVehicleStorage(@Nullable ClientVehicleStorage clientVehicleStorage) {
      this.clientVehicleStorage = clientVehicleStorage;
   }
}

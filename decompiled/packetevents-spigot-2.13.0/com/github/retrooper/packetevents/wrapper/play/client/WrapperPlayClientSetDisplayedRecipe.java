package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayId;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class WrapperPlayClientSetDisplayedRecipe extends PacketWrapper<WrapperPlayClientSetDisplayedRecipe> {
   @Obsolete
   private ResourceLocation recipe;
   private RecipeDisplayId recipeId;

   public WrapperPlayClientSetDisplayedRecipe(PacketReceiveEvent event) {
      super(event);
   }

   @Obsolete
   public WrapperPlayClientSetDisplayedRecipe(ResourceLocation recipe) {
      super(PacketType.Play.Client.SET_DISPLAYED_RECIPE);
      this.recipe = recipe;
   }

   public WrapperPlayClientSetDisplayedRecipe(RecipeDisplayId recipeId) {
      super(PacketType.Play.Client.SET_DISPLAYED_RECIPE);
      this.recipeId = recipeId;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         this.recipeId = RecipeDisplayId.read(this);
      } else {
         this.recipe = this.readIdentifier();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         RecipeDisplayId.write(this, this.recipeId);
      } else {
         this.writeIdentifier(this.recipe);
      }
   }

   public void copy(WrapperPlayClientSetDisplayedRecipe packet) {
      this.recipe = packet.recipe;
      this.recipeId = packet.recipeId;
   }

   @Obsolete
   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   @Obsolete
   public void setRecipe(ResourceLocation recipe) {
      this.recipe = recipe;
   }

   public RecipeDisplayId getRecipeId() {
      return this.recipeId;
   }

   public void setRecipeId(RecipeDisplayId recipeId) {
      this.recipeId = recipeId;
   }
}

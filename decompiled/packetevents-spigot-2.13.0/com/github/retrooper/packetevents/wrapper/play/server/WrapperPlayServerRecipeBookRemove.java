package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.recipe.RecipeDisplayId;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.List;

public class WrapperPlayServerRecipeBookRemove extends PacketWrapper<WrapperPlayServerRecipeBookRemove> {
   private List<RecipeDisplayId> recipeIds;

   public WrapperPlayServerRecipeBookRemove(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerRecipeBookRemove(List<RecipeDisplayId> recipeIds) {
      super(PacketType.Play.Server.RECIPE_BOOK_REMOVE);
      this.recipeIds = recipeIds;
   }

   @Override
   public void read() {
      this.recipeIds = this.readList(RecipeDisplayId::read);
   }

   @Override
   public void write() {
      this.writeList(this.recipeIds, RecipeDisplayId::write);
   }

   public void copy(WrapperPlayServerRecipeBookRemove wrapper) {
      this.recipeIds = wrapper.recipeIds;
   }

   public List<RecipeDisplayId> getRecipeIds() {
      return this.recipeIds;
   }

   public void setRecipeIds(List<RecipeDisplayId> recipeIds) {
      this.recipeIds = recipeIds;
   }
}

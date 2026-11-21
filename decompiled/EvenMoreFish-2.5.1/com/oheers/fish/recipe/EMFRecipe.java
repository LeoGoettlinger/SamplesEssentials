package com.oheers.fish.recipe;

import org.bukkit.Bukkit;
import org.bukkit.inventory.CraftingRecipe;
import org.jspecify.annotations.NonNull;

public abstract class EMFRecipe<R extends CraftingRecipe> {
   protected boolean registered = false;
   protected R recipe;

   protected EMFRecipe() {
   }

   public boolean isRegistered() {
      return this.registered;
   }

   public void register() {
      if (this.isRegistered()) {
         throw new RuntimeException("Attempted to register a recipe that is already registered.");
      } else {
         if (this.recipe == null) {
            this.recipe = this.prepareRecipe();
         }

         Bukkit.addRecipe(this.recipe);
         this.registered = true;
      }
   }

   public void unregister() {
      if (!this.isRegistered()) {
         throw new RuntimeException("Attempted to unregister a recipe that is not registered.");
      } else {
         Bukkit.removeRecipe(this.recipe.getKey());
         this.registered = false;
      }
   }

   @NonNull
   protected abstract R prepareRecipe();
}

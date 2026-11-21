package me.usainsrht.ujobs.models;

public class BuiltInActions {
   public static Action get(String name) {
      for (BuiltInActions.Entity entity : BuiltInActions.Entity.values()) {
         if (entity.getName().equalsIgnoreCase(name)) {
            return entity;
         }
      }

      for (BuiltInActions.Material material : BuiltInActions.Material.values()) {
         if (material.getName().equalsIgnoreCase(name)) {
            return material;
         }
      }

      for (BuiltInActions.Special special : BuiltInActions.Special.values()) {
         if (special.getName().equalsIgnoreCase(name)) {
            return special;
         }
      }

      return null;
   }

   public static enum Entity implements EntityAction {
      KILL("kill"),
      BREED("breed"),
      TAME("tame");

      final String name;

      private Entity(String name) {
         this.name = name;
      }

      @Override
      public String getName() {
         return this.name;
      }
   }

   public static enum Material implements MaterialAction {
      BREAK("break"),
      PLACE("place"),
      FISH("fish"),
      ANVIL_MERGE("anvil_merge"),
      CRAFT("craft"),
      TRADE("trade"),
      HARVEST("harvest"),
      SMELT("smelt");

      final String name;

      private Material(String name) {
         this.name = name;
      }

      @Override
      public String getName() {
         return this.name;
      }
   }

   public static enum Special implements SpecialAction {
      ENCHANT("enchant"),
      GENERATE_LOOT("generate_loot"),
      RAID("raid");

      final String name;

      private Special(String name) {
         this.name = name;
      }

      @Override
      public String getName() {
         return this.name;
      }
   }
}

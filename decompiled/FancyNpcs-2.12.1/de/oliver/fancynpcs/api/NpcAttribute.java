package de.oliver.fancynpcs.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.bukkit.entity.EntityType;

public class NpcAttribute {
   private final String name;
   private final List<String> possibleValues;
   private final Supplier<List<String>> possibleValuesSupplier;
   private final List<EntityType> types;
   private final BiConsumer<Npc, String> applyFunc;

   public NpcAttribute(String name, List<String> possibleValues, List<EntityType> types, BiConsumer<Npc, String> applyFunc) {
      this.name = name;
      this.possibleValues = possibleValues;
      this.possibleValuesSupplier = List::of;
      this.types = types;
      this.applyFunc = applyFunc;
   }

   public NpcAttribute(String name, Supplier<List<String>> possibleValuesSupplier, List<EntityType> types, BiConsumer<Npc, String> applyFunc) {
      this.name = name;
      this.possibleValues = List.of();
      this.possibleValuesSupplier = possibleValuesSupplier;
      this.types = types;
      this.applyFunc = applyFunc;
   }

   public boolean isValidValue(String value) {
      List<String> fromSupplier = this.possibleValuesSupplier.get();
      if (this.possibleValues.isEmpty() && fromSupplier.isEmpty()) {
         return true;
      } else {
         for (String pv : this.possibleValues) {
            if (pv.equalsIgnoreCase(value)) {
               return true;
            }
         }

         for (String pvx : fromSupplier) {
            if (pvx.equalsIgnoreCase(value)) {
               return true;
            }
         }

         return false;
      }
   }

   public void apply(Npc npc, String value) {
      this.applyFunc.accept(npc, value);
   }

   public String getName() {
      return this.name;
   }

   public List<String> getPossibleValues() {
      List<String> values = new ArrayList<>();
      values.addAll(this.possibleValuesSupplier.get());
      values.addAll(this.possibleValues);
      return values;
   }

   public List<EntityType> getTypes() {
      return this.types;
   }
}

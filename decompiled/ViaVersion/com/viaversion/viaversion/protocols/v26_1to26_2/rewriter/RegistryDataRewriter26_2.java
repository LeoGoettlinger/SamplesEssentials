package com.viaversion.viaversion.protocols.v26_1to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class RegistryDataRewriter26_2 extends RegistryDataRewriter {
   public RegistryDataRewriter26_2(Protocol26_1To26_2 protocol) {
      super(protocol);
   }

   @Override
   public void updateEnchantmentTerm(CompoundTag term) {
      super.updateEnchantmentTerm(term);
      String condition = term.getString("condition");
      if (Key.equals(condition, "entity_properties")) {
         CompoundTag predicate = term.getCompoundTag("predicate");
         if (predicate != null) {
            this.updateEntityPredicate(predicate);
         }
      } else if (Key.equals(condition, "damage_source_properties")) {
         CompoundTag predicate = term.getCompoundTag("predicate");
         if (predicate != null) {
            this.updateNestedEntityPredicate(predicate, "source_entity");
            this.updateNestedEntityPredicate(predicate, "direct_entity");
         }
      }
   }

   private void updateEntityPredicate(CompoundTag predicate) {
      CompoundTag typeSpecific = predicate.removeUnchecked("type_specific");
      if (typeSpecific != null) {
         this.updateTypeSpecificTerm(predicate, typeSpecific);
      }

      Tag typeTag = predicate.remove("type");
      if (typeTag != null) {
         predicate.put("entity_type", typeTag);
      }

      this.updateNestedEntityPredicate(predicate, "vehicle");
      this.updateNestedEntityPredicate(predicate, "passenger");
      this.updateNestedEntityPredicate(predicate, "targeted_entity");
   }

   private void updateNestedEntityPredicate(CompoundTag predicate, String key) {
      CompoundTag nestedPredicate = predicate.getCompoundTag(key);
      if (nestedPredicate != null) {
         this.updateEntityPredicate(nestedPredicate);
      }
   }

   private void updateTypeSpecificTerm(CompoundTag predicate, CompoundTag typeSpecific) {
      StringTag type = typeSpecific.removeUnchecked("type");
      if (type != null) {
         String strippedType = Key.stripMinecraftNamespace(type.getValue());
         switch (strippedType) {
            case "player":
            case "lightning":
            case "fishing_hook":
            case "raider":
            case "sheep":
               predicate.put("type_specific/" + strippedType, typeSpecific);
               break;
            case "slime":
               predicate.put("type_specific/cube_mob", typeSpecific);
         }
      }
   }
}

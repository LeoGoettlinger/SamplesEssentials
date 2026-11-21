package com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.protocol.v26_3to26_2.Protocol26_3To26_2;
import com.viaversion.viaversion.util.Key;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class RegistryDataRewriter26_3 extends BackwardsRegistryRewriter {
   public RegistryDataRewriter26_3(Protocol26_3To26_2 protocol) {
      super(protocol);
   }

   public void updateEnchantmentTerm(CompoundTag term) {
      StringTag typeTag = (StringTag)term.removeUnchecked("type");
      if (typeTag != null) {
         term.put("condition", typeTag);
      }

      String type = typeTag != null ? typeTag.getValue() : null;
      if (term.get("terms") instanceof CompoundTag inlineTerm) {
         ListTag<CompoundTag> termsList = new ListTag(CompoundTag.class);
         termsList.add(inlineTerm);
         term.put("terms", termsList);
      }

      if (Key.equals(type, "damage_source_properties")) {
         CompoundTag predicate = term.getCompoundTag("predicate");
         if (predicate != null) {
            ListTag<CompoundTag> tags = predicate.getListTag("tags", CompoundTag.class);
            if (tags != null) {
               this.updateTagKey(tags);
            }
         }
      } else if (Key.equals(type, "match_block")) {
         this.updateMatchBlock(term);
      } else if (Key.equals(type, "int_value_check") || Key.equals(type, "float_value_check")) {
         term.putString("condition", "minecraft:value_check");
         if (!convert(term, "value", "value", RegistryDataRewriter26_3::toNumberProvider)
            || !convert(term, "test", "range", RegistryDataRewriter26_3::toIntRange)) {
            replaceWithAlwaysTrue(term);
         }
      } else if (Key.equals(type, "time_check")) {
         if (!convert(term, "value", "value", RegistryDataRewriter26_3::toIntRange)) {
            replaceWithAlwaysTrue(term);
         }
      } else if (Key.equals(type, "random_chance") && !convert(term, "chance", "chance", RegistryDataRewriter26_3::toNumberProvider)) {
         replaceWithAlwaysTrue(term);
      }

      super.updateEnchantmentTerm(term);
   }

   public boolean updateBlockStateProvider(CompoundTag tag) {
      String type = tag.getString("type");
      if (type == null) {
         CompoundTag stateTag = tag.copy();
         this.handleFullBlockState(stateTag);
         tag.put("state", stateTag);
         tag.putString("type", "simple_state_provider");
         return true;
      } else {
         type = Key.stripMinecraftNamespace(type);
         switch (type) {
            case "simple_state_provider":
            case "rotated_block_provider":
               this.handleBlockState(tag, "state");
               break;
            case "weighted_state_provider":
               for (CompoundTag entry : tag.getListTag("entries", CompoundTag.class)) {
                  this.handleBlockState(entry, "data");
               }
               break;
            case "noise_threshold_provider":
               this.handleBlockState(tag, "default_state");
         }

         return super.updateBlockStateProvider(tag);
      }
   }

   protected void handleParticleData(CompoundTag particleData) {
      super.handleParticleData(particleData);
      if (particleData.get("block_state") instanceof CompoundTag blockState) {
         this.handleFullBlockState(blockState);
      }
   }

   private void handleBlockState(CompoundTag parent, String key) {
      Tag blockStateTag = parent.get(key);
      if (blockStateTag instanceof CompoundTag compoundTag) {
         this.handleFullBlockState(compoundTag);
      } else if (blockStateTag instanceof StringTag stringTag) {
         CompoundTag updatedBlockStateTag = new CompoundTag();
         updatedBlockStateTag.putString("Name", stringTag.getValue());
         parent.put(key, updatedBlockStateTag);
      }
   }

   private void handleFullBlockState(CompoundTag tag) {
      Tag id = tag.remove("id");
      tag.put("Name", id);
      Tag properties = tag.remove("properties");
      if (properties != null) {
         tag.put("Properties", properties);
      }
   }

   private void updateMatchBlock(CompoundTag term) {
      if (term.remove("blocks") instanceof StringTag block && !block.getValue().startsWith("#")) {
         term.putString("condition", "minecraft:block_state_property");
         term.put("block", block);
         Tag state = term.remove("state");
         if (state != null) {
            term.put("properties", state);
         }
      } else {
         replaceWithAlwaysTrue(term);
      }
   }

   private static void replaceWithAlwaysTrue(CompoundTag term) {
      term.clear();
      term.putString("condition", "minecraft:all_of");
      term.put("terms", new ListTag(CompoundTag.class));
   }

   @Nullable
   private static Tag toNumberProvider(Tag tag) {
      if (tag instanceof NumberTag) {
         return tag;
      } else if (tag instanceof CompoundTag provider) {
         String var2 = Key.stripMinecraftNamespace(provider.getString("type", ""));

         return (Tag)(switch (var2) {
            case "constant", "enchantment_level", "environment_attribute" -> provider;
            case "storage", "score" -> {
               provider.remove("fallback");
               yield provider;
            }
            case "uniform" -> convert(provider, "min", "min", RegistryDataRewriter26_3::toNumberProvider)
                  && convert(provider, "max", "max", RegistryDataRewriter26_3::toNumberProvider)
               ? provider
               : null;
            case "binomial" -> convert(provider, "n", "n", RegistryDataRewriter26_3::toNumberProvider)
                  && convert(provider, "p", "p", RegistryDataRewriter26_3::toNumberProvider)
               ? provider
               : null;
            case "add" -> {
               provider.putString("type", "minecraft:sum");
               yield convert(provider, "inputs", "summands", RegistryDataRewriter26_3::toNumberProviderList) ? provider : null;
            }
            case "from_int", "from_float", "round" -> {
               Tag input = provider.get("input");
               yield input != null ? toNumberProvider(input) : null;
            }
            default -> null;
         });
      } else {
         return null;
      }
   }

   // $VF: One or more variable merging failures!
   // $VF: Could not properly define all variable types!
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   @Nullable
   private static Tag toNumberProviderList(Tag tag) {
      ListTag<CompoundTag> list = new ListTag(CompoundTag.class);

      <unknown> var10000;
      for (Tag element : var10000_1) {
         Tag provider = toNumberProvider(element);
         if (provider == null) {
            return null;
         }

         if (provider instanceof CompoundTag compoundTag) {
            list.add(compoundTag);
         } else {
            CompoundTag constant = new CompoundTag();
            constant.putString("type", "minecraft:constant");
            constant.put("value", provider);
            list.add(constant);
         }
      }

      return list;
   }

   @Nullable
   private static Tag toIntRange(Tag tag) {
      if (tag instanceof NumberTag numberTag) {
         return (Tag)(tag instanceof IntTag ? tag : new IntTag(Math.round(numberTag.asFloat())));
      } else if (tag instanceof CompoundTag range) {
         if (range.contains("type")) {
            Tag value = toNumberProvider(range);
            if (value == null) {
               return null;
            } else {
               CompoundTag exactRange = new CompoundTag();
               exactRange.put("min", value);
               exactRange.put("max", value.copy());
               return exactRange;
            }
         } else {
            return convert(range, "min", "min", RegistryDataRewriter26_3::toNumberProvider)
                  && convert(range, "max", "max", RegistryDataRewriter26_3::toNumberProvider)
               ? range
               : null;
         }
      } else {
         return null;
      }
   }

   private static boolean convert(CompoundTag tag, String key, String newKey, Function<Tag, Tag> converter) {
      Tag value = tag.remove(key);
      if (value == null) {
         return true;
      } else {
         Tag converted = converter.apply(value);
         if (converted == null) {
            return false;
         } else {
            tag.put(newKey, converted);
            return true;
         }
      }
   }

   private void updateTagKey(ListTag<CompoundTag> tags) {
      for (CompoundTag tag : tags) {
         if (tag.get("id") instanceof StringTag tagKey) {
            tagKey.setValue(tagKey.getValue().substring(1));
         } else {
            tag.putString("id", "wool");
         }
      }
   }
}

package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.function.UnaryOperator;

public final class RegistryDataRewriter26_3 extends RegistryDataRewriter {
   public RegistryDataRewriter26_3(Protocol26_2To26_3 protocol) {
      super(protocol);
   }

   @Override
   public void updateEnchantmentTerm(CompoundTag term) {
      StringTag condition = term.removeUnchecked("condition");
      if (condition != null) {
         term.put("type", condition);
         updateNumberProviders(term, condition);
      }

      if (condition != null && Key.equals(condition.getValue(), "damage_source_properties")) {
         CompoundTag predicate = term.getCompoundTag("predicate");
         if (predicate != null) {
            ListTag<CompoundTag> tags = predicate.getListTag("tags", CompoundTag.class);
            if (tags != null) {
               this.updateTagKey(tags);
            }
         }
      }

      super.updateEnchantmentTerm(term);
      if (condition != null && Key.equals(condition.getValue(), "block_state_property")) {
         condition.setValue("minecraft:match_block");
         term.put("blocks", term.remove("block"));
         Tag properties = term.remove("properties");
         if (properties != null) {
            term.put("state", properties);
         }
      }
   }

   private static void updateNumberProviders(CompoundTag term, StringTag condition) {
      if (Key.equals(condition.getValue(), "value_check")) {
         condition.setValue("int_value_check");
         convert(term, "value", "value", RegistryDataRewriter26_3::toIntProvider);
         convert(term, "range", "test", RegistryDataRewriter26_3::updateIntRange);
      } else if (Key.equals(condition.getValue(), "time_check")) {
         convert(term, "value", "value", RegistryDataRewriter26_3::updateIntRange);
      } else if (Key.equals(condition.getValue(), "random_chance")) {
         convert(term, "chance", "chance", RegistryDataRewriter26_3::toFloatProvider);
      }
   }

   private static Tag updateIntRange(Tag range) {
      if (range instanceof CompoundTag rangeTag) {
         convert(rangeTag, "min", "min", RegistryDataRewriter26_3::toIntProvider);
         convert(rangeTag, "max", "max", RegistryDataRewriter26_3::toIntProvider);
      }

      return range;
   }

   private static Tag toIntProvider(Tag tag) {
      if (tag instanceof NumberTag numberTag) {
         return new IntTag(Math.round(numberTag.asFloat()));
      } else {
         if (tag instanceof CompoundTag provider) {
            String type = provider.getString("type");
            if (Key.equals(type, "binomial")) {
               convert(provider, "n", "n", RegistryDataRewriter26_3::toIntProvider);
               convert(provider, "p", "p", RegistryDataRewriter26_3::toFloatProvider);
               return provider;
            }

            if (Key.equals(type, "score") && provider.getFloat("scale", 1.0F) == 1.0F) {
               provider.remove("scale");
               return provider;
            }
         }

         return unary("from_float", unary("round", toFloatProvider(tag)));
      }
   }

   private static Tag toFloatProvider(Tag tag) {
      if (tag instanceof CompoundTag provider) {
         String var2 = Key.stripMinecraftNamespace(provider.getString("type", "uniform"));
         switch (var2) {
            case "uniform":
               provider.putString("type", "minecraft:uniform");
               convert(provider, "min", "min", RegistryDataRewriter26_3::toFloatProvider);
               convert(provider, "max", "max", RegistryDataRewriter26_3::toFloatProvider);
               break;
            case "sum":
               provider.putString("type", "minecraft:add");
               convert(provider, "summands", "inputs", summands -> {
                  ListTag<CompoundTag> inputsx = new ListTag<>(CompoundTag.class);
                  if (summands instanceof ListTag) {
                     for (Tag summand : (ListTag)summands) {
                        inputsx.add(toFloatProviderCompound(summand));
                     }
                  }

                  return inputsx;
               });
               break;
            case "binomial":
               return unary("from_int", toIntProvider(provider));
            case "score":
               Tag scale = provider.remove("scale");
               CompoundTag score = unary("from_int", provider);
               if (scale == null) {
                  return score;
               }

               ListTag<CompoundTag> inputs = new ListTag<>(CompoundTag.class);
               inputs.add(score);
               inputs.add(toFloatProviderCompound(scale));
               CompoundTag product = new CompoundTag();
               product.putString("type", "minecraft:mul");
               product.put("inputs", inputs);
               return product;
         }

         return provider;
      } else {
         return tag;
      }
   }

   private static CompoundTag toFloatProviderCompound(Tag tag) {
      Tag provider = toFloatProvider(tag);
      if (provider instanceof CompoundTag compoundTag) {
         return compoundTag;
      } else {
         CompoundTag constant = new CompoundTag();
         constant.putString("type", "minecraft:constant");
         constant.put("value", provider);
         return constant;
      }
   }

   private static CompoundTag unary(String type, Tag input) {
      CompoundTag provider = new CompoundTag();
      provider.putString("type", "minecraft:" + type);
      provider.put("input", input);
      return provider;
   }

   private static void convert(CompoundTag tag, String key, String newKey, UnaryOperator<Tag> converter) {
      Tag value = tag.remove(key);
      if (value != null) {
         tag.put(newKey, converter.apply(value));
      }
   }

   @Override
   public boolean updateBlockStateProvider(CompoundTag tag) {
      StringTag typeTag = tag.getStringTag("type");
      String strippedName = Key.stripMinecraftNamespace(typeTag.getValue());

      typeTag.setValue(switch (strippedName) {
         case "randomized_int_state_provider" -> "randomized_int";
         case "rotated_block_provider" -> "rotated";
         case "rule_based_state_provider" -> "rule_based";
         case "simple_state_provider" -> "simple";
         case "weighted_state_provider" -> "weighted";
         default -> strippedName.replace("_provider", "");
      });
      return super.updateBlockStateProvider(tag);
   }

   @Override
   protected boolean updateBlockState(Tag blockStateTag) {
      if (blockStateTag instanceof CompoundTag compoundTag) {
         Tag id = compoundTag.remove("Name");
         if (id != null) {
            compoundTag.put("id", id);
            Tag properties = compoundTag.remove("Properties");
            if (properties != null) {
               compoundTag.put("properties", properties);
            }
         }
      }

      return super.updateBlockState(blockStateTag);
   }

   private void updateTagKey(ListTag<CompoundTag> tags) {
      for (CompoundTag tag : tags) {
         StringTag tagKey = tag.getStringTag("id");
         tagKey.setValue("#" + tagKey.getValue());
      }
   }
}

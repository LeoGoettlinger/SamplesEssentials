package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.LockCode;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ApplyStatusEffects;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.Consumable1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ChunkLoadTracker;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.LastExplosionPowerStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ProtocolStorables1_21_2;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import com.viaversion.viaversion.util.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BlockItemPacketRewriter1_21_2 extends StructuredItemRewriter<ClientboundPacket1_21, ServerboundPacket1_21_2, Protocol1_21To1_21_2> {
   public static final List<StructuredDataKey<?>> NEW_DATA_TO_REMOVE = List.of(
      StructuredDataKey.REPAIRABLE,
      StructuredDataKey.ENCHANTABLE,
      StructuredDataKey.CONSUMABLE1_21_2,
      StructuredDataKey.V1_21_2.useRemainder,
      StructuredDataKey.USE_COOLDOWN,
      StructuredDataKey.ITEM_MODEL,
      StructuredDataKey.EQUIPPABLE1_21_2,
      StructuredDataKey.GLIDER,
      StructuredDataKey.TOOLTIP_STYLE,
      StructuredDataKey.DEATH_PROTECTION1_21_2
   );
   private static final int RECIPE_NOTIFICATION_FLAG = 1;
   private static final int RECIPE_HIGHLIGHT_FLAG = 2;
   private static final int RECIPE_INIT = 0;
   private static final int RECIPE_ADD = 1;
   private static final int RECIPE_REMOVE = 2;

   public BlockItemPacketRewriter1_21_2(Protocol1_21To1_21_2 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets1_21.COOLDOWN, wrapper -> {
         MappingData mappingData = this.protocol.getMappingData();
         int itemId = wrapper.read(Types.VAR_INT);
         int mappedItemId = mappingData.getNewItemId(itemId);
         wrapper.write(Types.STRING, mappingData.getFullItemMappings().mappedIdentifier(mappedItemId));
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21.CONTAINER_SET_CONTENT, wrapper -> {
         this.unsignedByteToVarInt(wrapper);
         wrapper.passthrough(Types.VAR_INT);
         Item[] items = wrapper.read(this.itemArrayType());
         wrapper.write(this.mappedItemArrayType(), items);

         for (int i = 0; i < items.length; i++) {
            items[i] = this.handleItemToClient(wrapper.user(), items[i]);
         }

         this.passthroughClientboundItem(wrapper);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21.CONTAINER_SET_SLOT, wrapper -> {
         this.byteToVarInt(wrapper);
         int containerId = wrapper.get(Types.VAR_INT, 0);
         if (containerId == -1) {
            wrapper.setPacketType(ClientboundPackets1_21_2.SET_CURSOR_ITEM);
            wrapper.resetReader();
            wrapper.read(Types.VAR_INT);
            wrapper.read(Types.VAR_INT);
            wrapper.read(Types.SHORT);
         } else if (containerId == -2) {
            wrapper.setPacketType(ClientboundPackets1_21_2.SET_PLAYER_INVENTORY);
            wrapper.resetReader();
            wrapper.read(Types.VAR_INT);
            wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, Integer.valueOf(wrapper.read(Types.SHORT)));
         } else {
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.SHORT);
         }

         this.passthroughClientboundItem(wrapper);
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_CLOSE, this::unsignedByteToVarInt);
      this.protocol.registerClientbound(ClientboundPackets1_21.CONTAINER_SET_DATA, this::unsignedByteToVarInt);
      this.protocol.registerClientbound(ClientboundPackets1_21.HORSE_SCREEN_OPEN, this::unsignedByteToVarInt);
      this.protocol.registerClientbound(ClientboundPackets1_21.SET_CARRIED_ITEM, ClientboundPackets1_21_2.SET_HELD_SLOT);
      this.protocol.registerServerbound(ServerboundPackets1_21_2.CONTAINER_CLOSE, this::varIntToByte);
      this.protocol.replaceServerbound(ServerboundPackets1_21_2.CONTAINER_CLICK, wrapper -> {
         this.varIntToByte(wrapper);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.SHORT);
         wrapper.passthrough(Types.BYTE);
         wrapper.passthrough(Types.VAR_INT);
         int length = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);

         for (int i = 0; i < length; i++) {
            wrapper.passthrough(Types.SHORT);
            wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType())));
         }

         wrapper.write(this.itemType(), this.handleItemToServer(wrapper.user(), wrapper.read(this.mappedItemType())));
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.PLACE_GHOST_RECIPE, wrapper -> {
         this.byteToVarInt(wrapper);
         String recipeKey = wrapper.read(Types.STRING);
         RecipeRewriter1_21_2.Recipe recipe = wrapper.user().get(RecipeRewriter1_21_2.class).recipe(recipeKey);
         if (recipe == null) {
            wrapper.cancel();
         } else {
            wrapper.write(Types.VAR_INT, recipe.recipeDisplayId());
            recipe.writeRecipeDisplay(wrapper);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.PLACE_RECIPE, wrapper -> {
         this.varIntToByte(wrapper);
         this.convertServerboundRecipeDisplayId(wrapper);
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_2.RECIPE_BOOK_SEEN_RECIPE, this::convertServerboundRecipeDisplayId);
      this.protocol.registerServerbound(ServerboundPackets1_21_2.USE_ITEM_ON, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
      });
      this.protocol.replaceClientbound(ClientboundPackets1_21.EXPLODE, wrapper -> {
         int centerX = (int)Math.floor(wrapper.passthrough(Types.DOUBLE));
         int centerY = (int)Math.floor(wrapper.passthrough(Types.DOUBLE));
         int centerZ = (int)Math.floor(wrapper.passthrough(Types.DOUBLE));
         float power = wrapper.read(Types.FLOAT);
         List<BlockPosition> affectedBlocks = new ArrayList<>();
         int blocks = wrapper.read(Types.VAR_INT);

         for (int i = 0; i < blocks; i++) {
            int x = centerX + wrapper.read(Types.BYTE);
            int y = centerY + wrapper.read(Types.BYTE);
            int z = centerZ + wrapper.read(Types.BYTE);
            affectedBlocks.add(new BlockPosition(x, y, z));
         }

         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         LastExplosionPowerStorage lastExplosionPowerStorage = storables.lastExplosionPowerStorage();
         if (lastExplosionPowerStorage != null) {
            lastExplosionPowerStorage.setPower(power);
            lastExplosionPowerStorage.setAffectedBlocks(affectedBlocks.size());
         }

         float knockbackX = wrapper.read(Types.FLOAT);
         float knockbackY = wrapper.read(Types.FLOAT);
         float knockbackZ = wrapper.read(Types.FLOAT);
         if (knockbackX == 0.0F && knockbackY == 0.0F && knockbackZ == 0.0F) {
            wrapper.write(Types.BOOLEAN, false);
         } else {
            wrapper.write(Types.BOOLEAN, true);
            wrapper.write(Types.DOUBLE, (double)knockbackX);
            wrapper.write(Types.DOUBLE, (double)knockbackY);
            wrapper.write(Types.DOUBLE, (double)knockbackZ);
         }

         int blockInteractionMode = wrapper.read(Types.VAR_INT);
         if (blockInteractionMode == 1 || blockInteractionMode == 2) {
            for (BlockPosition affectedBlock : affectedBlocks) {
               PacketWrapper blockUpdate = PacketWrapper.create(ClientboundPackets1_21_2.BLOCK_UPDATE, wrapper.user());
               blockUpdate.write(Types.BLOCK_POSITION1_14, affectedBlock);
               blockUpdate.write(Types.VAR_INT, 0);
               blockUpdate.send(Protocol1_21To1_21_2.class);
            }
         }

         Particle smallExplosionParticle = wrapper.read(VersionedTypes.V1_21.particle);
         Particle largeExplosionParticle = wrapper.read(VersionedTypes.V1_21.particle);
         if (power >= 2.0F && blockInteractionMode != 0) {
            this.protocol.getParticleRewriter().rewriteParticle(wrapper.user(), largeExplosionParticle);
            wrapper.write(VersionedTypes.V1_21_2.particle, largeExplosionParticle);
         } else {
            this.protocol.getParticleRewriter().rewriteParticle(wrapper.user(), smallExplosionParticle);
            wrapper.write(VersionedTypes.V1_21_2.particle, smallExplosionParticle);
         }

         this.protocol.getSoundRewriter().soundHolderHandler().handle(wrapper);
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.UPDATE_RECIPES, wrapper -> {
         FullMappings recipeSerializerMappings = this.protocol.getMappingData().getRecipeSerializerMappings();
         RecipeRewriter1_21_2 rewriter = new RecipeRewriter1_21_2(this.protocol);
         wrapper.user().put(rewriter);
         int size = wrapper.read(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            String recipeIdentifier = wrapper.read(Types.STRING);
            int serializerTypeId = wrapper.read(Types.VAR_INT);
            String serializerTypeIdentifier = recipeSerializerMappings.identifier(serializerTypeId);
            rewriter.setCurrentRecipeIdentifier(recipeIdentifier);
            rewriter.handleRecipeType(wrapper, serializerTypeIdentifier);
         }

         rewriter.finalizeRecipes();
         rewriter.writeUpdateRecipeInputs(wrapper);
      });
      this.protocol
         .registerClientbound(
            ClientboundPackets1_21.RECIPE,
            ClientboundPackets1_21_2.RECIPE_BOOK_ADD,
            wrapper -> {
               int state = wrapper.passthrough(Types.VAR_INT);
               PacketWrapper settingsPacket = wrapper.create(ClientboundPackets1_21_2.RECIPE_BOOK_SETTINGS);

               for (int i = 0; i < 8; i++) {
                  settingsPacket.write(Types.BOOLEAN, wrapper.read(Types.BOOLEAN));
               }

               settingsPacket.send(Protocol1_21To1_21_2.class);
               String[] recipes = wrapper.read(Types.STRING_ARRAY);
               Set<String> toHighlight = Set.of();
               if (state == 0) {
                  String[] highlightRecipes = wrapper.read(Types.STRING_ARRAY);
                  toHighlight = Set.of(highlightRecipes);
               }

               RecipeRewriter1_21_2 recipeRewriter = wrapper.user().get(RecipeRewriter1_21_2.class);
               if (recipeRewriter == null) {
                  this.protocol.getLogger().severe("Recipes not yet sent for recipe add packet");
                  wrapper.cancel();
               } else {
                  wrapper.clearPacket();
                  if (state == 2) {
                     wrapper.setPacketType(ClientboundPackets1_21_2.RECIPE_BOOK_REMOVE);
                     int[] ids = new int[recipes.length];

                     for (int i = 0; i < recipes.length; i++) {
                        String recipeKey = recipes[i];
                        RecipeRewriter1_21_2.Recipe recipe = recipeRewriter.recipe(recipeKey);
                        if (recipe == null) {
                           this.protocol.getLogger().severe("Recipe not found for key " + recipeKey);
                           wrapper.cancel();
                           return;
                        }

                        ids[i] = recipe.index();
                     }

                     wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, ids);
                  } else {
                     int size = recipes.length;
                     wrapper.write(Types.VAR_INT, size);

                     for (String recipeKey : recipes) {
                        RecipeRewriter1_21_2.Recipe recipe = recipeRewriter.recipe(recipeKey);
                        if (recipe == null) {
                           size--;
                        } else {
                           wrapper.write(Types.VAR_INT, recipe.index());
                           wrapper.write(Types.VAR_INT, recipe.recipeDisplayId());
                           recipe.writeRecipeDisplay(wrapper);
                           wrapper.write(Types.OPTIONAL_VAR_INT, recipe.group() != -1 ? recipe.group() : null);
                           wrapper.write(Types.VAR_INT, recipe.category());
                           Item[][] ingredients = recipe.ingredients();
                           if (ingredients != null) {
                              wrapper.write(Types.BOOLEAN, true);
                              List<HolderSet> filteredIngredients = Arrays.stream(ingredients)
                                 .filter(ingredientx -> ingredientx.length > 0)
                                 .map(recipeRewriter::toHolderSet)
                                 .toList();
                              wrapper.write(Types.VAR_INT, filteredIngredients.size());

                              for (HolderSet ingredient : filteredIngredients) {
                                 wrapper.write(Types.HOLDER_SET, ingredient);
                              }
                           } else {
                              wrapper.write(Types.BOOLEAN, false);
                           }

                           byte flags = 0;
                           if (state == 1) {
                              if (recipe.showNotification()) {
                                 flags = (byte)(flags | 1);
                              }

                              flags = (byte)(flags | 2);
                           } else if (toHighlight.contains(recipeKey)) {
                              flags = (byte)(flags | 2);
                           }

                           wrapper.write(Types.BYTE, flags);
                        }
                     }

                     wrapper.set(Types.VAR_INT, 0, size);
                     wrapper.write(Types.BOOLEAN, state == 0);
                  }
               }
            }
         );
      this.protocol.replaceClientbound(ClientboundPackets1_21.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
         Chunk chunk = this.protocol.getBlockRewriter().handleChunk1_18(wrapper);
         Mappings blockEntityMappings = this.protocol.getMappingData().getBlockEntityMappings();
         if (blockEntityMappings != null) {
            List<BlockEntity> blockEntities = chunk.blockEntities();

            for (int i = 0; i < blockEntities.size(); i++) {
               BlockEntity blockEntity = blockEntities.get(i);
               int id = blockEntity.typeId();
               int mappedId = blockEntityMappings.getNewIdOrDefault(id, id);
               if (id != mappedId) {
                  blockEntities.set(i, blockEntity.withTypeId(mappedId));
               }
            }
         }

         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ChunkLoadTracker chunkLoadTracker = storables.chunkLoadTracker();
         if (chunkLoadTracker != null) {
            if (chunkLoadTracker.isChunkLoaded(chunk.getX(), chunk.getZ())) {
               boolean isBundling = storables.bundleStateTracker().isBundling();
               if (!isBundling) {
                  PacketWrapper bundleStart = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
                  bundleStart.send(Protocol1_21To1_21_2.class);
               }

               PacketWrapper forgetLevelChunk = wrapper.create(ClientboundPackets1_21_2.FORGET_LEVEL_CHUNK);
               forgetLevelChunk.write(Types.CHUNK_POSITION, new ChunkPosition(chunk.getX(), chunk.getZ()));
               forgetLevelChunk.send(Protocol1_21To1_21_2.class);
               wrapper.send(Protocol1_21To1_21_2.class);
               wrapper.cancel();
               if (!isBundling) {
                  PacketWrapper bundleEnd = wrapper.create(ClientboundPackets1_21_2.BUNDLE_DELIMITER);
                  bundleEnd.send(Protocol1_21To1_21_2.class);
               }
            } else {
               chunkLoadTracker.addChunk(chunk.getX(), chunk.getZ());
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_21.FORGET_LEVEL_CHUNK, wrapper -> {
         ChunkPosition chunkPosition = wrapper.passthrough(Types.CHUNK_POSITION);
         ProtocolStorables1_21_2 storables = wrapper.user().storables(this.protocol);
         ChunkLoadTracker chunkLoadTracker = storables.chunkLoadTracker();
         if (chunkLoadTracker != null) {
            chunkLoadTracker.removeChunk(chunkPosition.chunkX(), chunkPosition.chunkZ());
         }
      });
   }

   private void convertServerboundRecipeDisplayId(PacketWrapper wrapper) {
      int recipeDisplayId = wrapper.read(Types.VAR_INT);
      RecipeRewriter1_21_2.Recipe recipe = wrapper.user().get(RecipeRewriter1_21_2.class).recipe(recipeDisplayId);
      if (recipe == null) {
         wrapper.cancel();
      } else {
         wrapper.write(Types.STRING, recipe.identifier());
      }
   }

   @Override
   public Item handleItemToClient(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return item;
      } else {
         super.handleItemToClient(connection, item);
         StructuredDataContainer data = item.dataContainer();
         FoodProperties1_20_5 food = data.get(StructuredDataKey.FOOD1_21);
         if (food != null && food.usingConvertsTo() != null) {
            this.handleItemToClient(connection, food.usingConvertsTo());
         }

         updateItemData(item);
         Enchantments enchantments = data.get(StructuredDataKey.ENCHANTMENTS1_20_5);
         if (enchantments != null && enchantments.size() != 0) {
            IntList enchantmentIds = new IntArrayList();
            enchantments.enchantments().int2IntEntrySet().removeIf(entry -> {
               if (entry.getIntValue() == 0) {
                  enchantmentIds.add(entry.getIntKey());
                  return true;
               } else {
                  return false;
               }
            });
            if (!enchantmentIds.isEmpty()) {
               IntArrayTag enchantmentIdsTag = new IntArrayTag(enchantmentIds.toIntArray());
               this.saveTag(this.createCustomTag(item), enchantmentIdsTag, "0_enchants");
            }

            if (enchantments.size() == 0 && !data.has(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE)) {
               data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
               this.saveTag(this.createCustomTag(item), new ByteTag(true), "remove_glint");
            }
         }

         int identifier = item.identifier();
         if (identifier == 952
            || identifier == 1147
            || identifier == 1039
            || identifier == 1203
            || identifier == 1200
            || identifier == 1204
            || identifier == 1202) {
            Tag itemName = data.get(StructuredDataKey.ITEM_NAME);
            if (itemName != null && !data.has(StructuredDataKey.CUSTOM_NAME)) {
               CompoundTag name = new CompoundTag();
               name.putBoolean("italic", false);
               name.putString("text", "");
               name.put("extra", new ListTag<>(Collections.singletonList(itemName)));
               data.set(StructuredDataKey.CUSTOM_NAME, name);
               this.saveTag(this.createCustomTag(item), new ByteTag(true), "remove_custom_name");
            }
         }

         return item;
      }
   }

   @Override
   public Item handleItemToServer(UserConnection connection, Item item) {
      if (item.isEmpty()) {
         return item;
      } else {
         super.handleItemToServer(connection, item);
         downgradeItemData(item);
         StructuredDataContainer dataContainer = item.dataContainer();
         CompoundTag customData = dataContainer.get(StructuredDataKey.CUSTOM_DATA);
         if (customData == null) {
            return item;
         } else {
            if (customData.remove(this.nbtTagName("remove_custom_name")) != null) {
               dataContainer.remove(StructuredDataKey.CUSTOM_NAME);
               this.removeCustomTag(dataContainer, customData);
            }

            IntArrayTag emptyEnchantments = customData.getIntArrayTag(this.nbtTagName("0_enchants"));
            if (emptyEnchantments != null) {
               Enchantments enchantments = dataContainer.get(StructuredDataKey.ENCHANTMENTS1_20_5);
               if (enchantments == null) {
                  enchantments = new Enchantments(true);
                  dataContainer.set(StructuredDataKey.ENCHANTMENTS1_20_5, enchantments);
               }

               for (int enchantmentId : emptyEnchantments.getValue()) {
                  enchantments.enchantments().put(enchantmentId, 0);
               }

               customData.remove(this.nbtTagName("0_enchants"));
               if (customData.remove(this.nbtTagName("remove_glint")) != null) {
                  dataContainer.remove(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE);
               }

               this.removeCustomTag(dataContainer, customData);
            }

            return item;
         }
      }
   }

   private void unsignedByteToVarInt(PacketWrapper wrapper) {
      short containerId = wrapper.read(Types.UNSIGNED_BYTE);
      wrapper.write(Types.VAR_INT, Integer.valueOf(containerId));
   }

   private void byteToVarInt(PacketWrapper wrapper) {
      byte containerId = wrapper.read(Types.BYTE);
      wrapper.write(Types.VAR_INT, Integer.valueOf(containerId));
   }

   private void varIntToByte(PacketWrapper wrapper) {
      int containerId = wrapper.read(Types.VAR_INT);
      wrapper.write(Types.BYTE, (byte)containerId);
   }

   public static void updateItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replace(StructuredDataKey.INSTRUMENT1_20_5, StructuredDataKey.INSTRUMENT1_21_2, instrument -> {
         if (instrument.hasId()) {
            return Holder.of(instrument.id());
         } else {
            Instrument1_20_5 value = instrument.value();
            return Holder.of(new Instrument1_21_2(value.soundEvent(), value.useDuration() / 20.0F, value.range(), new StringTag("")));
         }
      });
      dataContainer.replace(StructuredDataKey.FOOD1_21, StructuredDataKey.FOOD1_21_2, food -> {
         Holder<SoundEvent> sound = Holder.of(new SoundEvent("minecraft:entity.generic.eat", null));
         ConsumeEffect<?>[] consumeEffects = new ConsumeEffect[food.possibleEffects().length];

         for (int i = 0; i < consumeEffects.length; i++) {
            FoodProperties1_20_5.FoodEffect effect = food.possibleEffects()[i];
            ApplyStatusEffects applyStatusEffects = new ApplyStatusEffects(new PotionEffect[]{effect.effect()}, effect.probability());
            consumeEffects[i] = new ConsumeEffect<>(0, ApplyStatusEffects.TYPE, applyStatusEffects);
         }

         dataContainer.set(StructuredDataKey.CONSUMABLE1_21_2, new Consumable1_21_2(food.eatSeconds(), 1, sound, true, consumeEffects));
         if (food.usingConvertsTo() != null) {
            dataContainer.set(StructuredDataKey.V1_21_2.useRemainder, food.usingConvertsTo());
         }

         return new FoodProperties1_21_2(food.nutrition(), food.saturationModifier(), food.canAlwaysEat());
      }, () -> {
         dataContainer.setEmpty(StructuredDataKey.CONSUMABLE1_21_2);
         dataContainer.setEmpty(StructuredDataKey.V1_21_2.useRemainder);
      });
      dataContainer.replaceKey(StructuredDataKey.POTION_CONTENTS1_20_5, StructuredDataKey.POTION_CONTENTS1_21_2);
      dataContainer.replace(
         StructuredDataKey.FIRE_RESISTANT, StructuredDataKey.DAMAGE_RESISTANT1_21_2, fireResistant -> new DamageResistant1_21_2(Key.of("minecraft:is_fire"))
      );
      dataContainer.replace(StructuredDataKey.LOCK1_20_5, StructuredDataKey.LOCK1_21_2, tag -> {
         String lock = ((StringTag)tag).getValue();
         CompoundTag predicateTag = new CompoundTag();
         CompoundTag itemComponentsTag = new CompoundTag();
         predicateTag.put("components", itemComponentsTag);
         itemComponentsTag.putString("custom_name", ComponentUtil.plainToJson(lock).toString());
         return new LockCode(predicateTag);
      });
      dataContainer.replace(StructuredDataKey.TRIM1_20_5, StructuredDataKey.TRIM1_21_2, trim -> {
         if (trim.material().isDirect()) {
            trim.material().value().overrideArmorMaterials().clear();
         }

         return trim;
      });
   }

   public static void downgradeItemData(Item item) {
      StructuredDataContainer dataContainer = item.dataContainer();
      dataContainer.replace(
         StructuredDataKey.LOCK1_21_2,
         StructuredDataKey.LOCK1_20_5,
         lock -> {
            CompoundTag predicateTag = lock.tag();
            CompoundTag itemComponentsTag = predicateTag.getCompoundTag("components");
            if (itemComponentsTag == null) {
               return null;
            } else {
               StringTag customName = TagUtil.getNamespacedStringTag(itemComponentsTag, "custom_name");
               return customName != null && !customName.getValue().equals("null")
                  ? new StringTag(SerializerVersion.V1_20_5.toComponent(customName.getValue()).asUnformattedString())
                  : null;
            }
         }
      );
      dataContainer.replace(StructuredDataKey.INSTRUMENT1_21_2, StructuredDataKey.INSTRUMENT1_20_5, instrument -> {
         if (instrument.hasId()) {
            return Holder.of(instrument.id());
         } else {
            Instrument1_21_2 value = instrument.value();
            return Holder.of(new Instrument1_20_5(value.soundEvent(), (int)(value.useDuration() * 20.0F), value.range()));
         }
      });
      dataContainer.replace(
         StructuredDataKey.FOOD1_21_2,
         StructuredDataKey.FOOD1_21,
         food -> {
            Consumable1_21_2 consumableData = dataContainer.get(StructuredDataKey.CONSUMABLE1_21_2);
            Item useRemainderData = dataContainer.get(StructuredDataKey.V1_21_2.useRemainder);
            float eatSeconds = consumableData != null ? consumableData.consumeSeconds() : 1.6F;
            List<FoodProperties1_20_5.FoodEffect> foodEffects = new ArrayList<>();
            if (consumableData != null) {
               for (ConsumeEffect<?> consumeEffect : consumableData.consumeEffects()) {
                  if (consumeEffect.value() instanceof ApplyStatusEffects applyStatusEffects) {
                     for (PotionEffect effect : applyStatusEffects.effects()) {
                        foodEffects.add(new FoodProperties1_20_5.FoodEffect(effect, applyStatusEffects.probability()));
                     }
                  }
               }
            }

            return new FoodProperties1_20_5(
               food.nutrition(),
               food.saturationModifier(),
               food.canAlwaysEat(),
               eatSeconds,
               useRemainderData,
               foodEffects.toArray(new FoodProperties1_20_5.FoodEffect[0])
            );
         }
      );
      dataContainer.replace(StructuredDataKey.TRIM1_21_2, StructuredDataKey.TRIM1_20_5, trim -> {
         if (trim.material().isDirect()) {
            trim.material().value().overrideArmorMaterials().clear();
         }

         return trim;
      });
      dataContainer.replaceKey(StructuredDataKey.POTION_CONTENTS1_21_2, StructuredDataKey.POTION_CONTENTS1_20_5);
      dataContainer.replace(
         StructuredDataKey.DAMAGE_RESISTANT1_21_2,
         StructuredDataKey.FIRE_RESISTANT,
         damageResistant -> damageResistant.typesTagKey().equals("is_fire") ? Unit.INSTANCE : null
      );
      dataContainer.remove(NEW_DATA_TO_REMOVE);
   }
}

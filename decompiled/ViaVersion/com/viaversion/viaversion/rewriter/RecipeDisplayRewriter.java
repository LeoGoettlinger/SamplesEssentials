package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;

public class RecipeDisplayRewriter<C extends ClientboundPacketType> {
   protected final Map<String, RecipeDisplayRewriter.SlotDisplayConsumer> slotDisplayHandlers = new HashMap<>();
   protected final Protocol<C, ?, ?, ?> protocol;

   public RecipeDisplayRewriter(Protocol<C, ?, ?, ?> protocol) {
      this.protocol = protocol;
      this.slotDisplayHandlers.put("with_any_potion", this::handleSlotDisplay);
      this.slotDisplayHandlers.put("only_with_component", this::handleOnlyWithComponentSlotDisplay);
      this.slotDisplayHandlers.put("item", this::handleItemId);
      this.slotDisplayHandlers.put("item_stack", this::handleItem);
      this.slotDisplayHandlers.put("tag", this::handleTag);
      this.slotDisplayHandlers.put("dyed", this::handleDyedSlotDisplay);
      this.slotDisplayHandlers.put("smithing_trim", this::handleSmithingTrimSlotDisplay);
      this.slotDisplayHandlers.put("with_remainder", this::handleWithRemainderSlotDisplay);
      this.slotDisplayHandlers.put("composite", this::handleSlotDisplayList);
   }

   public void registerUpdateRecipes(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            this.rewriteItemIds(wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE));
         }

         int stonecutterRecipesSize = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < stonecutterRecipesSize; i++) {
            this.handleIngredient(wrapper);
            this.handleSlotDisplay(wrapper);
         }
      });
   }

   public void registerRecipeBookAdd(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.VAR_INT);
            this.handleRecipeDisplay(wrapper);
            wrapper.passthrough(Types.OPTIONAL_VAR_INT);
            wrapper.passthrough(Types.VAR_INT);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               int ingredientsSize = wrapper.passthrough(Types.VAR_INT);

               for (int j = 0; j < ingredientsSize; j++) {
                  this.handleIngredient(wrapper);
               }
            }

            wrapper.passthrough(Types.BYTE);
         }
      });
   }

   public void registerPlaceGhostRecipe(C packetType) {
      this.protocol.registerClientbound(packetType, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         this.handleRecipeDisplay(wrapper);
      });
   }

   protected void handleTag(PacketWrapper wrapper) {
      wrapper.passthrough(Types.STRING);
   }

   protected void handleShapeless(PacketWrapper wrapper) {
      this.handleSlotDisplayList(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleShaped(PacketWrapper wrapper) {
      wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.VAR_INT);
      this.handleSlotDisplayList(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleFurnace(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.FLOAT);
   }

   protected void handleStoneCutter(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleSmithing(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleRecipeDisplay(PacketWrapper wrapper) {
      int type = wrapper.passthrough(Types.VAR_INT);
      switch (type) {
         case 0:
            this.handleShapeless(wrapper);
            break;
         case 1:
            this.handleShaped(wrapper);
            break;
         case 2:
            this.handleFurnace(wrapper);
            break;
         case 3:
            this.handleStoneCutter(wrapper);
            break;
         case 4:
            this.handleSmithing(wrapper);
      }
   }

   protected void handleSlotDisplay(PacketWrapper wrapper) {
      FullMappings mappings = this.protocol.getMappingData().getSlotDisplayMappings();
      int type = wrapper.read(Types.VAR_INT);
      int mappedType = mappings.getNewId(type);
      if (mappedType != -1) {
         wrapper.write(Types.VAR_INT, mappedType);
         this.runSlotDisplayHandler(wrapper, mappings, type);
      } else {
         wrapper.write(Types.VAR_INT, 0);
         wrapper.consumeReadsOnly(() -> this.runSlotDisplayHandler(wrapper, mappings, type));
      }
   }

   private void runSlotDisplayHandler(PacketWrapper wrapper, FullMappings mappings, int type) {
      String identifier = mappings.identifier(type);
      RecipeDisplayRewriter.SlotDisplayConsumer handler = this.slotDisplayHandlers.get(Key.stripMinecraftNamespace(identifier));
      if (handler != null) {
         handler.accept(wrapper);
      }
   }

   protected void handleSlotDisplayList(PacketWrapper wrapper) {
      int size = wrapper.passthrough(Types.VAR_INT);

      for (int i = 0; i < size; i++) {
         this.handleSlotDisplay(wrapper);
      }
   }

   protected void handleSmithingTrimSlotDisplay(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleWithRemainderSlotDisplay(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleOnlyWithComponentSlotDisplay(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      int mappedDataComponentType = this.protocol.getMappingData().getDataComponentSerializerMappings().getNewIdOrDefault(wrapper.read(Types.VAR_INT), 0);
      wrapper.write(Types.VAR_INT, mappedDataComponentType);
   }

   protected void handleDyedSlotDisplay(PacketWrapper wrapper) {
      this.handleSlotDisplay(wrapper);
      this.handleSlotDisplay(wrapper);
   }

   protected void handleIngredient(PacketWrapper wrapper) {
      HolderSet items = wrapper.passthrough(Types.HOLDER_SET);
      if (!items.hasTagKey()) {
         int[] ids = items.ids();

         for (int i = 0; i < ids.length; i++) {
            ids[i] = this.rewriteItemId(ids[i]);
         }
      }
   }

   protected void handleItemId(PacketWrapper wrapper) {
      int id = wrapper.read(Types.VAR_INT);
      wrapper.write(Types.VAR_INT, this.rewriteItemId(id));
   }

   protected void handleItem(PacketWrapper wrapper) {
      com.viaversion.viaversion.api.rewriter.ItemRewriter<?> itemRewriter = this.protocol.getItemRewriter();
      Item item = wrapper.read(itemRewriter.itemTemplateType());
      itemRewriter.handleItemToClient(wrapper.user(), item);
      wrapper.write(itemRewriter.mappedItemTemplateType(), item);
   }

   protected int rewriteItemId(int id) {
      MappingData mappingData = this.protocol.getMappingData();
      return mappingData != null && mappingData.getItemMappings() != null ? mappingData.getItemMappings().getNewIdOrDefault(id, id) : id;
   }

   protected void rewriteItemIds(int[] ids) {
      for (int i = 0; i < ids.length; i++) {
         int id = ids[i];
         ids[i] = this.rewriteItemId(id);
      }
   }

   @FunctionalInterface
   public interface SlotDisplayConsumer {
      void accept(PacketWrapper var1);
   }
}

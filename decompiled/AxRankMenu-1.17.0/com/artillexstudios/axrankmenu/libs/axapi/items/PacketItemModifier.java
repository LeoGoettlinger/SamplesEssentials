package com.artillexstudios.axrankmenu.libs.axapi.items;

import com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvents;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketListener;
import com.artillexstudios.axrankmenu.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.data.MerchantOffer;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundContainerSetContentWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundContainerSetSlotWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundEntityMetadataWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundMerchantOffersWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundSetCursorItemWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.serverbound.ServerboundContainerClickWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper.serverbound.ServerboundSetCreativeModeSlotWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axrankmenu.libs.axapi.utils.EquipmentSlot;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Pair;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Version;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class PacketItemModifier {
   private static boolean listening = false;
   private static final ObjectArrayList<PacketItemModifierListener> listeners = new ObjectArrayList();
   private static final Map<Player, HashedStack> stacks = new ConcurrentHashMap<>();

   public static void registerModifierListener(PacketItemModifierListener listener) {
      if (!listening) {
         registerListener();
      }

      if (!listeners.contains(listener)) {
         listeners.add(listener);
         listening = true;
      }
   }

   private static void registerListener() {
      PacketEvents.INSTANCE.addListener(new PacketListener() {
         @Override
         public void onPacketSending(PacketEvent event) {
            if (PacketItemModifier.isListening()) {
               if (event.type() == ClientboundPacketTypes.CONTAINER_SET_SLOT) {
                  ClientboundContainerSetSlotWrapper wrapper = new ClientboundContainerSetSlotWrapper(event);
                  PacketItemModifier.callModify(wrapper.stack(), event.player(), PacketItemModifier.Context.SET_SLOT);
                  wrapper.markDirty();
               } else if (event.type() == ClientboundPacketTypes.CONTAINER_CONTENT) {
                  ClientboundContainerSetContentWrapper wrapper = new ClientboundContainerSetContentWrapper(event);

                  for (WrappedItemStack item : wrapper.items()) {
                     PacketItemModifier.callModify(item, event.player(), PacketItemModifier.Context.SET_CONTENTS);
                  }

                  PacketItemModifier.callModify(wrapper.carriedItem(), event.player(), PacketItemModifier.Context.SET_CONTENTS);
                  wrapper.markDirty();
               } else if (event.type() == ClientboundPacketTypes.SET_CURSOR_ITEM) {
                  ClientboundSetCursorItemWrapper wrapper = new ClientboundSetCursorItemWrapper(event);
                  ServerPlayerWrapper serverPlayer = ServerPlayerWrapper.wrap(event.player());
                  if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
                     WrappedItemStack copy = wrapper.getItemStack().copy();
                     HashedStack hashedStack = copy.toHashedStack(serverPlayer.hashGenerator());
                     PacketItemModifier.callModify(wrapper.getItemStack(), event.player(), PacketItemModifier.Context.SET_CURSOR);
                     if (!copy.equals(wrapper.getItemStack())) {
                        PacketItemModifier.stacks.put(event.player(), hashedStack);
                     }
                  } else {
                     PacketItemModifier.callModify(wrapper.getItemStack(), event.player(), PacketItemModifier.Context.SET_CURSOR);
                  }

                  wrapper.markDirty();
               } else if (event.type() == ClientboundPacketTypes.SET_EQUIPMENT) {
                  ClientboundSetEquipmentWrapper wrapper = new ClientboundSetEquipmentWrapper(event);

                  for (Pair<EquipmentSlot, WrappedItemStack> item : wrapper.items()) {
                     PacketItemModifier.callModify(item.second(), event.player(), PacketItemModifier.Context.EQUIPMENT);
                  }

                  wrapper.markDirty();
               } else if (event.type() == ClientboundPacketTypes.MERCHANT_OFFERS) {
                  ClientboundMerchantOffersWrapper wrapper = new ClientboundMerchantOffersWrapper(event);

                  for (MerchantOffer offer : wrapper.merchantOffers()) {
                     PacketItemModifier.callModify(offer.getItem1(), event.player(), PacketItemModifier.Context.MERCHANT_OFFER);
                     offer.getItem2().ifPresent(cost -> PacketItemModifier.callModify(cost, event.player(), PacketItemModifier.Context.MERCHANT_OFFER));
                     PacketItemModifier.callModify(offer.getOutput(), event.player(), PacketItemModifier.Context.MERCHANT_OFFER);
                  }

                  wrapper.markDirty();
               } else if (event.type() == ClientboundPacketTypes.SET_ENTITY_DATA) {
                  ClientboundEntityMetadataWrapper wrapper = new ClientboundEntityMetadataWrapper(event);

                  for (Metadata.DataItem<?> item : wrapper.items()) {
                     if (item.getValue() instanceof WrappedItemStack stack) {
                        PacketItemModifier.callModify(stack, event.player(), PacketItemModifier.Context.DROPPED_ITEM);
                     }
                  }

                  wrapper.markDirty();
               }
            }
         }

         @Override
         public void onPacketReceive(PacketEvent event) {
            if (event.type() == ServerboundPacketTypes.SET_CREATIVE_MODE_SLOT) {
               ServerboundSetCreativeModeSlotWrapper wrapper = new ServerboundSetCreativeModeSlotWrapper(event);
               PacketItemModifier.restore(wrapper.stack());
               wrapper.markDirty();
            } else if (event.type() == ServerboundPacketTypes.CONTAINER_CLICK && Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_21_4)) {
               ServerboundContainerClickWrapper wrapper = new ServerboundContainerClickWrapper(event);
               HashedStack stack = PacketItemModifier.stacks.remove(event.player());
               if (stack != null) {
                  wrapper.setCarriedItem(stack);
                  wrapper.markDirty();
               }
            }
         }
      });
   }

   public static void unregister(PacketItemModifierListener listener) {
      listeners.remove(listener);
      if (listeners.isEmpty()) {
         listening = false;
      }
   }

   public static boolean isListening() {
      return listening;
   }

   public static void callModify(WrappedItemStack itemStack, Player player, PacketItemModifier.Context context) {
      ObjectListIterator var3 = listeners.iterator();

      while (var3.hasNext()) {
         PacketItemModifierListener listener = (PacketItemModifierListener)var3.next();
         listener.modifyItemStack(player, itemStack, context);
      }
   }

   public static void restore(WrappedItemStack itemStack) {
      ObjectListIterator var1 = listeners.iterator();

      while (var1.hasNext()) {
         PacketItemModifierListener listener = (PacketItemModifierListener)var1.next();
         listener.restore(itemStack);
      }
   }

   public static enum Context {
      SET_SLOT,
      SET_CURSOR,
      SET_CONTENTS,
      EQUIPMENT,
      DROPPED_ITEM,
      MERCHANT_OFFER;
   }
}

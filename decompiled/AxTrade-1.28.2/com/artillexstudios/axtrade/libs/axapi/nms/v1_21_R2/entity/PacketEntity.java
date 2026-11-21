package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.entity;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.collections.RawReferenceOpenHashSet;
import com.artillexstudios.axtrade.libs.axapi.events.PacketEntityInteractEvent;
import com.artillexstudios.axtrade.libs.axapi.hologram.Holograms;
import com.artillexstudios.axtrade.libs.axapi.hologram.page.HologramPage;
import com.artillexstudios.axtrade.libs.axapi.hologram.page.LineData;
import com.artillexstudios.axtrade.libs.axapi.hologram.page.TextDisplayHologramPage;
import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.packet.ClientboundSetPassengersWrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.packet.ClientboundTeleportEntityWrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.wrapper.ServerWrapper;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.EntityMetaFactory;
import com.artillexstudios.axtrade.libs.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axtrade.libs.axapi.packetentity.tracker.EntityTracker;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PaperPlaceholderHandler;
import com.artillexstudios.axtrade.libs.axapi.placeholders.PlaceholderParameters;
import com.artillexstudios.axtrade.libs.axapi.utils.EquipmentSlot;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.network.syncher.DataWatcher.c;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class PacketEntity implements com.artillexstudios.axtrade.libs.axapi.packetentity.PacketEntity {
   private final int id;
   private final EntityMeta meta;
   private final EntityTypes<?> type;
   private final VecDeltaCodec codec = new VecDeltaCodec();
   private final Set<Player> invertedVisibilityEntities = Collections.newSetFromMap(new WeakHashMap<>());
   private final NonNullList<ItemStack> handSlots;
   private final NonNullList<ItemStack> armorSlots;
   private Location location;
   private volatile EntityTracker.TrackedEntity tracker;
   private List<c<?>> trackedValues;
   private Vec3D vec3;
   private volatile boolean shouldTeleport = false;
   private volatile boolean itemDirty = false;
   private boolean visibleByDefault = true;
   private int riddenEntityId = -1;
   private Consumer<PacketEntityInteractEvent> interactConsumer;
   private boolean hasInvertedVisibility = false;
   private float yHeadRot = 0.0F;
   private int viewDistanceSquared = 1024;

   public PacketEntity(EntityType entityType, Location location) {
      this.id = ServerWrapper.INSTANCE.nextEntityId();
      this.type = EntityTypes.a(entityType.getName()).orElse(EntityTypes.f);
      this.meta = EntityMetaFactory.getForType(entityType);
      this.location = location;
      this.handSlots = NonNullList.a(2, ItemStack.j);
      this.armorSlots = NonNullList.a(4, ItemStack.j);
   }

   private static List<c<?>> transform(List<Metadata.DataItem<?>> toTransform) {
      List<c<?>> dataValues = null;
      if (toTransform != null) {
         dataValues = new ArrayList<>(toTransform.size());

         for (Metadata.DataItem<?> dataItem : toTransform) {
            Serializers.Transformer<?> transformer = Serializers.transformer(dataItem.serializer());
            dataValues.add(new c(dataItem.id(), transformer.serializer(), transformer.transform(dataItem.getValue())));
         }
      }

      return dataValues;
   }

   private static PacketPlayOutSpawnEntity getAddEntityPacket(PacketEntity entity) {
      return new PacketPlayOutSpawnEntity(
         entity.id(),
         UUID.randomUUID(),
         entity.location().getX(),
         entity.location().getY(),
         entity.location().getZ(),
         entity.location().getPitch(),
         entity.location().getYaw(),
         entity.type,
         0,
         Vec3D.c,
         0.0
      );
   }

   @Override
   public void teleport(Location location) {
      this.location = location;
      synchronized (this.codec) {
         this.vec3 = new Vec3D(location.getX(), location.getY(), location.getZ());
         this.shouldTeleport = true;
      }
   }

   @Override
   public Location location() {
      return this.location;
   }

   @Override
   public EntityMeta meta() {
      return this.meta;
   }

   @Override
   public int id() {
      return this.id;
   }

   @Override
   public int viewDistanceSquared() {
      return this.viewDistanceSquared;
   }

   @Override
   public void viewDistance(int blocks) {
      this.viewDistanceSquared = blocks * blocks;
   }

   @Override
   public void spawn() {
      this.meta.metadata().markNotDirty();
      this.trackedValues = transform(this.meta.metadata().getNonDefaultValues());
      ((AxPlugin)AxPlugin.getPlugin(AxPlugin.class)).tracker.addEntity(this);
   }

   @Override
   public void hide(Player player) {
      if (this.visibleByDefault) {
         this.invertedVisibilityEntities.add(player);
         this.hasInvertedVisibility = true;
      } else {
         this.invertedVisibilityEntities.remove(player);
         if (this.invertedVisibilityEntities.isEmpty()) {
            this.hasInvertedVisibility = false;
         }
      }
   }

   @Override
   public void show(Player player) {
      if (this.visibleByDefault) {
         this.invertedVisibilityEntities.remove(player);
         if (this.invertedVisibilityEntities.isEmpty()) {
            this.hasInvertedVisibility = false;
         }
      } else {
         this.invertedVisibilityEntities.add(player);
         this.hasInvertedVisibility = true;
      }
   }

   @Override
   public void setVisibleByDefault(boolean visible) {
      this.visibleByDefault = visible;
   }

   @Override
   public void setItem(EquipmentSlot slot, WrappedItemStack item) {
      if (slot.getType() == EquipmentSlot.Type.HAND) {
         this.handSlots
            .set(slot.getIndex(), item == null ? ItemStack.j : ((com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.items.WrappedItemStack)item).itemStack);
      } else {
         this.armorSlots
            .set(slot.getIndex(), item == null ? ItemStack.j : ((com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.items.WrappedItemStack)item).itemStack);
      }

      this.itemDirty = true;
   }

   @Override
   public WrappedItemStack getItem(EquipmentSlot slot) {
      return new com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R2.items.WrappedItemStack(
         slot.getType() == EquipmentSlot.Type.ARMOR ? (ItemStack)this.armorSlots.get(slot.getIndex()) : (ItemStack)this.handSlots.get(slot.getIndex())
      );
   }

   @Override
   public void sendChanges() {
      List<Metadata.DataItem<?>> items = this.meta.metadata().packDirty();
      if (items != null) {
         List<c<?>> dirty = transform(items);
         this.trackedValues = transform(this.meta.metadata().getNonDefaultValues());
         HologramPage<?, ?> page = Holograms.byId(this.id);
         if (page != null && page.containsPlaceholders()) {
            PlaceholderParameters parameters = new PlaceholderParameters();
            parameters.merge(page.getParameters());

            for (Object player : RawReferenceOpenHashSet.rawSet(this.tracker.seenBy)) {
               if (player != null) {
                  ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
                  parameters.withParameter(Player.class, wrapper.wrapped());
                  wrapper.sendPacket(new PacketPlayOutEntityMetadata(this.id, this.translate(parameters, page, dirty)));
               }
            }
         } else {
            this.tracker.broadcast(new PacketPlayOutEntityMetadata(this.id, dirty));
         }
      }

      if (this.itemDirty) {
         this.itemDirty = false;
         List<Pair<EnumItemSlot, ItemStack>> equipments = Lists.newArrayList();

         for (EnumItemSlot slot : EnumItemSlot.values()) {
            ItemStack item = this.getItemBySlot(slot);
            if (item != null && !item.f()) {
               ItemStack sanitised = item.v();
               equipments.add(Pair.of(slot, sanitised));
            } else {
               equipments.add(Pair.of(slot, ItemStack.j));
            }
         }

         if (!equipments.isEmpty()) {
            this.tracker.broadcast(new PacketPlayOutEntityEquipment(this.id, equipments));
         }
      }

      if (this.shouldTeleport) {
         synchronized (this.codec) {
            this.shouldTeleport = false;
            long k = this.codec.a(this.vec3);
            long l = this.codec.b(this.vec3);
            long i1 = this.codec.c(this.vec3);
            boolean flag6 = k < -32768L || k > 32767L || l < -32768L || l > 32767L || i1 < -32768L || i1 > 32767L;
            this.codec.e(this.vec3);
            if (!flag6) {
               this.tracker
                  .broadcast(
                     new PacketPlayOutRelEntityMoveLook(
                        this.id,
                        (short)k,
                        (short)l,
                        (short)i1,
                        (byte)(this.location.getYaw() * 256.0F / 360.0F),
                        (byte)(this.location.getPitch() * 256.0F / 360.0F),
                        true
                     )
                  );
            } else {
               this.tracker.broadcast(ClientboundTeleportEntityWrapper.createNew(this, this.location));
            }
         }
      }
   }

   @Override
   public void removePairing(Player player) {
      EntityPlayer serverPlayer = ((CraftPlayer)player).getHandle();
      serverPlayer.f.b(new PacketPlayOutEntityDestroy(new int[]{this.id}));
   }

   @Override
   public void addPairing(Player player) {
      EntityPlayer serverPlayer = ((CraftPlayer)player).getHandle();
      ArrayList<Packet<? super PacketListenerPlayOut>> list = new ArrayList<>();
      list.add(getAddEntityPacket(this));
      if (this.trackedValues != null) {
         HologramPage<?, ?> page = Holograms.byId(this.id);
         if (page != null && page.containsPlaceholders()) {
            list.add(
               new PacketPlayOutEntityMetadata(
                  this.id,
                  this.translate(new PlaceholderParameters().merge(page.getParameters()).withParameter(Player.class, player), page, this.trackedValues)
               )
            );
         } else {
            list.add(new PacketPlayOutEntityMetadata(this.id, this.trackedValues));
         }
      }

      List<Pair<EnumItemSlot, ItemStack>> equipments = Lists.newArrayList();

      for (EnumItemSlot slot : EnumItemSlot.values()) {
         ItemStack item = this.getItemBySlot(slot);
         if (item != null && !item.f()) {
            ItemStack sanitised = item.v();
            equipments.add(Pair.of(slot, sanitised));
         }
      }

      if (!equipments.isEmpty()) {
         list.add(new PacketPlayOutEntityEquipment(this.id, equipments));
      }

      if (this.riddenEntityId != -1) {
         list.add(ClientboundSetPassengersWrapper.createNew(this.riddenEntityId, new int[]{this.id}));
      }

      PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
      buf.c(this.id);
      buf.l((byte)MathHelper.d(this.yHeadRot * 256.0F / 360.0F));
      list.add((Packet<? super PacketListenerPlayOut>)PacketPlayOutEntityHeadRotation.a.decode(buf));
      buf.release();
      serverPlayer.f.b(new ClientboundBundlePacket(list));
   }

   @Override
   public boolean canSee(Player player) {
      return !this.hasInvertedVisibility ? this.visibleByDefault : this.visibleByDefault ^ this.invertedVisibilityEntities.contains(player);
   }

   @Override
   public void remove() {
      ((AxPlugin)AxPlugin.getPlugin(AxPlugin.class)).tracker.removeEntity(this);
   }

   @Override
   public void onInteract(Consumer<PacketEntityInteractEvent> event) {
      this.interactConsumer = event;
   }

   @Override
   public void callInteract(PacketEntityInteractEvent event) {
      if (this.interactConsumer != null) {
         this.interactConsumer.accept(event);
      }
   }

   @Override
   public void ride(int entityId) {
      this.riddenEntityId = entityId;
      this.tracker.broadcast(ClientboundSetPassengersWrapper.createNew(this.riddenEntityId, new int[]{this.id}));
   }

   @Override
   public void unRide(int entityId) {
      if (this.riddenEntityId != -1) {
         this.tracker.broadcast(ClientboundSetPassengersWrapper.createNew(this.riddenEntityId, new int[0]));
         this.riddenEntityId = -1;
      }
   }

   @Override
   public int riddenEntity() {
      return this.riddenEntityId;
   }

   @Override
   public void rotate(float yaw, float pitch) {
      this.location.setYaw(yaw);
      this.location.setPitch(pitch);
      this.tracker
         .broadcast(new PacketPlayOutEntityLook(this.id, (byte)MathHelper.d(yaw * 256.0F / 360.0F), (byte)MathHelper.d(pitch * 256.0F / 360.0F), true));
   }

   @Override
   public void rotateHead(float yaw) {
      this.yHeadRot = yaw;
      PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
      buf.c(this.id);
      buf.l((byte)MathHelper.d(yaw * 256.0F / 360.0F));
      PacketPlayOutEntityHeadRotation packet = (PacketPlayOutEntityHeadRotation)PacketPlayOutEntityHeadRotation.a.decode(buf);
      this.tracker.broadcast(packet);
      buf.release();
   }

   @Override
   public void update() {
      List<c<?>> transformed = transform(this.meta.metadata().packForNameUpdate());
      HologramPage<?, ?> page = Holograms.byId(this.id);
      if (page != null && this.tracker != null && page.containsPlaceholders()) {
         PlaceholderParameters parameters = new PlaceholderParameters();
         parameters.merge(page.getParameters());

         for (Object player : RawReferenceOpenHashSet.rawSet(this.tracker.seenBy)) {
            if (player != null) {
               ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
               parameters.withParameter(Player.class, wrapper.wrapped());
               wrapper.sendPacket(new PacketPlayOutEntityMetadata(this.id, this.translate(parameters, page, transformed)));
            }
         }
      }
   }

   private List<c<?>> translate(PlaceholderParameters parameters, HologramPage<?, ?> page, List<c<?>> values) {
      if (!page.containsPlaceholders()) {
         return values;
      } else {
         List<c<?>> dataValues = new ArrayList<>(values);
         ListIterator<c<?>> iterator = dataValues.listIterator();

         while (iterator.hasNext()) {
            c<?> value = iterator.next();
            if (value.a() == 23 && page instanceof TextDisplayHologramPage textPage) {
               IChatMutableComponent components = IChatMutableComponent.a(LiteralContents.c);

               for (LineData lineData : textPage.getData()) {
                  if (!lineData.containsPlaceholders()) {
                     components.b((IChatBaseComponent)lineData.component());
                  } else {
                     String parsed = PaperPlaceholderHandler.parseWithPlaceholderAPI(lineData.content(), parameters);
                     components.b((IChatBaseComponent)placeholderFormatCache.get(parsed));
                  }
               }

               iterator.remove();
               iterator.add(new c(value.a(), value.b(), components));
               break;
            }
         }

         return dataValues;
      }
   }

   private ItemStack getItemBySlot(EnumItemSlot slot) {
      return switch (slot.a()) {
         case b -> (ItemStack)this.armorSlots.get(slot.b());
         case a -> (ItemStack)this.handSlots.get(slot.b());
         default -> null;
      };
   }
}

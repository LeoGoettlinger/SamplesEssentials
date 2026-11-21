package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

final class SnapshotCustody implements PlayerCustody {
   private static final int MAGIC = 1296256339;
   private static final int ETERNAL_TD_MAGIC = 1163150419;
   private static final int FORMAT_VERSION = 1;
   private static final int MAX_INVENTORY_SLOTS = 100;
   private static final int MAX_PAYLOAD_BYTES = 8388608;
   private final NamespacedKey key;
   private final Consumer<Player> flush;
   private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();

   SnapshotCustody(NamespacedKey key) {
      this(key, Player::saveData);
   }

   SnapshotCustody(NamespacedKey key, Consumer<Player> flush) {
      this.key = key;
      this.flush = flush;
   }

   private static byte[] encode(SnapshotCustody.Snapshot snapshot) {
      try {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         BukkitObjectOutputStream output = new BukkitObjectOutputStream(bytes);

         try {
            output.writeInt(1296256339);
            output.writeInt(1);
            output.writeInt(snapshot.inventoryContents().length);

            for (ItemStack itemStack : snapshot.inventoryContents()) {
               output.writeObject(itemStack == null ? null : itemStack.clone());
            }

            output.writeInt(snapshot.heldSlot());
            output.writeFloat(snapshot.flySpeed());
            output.writeBoolean(snapshot.allowFlight());
            output.writeBoolean(snapshot.flying());
            snapshot.returnLocation().write(output);
         } catch (Throwable var8) {
            try {
               output.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         output.close();
         byte[] encoded = bytes.toByteArray();
         if (encoded.length > 8388608) {
            throw new IllegalStateException("Match custody payload exceeds 8388608 bytes");
         } else {
            return encoded;
         }
      } catch (IOException var9) {
         throw new IllegalStateException("Could not encode match custody state", var9);
      }
   }

   private static SnapshotCustody.Snapshot decode(byte[] encoded) {
      if (encoded.length != 0 && encoded.length <= 8388608) {
         try {
            BukkitObjectInputStream input = new BukkitObjectInputStream(new ByteArrayInputStream(encoded));

            SnapshotCustody.Snapshot var11;
            try {
               int magic = input.readInt();
               if (magic != 1296256339 && magic != 1163150419) {
                  throw new IllegalArgumentException("Unknown custody payload magic");
               }

               int version = input.readInt();
               if (version != 1) {
                  throw new IllegalArgumentException("Unsupported custody payload version " + version);
               }

               int slotCount = input.readInt();
               if (slotCount < 0 || slotCount > 100) {
                  throw new IllegalArgumentException("Invalid custody inventory size " + slotCount);
               }

               ItemStack[] contents = new ItemStack[slotCount];

               for (int index = 0; index < slotCount; index++) {
                  Object item = input.readObject();
                  if (item != null && !(item instanceof ItemStack)) {
                     throw new IllegalArgumentException("Invalid custody item in slot " + index);
                  }

                  contents[index] = item == null ? null : ((ItemStack)item).clone();
               }

               int heldSlot = input.readInt();
               if (heldSlot < 0 || heldSlot > 8) {
                  throw new IllegalArgumentException("Invalid held inventory slot " + heldSlot);
               }

               float flySpeed = input.readFloat();
               if (!Float.isFinite(flySpeed) || flySpeed < -1.0F || flySpeed > 1.0F) {
                  throw new IllegalArgumentException("Invalid fly speed " + flySpeed);
               }

               boolean allowFlight = input.readBoolean();
               boolean flying = input.readBoolean();
               if (flying && !allowFlight) {
                  throw new IllegalArgumentException("Custody state flies without flight permission");
               }

               SnapshotCustody.StoredLocation location = SnapshotCustody.StoredLocation.read(input);
               if (input.read() != -1) {
                  throw new IllegalArgumentException("Custody payload has trailing data");
               }

               var11 = new SnapshotCustody.Snapshot(contents, heldSlot, flySpeed, allowFlight, flying, location);
            } catch (Throwable var13) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            input.close();
            return var11;
         } catch (EOFException var14) {
            throw new IllegalArgumentException("Match custody payload is truncated", var14);
         } catch (ClassNotFoundException | IOException var15) {
            throw new IllegalArgumentException("Could not decode match custody payload", var15);
         }
      } else {
         throw new IllegalArgumentException("Invalid match custody payload size " + encoded.length);
      }
   }

   private static ItemStack[] cloneContents(ItemStack[] contents) {
      ItemStack[] copy = new ItemStack[contents.length];

      for (int index = 0; index < contents.length; index++) {
         copy[index] = contents[index] == null ? null : contents[index].clone();
      }

      return copy;
   }

   @Override
   public void capture(Player player) {
      PersistentDataContainer data = player.getPersistentDataContainer();
      if (data.get(this.key, PersistentDataType.BYTE_ARRAY) != null) {
         throw new IllegalStateException("Player " + player.getUniqueId() + " already has a match custody lease");
      } else {
         byte[] encoded = encode(SnapshotCustody.Snapshot.capture(player));
         data.set(this.key, PersistentDataType.BYTE_ARRAY, encoded);

         try {
            this.flush.accept(player);
         } catch (RuntimeException var5) {
            data.remove(this.key);
            throw new IllegalStateException("Could not persist match custody state for " + player.getUniqueId(), var5);
         }

         this.scoreboards.put(player.getUniqueId(), player.getScoreboard());
      }
   }

   @Override
   public void restore(Player player) {
      this.quarantineOnFailure(player, this.restoreIfOwned(player, false));
   }

   @Override
   public void recover(Player player) {
      this.quarantineOnFailure(player, this.restoreIfOwned(player, true));
   }

   private void quarantineOnFailure(Player player, boolean restored) {
      if (!restored && player.isOnline()) {
         player.kickPlayer(
            ChatColor.RED + "Your pre-match state could not be restored safely. Please rejoin; if this repeats, ask an administrator to check the server log."
         );
      }
   }

   private boolean restoreIfOwned(Player player, boolean returnPlayer) {
      PersistentDataContainer data = player.getPersistentDataContainer();
      byte[] encoded = (byte[])data.get(this.key, PersistentDataType.BYTE_ARRAY);
      if (encoded == null) {
         return true;
      } else {
         try {
            SnapshotCustody.Snapshot snapshot = decode(encoded);
            apply(player, snapshot, this.scoreboards.get(player.getUniqueId()), returnPlayer);
            this.flush.accept(player);
            data.remove(this.key);

            try {
               this.flush.accept(player);
            } catch (RuntimeException var7) {
               data.set(this.key, PersistentDataType.BYTE_ARRAY, encoded);
               throw var7;
            }

            this.scoreboards.remove(player.getUniqueId());
            return true;
         } catch (RuntimeException var8) {
            Logger.warn("Could not restore match custody state for " + player.getUniqueId() + "; the lease was kept: " + var8.getMessage());
            return false;
         }
      }
   }

   private static void apply(Player player, SnapshotCustody.Snapshot snapshot, Scoreboard scoreboard, boolean returnPlayer) {
      int currentSlotCount = player.getInventory().getContents().length;
      if (snapshot.inventoryContents().length != currentSlotCount) {
         throw new IllegalStateException("Custody inventory has " + snapshot.inventoryContents().length + " slots, current inventory has " + currentSlotCount);
      } else {
         player.setFlying(false);
         player.getInventory().clear();
         player.getInventory().setContents(cloneContents(snapshot.inventoryContents()));
         player.getInventory().setHeldItemSlot(snapshot.heldSlot());
         player.setFlySpeed(snapshot.flySpeed());
         player.setAllowFlight(snapshot.allowFlight());
         if (snapshot.allowFlight() && snapshot.flying()) {
            player.setFlying(true);
         }

         if (scoreboard != null) {
            player.setScoreboard(scoreboard);
         }

         if (returnPlayer) {
            Location returnLocation = snapshot.returnLocation().resolve();
            if (returnLocation != null && !player.teleport(returnLocation)) {
               Logger.warn("Return teleport was rejected for " + player.getUniqueId() + "; the inventory and flight state were still restored");
            }
         }
      }
   }

   private record Snapshot(
      ItemStack[] inventoryContents, int heldSlot, float flySpeed, boolean allowFlight, boolean flying, SnapshotCustody.StoredLocation returnLocation
   ) {
      private Snapshot(
         ItemStack[] inventoryContents, int heldSlot, float flySpeed, boolean allowFlight, boolean flying, SnapshotCustody.StoredLocation returnLocation
      ) {
         inventoryContents = SnapshotCustody.cloneContents(inventoryContents);
         this.inventoryContents = inventoryContents;
         this.heldSlot = heldSlot;
         this.flySpeed = flySpeed;
         this.allowFlight = allowFlight;
         this.flying = flying;
         this.returnLocation = returnLocation;
      }

      private static SnapshotCustody.Snapshot capture(Player player) {
         return new SnapshotCustody.Snapshot(
            player.getInventory().getContents(),
            player.getInventory().getHeldItemSlot(),
            player.getFlySpeed(),
            player.getAllowFlight(),
            player.isFlying(),
            SnapshotCustody.StoredLocation.capture(player.getLocation())
         );
      }
   }

   private record StoredLocation(UUID worldId, String worldName, double x, double y, double z, float yaw, float pitch) {
      private static SnapshotCustody.StoredLocation capture(Location location) {
         World world = location.getWorld();
         return new SnapshotCustody.StoredLocation(
            world == null ? null : world.getUID(),
            world == null ? null : world.getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
         );
      }

      private static SnapshotCustody.StoredLocation read(BukkitObjectInputStream input) throws IOException {
         UUID worldId = null;
         String worldName = null;
         if (input.readBoolean()) {
            worldId = new UUID(input.readLong(), input.readLong());
            worldName = input.readUTF();
            if (worldName.isEmpty()) {
               worldName = null;
            }
         }

         double x = input.readDouble();
         double y = input.readDouble();
         double z = input.readDouble();
         float yaw = input.readFloat();
         float pitch = input.readFloat();
         if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) && Float.isFinite(yaw) && Float.isFinite(pitch)) {
            return new SnapshotCustody.StoredLocation(worldId, worldName, x, y, z, yaw, pitch);
         } else {
            throw new IllegalArgumentException("Custody location contains non-finite coordinates");
         }
      }

      private void write(BukkitObjectOutputStream output) throws IOException {
         output.writeBoolean(this.worldId != null);
         if (this.worldId != null) {
            output.writeLong(this.worldId.getMostSignificantBits());
            output.writeLong(this.worldId.getLeastSignificantBits());
            output.writeUTF(this.worldName == null ? "" : this.worldName);
         }

         output.writeDouble(this.x);
         output.writeDouble(this.y);
         output.writeDouble(this.z);
         output.writeFloat(this.yaw);
         output.writeFloat(this.pitch);
      }

      private Location resolve() {
         if (this.worldId == null) {
            return null;
         } else {
            World world = Bukkit.getWorld(this.worldId);
            if (world == null && this.worldName != null) {
               world = Bukkit.getWorld(this.worldName);
            }

            return world == null ? null : new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
         }
      }
   }
}

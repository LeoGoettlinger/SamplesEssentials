package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentAcquisition {
   private final UUID id = UUID.randomUUID();
   private final Player player;
   private final ItemStack source;
   private final ItemStack book;
   private final ItemStack ticket;
   private final UpgradeSystem.Preview preview;
   private final EnchantmentProgression.Quote quote;
   private ItemStack upgraded;
   private EnchantmentAcquisition.State state = EnchantmentAcquisition.State.PREPARED;

   public EnchantmentAcquisition(Player player, ItemStack source, ItemStack book, ItemStack ticket) {
      this.player = player;
      this.source = source.clone();
      this.book = book.clone();
      this.ticket = ticket == null ? null : ticket.clone();
      if (ticket == null || ticket.getAmount() >= 1 && EliteEnchantmentItems.isEliteLuckyTicket(ticket)) {
         this.preview = UpgradeSystem.preview(this.source, this.book);
         this.quote = EnchantmentProgression.quote(this.source, ticket != null);
         this.upgraded = this.preview.apply(this.source, this.book);
      } else {
         throw new IllegalArgumentException("Invalid lucky ticket");
      }
   }

   public EnchantmentProgression.Quote quote() {
      return this.quote;
   }

   public ItemStack original() {
      return single(this.source);
   }

   public ItemStack upgraded() {
      return this.upgraded.clone();
   }

   public boolean isOwned() {
      return this.state == EnchantmentAcquisition.State.OWNED;
   }

   public boolean purchase(Inventory inventory, int sourceSlot, int bookSlot, int ticketSlot, BooleanSupplier menuActive) {
      if (this.state != EnchantmentAcquisition.State.PREPARED) {
         throw new IllegalStateException("Purchase already attempted");
      } else if (!menuActive.getAsBoolean()) {
         throw new IllegalStateException("Menu is no longer active");
      } else {
         this.checkInputs(inventory, sourceSlot, bookSlot, ticketSlot);
         this.validateProviders();
         this.state = EnchantmentAcquisition.State.WITHDRAWING;

         boolean paid;
         try {
            paid = EconomyHandler.tryWithdraw(this.player.getUniqueId(), this.quote.price());
         } catch (RuntimeException var9) {
            this.state = EnchantmentAcquisition.State.RESOLVED;
            this.recovery("withdrawal outcome unknown; inputs remain in menu", "unknown", List.of(), var9);
            return false;
         }

         if (!paid) {
            this.state = EnchantmentAcquisition.State.RESOLVED;
            return false;
         } else {
            try {
               if (!menuActive.getAsBoolean()) {
                  throw new IllegalStateException("Menu closed during payment");
               }

               this.checkInputs(inventory, sourceSlot, bookSlot, ticketSlot);
               this.validateProviders();
            } catch (RuntimeException var10) {
               this.state = EnchantmentAcquisition.State.RESOLVED;
               String refund = this.refund();
               if (!refund.equals("refunded")) {
                  this.recovery("purchase invalidated; inputs remain in menu", refund, List.of(), var10);
               }

               return false;
            }

            inventory.clear(sourceSlot);
            inventory.clear(bookSlot);
            inventory.clear(ticketSlot);
            this.state = EnchantmentAcquisition.State.OWNED;
            return true;
         }
      }
   }

   public void validateProviders() {
      this.upgraded = this.preview.apply(this.source, this.book);
   }

   private void checkInputs(Inventory inventory, int sourceSlot, int bookSlot, int ticketSlot) {
      if (!this.source.equals(inventory.getItem(sourceSlot))
         || !this.book.equals(inventory.getItem(bookSlot))
         || !Objects.equals(this.ticket, inventory.getItem(ticketSlot))
         || !this.player.isOnline()) {
         throw new ConcurrentModificationException("Enchantment inputs changed before purchase");
      }
   }

   public void success() {
      this.settle(this.upgraded, false, "success");
   }

   public void failure() {
      this.settle(single(this.source), false, "gameplay failure");
   }

   public void criticalFailure() {
      this.settle(null, false, "gameplay destruction");
   }

   public void abort(String reason) {
      this.settle(single(this.source), true, reason);
   }

   private void settle(ItemStack result, boolean restore, String reason) {
      if (this.state == EnchantmentAcquisition.State.OWNED) {
         this.state = EnchantmentAcquisition.State.RESOLVED;
         List<ItemStack> delivery = new ArrayList<>();
         remainder(delivery, this.source);
         remainder(delivery, this.book);
         remainder(delivery, this.ticket);
         if (result != null) {
            delivery.add(result.clone());
         }

         if (restore) {
            delivery.add(single(this.book));
            if (this.ticket != null) {
               delivery.add(single(this.ticket));
            }
         }

         String payment = restore ? this.refund() : "charged";
         List<Map<String, Object>> unresolved = new ArrayList<>();
         boolean deliveryFailed = false;

         for (ItemStack item : delivery) {
            if (!deliveryFailed && this.player.isOnline()) {
               try {
                  HashMap<Integer, ItemStack> leftovers = this.player.getInventory().addItem(new ItemStack[]{item.clone()});

                  for (ItemStack leftover : leftovers.values()) {
                     Item dropped = this.player.getWorld().dropItem(this.player.getLocation(), leftover);
                     if (!dropped.isValid()) {
                        throw new IllegalStateException("Item drop was rejected");
                     }
                  }
               } catch (RuntimeException var14) {
                  unresolved.add(Map.of("status", "delivery-unknown", "item", item.clone()));
                  deliveryFailed = true;
               }
            } else {
               unresolved.add(Map.of("status", "not-delivered", "item", item.clone()));
            }
         }

         if (!unresolved.isEmpty() || restore && !payment.equals("refunded")) {
            this.recovery(reason, payment, unresolved, null);
         }
      }
   }

   private String refund() {
      try {
         return EconomyHandler.refundPayment(this.player.getUniqueId(), this.quote.price()) ? "refunded" : "refund-rejected";
      } catch (RuntimeException var2) {
         return "refund-unknown";
      }
   }

   private void recovery(String reason, String payment, List<Map<String, Object>> items, RuntimeException error) {
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.set("transaction", this.id.toString());
      yaml.set("player", this.player.getUniqueId().toString());
      yaml.set("reason", reason);
      yaml.set("price", this.quote.price());
      yaml.set("payment", payment);
      yaml.set("owed-amount", payment.equals("refund-rejected") ? this.quote.price() : 0);
      yaml.set("manual-payment-reconciliation-required", payment.equals("unknown") || payment.equals("refund-unknown"));
      yaml.set("items", items);
      yaml.set("source-snapshot", this.source);
      yaml.set("book-snapshot", this.book);
      yaml.set("ticket-snapshot", this.ticket);
      if (error != null) {
         yaml.set("error", error.toString());
      }

      Path path = MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("enchantment_recovery").resolve(this.id + ".yml");

      try {
         Files.createDirectories(path.getParent());
         Path temp = Files.createTempFile(path.getParent(), ".enchantment-", ".tmp");

         try {
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE)) {
               ByteBuffer bytes = ByteBuffer.wrap(yaml.saveToString().getBytes(StandardCharsets.UTF_8));

               while (bytes.hasRemaining()) {
                  channel.write(bytes);
               }

               channel.force(true);
            }

            try {
               Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException var17) {
               Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
         } finally {
            Files.deleteIfExists(temp);
         }

         MetadataHandler.PLUGIN.getLogger().severe("Enchantment acquisition requires operator recovery: " + path);
      } catch (IOException var21) {
         MetadataHandler.PLUGIN.getLogger().severe("Could not persist enchantment recovery " + this.id + ": " + var21 + "\n" + yaml.saveToString());
      }

      if (this.player.isOnline()) {
         this.player.sendMessage("§cThis enchantment transaction needs administrator recovery. Reference: " + this.id);
      }
   }

   private static void remainder(List<ItemStack> items, ItemStack stack) {
      if (stack != null && stack.getAmount() > 1) {
         ItemStack remainder = stack.clone();
         remainder.setAmount(stack.getAmount() - 1);
         items.add(remainder);
      }
   }

   private static ItemStack single(ItemStack item) {
      ItemStack clone = item.clone();
      clone.setAmount(1);
      return clone;
   }

   private static enum State {
      PREPARED,
      WITHDRAWING,
      OWNED,
      RESOLVED;
   }
}

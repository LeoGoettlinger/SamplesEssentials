package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public final class EnchantmentAnvil {
   private EnchantmentAnvil() {
   }

   public static EnchantmentAnvil.Registration register(Plugin owner, Function<ItemStack, EnchantmentItemProfile> classifier, Function<ItemStack, String> veto) {
      EnchantmentProviders.requireServerThread();
      return new EnchantmentAnvil.Registration(owner, Objects.requireNonNull(classifier), Objects.requireNonNull(veto));
   }

   private static List<EnchantmentAnvil.Peer> peers() {
      ArrayList<EnchantmentAnvil.Peer> peers = new ArrayList<>();

      for (RegisteredServiceProvider<BiFunction> service : Bukkit.getServicesManager().getRegistrations(BiFunction.class)) {
         if (service.getPlugin().isEnabled() && ((BiFunction)service.getProvider()).getClass().getSimpleName().equals("EnchantmentAnvilEndpoint")) {
            BiFunction<String, ItemStack, Map<String, Object>> endpoint = (BiFunction<String, ItemStack, Map<String, Object>>)service.getProvider();
            Map<String, Object> description = endpoint.apply("describe", null);
            if ("nightbreak.enchantment-anvil.v1".equals(description.get("protocol")) && service.getPlugin().getName().equals(description.get("owner"))) {
               peers.add(new EnchantmentAnvil.Peer(service.getPlugin().getName(), endpoint));
            }
         }
      }

      peers.sort(Comparator.comparing(EnchantmentAnvil.Peer::owner));
      return peers;
   }

   private static EnchantmentItemProfile profile(ItemStack item) {
      return profile(item, true);
   }

   static EnchantmentItemProfile profile(ItemStack item, boolean anvilPolicy) {
      EnchantmentItemProfile selected = null;

      for (EnchantmentAnvil.Peer peer : peers()) {
         Map<String, Object> result = peer.endpoint.apply(anvilPolicy ? "policy" : "classify", item.clone());
         if (result.get("denied") instanceof String reason) {
            throw new IllegalArgumentException(reason);
         }

         if (!Boolean.TRUE.equals(result.get("accepted"))) {
            throw new IllegalStateException("Anvil policy unavailable: " + peer.owner);
         }

         if (result.get("type") instanceof String type) {
            HashSet<EnchantmentDefinition.Slot> slots = new HashSet<>();

            for (Object slot : (List)result.get("slots")) {
               slots.add(EnchantmentDefinition.Slot.valueOf((String)slot));
            }

            HashSet<String> attacks = new HashSet<>();

            for (Object attack : (List)result.get("attacks")) {
               attacks.add((String)attack);
            }

            EnchantmentItemProfile candidate = new EnchantmentItemProfile(EnchantmentDefinition.ItemType.valueOf(type), slots, attacks);
            if (selected != null && !selected.equals(candidate)) {
               throw new IllegalArgumentException("Conflicting authored item classification");
            }

            selected = candidate;
         }
      }

      return selected == null ? EnchantmentItemProfile.vanilla(item) : selected;
   }

   private static int priorWork(ItemStack item) {
      int cost = item.getItemMeta() instanceof Repairable repair ? repair.getRepairCost() : 0;
      if (cost < 0) {
         throw new IllegalArgumentException("Invalid prior-work cost");
      } else {
         return cost;
      }
   }

   private static ItemStack consumeOne(ItemStack item) {
      if (item.getAmount() == 1) {
         return null;
      } else {
         ItemStack remainder = item.clone();
         remainder.setAmount(item.getAmount() - 1);
         return remainder;
      }
   }

   private static ItemStack[] planStorage(PlayerInventory inventory, ItemStack item) {
      ItemStack[] slots = inventory.getStorageContents();
      int remaining = item.getAmount();

      for (int i = 0; i < slots.length; i++) {
         if (slots[i] != null && !slots[i].getType().isAir()) {
            slots[i] = slots[i].clone();
            if (slots[i].isSimilar(item)) {
               int transferred = Math.min(remaining, Math.max(0, Math.min(inventory.getMaxStackSize(), slots[i].getMaxStackSize()) - slots[i].getAmount()));
               slots[i].setAmount(slots[i].getAmount() + transferred);
               remaining -= transferred;
            }
         }
      }

      for (int ix = 0; ix < slots.length && remaining > 0; ix++) {
         if (slots[ix] == null || slots[ix].getType().isAir()) {
            slots[ix] = item.clone();
            int transferred = Math.min(remaining, Math.min(inventory.getMaxStackSize(), item.getMaxStackSize()));
            slots[ix].setAmount(transferred);
            remaining -= transferred;
         }
      }

      return remaining == 0 ? slots : null;
   }

   private static final class AnvilListener implements Listener {
      private final EnchantmentItems items = new EnchantmentItems(EnchantmentDefinitions::resolve, EnchantmentAnvil::profile);
      private final EnchantmentAnvilMerge merge = new EnchantmentAnvilMerge(this.items);
      private final Map<Inventory, EnchantmentAnvil.AnvilListener.Prepared> prepared = new IdentityHashMap<>();
      private final Map<Inventory, String> denied = new IdentityHashMap<>();
      private final Set<Inventory> committing = Collections.newSetFromMap(new IdentityHashMap<>());

      void invalidate() {
         for (Inventory inventory : new ArrayList<>(this.prepared.keySet())) {
            inventory.setItem(2, null);
         }

         this.prepared.clear();
         this.denied.clear();
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void prepare(PrepareAnvilEvent event) {
         Inventory inventory = event.getInventory();
         if (this.committing.contains(inventory)) {
            event.setResult(null);
         } else {
            this.prepared.remove(inventory);
            this.denied.remove(inventory);
            ItemStack source = inventory.getItem(0);
            ItemStack book = inventory.getItem(1);
            if (source != null && book != null && book.getType() == Material.ENCHANTED_BOOK) {
               try {
                  if (!this.hasCustom(source) && !this.hasCustom(book)) {
                     return;
                  }

                  EnchantmentAnvil.profile(source);
                  EnchantmentAnvil.profile(book);
                  AnvilView view = event.getView();
                  ItemStack nativeResult = event.getResult();
                  int baseCost;
                  if (nativeResult != null && !nativeResult.getType().isAir()) {
                     baseCost = view.getRepairCost();
                  } else {
                     baseCost = Math.addExact(EnchantmentAnvil.priorWork(source), EnchantmentAnvil.priorWork(book));
                     String oldName = source.getItemMeta().hasDisplayName() ? source.getItemMeta().getDisplayName() : "";
                     String rename = Objects.requireNonNullElse(view.getRenameText(), "");
                     if (!oldName.equals(rename)) {
                        baseCost = Math.addExact(baseCost, 1);
                     }

                     nativeResult = null;
                  }

                  EnchantmentAnvilMerge.Preview candidate = this.merge.prepare(source, book, nativeResult, baseCost);
                  ItemStack shown = candidate.item();
                  if (nativeResult == null) {
                     ItemMeta meta = shown.getItemMeta();
                     String rename = view.getRenameText();
                     meta.setDisplayName(rename != null && !rename.isBlank() ? rename : null);
                     if (meta instanceof Repairable repair) {
                        repair.setRepairCost(
                           Math.addExact(Math.multiplyExact(Math.max(EnchantmentAnvil.priorWork(source), EnchantmentAnvil.priorWork(book)), 2), 1)
                        );
                     }

                     shown.setItemMeta(meta);
                  }

                  view.setRepairCost(candidate.cost());
                  boolean creative = event.getView().getPlayer().getGameMode() == GameMode.CREATIVE;
                  if (!creative && candidate.cost() >= view.getMaximumRepairCost()) {
                     throw new IllegalArgumentException("Enchantment cost exceeds the anvil limit");
                  }

                  this.prepared.put(inventory, new EnchantmentAnvil.AnvilListener.Prepared(candidate, shown.clone(), view.getRenameText(), candidate.cost()));
                  event.setResult(shown);
               } catch (RuntimeException var13) {
                  this.denied.put(inventory, Objects.requireNonNullElse(var13.getMessage(), "Invalid enchantment combination"));
                  event.setResult(null);
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST,
         ignoreCancelled = true
      )
      public void pickup(InventoryClickEvent event) {
         if (event.getView() instanceof AnvilView view && event.getRawSlot() == 2) {
            Inventory inventory = view.getTopInventory();
            EnchantmentAnvil.AnvilListener.Prepared attempt = this.prepared.get(inventory);
            if (attempt != null || this.denied.containsKey(inventory)) {
               event.setCancelled(true);
               if (attempt == null) {
                  event.getWhoClicked().sendMessage("§c" + this.denied.get(inventory));
               } else if (event.getWhoClicked() instanceof Player player && this.committing.add(inventory)) {
                  try {
                     if (!Objects.equals(attempt.rename, view.getRenameText())
                        || attempt.cost != view.getRepairCost()
                        || !attempt.shown.equals(inventory.getItem(2))) {
                        throw new ConcurrentModificationException("Anvil result changed");
                     }

                     ItemStack source = inventory.getItem(0);
                     ItemStack book = inventory.getItem(1);
                     EnchantmentAnvil.profile(source);
                     EnchantmentAnvil.profile(book);
                     attempt.merge.apply(source, book);
                     boolean creative = player.getGameMode() == GameMode.CREATIVE;
                     if (!creative && (attempt.cost >= view.getMaximumRepairCost() || player.getLevel() < attempt.cost)) {
                        return;
                     }

                     boolean shift = event.isShiftClick();
                     if (!shift && event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
                        return;
                     }

                     ItemStack[] storage = null;
                     ItemStack cursor = event.getCursor();
                     if (shift) {
                        storage = EnchantmentAnvil.planStorage(player.getInventory(), attempt.shown);
                        if (storage == null) {
                           return;
                        }
                     } else if (cursor != null
                        && !cursor.getType().isAir()
                        && (!cursor.isSimilar(attempt.shown) || cursor.getAmount() + attempt.shown.getAmount() > cursor.getMaxStackSize())) {
                        return;
                     }

                     this.prepared.remove(inventory);
                     inventory.setItem(2, null);
                     inventory.setItem(0, EnchantmentAnvil.consumeOne(source));
                     inventory.setItem(1, EnchantmentAnvil.consumeOne(book));
                     if (!creative) {
                        player.giveExpLevels(-attempt.cost);
                     }

                     if (shift) {
                        player.getInventory().setStorageContents(storage);
                     } else {
                        ItemStack result = attempt.shown.clone();
                        if (cursor != null && !cursor.getType().isAir()) {
                           result.setAmount(result.getAmount() + cursor.getAmount());
                        }

                        player.setItemOnCursor(result);
                     }
                  } catch (RuntimeException var16) {
                     this.prepared.remove(inventory);
                     inventory.setItem(2, null);
                     player.sendMessage("§cThe enchantment result changed; place the book again.");
                     return;
                  } finally {
                     this.committing.remove(inventory);
                  }
               }
            }
         }
      }

      @EventHandler
      public void closed(InventoryCloseEvent event) {
         this.prepared.remove(event.getInventory());
         this.denied.remove(event.getInventory());
      }

      private boolean hasCustom(ItemStack item) {
         return !EnchantmentItems.inspectCustom(item.getItemMeta()).isEmpty();
      }

      private record Prepared(EnchantmentAnvilMerge.Preview merge, ItemStack shown, String rename, int cost) {
      }
   }

   private record Peer(String owner, BiFunction<String, ItemStack, Map<String, Object>> endpoint) {
   }

   public static final class Registration implements AutoCloseable, Listener {
      private final EnchantmentAnvilEndpoint endpoint;
      private EnchantmentAnvil.AnvilListener listener;
      private boolean closed;

      private Registration(Plugin owner, Function<ItemStack, EnchantmentItemProfile> classifier, Function<ItemStack, String> veto) {
         if (!owner.isEnabled()) {
            throw new IllegalStateException("Anvil provider is disabled");
         } else if (EnchantmentAnvil.peers().stream().anyMatch(peer -> peer.owner.equals(owner.getName()))) {
            throw new IllegalStateException("Anvil policy already registered for " + owner.getName());
         } else {
            this.endpoint = new EnchantmentAnvilEndpoint(owner, classifier, veto);
            Bukkit.getPluginManager().registerEvents(this, owner);
            Bukkit.getServicesManager().register(BiFunction.class, this.endpoint, owner, ServicePriority.Normal);
            this.reconcile();
         }
      }

      @EventHandler
      public void registered(ServiceRegisterEvent event) {
         if (event.getProvider().getService() == BiFunction.class) {
            this.reconcile();
         }
      }

      @EventHandler
      public void unregistered(ServiceUnregisterEvent event) {
         if (event.getProvider().getService() == BiFunction.class) {
            this.reconcile();
         }
      }

      @EventHandler
      public void disabled(PluginDisableEvent event) {
         if (event.getPlugin() == this.endpoint.owner) {
            this.close();
         } else {
            this.reconcile();
         }
      }

      private void reconcile() {
         if (!this.closed) {
            if (this.listener != null) {
               this.listener.invalidate();
            }

            List<EnchantmentAnvil.Peer> peers = EnchantmentAnvil.peers();
            boolean elected = !peers.isEmpty() && peers.getFirst().endpoint == this.endpoint;
            if (!elected && this.listener != null) {
               HandlerList.unregisterAll(this.listener);
               this.listener = null;
            }

            if (elected && this.listener == null) {
               this.listener = new EnchantmentAnvil.AnvilListener();
               Bukkit.getPluginManager().registerEvents(this.listener, this.endpoint.owner);
            }
         }
      }

      @Override
      public void close() {
         EnchantmentProviders.requireServerThread();
         if (!this.closed) {
            this.closed = true;
            this.endpoint.active = false;
            if (this.listener != null) {
               this.listener.invalidate();
               HandlerList.unregisterAll(this.listener);
               this.listener = null;
            }

            HandlerList.unregisterAll(this);
            Bukkit.getServicesManager().unregister(BiFunction.class, this.endpoint);
         }
      }
   }
}

package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

final class ClassMenuInventoryAdapter implements ClassMenuRenderer, Listener {
   private static final int MENU_SIZE = 54;
   private static final int[] ROOT_SLOTS = new int[]{20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 34, 35};
   private final PlayerActionTokenRegistry tokens;
   private final Map<Inventory, ClassMenuInventoryAdapter.InventorySession> sessions = new HashMap<>();
   private ClassMenuCoordinator coordinator;

   ClassMenuInventoryAdapter(PlayerActionTokenRegistry tokens) {
      this.tokens = tokens;
   }

   void installCoordinator(ClassMenuCoordinator coordinator) {
      if (this.coordinator != null) {
         throw new IllegalStateException("Class menu coordinator already installed");
      } else {
         this.coordinator = coordinator;
      }
   }

   @Override
   public void showOverview(Player player, ClassMenuView view) {
      ClassMenuPresentation page = ClassMenuPresenter.overview(view);
      Inventory inventory = inventory(player, "&6Classes");
      ClassMenuInventoryAdapter.InventorySession session = new ClassMenuInventoryAdapter.InventorySession(player.getUniqueId());
      inventory.setItem(4, item(Material.NETHER_STAR, page.bodyLines().get(0), List.of()));
      int rootIndex = 0;

      for (ClassMenuPresentation.ActionView action : page.actions()) {
         if (action.kind() == ClassMenuPresentation.ActionKind.FORM) {
            if (rootIndex < ROOT_SLOTS.length) {
               ClassMenuAction.OpenForm openForm = (ClassMenuAction.OpenForm)action.action();
               ClassMenuView.FormView root = view.requireForm(openForm.formId());
               this.action(inventory, session, ROOT_SLOTS[rootIndex++], rootMaterial(root), action.label(), tooltipLore(action), player, action.action());
            }
         } else if (action.kind() == ClassMenuPresentation.ActionKind.CONTROLS) {
            this.action(inventory, session, 40, Material.WRITABLE_BOOK, action.label(), tooltipLore(action), player, action.action());
         } else if (action.kind() == ClassMenuPresentation.ActionKind.DEACTIVATE) {
            this.action(inventory, session, 45, Material.RED_STAINED_GLASS_PANE, action.label(), tooltipLore(action), player, action.action());
         }
      }

      inventory.setItem(49, item(Material.BARRIER, "&cClose", List.of("&7Close this menu.")));
      this.open(player, inventory, session);
   }

   @Override
   public void showForm(Player player, ClassMenuView view, ClassMenuView.FormView form, boolean showAllClasses) {
      ClassMenuPresentation page = ClassMenuPresenter.form(view, form, showAllClasses);
      Inventory inventory = inventory(player, "&6Class: " + form.displayName());
      ClassMenuInventoryAdapter.InventorySession session = new ClassMenuInventoryAdapter.InventorySession(player.getUniqueId());
      List<String> summary = new ArrayList<>();
      if (form.lineage().size() > 1) {
         summary.add("&7" + String.join(" &8> &7", form.lineage()));
      }

      summary.add(ClassMenuStyle.state(form));
      if (form.unlocked()) {
         summary.add("&7Level &8• &f" + form.effectiveLevel() + "/" + form.effectiveCap());
         summary.add("&7XP &8• &f" + form.xpSummary());
      }

      summary.add(ClassMenuStyle.themed(form, form.resourceName()) + " &8• &7" + form.resourceDescription());
      inventory.setItem(13, item(rootMaterial(form), ClassMenuStyle.themed(form, form.displayName()), summary));
      inventory.setItem(20, abilityItem(Material.FEATHER, ClassMenuStyle.section("#355CA8:#5FA9E8", "Mobility"), form.mobility()));
      inventory.setItem(22, abilityItem(Material.NETHER_STAR, ClassMenuStyle.section("#A63D2F:#E97932", "Signature"), form.signature()));
      inventory.setItem(24, abilityItem(Material.COMPASS, ClassMenuStyle.section("#2E7D4F:#69C56F", "Utility"), form.utility()));
      inventory.setItem(29, requirementsItem(form));
      inventory.setItem(33, passivesItem(form));
      int childIndex = 0;
      int[] childSlots = new int[]{16, 17};

      for (ClassMenuPresentation.ActionView action : page.actions()) {
         switch (action.kind()) {
            case PARENT:
               this.action(inventory, session, 9, Material.ARROW, action.label(), tooltipLore(action), player, action.action());
               break;
            case FORM:
               if (childIndex < childSlots.length) {
                  ClassMenuAction.OpenForm openForm = (ClassMenuAction.OpenForm)action.action();
                  ClassMenuView.FormView child = view.requireForm(openForm.formId());
                  this.action(inventory, session, childSlots[childIndex++], rootMaterial(child), action.label(), tooltipLore(action), player, action.action());
               }
               break;
            case SELECT:
               this.action(inventory, session, 40, Material.GREEN_STAINED_GLASS_PANE, action.label(), tooltipLore(action), player, action.action());
               break;
            case OVERVIEW:
               this.action(inventory, session, 49, Material.ARROW, action.label(), tooltipLore(action), player, action.action());
         }
      }

      this.open(player, inventory, session);
   }

   @Override
   public void showControls(Player player, ClassMenuView view) {
      ClassMenuPresentation page = ClassMenuPresenter.controls(view);
      Inventory inventory = inventory(player, "&6Class Ability Controls");
      ClassMenuInventoryAdapter.InventorySession session = new ClassMenuInventoryAdapter.InventorySession(player.getUniqueId());
      inventory.setItem(4, item(Material.WRITABLE_BOOK, ClassMenuStyle.title("Ability Controls"), page.bodyLines()));

      for (ClassMenuPresentation.ActionView action : page.actions()) {
         switch (action.kind()) {
            case OVERVIEW:
               this.action(inventory, session, 49, Material.ARROW, action.label(), tooltipLore(action), player, action.action());
         }
      }

      this.open(player, inventory, session);
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      Inventory top = event.getView().getTopInventory();
      ClassMenuInventoryAdapter.InventorySession session = this.sessions.get(top);
      if (session != null) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player player) {
            if (session.playerId().equals(player.getUniqueId())) {
               int rawSlot = event.getRawSlot();
               if (rawSlot >= 0 && rawSlot < top.getSize()) {
                  if (rawSlot == 49 && session.tokensBySlot().get(rawSlot) == null) {
                     player.closeInventory();
                  } else {
                     String token = session.tokensBySlot().get(rawSlot);
                     if (token != null && this.coordinator != null) {
                        this.coordinator.dispatch(player, token);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryDrag(InventoryDragEvent event) {
      Inventory top = event.getView().getTopInventory();
      if (this.sessions.containsKey(top)) {
         if (event.getRawSlots().stream().anyMatch(slot -> slot < top.getSize())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      this.sessions.remove(event.getView().getTopInventory());
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.sessions.entrySet().removeIf(entry -> entry.getValue().playerId().equals(playerId));
      this.tokens.invalidate(playerId);
   }

   void shutdown() {
      this.sessions.clear();
   }

   private static Inventory inventory(Player player, String title) {
      return Bukkit.createInventory(player, 54, ChatColorConverter.convert(title));
   }

   private void open(Player player, Inventory inventory, ClassMenuInventoryAdapter.InventorySession session) {
      player.openInventory(inventory);
      this.sessions.put(inventory, session);
   }

   private void action(
      Inventory inventory,
      ClassMenuInventoryAdapter.InventorySession session,
      int slot,
      Material material,
      String name,
      List<String> lore,
      Player player,
      ClassMenuAction action
   ) {
      inventory.setItem(slot, item(material, name, lore));
      session.tokensBySlot().put(slot, this.tokens.issue(player.getUniqueId(), action));
   }

   private static ItemStack item(Material material, String name, List<String> lore) {
      return ItemStackGenerator.generateItemStack(material, name, lore);
   }

   private static ItemStack abilityItem(Material material, String heading, ClassMenuView.AbilityView ability) {
      return item(material, heading, abilityLore(ability));
   }

   static List<String> abilityLore(ClassMenuView.AbilityView ability) {
      return List.of("&f" + ability.displayName(), "", "&7" + ability.description());
   }

   private static ItemStack requirementsItem(ClassMenuView.FormView form) {
      List<String> lore = new ArrayList<>();

      for (ClassMenuView.FoundationRequirement requirement : form.requirements()) {
         lore.add((requirement.met() ? "&a" : "&c") + requirement.displayName() + " " + requirement.currentLevel() + "/" + requirement.requiredLevel());
      }

      if (!form.blockers().isEmpty()) {
         lore.add("");

         for (ClassMenuView.BlockerView blocker : form.blockers()) {
            lore.addAll(ClassMenuText.wrap("", ClassMenuStyle.blockerText(blocker)));
         }
      }

      return item(Material.EXPERIENCE_BOTTLE, ClassMenuStyle.section("#B8860B:#F0C040", "Skills"), lore);
   }

   private static ItemStack passivesItem(ClassMenuView.FormView form) {
      List<String> lore = new ArrayList<>();

      for (ClassMenuView.PassiveView passive : form.passives()) {
         if (!lore.isEmpty()) {
            lore.add("");
         }

         lore.addAll(ClassMenuBonusText.lines(form, passive));
      }

      return item(Material.ENCHANTED_BOOK, ClassMenuStyle.section("#6D3AA8:#A855F7", "Bonuses"), lore);
   }

   private static List<String> tooltipLore(ClassMenuPresentation.ActionView action) {
      return List.of(action.tooltip().split("\\n"));
   }

   private static Material rootMaterial(ClassMenuView.FormView form) {
      String var1 = form.resourceName();

      return switch (var1) {
         case "Resolve" -> Material.SHIELD;
         case "Fury" -> Material.IRON_AXE;
         case "Focus" -> Material.BOW;
         case "Grace" -> Material.GHAST_TEAR;
         case "Mana" -> Material.BLAZE_ROD;
         default -> Material.NETHER_STAR;
      };
   }

   private record InventorySession(UUID playerId, Map<Integer, String> tokensBySlot) {
      private InventorySession(UUID playerId) {
         this(playerId, new HashMap<>());
      }
   }
}

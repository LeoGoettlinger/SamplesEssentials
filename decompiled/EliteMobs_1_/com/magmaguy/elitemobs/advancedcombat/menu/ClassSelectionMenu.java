package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.menus.MenuPresentation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public final class ClassSelectionMenu {
   private static final PlayerActionTokenRegistry TOKENS = new PlayerActionTokenRegistry();
   private static final ClassMenuDialogAdapter DIALOGS = new ClassMenuDialogAdapter(TOKENS);
   private static final ClassMenuInventoryAdapter INVENTORIES = new ClassMenuInventoryAdapter(TOKENS);
   private static final ClassMenuCoordinator COORDINATOR = new ClassMenuCoordinator(TOKENS, DIALOGS, INVENTORIES);
   private static final Collection<AdvancedCommand> COMMANDS = List.of(new ClassMenuActionCommand(COORDINATOR));

   private ClassSelectionMenu() {
   }

   public static void open(Player player) {
      COORDINATOR.open(Objects.requireNonNull(player, "player"));
   }

   public static void openForm(Player player, String formId) {
      COORDINATOR.openForm(Objects.requireNonNull(player, "player"), formId);
   }

   public static void openTrainer(Player player, String rootId) {
      COORDINATOR.openForm(Objects.requireNonNull(player, "player"), rootId, false);
   }

   public static Collection<AdvancedCommand> commands() {
      return COMMANDS;
   }

   public static Listener listener() {
      return INVENTORIES;
   }

   public static void shutdown() {
      INVENTORIES.shutdown();
      TOKENS.clear();
   }

   static boolean supportsDialogs(Player player) {
      return MenuPresentation.supportsDialogs(player);
   }

   static {
      INVENTORIES.installCoordinator(COORDINATOR);
   }
}

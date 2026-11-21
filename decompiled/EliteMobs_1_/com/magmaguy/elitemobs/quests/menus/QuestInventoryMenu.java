package com.magmaguy.elitemobs.quests.menus;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.statusscreen.CoverPage;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class QuestInventoryMenu {
   private static final int trackEntry = 8;
   private static final int acceptEntry = 26;
   private static final int directoryBackEntry = 26;
   private static final int questBackEntry = 0;
   private static final int directoryPreviousEntry = 18;
   private static final int directoryNextEntry = 22;
   private static final int detailPreviousEntry = 2;
   private static final int detailNextEntry = 6;
   private static final List<Integer> questSlots = List.of(13, 11, 15, 9, 17, 10, 16, 12, 14, 8);
   private static final HashMap<Inventory, QuestInventoryMenu.QuestDirectory> questDirectories = new HashMap<>();
   private static final HashMap<Inventory, QuestInventoryMenu.QuestInventory> questInventories = new HashMap<>();

   public static void shutdown() {
      List<Inventory> ownedInventories = new ArrayList<>(questDirectories.keySet());
      ownedInventories.addAll(questInventories.keySet());

      for (Inventory inventory : ownedInventories) {
         for (HumanEntity viewer : new ArrayList(inventory.getViewers())) {
            viewer.closeInventory();
         }
      }

      questDirectories.clear();
      questInventories.clear();
   }

   private QuestInventoryMenu() {
   }

   public static void generateInventoryQuestEntries(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
      generateInventoryQuestEntries(quests, player, npcEntity, false);
   }

   public static boolean isQuestInventory(Inventory inventory) {
      return questDirectories.containsKey(inventory) || questInventories.containsKey(inventory);
   }

   public static void generateInventoryQuestEntries(List<? extends Quest> quests, Player player, NPCEntity npcEntity, boolean returnToPlayerStatus) {
      if (quests.size() == 1) {
         generateInventoryQuestEntry(quests.get(0), player, npcEntity, returnToPlayerStatus);
      } else {
         generateInventoryQuestDirectory(quests, player, npcEntity, returnToPlayerStatus);
      }
   }

   public static void generateInventoryQuestDirectory(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
      generateInventoryQuestDirectory(quests, player, npcEntity, false);
   }

   public static void generateInventoryQuestDirectory(List<? extends Quest> quests, Player player, NPCEntity npcEntity, boolean returnToPlayerStatus) {
      String menuTitle = "Quests";
      Inventory questInventory = Bukkit.createInventory(player, 27, menuTitle);
      QuestInventoryMenu.QuestDirectory questDirectory = new QuestInventoryMenu.QuestDirectory(
         player, List.copyOf(quests), questInventory, npcEntity, returnToPlayerStatus
      );
      renderDirectory(questDirectory);
      if (player.openInventory(questInventory) != null) {
         questDirectories.put(questInventory, questDirectory);
      }
   }

   private static void renderDirectory(QuestInventoryMenu.QuestDirectory menu) {
      menu.inventory.clear();
      menu.questMap.clear();
      int first = menu.page * questSlots.size();

      for (int index = first; index < Math.min(first + questSlots.size(), menu.quests.size()); index++) {
         Quest quest = menu.quests.get(index);
         int slot = questSlots.get(index - first);
         menu.questMap.put(slot, quest);
         Material material = !quest.isAccepted()
            ? Material.GREEN_STAINED_GLASS_PANE
            : (!quest.getQuestObjectives().isOver() ? Material.RED_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE);
         menu.inventory.setItem(slot, ItemStackGenerator.generateItemStack(material, QuestMenu.generateHeader(quest).toPlainText()));
      }

      if (menu.returnToPlayerStatus) {
         menu.inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
      }

      renderNavigation(menu.inventory, menu.page, menu.pageCount(), 18, 22);
   }

   private static void renderNavigation(Inventory inventory, int page, int pageCount, int previousSlot, int nextSlot) {
      List<String> indicator = List.of(page + 1 + " / " + pageCount);
      inventory.setItem(
         previousSlot, page > 0 ? ItemStackGenerator.generateItemStack(Material.ARROW, QuestsConfig.getPreviousInventoryPage(), indicator) : null
      );
      inventory.setItem(
         nextSlot, page + 1 < pageCount ? ItemStackGenerator.generateItemStack(Material.ARROW, QuestsConfig.getNextInventoryPage(), indicator) : null
      );
   }

   public static void generateInventoryQuestEntry(Quest quest, Player player, NPCEntity npcEntity) {
      generateInventoryQuestEntry(quest, player, npcEntity, false);
   }

   public static void generateInventoryQuestEntry(Quest quest, Player player, NPCEntity npcEntity, boolean returnToPlayerStatus) {
      QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);
      String title = "";
      if (questText.getHeader().toPlainText() != null) {
         title = questText.getHeader().toPlainText();
      }

      Inventory questInventory = Bukkit.createInventory(player, 27, title);
      int titleEntry = 4;
      List<Integer> loreEntries = new ArrayList<>(new ArrayList<>(List.of(13, 14, 12, 15, 11, 16, 10, 17, 9)));
      List<Integer> objectivesEntries = new ArrayList<>(new ArrayList<>(List.of(21, 20, 19, 18)));
      List<Integer> rewardEntries = new ArrayList<>(new ArrayList<>(List.of(23, 24, 25)));
      Material titleMaterial = Material.PAINTING;
      Material trackingMaterial = Material.TARGET;
      Material loreMaterial = Material.BOOK;
      Material objectivesMaterial = Material.ITEM_FRAME;
      Material rewardsMaterial = Material.GOLD_INGOT;
      Material acceptMaterial = Material.EMERALD;
      questInventory.setItem(titleEntry, generateItemStackEntry(questText.getHeader(), new TextComponent(), titleMaterial).get(0));
      if (quest instanceof CustomQuest && quest.isAccepted()) {
         questInventory.setItem(8, generateItemStackEntry(questText.getTrack(), new TextComponent(), trackingMaterial).get(0));
      }

      questInventory.setItem(26, generateItemStackEntry(questText.getAccept(), new TextComponent(), acceptMaterial).get(0));
      List<QuestInventoryMenu.CardSection> sections = new ArrayList<>();
      if (quest instanceof CustomQuest) {
         sections.add(new QuestInventoryMenu.CardSection(loreEntries, generateItemStackEntry(new TextComponent(" "), questText.getBody(), loreMaterial)));
      }

      sections.add(
         new QuestInventoryMenu.CardSection(objectivesEntries, generateItemStackEntry(questText.getFixedSummary(), questText.getSummary(), objectivesMaterial))
      );
      sections.add(
         new QuestInventoryMenu.CardSection(rewardEntries, generateItemStackEntry(questText.getFixedRewards(), questText.getRewards(), rewardsMaterial))
      );
      if (returnToPlayerStatus) {
         questInventory.setItem(0, PlayerStatusMenuConfig.getBackItem());
      }

      QuestInventoryMenu.QuestInventory questMenu = new QuestInventoryMenu.QuestInventory(
         player, quest, questInventory, npcEntity, returnToPlayerStatus, sections
      );
      renderDetail(questMenu);
      if (player.openInventory(questInventory) != null) {
         questInventories.put(questInventory, questMenu);
      }
   }

   private static void renderDetail(QuestInventoryMenu.QuestInventory menu) {
      for (QuestInventoryMenu.CardSection section : menu.sections) {
         for (int slot : section.slots()) {
            menu.inventory.setItem(slot, null);
         }

         int first = menu.page * section.slots().size();
         int count = Math.min(section.slots().size(), Math.max(0, section.cards().size() - first));
         List<Integer> slots = new ArrayList<>(section.slots().subList(0, count));
         Collections.sort(slots);

         for (int i = 0; i < count; i++) {
            menu.inventory.setItem(slots.get(i), section.cards().get(first + i));
         }
      }

      renderNavigation(menu.inventory, menu.page, menu.pageCount(), 2, 6);
   }

   public static List<ItemStack> generateItemStackEntry(TextComponent title, List<TextComponent> textComponents, Material material) {
      List<String> list = new ArrayList<>();
      textComponents.forEach(
         component -> list.add(ChatColor.WHITE + ChatColorConverter.convert(component.toPlainText().replace(ChatColor.BLACK + "", ChatColor.WHITE + "")))
      );
      return generateParsedItemStackEntry(title.toPlainText(), list, material);
   }

   public static List<ItemStack> generateItemStackEntry(TextComponent title, TextComponent textComponent, Material material) {
      return generateParsedItemStackEntry(title.toPlainText(), Collections.singletonList(textComponent.toPlainText()), material);
   }

   public static List<ItemStack> generateParsedItemStackEntry(String title, List<String> rawLore, Material material) {
      title = title.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString());
      int characterLimit = Math.max(1, QuestsConfig.getItemEntryCharacterLimitBedrockMenu());
      int lineLimit = Math.max(1, Math.min(characterLimit, QuestsConfig.getHorizontalCharacterLimitBedrockMenu()));
      List<ItemStack> items = new ArrayList<>();
      List<String> card = new ArrayList<>();
      int used = 0;

      for (String raw : rawLore) {
         for (String line : wrapLore(raw.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString()), lineLimit)) {
            String plain = ChatColor.stripColor(line);
            int size = plain.codePointCount(0, plain.length());
            if (!card.isEmpty() && used + size > characterLimit) {
               items.add(ItemStackGenerator.generateItemStack(material, title, card));
               card = new ArrayList<>();
               used = 0;
            }

            card.add(line);
            used += size;
         }
      }

      if (!card.isEmpty() || items.isEmpty()) {
         items.add(ItemStackGenerator.generateItemStack(material, title, card));
      }

      return items;
   }

   private static List<String> wrapLore(String text, int limit) {
      List<String> lines = new ArrayList<>();
      String color = ChatColor.WHITE.toString();

      for (String paragraph : text.split("\\R", -1)) {
         if (paragraph.isEmpty()) {
            lines.add(color);
         }

         int start = 0;

         while (start < paragraph.length()) {
            int end = start;
            int visible = 0;
            int space = -1;

            while (end < paragraph.length() && visible < limit) {
               char character = paragraph.charAt(end);
               if (character == 167 && end + 1 < paragraph.length()) {
                  end += 2;
               } else {
                  if (character == ' ') {
                     space = end;
                  }

                  end += Character.charCount(paragraph.codePointAt(end));
                  visible++;
               }
            }

            if (end < paragraph.length() && space > start) {
               end = space + 1;
            }

            String line = color + paragraph.substring(start, end);
            lines.add(line);
            color = ChatColor.getLastColors(line);
            start = end;
         }
      }

      return lines;
   }

   private record CardSection(List<Integer> slots, List<ItemStack> cards) {
   }

   private static class QuestDirectory {
      final HashMap<Integer, Quest> questMap = new HashMap<>();
      final List<? extends Quest> quests;
      int page;
      Inventory inventory;
      NPCEntity npcEntity;
      Player player;
      boolean returnToPlayerStatus;

      private QuestDirectory(Player player, List<? extends Quest> quests, Inventory inventory, NPCEntity npcEntity, boolean returnToPlayerStatus) {
         this.quests = quests;
         this.inventory = inventory;
         this.npcEntity = npcEntity;
         this.player = player;
         this.returnToPlayerStatus = returnToPlayerStatus;
      }

      private int pageCount() {
         return Math.max(1, (this.quests.size() + QuestInventoryMenu.questSlots.size() - 1) / QuestInventoryMenu.questSlots.size());
      }
   }

   private static class QuestInventory {
      final List<QuestInventoryMenu.CardSection> sections;
      int page;
      Quest quest;
      Inventory inventory;
      NPCEntity npcEntity;
      Player player;
      boolean returnToPlayerStatus;

      private QuestInventory(
         Player player, Quest quest, Inventory inventory, NPCEntity npcEntity, boolean returnToPlayerStatus, List<QuestInventoryMenu.CardSection> sections
      ) {
         this.sections = sections;
         this.quest = quest;
         this.inventory = inventory;
         this.npcEntity = npcEntity;
         this.player = player;
         this.returnToPlayerStatus = returnToPlayerStatus;
      }

      private int pageCount() {
         return this.sections.stream().mapToInt(section -> (section.cards().size() + section.slots().size() - 1) / section.slots().size()).max().orElse(1);
      }
   }

   public static class QuestInventoryMenuEvents implements Listener {
      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onInventoryInteract(InventoryClickEvent event) {
         Player player = ((Player)event.getWhoClicked()).getPlayer();
         if (QuestInventoryMenu.questDirectories.containsKey(event.getInventory())) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
               return;
            }

            QuestInventoryMenu.QuestDirectory questDirectory = QuestInventoryMenu.questDirectories.get(event.getInventory());
            if (event.getSlot() == 18 && questDirectory.page > 0) {
               questDirectory.page--;
               QuestInventoryMenu.renderDirectory(questDirectory);
               return;
            }

            if (event.getSlot() == 22 && questDirectory.page + 1 < questDirectory.pageCount()) {
               questDirectory.page++;
               QuestInventoryMenu.renderDirectory(questDirectory);
               return;
            }

            if (questDirectory.returnToPlayerStatus && event.getSlot() == 26) {
               player.closeInventory();
               CoverPage.coverPage(player);
               return;
            }

            if (questDirectory.questMap.get(event.getSlot()) == null) {
               return;
            }

            player.closeInventory();
            QuestInventoryMenu.generateInventoryQuestEntry(
               questDirectory.questMap.get(event.getSlot()), questDirectory.player, questDirectory.npcEntity, questDirectory.returnToPlayerStatus
            );
         } else if (QuestInventoryMenu.questInventories.containsKey(event.getInventory())) {
            event.setCancelled(true);
            if (event.getClickedInventory() != event.getView().getTopInventory()) {
               return;
            }

            QuestInventoryMenu.QuestInventory questInventory = QuestInventoryMenu.questInventories.get(event.getInventory());
            if (event.getSlot() == 2 && questInventory.page > 0) {
               questInventory.page--;
               QuestInventoryMenu.renderDetail(questInventory);
               return;
            }

            if (event.getSlot() == 6 && questInventory.page + 1 < questInventory.pageCount()) {
               questInventory.page++;
               QuestInventoryMenu.renderDetail(questInventory);
               return;
            }

            if (questInventory.returnToPlayerStatus && event.getSlot() == 0) {
               player.closeInventory();
               CoverPage.coverPage(player);
               return;
            }

            switch (event.getSlot()) {
               case 8:
                  QuestCommand.trackQuest(QuestInventoryMenu.questInventories.get(event.getInventory()).quest.getQuestID().toString(), player);
                  player.closeInventory();
                  break;
               case 26:
                  Quest quest = QuestInventoryMenu.questInventories.get(event.getInventory()).quest;
                  if (!quest.isAccepted()) {
                     QuestCommand.joinQuest(quest.getQuestID().toString(), player);
                  } else if (!quest.getQuestObjectives().isOver()) {
                     QuestCommand.leaveQuest(player, quest.getQuestID().toString());
                  } else {
                     QuestCommand.completeQuest(quest.getQuestID().toString(), player);
                  }

                  player.closeInventory();
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onInventoryClose(InventoryCloseEvent event) {
         QuestInventoryMenu.questDirectories.remove(event.getInventory());
         QuestInventoryMenu.questInventories.remove(event.getInventory());
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onInventoryDrag(InventoryDragEvent event) {
         Inventory topInventory = event.getView().getTopInventory();
         if (QuestInventoryMenu.questDirectories.containsKey(topInventory) || QuestInventoryMenu.questInventories.containsKey(topInventory)) {
            if (event.getRawSlots().stream().anyMatch(slot -> slot < topInventory.getSize())) {
               event.setCancelled(true);
            }
         }
      }
   }
}

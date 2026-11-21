package com.magmaguy.elitemobs.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.magmacore.dialog.DialogManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.quests.menus.QuestScreenSession;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import java.util.List;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DialogMaker {
   private static final int questDialogWidth = 300;

   private DialogMaker() {
   }

   private static String processText(String text) {
      return text == null ? null : text.replace("§0", "§f").replace("&0", "&f").replace("\r", "");
   }

   private static String processSingleLineText(String text) {
      String processedText = processText(text);
      return processedText == null ? null : processedText.replace('\n', ' ').replaceAll("\\s+", " ").trim();
   }

   public static void sendQuestMessage(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
      if (!quests.isEmpty()) {
         if (quests.size() == 1) {
            DialogManager.sendDialog(player, buildQuestDialogBuilder(quests, 0, player, npcEntity, false), QuestScreenSession.DIALOG_OWNER);
         } else {
            showQuestListDialog(quests, player, npcEntity);
         }
      }
   }

   private static void showQuestListDialog(List<? extends Quest> quests, Player player, NPCEntity npcEntity) {
      DialogManager.DialogListDialogBuilder listBuilder = new DialogManager.DialogListDialogBuilder();
      listBuilder.title("Available Quests");

      for (int i = 0; i < quests.size(); i++) {
         DialogManager.MultiActionDialogBuilder questDialog = buildQuestDialogBuilder(quests, i, player, npcEntity, true);
         listBuilder.addDialog(DialogManager.DialogReference.inline(questDialog));
      }

      listBuilder.columns(1);
      listBuilder.buttonWidth(300);
      DialogManager.sendDialog(player, listBuilder, QuestScreenSession.DIALOG_OWNER);
   }

   private static DialogManager.MultiActionDialogBuilder buildQuestDialogBuilder(
      List<? extends Quest> quests, int questIndex, Player player, NPCEntity npcEntity, boolean addBackButton
   ) {
      Quest quest = quests.get(questIndex);
      QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, npcEntity, player);
      DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();
      builder.afterAction(DialogManager.AfterAction.CLOSE);
      setDialogTitles(quest, builder, questText, questIndex, quests.size());
      addQuestBodySections(builder, quest, questText);
      addActionButtons(builder, quest, questText);
      addExitButton(builder);
      if (addBackButton && npcEntity != null) {
         String backCommand = "/elitemobs npc questList " + npcEntity.getUuid();
         builder.addAction(DialogManager.ActionButton.of("← Back to Quest List", new DialogManager.RunCommandAction(backCommand)).width(300));
      }

      return builder;
   }

   private static void setDialogTitles(
      Quest quest, DialogManager.MultiActionDialogBuilder builder, QuestMenu.QuestText questText, int questIndex, int totalQuests
   ) {
      String title = processSingleLineText(componentText(questText.getHeader()));
      if (title != null && !title.isEmpty()) {
         builder.title(title);
      }

      String externalTitle = title
         + (
            quest.isAccepted()
               ? (
                  quest.getQuestObjectives().isOver()
                     ? ChatColor.WHITE + " | " + ChatColor.DARK_GREEN + "Turn in!"
                     : ChatColor.WHITE + " | " + ChatColor.GREEN + "Accepted"
               )
               : ""
         );
      builder.externalTitle(processSingleLineText(externalTitle));
   }

   private static void addQuestBodySections(DialogManager.MultiActionDialogBuilder builder, Quest quest, QuestMenu.QuestText questText) {
      addBodySection(builder, questText.getBody());
      addBodySectionWithHeader(builder, questText.getFixedSummary(), questText.getSummary());
      addQuestRewardSection(builder, quest, questText);
   }

   private static void addBodySection(DialogManager.MultiActionDialogBuilder builder, List<TextComponent> components) {
      if (components != null && !components.isEmpty()) {
         StringBuilder text = new StringBuilder();

         for (TextComponent component : components) {
            String componentText = componentText(component);
            if (componentText != null) {
               text.append(processText(componentText)).append("\n");
            }
         }

         if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()));
         }
      }
   }

   private static void addBodySectionWithHeader(DialogManager.MultiActionDialogBuilder builder, TextComponent header, List<TextComponent> items) {
      if (header != null && componentText(header) != null) {
         builder.addBody(DialogManager.PlainMessageBody.of(processText(componentText(header))).width(300));
      }

      addBodySection(builder, items);
   }

   public static void addQuestRewardSection(DialogManager.MultiActionDialogBuilder builder, Quest quest, QuestMenu.QuestText questText) {
      TextComponent header = questText.getFixedRewards();
      if (header != null && componentText(header) != null) {
         builder.addBody(DialogManager.PlainMessageBody.of(processText(componentText(header))).width(300));
      }

      List<QuestReward.PreviewEntry> previews;
      try {
         previews = quest.getQuestObjectives().getQuestReward().previewRewardEntries();
         if (previews.isEmpty()) {
            addBodySection(builder, questText.getRewards());
            return;
         }
      } catch (RuntimeException var11) {
         Logger.warn("Could not preview quest rewards for " + quest.getQuestID() + ": " + var11.getMessage());
         builder.addBody(DialogManager.PlainMessageBody.of(QuestsConfig.getRewardPreviewUnavailable()).width(300));
         return;
      }

      for (QuestReward.PreviewEntry preview : previews) {
         String description = processText(componentText(QuestMenu.describeRewardPreview(quest, preview)));
         ItemStack item = preview.item();
         if (item == null) {
            builder.addBody(DialogManager.PlainMessageBody.of(description).width(300));
         } else {
            DialogManager.ItemBody body = DialogManager.ItemBody.of(item.getType().getKey().toString(), item.getAmount())
               .showTooltip(true)
               .showDecoration(true)
               .description(description);
            JsonObject components = DialogManager.serializeItemComponents(item);
            if (components != null && !components.entrySet().isEmpty()) {
               fixCustomModelDataFormat(components);
               body.components(components);
            }

            builder.addBody(body);
         }
      }
   }

   private static void addActionButtons(DialogManager.MultiActionDialogBuilder builder, Quest quest, QuestMenu.QuestText questText) {
      if (quest instanceof CustomQuest && quest.isAccepted()) {
         builder.columns(2);
      } else {
         builder.columns(1);
      }

      addButtonFromComponent(builder, questText.getAccept());
      if (quest instanceof CustomQuest) {
         addButtonFromComponent(builder, questText.getTrack());
      }
   }

   private static void addExitButton(DialogManager.MultiActionDialogBuilder builder) {
      builder.addAction(DialogManager.ActionButton.of("Exit", new DialogManager.CustomAction("elitemobs:close_quest_dialog")).width(100));
   }

   private static void addButtonFromComponent(DialogManager.MultiActionDialogBuilder builder, TextComponent component) {
      if (component != null && componentText(component) != null && !componentText(component).isEmpty()) {
         String text = processSingleLineText(componentText(component));
         if (text.contains("[Abandon]")) {
            text = "[Abandon]";
         }

         String command = extractCommandFromComponent(component);
         if (command != null && !command.isEmpty()) {
            builder.addAction(DialogManager.ActionButton.of(text, new DialogManager.RunCommandAction(command)).width(100));
         }
      }
   }

   private static String extractCommandFromComponent(TextComponent component) {
      if (component == null) {
         return null;
      } else {
         try {
            if (component.getClickEvent() != null && component.getClickEvent().getAction() == Action.RUN_COMMAND) {
               return component.getClickEvent().getValue();
            }
         } catch (Exception var2) {
         }

         return null;
      }
   }

   private static String componentText(TextComponent component) {
      if (component == null) {
         return null;
      } else {
         String text = component.toLegacyText();
         if (text == null || text.isEmpty()) {
            text = component.toPlainText();
         }

         return text;
      }
   }

   private static void fixCustomModelDataFormat(JsonObject components) {
      String key = "minecraft:custom_model_data";
      if (components.has(key)) {
         JsonElement element = components.get(key);
         if (element.isJsonPrimitive()) {
            double value = element.getAsDouble();
            JsonObject newFormat = new JsonObject();
            JsonArray floats = new JsonArray();
            floats.add(value);
            newFormat.add("floats", floats);
            components.add(key, newFormat);
         }
      }
   }
}

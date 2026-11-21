package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.dialog.DialogManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.utils.DialogMaker;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QuestCheckCommand extends AdvancedCommand {
   private static final int QUEST_DIALOG_WIDTH = 300;

   public QuestCheckCommand() {
      super(List.of("quest"));
      this.addLiteral("check");
      this.addArgument("questID", new ListStringCommandArgument("<questID>"));
      this.setUsage("Internal command.");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Internal command.");
   }

   private static String processText(String text) {
      return text == null
         ? null
         : ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text.replace("§0", "§f").replace("&0", "&f").replace("\r", "")));
   }

   private static String processSingleLineText(String text) {
      String processedText = processText(text);
      return processedText == null ? null : processedText.replace('\n', ' ').replaceAll("\\s+", " ").trim();
   }

   @Override
   public void execute(CommandData commandData) {
      Player player = commandData.getPlayerSender();
      UUID questID = UUID.fromString(commandData.getStringArgument("questID"));
      List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
      if (quests != null && !quests.isEmpty()) {
         Quest targetQuest = null;

         for (Quest quest : quests) {
            if (quest.getQuestID().equals(questID)) {
               targetQuest = quest;
               break;
            }
         }

         if (targetQuest == null) {
            player.sendMessage(CommandMessagesConfig.getQuestNotFoundMessage());
         } else {
            this.showQuestDialog(targetQuest, player);
         }
      } else {
         player.sendMessage(CommandMessagesConfig.getNoActiveQuestsMessage());
      }
   }

   private void showQuestDialog(Quest quest, Player player) {
      QuestMenu.QuestText questText = new QuestMenu.QuestText(quest, null, player);
      DialogManager.MultiActionDialogBuilder builder = new DialogManager.MultiActionDialogBuilder();
      String title = processSingleLineText(questText.getHeader().toPlainText());
      if (title != null && !title.isEmpty()) {
         builder.title(title);
      }

      String statusSuffix = "";
      if (quest.isAccepted()) {
         if (quest.getQuestObjectives().isOver()) {
            statusSuffix = QuestsConfig.getQuestTurnInStatus();
         } else {
            statusSuffix = QuestsConfig.getQuestAcceptedStatus();
         }
      }

      builder.externalTitle(processSingleLineText(title + statusSuffix));
      this.addBodySection(builder, questText.getBody());
      this.addBodySectionWithHeader(builder, questText.getFixedSummary(), questText.getSummary());
      DialogMaker.addQuestRewardSection(builder, quest, questText);
      this.addActionButtons(builder, quest, questText);
      builder.addAction(
         DialogManager.ActionButton.of(processSingleLineText(QuestsConfig.getQuestBackToStatusMenu()), new DialogManager.RunCommandAction("/elitemobs"))
            .width(300)
      );
      DialogManager.sendDialog(player, builder);
   }

   private void addBodySection(DialogManager.MultiActionDialogBuilder builder, List<TextComponent> components) {
      if (components != null && !components.isEmpty()) {
         StringBuilder text = new StringBuilder();

         for (TextComponent component : components) {
            if (component.toPlainText() != null) {
               text.append(processText(component.toPlainText())).append("\n");
            }
         }

         if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(300));
         }
      }
   }

   private void addBodySectionWithHeader(DialogManager.MultiActionDialogBuilder builder, TextComponent header, List<TextComponent> items) {
      if (header != null && header.toPlainText() != null) {
         builder.addBody(DialogManager.PlainMessageBody.of(processText(header.toPlainText())).width(300));
      }

      if (items != null && !items.isEmpty()) {
         StringBuilder text = new StringBuilder();

         for (TextComponent item : items) {
            if (item.toPlainText() != null) {
               text.append("  ").append(processText(item.toPlainText())).append("\n");
            }
         }

         if (!text.isEmpty()) {
            builder.addBody(DialogManager.PlainMessageBody.of(text.toString().trim()).width(300));
         }
      }
   }

   private void addActionButtons(DialogManager.MultiActionDialogBuilder builder, Quest quest, QuestMenu.QuestText questText) {
      builder.columns(1);
      this.addButtonFromComponent(builder, questText.getAccept());
      this.addButtonFromComponent(builder, questText.getTrack());
   }

   private void addButtonFromComponent(DialogManager.MultiActionDialogBuilder builder, TextComponent component) {
      if (component != null && component.toPlainText() != null && !component.toPlainText().isEmpty()) {
         String text = processSingleLineText(component.toPlainText());
         if (text.contains("[Abandon]")) {
            text = processSingleLineText(QuestsConfig.getQuestAbandonText());
         }

         String command = this.extractCommandFromComponent(component);
         if (command != null && !command.isEmpty()) {
            builder.addAction(DialogManager.ActionButton.of(text, new DialogManager.RunCommandAction(command)).width(100));
         }
      }
   }

   private String extractCommandFromComponent(TextComponent component) {
      if (component == null) {
         return null;
      } else {
         try {
            if (component.getClickEvent() != null && component.getClickEvent().getAction() == Action.RUN_COMMAND) {
               return component.getClickEvent().getValue();
            }
         } catch (Exception var3) {
         }

         return null;
      }
   }
}

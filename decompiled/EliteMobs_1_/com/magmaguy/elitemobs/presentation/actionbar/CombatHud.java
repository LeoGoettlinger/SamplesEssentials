package com.magmaguy.elitemobs.presentation.actionbar;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.advancedcombat.progression.FormProgressSnapshot;
import com.magmaguy.elitemobs.advancedcombat.resources.ClassResourceController;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.awt.Color;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class CombatHud {
   private static final String FONT = "elitemobs:combat_hud_concept_16";
   private static final int CONTENT_WIDTH = 190;
   private static final int PANEL_MARGIN = 10;
   private static final int END_TO_ORIGIN = 400;

   public CombatHud.Frame frame(Player player, boolean active) {
      int animationFrame = (int)Math.floorMod(MonotonicTickClock.currentTick() / 4L, 4L);
      StringBuilder line = new StringBuilder();
      line.append((char)(active ? '\ue001' : '\ue000')).append(spacing(-201));
      double health = player.getHealth();
      AttributeInstance maximumAttribute = player.getAttribute(Attribute.MAX_HEALTH);
      double maximum = maximumAttribute == null ? health : maximumAttribute.getValue();
      String healthText = number(health) + "/" + number(maximum);
      overlay(line, 25, healthText, healthText.length() * 6);
      bar(line, 25, '\ue800', health, maximum, 63, false, animationFrame);
      ClassResourceController.Snapshot resource = AdvancedCombatModule.resourceSnapshot(player.getUniqueId()).orElse(null);
      if (resource != null) {
         overlay(line, 171, String.valueOf(resourceIcon(resource.type())), 11);
      }

      String resourceText = resource == null ? "0/0" : number(resource.amount()) + "/" + number(resource.maximum());
      rightAlignedText(line, 165, resourceText, resourceText.length() * 6);
      bar(
         line,
         102,
         resource == null ? '\ue940' : resourceBar(resource.type()),
         resource == null ? 0.0 : resource.amount(),
         resource == null ? 0.0 : resource.maximum(),
         63,
         true,
         animationFrame
      );
      bar(line, 5, '\ue980', player.getExp(), 1.0, 180, false, animationFrame);
      classDiamond(line, player, animationFrame, active);
      String className = AdvancedCombatModule.activeClassLineageSnapshot(player.getUniqueId()).map(lineage -> lineage.activeForm().displayName()).orElse("");
      if (active) {
         abilityIcons(line, player);
      }

      return new CombatHud.Frame(line.append(spacing(200)).toString(), className);
   }

   public TextComponent component(String text) {
      TextComponent component = new TextComponent(text);
      component.setFont("elitemobs:combat_hud_concept_16");
      component.setColor(ChatColor.WHITE);
      CombatHudFeedback.shadow(component, new Color(0, true));
      return component;
   }

   public TextComponent component(CombatHud.Frame frame, String feedback, boolean legacy) {
      TextComponent component = this.component(frame.text());
      if (!frame.className().isBlank()) {
         BaseComponent[] label = CombatHudFeedback.className(frame.className());
         int width = CombatHudFeedback.classNameWidthInHalfPixels(label);
         int badgeWidth = Math.max(74, (width + 1) / 2 + 8);
         int left = 78 - badgeWidth;
         String border = "\ue600\ue101" + "\ue601\ue101".repeat(badgeWidth - 6) + "\ue602\ue101";
         component.addExtra(halfPixelSpacing(left * 2 - 400));
         component.addExtra(border);
         component.addExtra(halfPixelSpacing(400 - (left + badgeWidth) * 2));
         appendText(component, label, left * 2 + (badgeWidth * 2 - width) / 2, width);
      }

      if (feedback != null && !feedback.isBlank()) {
         BaseComponent[] message = CombatHudFeedback.components(feedback, legacy);
         int width = CombatHudFeedback.widthInHalfPixels(message);
         int start = (380 - width) / 2;
         appendText(component, message, start, width);
         return component;
      } else {
         return component;
      }
   }

   private static void appendText(TextComponent component, BaseComponent[] message, int start, int width) {
      component.addExtra(halfPixelSpacing(start - 400));

      for (BaseComponent part : message) {
         component.addExtra(part);
      }

      component.addExtra(halfPixelSpacing(400 - start - width));
   }

   private static String halfPixelSpacing(int halfPixels) {
      return spacing(halfPixels / 2) + (halfPixels % 2 == 0 ? "" : (halfPixels < 0 ? "\ue102" : "\ue103"));
   }

   private static void abilityIcons(StringBuilder line, Player player) {
      AdvancedCombatModule.activeClassLineageSnapshot(player.getUniqueId()).ifPresent(lineage -> {
         List<AbilitySlot> slots = List.of(AbilitySlot.SIGNATURE, AbilitySlot.UTILITY, AbilitySlot.MOBILITY);

         for (int index = 0; index < slots.size(); index++) {
            AdvancedCombatModule.AbilityResourceSnapshot state = AdvancedCombatModule.abilityResourceSnapshot(player, slots.get(index)).orElse(null);
            if (state != null) {
               int left = 4 + index * 61;
               if (!state.affordable()) {
                  overlay(line, left, String.valueOf((char)(59008 + index)), 61);
               }

               String glyph = CombatHudAbilityIcons.glyph(slots.get(index), state.affordable());
               if (!glyph.isEmpty()) {
                  overlay(line, left + 6, glyph, 14);
               }

               String digits = Long.toString((long)Math.ceil(state.cost()));
               int start = left + 38 - (digits.length() * 4 + 7) / 2;
               overlay(line, start, String.valueOf((char)('\ue540' + resourceIcon(lineage.resourceType()) - 58624)), 6);
               StringBuilder cost = new StringBuilder();

               for (char digit : digits.toCharArray()) {
                  cost.append((char)((state.affordable() ? '\ue700' : '\ue710') + digit - 48));
               }

               overlay(line, start + 8, cost.toString(), digits.length() * 4);
            }
         }
      });
   }

   private static String spacing(int pixels) {
      return String.valueOf((char)(pixels < 0 ? '\ue101' : '\ue100')).repeat(Math.abs(pixels));
   }

   private static void overlay(StringBuilder line, int offset, String text, int advance) {
      line.append(spacing(offset)).append(text).append(spacing(-offset - advance));
   }

   private static void rightAlignedText(StringBuilder line, int rightEdge, String text, int advance) {
      overlay(line, rightEdge - (advance - 1), text, advance);
   }

   private static void bar(StringBuilder line, int offset, char glyph, double amount, double maximum, int width, boolean rightAligned, int animationFrame) {
      int pixels = maximum <= 0.0 ? 0 : (int)Math.round(width * Math.max(0.0, Math.min(1.0, amount / maximum)));
      int start = rightAligned ? width - pixels : 0;
      StringBuilder fill = new StringBuilder(pixels * 2);

      for (int column = start; column < start + pixels; column++) {
         int textureColumn = (rightAligned ? width - 1 - column : column) % 16;
         fill.append((char)(glyph + animationFrame * 16 + textureColumn)).append('\ue101');
      }

      overlay(line, offset + start, fill.toString(), pixels);
   }

   private static void classDiamond(StringBuilder line, Player player, int animationFrame, boolean active) {
      FormProgressSnapshot progress = AdvancedCombatModule.classProgressSnapshot(player.getUniqueId()).orElse(null);
      if (progress != null && progress.unlocked()) {
         int level = progress.effectiveLevel();
         int bandStart = level - progress.localLevel() + 1;
         long currentThreshold = SkillXPCalculator.totalXPForLevel(level) - SkillXPCalculator.totalXPForLevel(bandStart);
         double fraction = progress.xp() >= progress.xpAtCap() ? 1.0 : (double)(progress.xp() - currentThreshold) / SkillXPCalculator.xpToNextLevel(level);
         int rows = (int)Math.round(28.0 * Math.max(0.0, Math.min(1.0, fraction)));
         overlay(line, 79, String.valueOf((char)(58368 + animationFrame * 32 + rows)), 33);
         overlay(line, 91, String.valueOf((char)(58656 + (active ? 4 : 0) + animationFrame)), 9);
         String digits = Integer.toString(level);
         StringBuilder glyphs = new StringBuilder();

         for (char digit : digits.toCharArray()) {
            glyphs.append((char)('\ue300' + digit - 48));
         }

         int advance = digits.length() * 6;
         overlay(line, 95 - advance / 2, glyphs.toString(), advance);
      }
   }

   private static String number(double value) {
      long rounded = Math.round(Math.max(0.0, value));
      if (rounded >= 1000000L) {
         return Math.round(rounded / 1000000.0) + "M";
      } else {
         return rounded >= 10000L ? Math.round(rounded / 1000.0) + "K" : Long.toString(rounded);
      }
   }

   private static char resourceIcon(ClassResourceType type) {
      return switch (type) {
         case RESOLVE -> '\ue500';
         case FURY -> '\ue501';
         case FOCUS -> '\ue502';
         case GRACE -> '\ue503';
         case MANA -> '\ue504';
         case STAMINA -> '\ue505';
      };
   }

   private static char resourceBar(ClassResourceType type) {
      return switch (type) {
         case RESOLVE -> '\ue840';
         case FURY -> '\ue880';
         case FOCUS -> '\ue8c0';
         case GRACE -> '\ue900';
         case MANA -> '\ue940';
         case STAMINA -> '\ue9c0';
      };
   }

   public record Frame(String text, String className) {
   }
}

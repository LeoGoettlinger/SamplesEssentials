package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class AdventurersGuildConfig extends ConfigurationFile {
   private static boolean agTeleport;
   private static String adventurersGuildMenuName;
   private static boolean skillBasedGearRestriction;
   private static String gearRestrictionMessage;

   public AdventurersGuildConfig() {
      super("AdventurersGuild.yml");
   }

   @Override
   public void initializeValues() {
      agTeleport = ConfigurationEngine.setBoolean(
         List.of("Sets if user commands get rerouted to the adventurer's guild hub. This is highly recommended for gameplay immersion and tutorial purposes."),
         this.fileConfiguration,
         "userCommandsTeleportToAdventurersGuild",
         true
      );
      adventurersGuildMenuName = ConfigurationEngine.setString(
         List.of("Sets the in-game display name of the adventurer's guild"),
         this.file,
         this.fileConfiguration,
         "adventurersGuildMenuName",
         "&6&lAdventurer's Hub",
         true
      );
      skillBasedGearRestriction = ConfigurationEngine.setBoolean(
         List.of(
            "Sets if players are restricted from equipping gear above their skill level.",
            "Gear type determines which skill is checked (e.g., axes check AXES skill, armor checks ARMOR skill)."
         ),
         this.fileConfiguration,
         "skillBasedGearRestriction",
         true
      );
      gearRestrictionMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message sent to players when they try to equip gear above their skill level.",
            "$itemLevel is the item level, $skillLevel is their current skill level, $skillType is the skill name."
         ),
         this.file,
         this.fileConfiguration,
         "gearRestrictionMessage",
         "&c[EM] You need $skillType level $itemLevel to equip this! (Current: $skillLevel)",
         true
      );
   }

   @Generated
   public static boolean isAgTeleport() {
      return agTeleport;
   }

   @Generated
   public static String getAdventurersGuildMenuName() {
      return adventurersGuildMenuName;
   }

   @Generated
   public static boolean isSkillBasedGearRestriction() {
      return skillBasedGearRestriction;
   }

   @Generated
   public static String getGearRestrictionMessage() {
      return gearRestrictionMessage;
   }
}

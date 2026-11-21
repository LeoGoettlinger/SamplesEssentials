package dev.vatten.baserad.configs;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Generated;
import org.mineskin.data.Visibility;

@Configuration
public class PluginConfig {
   @Comment({"", "The tags are only cached on the client for 5 minutes after rendering,", "which means to have a seamless experience with no visual \"skin loading\"", "the players have to be sent all the tags regularly.", "This is done by sending a title pushed off-screen on join, and continuously on a timer."})
   private PluginConfig.CacheSettings cacheSettings = new PluginConfig.CacheSettings();
   @Comment({"", "Settings for the MineSkin API (mineskin.org).", "MineSkin is used for the skin signature generation."})
   private PluginConfig.MineSkin mineSkinSettings = new PluginConfig.MineSkin();

   @Generated
   public PluginConfig.CacheSettings getCacheSettings() {
      return this.cacheSettings;
   }

   @Generated
   public PluginConfig.MineSkin getMineSkinSettings() {
      return this.mineSkinSettings;
   }

   @Configuration
   public static class CacheSettings {
      @Comment({"", "Whether cache updates should be sent."})
      private boolean enabled = true;
      @Comment({"", "Whether players should be sent cache updates on join."})
      private boolean onJoin = true;
      @Comment({"", "Whether players should continue being sent cache updates."})
      private boolean onTimer = true;
      @Comment({"", "How frequent the timer runs, in seconds."})
      private int intervalDuration = 300;
      @Comment({"", "The ratio of how much spacing is needed to push the tags off-screen in the title", "Each \"spacing\" consists of 2 spaces, which is as wide as a player head component,", "and the ratio is how many spacings per player head.", "If the tags are visible on-screen, consider raising this number.", "(This is mostly because the title feature has not been tested on larger screen sizes,", "this is very likely subject to change)"})
      private double offScreenRatio = 2.5;

      @Generated
      public boolean isEnabled() {
         return this.enabled;
      }

      @Generated
      public boolean isOnJoin() {
         return this.onJoin;
      }

      @Generated
      public boolean isOnTimer() {
         return this.onTimer;
      }

      @Generated
      public int getIntervalDuration() {
         return this.intervalDuration;
      }

      @Generated
      public double getOffScreenRatio() {
         return this.offScreenRatio;
      }
   }

   @Configuration
   public static class MineSkin {
      @Comment({"", "Your project's MineSkin API key.", "If you don't have one already, create an account and get one at https://account.mineskin.org/keys/"})
      private String apiKey = "";
      @Comment({"", "The visibility of the generated skin.", "PUBLIC - Will be publicly shown on the MineSkin website", "UNLISTED - Can only be accessed via the link", "PRIVATE - Not accessible by anyone besides the account owner. NOTE: This option is locked to paid plans only."})
      private Visibility visibility = Visibility.UNLISTED;
      @Comment({"", "If you have a paid plan on MineSkin for faster generation,", "you can put your higher limits below.", "If you don't have a paid plan, this can be ignored", "If you however don't know what to put, leave the mode at AUTO and it will sort itself out"})
      private PluginConfig.MineSkin.Limits limits = new PluginConfig.MineSkin.Limits();

      @Generated
      public String getApiKey() {
         return this.apiKey;
      }

      @Generated
      public Visibility getVisibility() {
         return this.visibility;
      }

      @Generated
      public PluginConfig.MineSkin.Limits getLimits() {
         return this.limits;
      }

      @Configuration
      public static class Limits {
         @Comment({"", "The mode for what limits to use. Can be AUTO for automatic allowance retrieving, or CUSTOM to use your own values."})
         private PluginConfig.MineSkin.Limits.Mode mode = PluginConfig.MineSkin.Limits.Mode.AUTO;
         @Comment({"", "How many milliseconds between each request.", "This value is ignored when mode is set to AUTO"})
         private int intervalMillis = 200;
         @Comment({"", "How many requests to handle concurrently.", "This value is ignored when mode is set to AUTO"})
         private int concurrency = 1;

         @Generated
         public PluginConfig.MineSkin.Limits.Mode getMode() {
            return this.mode;
         }

         @Generated
         public int getIntervalMillis() {
            return this.intervalMillis;
         }

         @Generated
         public int getConcurrency() {
            return this.concurrency;
         }

         public static enum Mode {
            AUTO,
            CUSTOM;
         }
      }
   }
}

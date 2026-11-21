package me.usainsrht.ujobs.yaml;

import java.time.Duration;
import java.util.List;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.configuration.ConfigurationSection;

public class YamlTitle {
   private String title;
   private String subTitle;
   private String stay;
   private String fadeIn;
   private String fadeOut;
   public static final int DEFAULT_FADE_IN = 10;
   public static final int DEFAULT_STAY = 70;
   public static final int DEFAULT_FADE_OUT = 20;
   private static final long MILLIS_IN_ONE_SECOND = 20L;

   public Title parse() {
      return this.parse(UJobsPlugin.instance.getMiniMessage());
   }

   public Title parse(TagResolver... placeholders) {
      return this.parse(UJobsPlugin.instance.getMiniMessage(), placeholders);
   }

   public Title parse(MiniMessage miniMessage, TagResolver... placeholders) {
      int timeFadeIn = this.fadeIn != null ? Integer.parseInt(this.fadeIn) : 10;
      int timeStay = this.stay != null ? Integer.parseInt(this.stay) : 70;
      int timeFadeOut = this.fadeOut != null ? Integer.parseInt(this.fadeOut) : 20;
      Times times = Times.times(Duration.ofMillis(timeFadeIn * 20L), Duration.ofMillis(timeStay * 20L), Duration.ofMillis(timeFadeOut * 20L));
      Component parsedTitle = miniMessage.deserialize(this.title != null ? this.title : "", placeholders);
      Component parsedSubtitle = miniMessage.deserialize(this.subTitle != null ? this.subTitle : "", placeholders);
      return Title.title(parsedTitle, parsedSubtitle, times);
   }

   public static YamlTitle getTitle(Object obj) {
      YamlTitle.YamlTitleBuilder builder = builder();
      if (obj instanceof String string) {
         builder.title(string);
      } else if (obj instanceof List<String> stringList) {
         builder.title(stringList.get(0));
         builder.subTitle(stringList.get(1));
      } else if (obj instanceof ConfigurationSection config) {
         if (config.isSet("title")) {
            builder.title(config.getString("title"));
         }

         if (config.isSet("subtitle")) {
            builder.subTitle(config.getString("subtitle"));
         }

         if (config.isSet("fade_in")) {
            builder.fadeIn(config.getString("fade_in"));
         }

         if (config.isSet("stay")) {
            builder.stay(config.getString("stay"));
         }

         if (config.isSet("fade_out")) {
            builder.fadeOut(config.getString("fade_out"));
         }
      }

      return builder.build();
   }

   @Generated
   YamlTitle(String title, String subTitle, String stay, String fadeIn, String fadeOut) {
      this.title = title;
      this.subTitle = subTitle;
      this.stay = stay;
      this.fadeIn = fadeIn;
      this.fadeOut = fadeOut;
   }

   @Generated
   public static YamlTitle.YamlTitleBuilder builder() {
      return new YamlTitle.YamlTitleBuilder();
   }

   @Generated
   public void setTitle(String title) {
      this.title = title;
   }

   @Generated
   public void setSubTitle(String subTitle) {
      this.subTitle = subTitle;
   }

   @Generated
   public void setStay(String stay) {
      this.stay = stay;
   }

   @Generated
   public void setFadeIn(String fadeIn) {
      this.fadeIn = fadeIn;
   }

   @Generated
   public void setFadeOut(String fadeOut) {
      this.fadeOut = fadeOut;
   }

   @Generated
   public String getTitle() {
      return this.title;
   }

   @Generated
   public String getSubTitle() {
      return this.subTitle;
   }

   @Generated
   public String getStay() {
      return this.stay;
   }

   @Generated
   public String getFadeIn() {
      return this.fadeIn;
   }

   @Generated
   public String getFadeOut() {
      return this.fadeOut;
   }

   @Generated
   public static class YamlTitleBuilder {
      @Generated
      private String title;
      @Generated
      private String subTitle;
      @Generated
      private String stay;
      @Generated
      private String fadeIn;
      @Generated
      private String fadeOut;

      @Generated
      YamlTitleBuilder() {
      }

      @Generated
      public YamlTitle.YamlTitleBuilder title(String title) {
         this.title = title;
         return this;
      }

      @Generated
      public YamlTitle.YamlTitleBuilder subTitle(String subTitle) {
         this.subTitle = subTitle;
         return this;
      }

      @Generated
      public YamlTitle.YamlTitleBuilder stay(String stay) {
         this.stay = stay;
         return this;
      }

      @Generated
      public YamlTitle.YamlTitleBuilder fadeIn(String fadeIn) {
         this.fadeIn = fadeIn;
         return this;
      }

      @Generated
      public YamlTitle.YamlTitleBuilder fadeOut(String fadeOut) {
         this.fadeOut = fadeOut;
         return this;
      }

      @Generated
      public YamlTitle build() {
         return new YamlTitle(this.title, this.subTitle, this.stay, this.fadeIn, this.fadeOut);
      }

      @Generated
      @Override
      public String toString() {
         return "YamlTitle.YamlTitleBuilder(title="
            + this.title
            + ", subTitle="
            + this.subTitle
            + ", stay="
            + this.stay
            + ", fadeIn="
            + this.fadeIn
            + ", fadeOut="
            + this.fadeOut
            + ")";
      }
   }
}

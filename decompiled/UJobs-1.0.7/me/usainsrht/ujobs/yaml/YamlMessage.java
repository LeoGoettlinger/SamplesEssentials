package me.usainsrht.ujobs.yaml;

import java.util.ArrayList;
import java.util.Collection;
import lombok.Generated;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Builder;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.configuration.ConfigurationSection;

public class YamlMessage {
   private Collection<String> chatMessages;
   private String actionBarMessage;
   private YamlTitle title;
   private Collection<Sound> sounds;

   public YamlMessage(Object obj) {
      if (obj instanceof String || obj instanceof Collection) {
         this.chatMessages = getMessages(obj);
      } else if (obj instanceof ConfigurationSection config) {
         if (config.isSet("chat")) {
            this.chatMessages = getMessages(config.get("chat"));
         }

         if (config.isSet("action_bar")) {
            this.actionBarMessage = config.getString("action_bar");
         }

         if (config.isSet("title")) {
            this.title = YamlTitle.getTitle(config.get("title"));
         }

         if (config.isSet("sound")) {
            this.sounds = getSounds(config.get("sound"));
         }
      } else if (obj instanceof YamlMessage other) {
         this.chatMessages = other.chatMessages;
         this.actionBarMessage = other.actionBarMessage;
         this.title = other.title;
         this.sounds = other.sounds;
      }
   }

   public static Collection<String> getMessages(Object object) {
      Collection<String> messages = new ArrayList<>();
      if (object instanceof String) {
         messages.add((String)object);
      } else if (object instanceof Collection) {
         messages.addAll((Collection<? extends String>)object);
      }

      return messages;
   }

   public static Collection<Sound> getSounds(Object object) {
      Collection<Sound> sounds = new ArrayList<>();
      if (object instanceof String string) {
         sounds.add(getSound(string));
      } else if (object instanceof Collection<String> strings) {
         sounds.addAll(strings.stream().map(YamlMessage::getSound).toList());
      } else if (object instanceof ConfigurationSection config) {
         Builder builder = Sound.sound();
         if (config.isSet("name")) {
            builder.type(Key.key(config.getString("name")));
         }

         if (config.isSet("volume")) {
            builder.volume((float)config.getDouble("volume"));
         }

         if (config.isSet("pitch")) {
            builder.pitch((float)config.getDouble("pitch"));
         }

         if (config.isSet("seed")) {
            builder.seed(config.getLong("seed"));
         }

         if (config.isSet("source")) {
            builder.source(Source.valueOf(config.getString("source")));
         }

         sounds.add((Sound)builder.build());
      }

      return sounds;
   }

   public static Sound getSound(String string) {
      Builder sound = Sound.sound();
      String[] splitted = string.split(",");
      sound.type(Key.key(splitted[0]));
      if (splitted.length > 1) {
         sound.volume(Float.parseFloat(splitted[1]));
         if (splitted.length > 2) {
            sound.pitch(Float.parseFloat(splitted[2]));
            if (splitted.length > 3) {
               sound.seed(Long.parseLong(splitted[3]));
               if (splitted.length > 4) {
                  sound.source(Source.valueOf(splitted[4]));
               }
            }
         }
      }

      return (Sound)sound.build();
   }

   @Generated
   public void setChatMessages(Collection<String> chatMessages) {
      this.chatMessages = chatMessages;
   }

   @Generated
   public void setActionBarMessage(String actionBarMessage) {
      this.actionBarMessage = actionBarMessage;
   }

   @Generated
   public void setTitle(YamlTitle title) {
      this.title = title;
   }

   @Generated
   public void setSounds(Collection<Sound> sounds) {
      this.sounds = sounds;
   }

   @Generated
   public Collection<String> getChatMessages() {
      return this.chatMessages;
   }

   @Generated
   public String getActionBarMessage() {
      return this.actionBarMessage;
   }

   @Generated
   public YamlTitle getTitle() {
      return this.title;
   }

   @Generated
   public Collection<Sound> getSounds() {
      return this.sounds;
   }
}

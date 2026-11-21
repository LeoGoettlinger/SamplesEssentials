package com.oheers.fish.messages.abstracted;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentListMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentSingleMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.MessageType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class EMFMessage {
   protected boolean perPlayer = true;
   protected OfflinePlayer relevantPlayer = null;

   public static EMFMessage fromUnderlying(@NonNull ComponentMessage<?, ?> message) {
      if (message instanceof ComponentListMessage listMessage) {
         return EMFListMessage.ofUnderlying(listMessage);
      } else if (message instanceof ComponentSingleMessage singleMessage) {
         return EMFSingleMessage.ofUnderlying(singleMessage);
      } else {
         throw new IllegalArgumentException("Unknown ComponentMessage type");
      }
   }

   protected EMFMessage() {
   }

   public abstract EMFMessage createCopy();

   @NonNull
   public abstract ComponentMessage<?, ?> getUnderlying();

   public abstract void setUnderlying(@NonNull ComponentMessage<?, ?> message);

   public abstract ComponentMessage<?, ?> processPlaceholders(@Nullable OfflinePlayer player);

   public final void send(@NonNull Audience target) {
      if (!this.getUnderlying().isEmpty()) {
         OfflinePlayer player = (OfflinePlayer)(target instanceof Player p ? p : this.relevantPlayer);
         this.processPlaceholders(player).send(target);
      }
   }

   public final void send(@NonNull Collection<? extends Audience> targets) {
      targets.forEach(this::send);
   }

   public final void sendActionBar(@NonNull Audience target) {
      if (!this.getUnderlying().isEmpty()) {
         OfflinePlayer player = (OfflinePlayer)(target instanceof Player p ? p : this.relevantPlayer);
         this.processPlaceholders(player).messageType(MessageType.ACTION_BAR).send(target);
      }
   }

   public final void sendActionBar(@NonNull Collection<? extends Audience> targets) {
      targets.forEach(this::sendActionBar);
   }

   @NonNull
   public final Component getComponentMessage() {
      return this.getComponentMessage(null);
   }

   @NonNull
   public abstract Component getComponentMessage(@Nullable OfflinePlayer player);

   @NonNull
   public final List<Component> getComponentListMessage() {
      return this.getComponentListMessage(null);
   }

   @NonNull
   public abstract List<Component> getComponentListMessage(@Nullable OfflinePlayer player);

   @NonNull
   public abstract String getLegacyMessage(@Nullable OfflinePlayer player);

   @NonNull
   public abstract List<String> getLegacyListMessage(@Nullable OfflinePlayer player);

   @NonNull
   public abstract String getPlainTextMessage(@Nullable OfflinePlayer player);

   @NonNull
   public abstract List<String> getPlainTextListMessage(@Nullable OfflinePlayer player);

   public abstract void formatPlaceholderAPI();

   public final void setPerPlayer(boolean perPlayer) {
      this.perPlayer = perPlayer;
   }

   @Nullable
   public final OfflinePlayer getRelevantPlayer() {
      return this.relevantPlayer;
   }

   public final void setRelevantPlayer(@Nullable OfflinePlayer relevantPlayer) {
      if (relevantPlayer != null) {
         this.relevantPlayer = relevantPlayer;
      }
   }

   public final boolean isEmpty() {
      return this.getUnderlying().isEmpty();
   }

   public abstract boolean containsString(@NonNull String string);

   public void broadcast() {
      this.send(Bukkit.getConsoleSender());
      this.send(Bukkit.getOnlinePlayers());
   }

   public void broadcastActionBar() {
      this.sendActionBar(Bukkit.getConsoleSender());
      this.sendActionBar(Bukkit.getOnlinePlayers());
   }

   public final void appendString(@NonNull String string) {
      this.setUnderlying(this.getUnderlying().append(string));
   }

   public final void appendStringList(@NonNull List<String> strings) {
      strings.forEach(this::appendString);
   }

   public final void appendMessage(@NonNull EMFMessage message) {
      this.setUnderlying(this.getUnderlying().append(message.getUnderlying()));
   }

   public final void appendMessageList(@NonNull List<EMFMessage> messages) {
      messages.forEach(this::appendMessage);
   }

   public final void appendComponent(@NonNull Component component) {
      this.setUnderlying(this.getUnderlying().append(component));
   }

   public final void appendComponentList(@NonNull List<Component> components) {
      components.forEach(this::appendComponent);
   }

   public final void prependString(@NonNull String string) {
      this.setUnderlying(this.getUnderlying().prepend(string));
   }

   public final void prependStringList(@NonNull List<String> strings) {
      strings.forEach(this::prependString);
   }

   public final void prependMessage(@NonNull EMFMessage message) {
      this.setUnderlying(this.getUnderlying().prepend(message.getUnderlying()));
   }

   public final void prependMessageList(@NonNull List<EMFMessage> messages) {
      messages.forEach(this::prependMessage);
   }

   public final void prependComponent(@NonNull Component component) {
      this.setUnderlying(this.getUnderlying().prepend(component));
   }

   public final void prependComponentList(@NonNull List<Component> components) {
      components.forEach(this::prependComponent);
   }

   public final void setVariable(@NonNull final String variable, @NonNull final Object replacement) {
      this.setUnderlying(this.getUnderlying().replace(variable, replacement));
   }

   public final void setVariables(@Nullable Map<String, ?> variableMap) {
      if (variableMap != null && !variableMap.isEmpty()) {
         this.setUnderlying(this.getUnderlying().replace(variableMap));
      }
   }

   public void setPlayer(@Nullable final OfflinePlayer player) {
      if (player != null) {
         this.relevantPlayer = player;
         this.setVariable("{player}", Optional.ofNullable(player.getName()).orElse("N/A"));
      }
   }

   public void setLength(@NonNull final Object length) {
      this.setVariable("{length}", length);
   }

   public void setFishCatchVariables(@NonNull IFish fish) {
      this.setLength(Processor.LENGTH_FORMAT.format(fish.getLength()));
      this.setRarity(fish.getRarity());
      Component display = fish.getDisplayName();
      ItemStack fishItem = fish.give();
      if (!fishItem.isEmpty()) {
         display = display.hoverEvent(fishItem);
      }

      this.setFishCaught(display);
   }

   public void setRarity(@NonNull final Object rarity) {
      this.setVariable("{rarity}", rarity);
      this.setVariable("{rarity_colour}", "");
   }

   public void setFishCaught(@NonNull final Object fish) {
      this.setVariable("{fish}", fish);
   }

   public void setSellPrice(@NonNull final Object sellPrice) {
      this.setVariable("{sell-price}", sellPrice);
   }

   public void setAmount(@NonNull final Object amount) {
      this.setVariable("{amount}", amount);
   }

   public void setPosition(@NonNull final Object position) {
      this.setVariable("{position}", position);
   }

   public void setTimeFormatted(@NonNull final Object timeFormatted) {
      this.setVariable("{time_formatted}", timeFormatted);
   }

   public void setTimeRaw(@NonNull final Object timeRaw) {
      this.setVariable("{time_raw}", timeRaw);
   }

   public void setBait(@NonNull final BaitHandler bait, @Nullable final ItemStack baitItem) {
      this.setVariable("{bait_theme}", "");
      Component display = bait.format(bait.getDisplayName()).getComponentMessage().hoverEvent(baitItem);
      this.setVariable("{bait}", display);
   }

   public void setBait(@NonNull final BaitHandler bait) {
      this.setVariable("{bait_theme}", "");
      this.setVariable("{bait}", bait.format(bait.getDisplayName()));
   }

   public void setBait(@NonNull final Object bait) {
      this.setVariable("{bait_theme}", "");
      this.setVariable("{bait}", bait);
   }

   public void setDays(@NonNull final Object days) {
      this.setVariable("{days}", days);
   }

   public void setHours(@NonNull final Object hours) {
      this.setVariable("{hours}", hours);
   }

   public void setMinutes(@NonNull final Object minutes) {
      this.setVariable("{minutes}", minutes);
   }

   public void setToggleMSG(@NonNull final Object toggleMSG) {
      this.setVariable("{toggle_msg}", toggleMSG);
   }

   public void setToggleIcon(@NonNull final Object toggleIcon) {
      this.setVariable("{toggle_icon}", toggleIcon);
   }

   public void setDay(@NonNull final Object day) {
      this.setVariable("{day}", day);
   }

   public void setName(@NonNull final Object name) {
      this.setVariable("{name}", name);
   }

   public void setNumCaught(@NonNull final Object numCaught) {
      this.setVariable("{num_caught}", numCaught);
   }

   public void setLargestSize(@NonNull final Object largestSize) {
      this.setVariable("{largest_size}", largestSize);
   }

   public void setFirstCaught(@NonNull final Object firstCaught) {
      this.setVariable("{first_caught}", firstCaught);
   }

   public void setTimeRemaining(@NonNull final Object timeRemaining) {
      this.setVariable("{time_remaining}", timeRemaining);
   }

   public void setCompetitionType(@NonNull final Object typeString) {
      this.setVariable("{type}", typeString);
   }

   public void setCurrentBaits(@NonNull final Object currentBaits) {
      this.setVariable("{current_baits}", currentBaits);
   }

   public void setMaxBaits(@NonNull final Object maxBaits) {
      this.setVariable("{max_baits}", maxBaits);
   }

   public EMFSingleMessage toSingleMessage() {
      EMFSingleMessage message = EMFSingleMessage.ofUnderlying(this.getUnderlying().toSingleMessage());
      message.perPlayer = this.perPlayer;
      message.relevantPlayer = this.relevantPlayer;
      return message;
   }

   public EMFListMessage toListMessage() {
      EMFListMessage message = EMFListMessage.ofUnderlying(this.getUnderlying().toListMessage());
      message.perPlayer = this.perPlayer;
      message.relevantPlayer = this.relevantPlayer;
      return message;
   }
}

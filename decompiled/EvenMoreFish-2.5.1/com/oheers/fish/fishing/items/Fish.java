package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.api.config.serializer.PotionEffectSerializer;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.selling.WorthNBT;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Fish implements IFish {
   private static final Random random = new Random();
   @NonNull
   private final Section section;
   private final String name;
   private final Rarity rarity;
   private final ItemFactory factory;
   @Nullable
   private OfflinePlayer fisherman;
   private float length;
   @NonNull
   private Requirement requirement;
   private boolean wasBaited;
   private boolean silent;
   private double weight;
   private final boolean disableFisherman;
   private final String displayName;
   private boolean showInJournal;
   private final int globalCatchLimit;
   private final int playerCatchLimit;

   private Fish(@NonNull Rarity rarity, @NonNull Section section) {
      this.section = section;
      this.rarity = rarity;
      this.name = Objects.requireNonNull(section.getNameAsString());
      this.weight = section.getDouble("weight");
      if (this.weight != 0.0) {
         rarity.setFishWeighted(true);
      }

      this.length = -1.0F;
      this.disableFisherman = section.getBoolean("disable-fisherman", rarity.isShouldDisableFisherman());
      ItemFactory factory = ItemFactory.itemFactory(section);
      factory.setFinalChanges(fish -> fish.editMeta(meta -> {
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      }));
      this.factory = factory;
      this.displayName = section.getString("displayname", section.getString("item.displayname"));
      this.showInJournal = section.getBoolean("journal", true);
      this.globalCatchLimit = section.getInt("catch-limit", rarity.getGlobalCatchLimit());
      this.playerCatchLimit = section.getInt("player-catch-limit", rarity.getPlayerCatchLimit());
      LoreItemConfig config = factory.getItemConfig(LoreItemConfig.class);
      if (config != null && config.isEnabled()) {
         config.setEnabled(!section.getBoolean("disable-lore", false));
      }

      this.checkSilent();
      this.requirement = this.loadRequirements();
   }

   public static Fish create(@NonNull Rarity rarity, @NonNull Section section) {
      return new Fish(rarity, section);
   }

   public static Fish createOrThrow(@NonNull Rarity rarity, @Nullable Section section) throws InvalidFishException {
      if (section == null) {
         throw new InvalidFishException("Fish could not be fetched from the config.");
      } else {
         return new Fish(rarity, section);
      }
   }

   private Requirement loadRequirements() {
      Section requirementSection = ConfigUtils.getSectionOfMany(this.section, "requirements", "requirement");
      return new Requirement(requirementSection);
   }

   @NonNull
   @Override
   public ItemStack give(int randomIndex) {
      int initialIndex = this.factory.getRandomIndex();
      this.factory.setRandomIndex(randomIndex);
      ItemStack item = this.give();
      this.factory.setRandomIndex(initialIndex);
      return item;
   }

   @NonNull
   @Override
   public ItemStack give() {
      ItemFactory factory = this.factory.createCopy();
      LoreItemConfig loreConfig = factory.getItemConfig(LoreItemConfig.class);
      if (loreConfig != null) {
         loreConfig.setTransformer(this::buildFishLore);
      }

      DisplayNameItemConfig displayConfig = factory.getItemConfig(DisplayNameItemConfig.class);
      if (displayConfig != null) {
         displayConfig.setTransformer(display -> display == null ? null : this.rarity.format(display).getUnderlying().getMiniMessage());
         displayConfig.setDefault(this.getDisplayNameMessage().getUnderlying().getMiniMessage());
      }

      ItemStack item = this.fisherman == null ? factory.createItem() : factory.createItem(this.fisherman.getUniqueId());
      if (!factory.isRawItem()) {
         WorthNBT.setNBT(item, this);
      }

      return item;
   }

   @Nullable
   private OfflinePlayer getFishermanPlayer() {
      return this.fisherman;
   }

   private void generateSize() {
      Optional<Double> set = this.getSetSize();
      if (set.isPresent()) {
         this.length = set.get().floatValue();
      } else {
         double minSize = this.getMinSize();
         double maxSize = this.getMaxSize();
         if (minSize < 0.0) {
            this.length = -1.0F;
         } else if (minSize == maxSize) {
            this.length = (float)minSize;
         } else {
            double size = random.nextDouble(minSize, maxSize);
            this.length = (float)FishUtils.roundDouble(size, 1);
         }
      }
   }

   @Override
   public double getWorthMultiplier() {
      return this.section.getDouble("worth-multiplier", this.rarity.getWorthMultiplier());
   }

   private void checkMessage() {
      String msg = this.section.getString("message");
      if (msg != null) {
         if (this.fisherman != null) {
            Player player = this.fisherman.getPlayer();
            if (player != null) {
               EMFSingleMessage.fromString(msg).send(player);
            }
         }
      }
   }

   private void checkEffects() {
      String effectConfig = this.section.getString("effect");
      if (effectConfig != null) {
         if (this.fisherman != null) {
            Player player = this.fisherman.getPlayer();
            if (player != null) {
               PotionEffect effect = PotionEffectSerializer.get().deserialize(effectConfig);
               if (effect == null) {
                  Logging.warn(effectConfig + " is not a valid potion effect for fish: " + this.getId());
               } else {
                  Scheduling.getInstance().runTask(player, () -> player.addPotionEffect(effect));
               }
            }
         }
      }
   }

   @Override
   public void init() {
      this.generateSize();
      this.checkMessage();
      this.checkEffects();
   }

   private List<String> getLoreOverride() {
      return this.section.getStringList("lore-override", this.rarity.getLoreOverride());
   }

   private List<Component> buildFishLore(@Nullable List<Component> configured) {
      List<String> loreOverride = this.getLoreOverride();
      EMFListMessage newLoreLine;
      if (loreOverride != null && !loreOverride.isEmpty()) {
         newLoreLine = EMFListMessage.fromStringList(loreOverride);
      } else {
         newLoreLine = ConfigMessage.FISH_LORE.getMessage().toListMessage();
      }

      OfflinePlayer fishermanPlayer = this.getFishermanPlayer();
      EMFListMessage fishLoreReplacement = configured != null && !configured.isEmpty() ? EMFListMessage.ofList(configured) : EMFListMessage.empty();
      newLoreLine.setVariableWithListInsertion("{fish_lore}", fishLoreReplacement);
      if (!this.disableFisherman && fishermanPlayer != null) {
         EMFMessage message = ConfigMessage.FISHERMAN_LORE.getMessage();
         newLoreLine.setVariableWithListInsertion("{fisherman_lore}", message.toListMessage().getUnderlying());
      } else {
         newLoreLine.setVariableWithListInsertion("{fisherman_lore}", EMFListMessage.empty());
      }

      if (this.length > 0.0F) {
         newLoreLine.setVariableWithListInsertion("{length_lore}", ConfigMessage.LENGTH_LORE.getMessage().toListMessage());
      } else {
         newLoreLine.setVariableWithListInsertion("{length_lore}", EMFListMessage.empty());
      }

      newLoreLine.setRelevantPlayer(fishermanPlayer);
      newLoreLine.setLength(this.length);
      newLoreLine.setRarity(this.rarity.getLorePrep());
      return newLoreLine.getComponentListMessage();
   }

   @Override
   public void checkSilent() {
      this.silent = this.section.getBoolean("silent", false);
   }

   @NonNull
   public Fish createCopy() {
      return create(this.rarity, this.section);
   }

   @Override
   public boolean hasFishermanDisabled() {
      return this.disableFisherman;
   }

   @NonNull
   @Override
   public Optional<Double> getSetSize() {
      Double size = FishUtils.fetchSize(this.section, "size", this.fisherman);
      return size == null ? this.rarity.getSetSize(this.fisherman) : Optional.of(size);
   }

   @Override
   public double getMinSize() {
      Double minSize = FishUtils.fetchSize(this.section, "size.minSize", this.fisherman);
      return minSize == null ? this.rarity.getMinSize(this.fisherman) : minSize;
   }

   @Override
   public double getMaxSize() {
      Double maxSize = FishUtils.fetchSize(this.section, "size.maxSize", this.fisherman);
      return maxSize == null ? this.rarity.getMaxSize(this.fisherman) : maxSize;
   }

   @Nullable
   @Override
   public UUID getFishermanUUID() {
      return this.fisherman == null ? null : this.fisherman.getUniqueId();
   }

   @Override
   public void setFisherman(@Nullable UUID uuid) {
      this.fisherman = uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
   }

   @Override
   public void setFisherman(@Nullable OfflinePlayer fisherman) {
      this.fisherman = fisherman;
   }

   @Override
   public double getSetWorth() {
      Double worth = FishUtils.parseDoubleOrRange(this.section.getString("set-worth"));
      return worth == null ? this.rarity.getSetWorth() : worth;
   }

   @NonNull
   public Rarity getRarity() {
      return this.rarity;
   }

   @Override
   public float getLength() {
      return this.length;
   }

   @Override
   public void setLength(Float length) {
      this.length = Optional.ofNullable(length).orElse(-1.0F);
   }

   @Override
   public double getWeight() {
      return this.weight;
   }

   @NonNull
   @Override
   public Component getDisplayName() {
      return this.getDisplayNameMessage().getComponentMessage(this.fisherman);
   }

   @NonNull
   public EMFSingleMessage getDisplayNameMessage() {
      return this.displayName == null ? this.rarity.format(this.name) : this.rarity.format(this.displayName);
   }

   @Override
   public int getGlobalCatchLimit() {
      return this.globalCatchLimit;
   }

   @Override
   public int getPlayerCatchLimit() {
      return this.playerCatchLimit;
   }

   @NonNull
   @Override
   public String getId() {
      return this.name;
   }

   @Override
   public void setWeight(double weight) {
      this.weight = weight;
   }

   @NonNull
   public ItemFactory getFactory() {
      return this.factory;
   }

   @NonNull
   @Override
   public Requirement getRequirement() {
      return this.requirement;
   }

   @Override
   public void setRequirement(@NonNull Requirement requirement) {
      this.requirement = requirement;
   }

   @Override
   public boolean isWasBaited() {
      return this.wasBaited;
   }

   @Override
   public void setWasBaited(boolean wasBaited) {
      this.wasBaited = wasBaited;
   }

   @Override
   public boolean isSilent() {
      return this.silent;
   }

   @Override
   public void setSilent(boolean silent) {
      this.silent = silent;
   }

   private String parseEventPlaceholders(String rewardString) {
      rewardString = rewardString.replace("{length}", String.valueOf(this.length));
      String rarityReplacement = "";
      if (this.rarity != null) {
         rarityReplacement = this.rarity.getId();
      }

      rewardString = rewardString.replace("{rarity}", rarityReplacement);
      String displayNameReplacement = this.getDisplayNameMessage().getPlainTextMessage(this.fisherman);
      rewardString = rewardString.replace("{displayname}", displayNameReplacement);
      String nameReplacement = "";
      if (this.name != null) {
         nameReplacement = this.name;
      }

      return rewardString.replace("{name}", nameReplacement);
   }

   @NonNull
   @Override
   public CatchType getCatchType() {
      String typeStr = this.section.getString("catch-type");
      if (typeStr == null) {
         return this.rarity.getCatchType();
      } else {
         CatchType catchType = FishUtils.getEnumValue(CatchType.class, typeStr);
         return catchType == null ? this.rarity.getCatchType() : catchType;
      }
   }

   @Override
   public boolean getShowInJournal() {
      return this.showInJournal;
   }

   @Override
   public void setShowInJournal(boolean showInJournal) {
      this.showInJournal = showInJournal;
   }

   @Override
   public boolean isTrackInDatabase() {
      return this.section.getBoolean("track-in-database", true);
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else {
         return !(other instanceof Fish fish) ? false : this.getRarity().equals(fish.getRarity()) && this.getId().equals(fish.getId());
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getRarity(), this.getId());
   }

   @Override
   public int getIndex() {
      return this.section.getInt("sort-index");
   }

   @NonNull
   @Override
   public List<Reward> getInteractRewards() {
      List<String> strings = this.section.getStringList("interact-event", this.rarity.getInteractRewards());
      return this.parseRewards(strings);
   }

   @NonNull
   @Override
   public List<Reward> getEatRewards() {
      List<String> strings = this.section.getStringList("eat-event", this.rarity.getEatRewards());
      return this.parseRewards(strings);
   }

   @NonNull
   @Override
   public List<Reward> getCatchRewards() {
      List<String> strings = this.section.getStringList("catch-event", this.rarity.getCatchRewards());
      return this.parseRewards(strings);
   }

   @NonNull
   @Override
   public List<Reward> getSellRewards() {
      List<String> strings = this.section.getStringList("sell-event", this.rarity.getSellRewards());
      return this.parseRewards(strings);
   }

   private List<Reward> parseRewards(@NonNull List<String> strings) {
      return strings.stream().map(this::parseEventPlaceholders).map(Reward::new).toList();
   }
}

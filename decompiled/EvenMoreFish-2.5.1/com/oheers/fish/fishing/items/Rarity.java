package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigUtils;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.exceptions.InvalidFishException;
import com.oheers.fish.fishing.items.config.RarityFileUpdates;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFSingleMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Rarity extends ConfigBase implements IRarity {
   @NonNull
   private final String id;
   private boolean fishWeighted;
   private boolean showInJournal = true;
   private final Requirement requirement;
   private final List<IFish> fishList;

   public Rarity(@NonNull File file) throws InvalidConfigurationException {
      super(file, EvenMoreFish.getInstance(), false);
      new RarityFileUpdates(this).update();
      this.id = this.validateId();
      this.requirement = this.loadRequirements();
      this.fishList = this.loadFish();
      this.showInJournal = this.getConfig().getBoolean("journal", true);
   }

   private String validateId() throws InvalidConfigurationException {
      String id = this.getConfig().getString("id");
      if (id == null) {
         throw new InvalidConfigurationException("Rarity " + this.getFileName() + " has no configured id.");
      } else {
         return id;
      }
   }

   @NonNull
   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public boolean isDisabled() {
      return this.getConfig().getBoolean("disabled");
   }

   @NonNull
   private String getRawFormat() {
      return this.getConfig().getString("format", "<white>{name}");
   }

   @NonNull
   public EMFSingleMessage getFormat() {
      return EMFSingleMessage.fromString(this.getRawFormat());
   }

   @NonNull
   public EMFSingleMessage format(@NonNull String name) {
      String format = this.getRawFormat().replace("{name}", name);
      return EMFSingleMessage.fromString(format);
   }

   @Override
   public double getWeight() {
      return this.getConfig().getDouble("weight");
   }

   public int getGlobalCatchLimit() {
      return this.getConfig().getInt("catch-limit", -1);
   }

   public int getPlayerCatchLimit() {
      return this.getConfig().getInt("player-catch-limit", -1);
   }

   @Override
   public boolean getBroadcastEnabled() {
      return this.getConfig().getBoolean("broadcast.enabled", true);
   }

   @Override
   public boolean getBroadcastOnlyRods() {
      return this.getConfig().getBoolean("broadcast.only-rods", false);
   }

   @Override
   public int getBroadcastRange() {
      return this.getConfig().getInt("broadcast.range", -1);
   }

   @Override
   public boolean getUseConfigCasing() {
      return this.getConfig().getBoolean("use-this-casing");
   }

   @NonNull
   @Override
   public Component getDisplayName() {
      return this.getDisplayNameMessage().getComponentMessage();
   }

   @NonNull
   public EMFSingleMessage getDisplayNameMessage() {
      String displayName = this.getConfig().getString("displayname", this.id);
      return this.format(displayName);
   }

   @NonNull
   public EMFSingleMessage getLorePrep() {
      String loreOverride = this.getConfig().getString("override-lore");
      if (loreOverride != null) {
         return EMFSingleMessage.fromString(loreOverride);
      } else {
         String displayName = this.getConfig().getString("displayname");
         if (displayName != null) {
            return EMFSingleMessage.fromString(displayName);
         } else {
            String finalName = this.getId();
            if (!this.getUseConfigCasing()) {
               finalName = finalName.toUpperCase();
            }

            return this.format(finalName);
         }
      }
   }

   protected List<String> getLoreOverride() {
      return this.getConfig().getStringList("lore-override");
   }

   @Nullable
   @Override
   public String getPermission() {
      return this.getConfig().getString("permission");
   }

   @NonNull
   @Override
   public Requirement getRequirement() {
      return this.requirement;
   }

   @Override
   public boolean isShouldDisableFisherman() {
      return this.getConfig().getBoolean("disable-fisherman", false);
   }

   @Override
   public double getSetWorth() {
      Double worth = FishUtils.parseDoubleOrRange(this.getConfig().getString("set-worth"));
      return worth == null ? -1.0 : worth;
   }

   @NonNull
   @Override
   public Optional<Double> getSetSize() {
      return this.getSetSize(null);
   }

   @NonNull
   public Optional<Double> getSetSize(@Nullable OfflinePlayer player) {
      return Optional.ofNullable(FishUtils.fetchSize(this.getConfig(), "size", player));
   }

   @Override
   public double getMinSize() {
      return this.getMinSize(null);
   }

   public double getMinSize(@Nullable OfflinePlayer player) {
      Double minSize = FishUtils.fetchSize(this.getConfig(), "size.minSize", player);
      return minSize == null ? 1.0 : minSize;
   }

   @Override
   public double getMaxSize() {
      return this.getMaxSize(null);
   }

   public double getMaxSize(@Nullable OfflinePlayer player) {
      Double maxSize = FishUtils.fetchSize(this.getConfig(), "size.maxSize", player);
      return maxSize == null ? 10.0 : maxSize;
   }

   @NonNull
   @Override
   public List<IFish> getOriginalFishList() {
      return this.fishList;
   }

   @NonNull
   @Override
   public List<IFish> getFishList() {
      return this.fishList.stream().map(IFish::createCopy).toList();
   }

   @Nullable
   @Override
   public IFish getEditableFish(@NonNull String name) {
      for (IFish fish : this.fishList) {
         if (fish.getId().equalsIgnoreCase(name)) {
            return fish;
         }
      }

      return null;
   }

   @Nullable
   @Override
   public IFish getFish(@NonNull String name) {
      IFish fish = this.getEditableFish(name);
      return fish == null ? null : fish.createCopy();
   }

   @Override
   public double getWorthMultiplier() {
      return this.getConfig().getDouble("worth-multiplier", 0.0);
   }

   @NonNull
   @Override
   public ItemStack getJournalItem() {
      ItemStack oldItem = ItemSerializer.get().deserialize(this.getConfig().getString("material"), true);
      if (oldItem != null) {
         return oldItem;
      } else {
         ItemFactory factory = ItemFactory.itemFactory(this.getConfig());
         return factory.createItem();
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
   public boolean isFishWeighted() {
      return this.fishWeighted;
   }

   @Override
   public void setFishWeighted(boolean fishWeighted) {
      this.fishWeighted = fishWeighted;
   }

   private List<IFish> loadFish() {
      Section rootFishSection = this.getConfig().getSection("fish");
      if (rootFishSection == null) {
         return List.of();
      } else {
         List<IFish> fishList = new ArrayList<>();
         rootFishSection.getRoutesAsStrings(false).forEach(fishStr -> {
            Section fishSection = rootFishSection.getSection(fishStr);
            if (fishSection == null) {
               fishSection = rootFishSection.createSection(fishStr);
            }

            try {
               fishList.add(Fish.createOrThrow(this, fishSection));
            } catch (InvalidFishException var6) {
               EvenMoreFish.getInstance().getLogger().log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         });
         return List.copyOf(fishList);
      }
   }

   private Requirement loadRequirements() {
      Section requirementSection = ConfigUtils.getSectionOfMany(this.getConfig(), "requirements", "requirement");
      return new Requirement(requirementSection);
   }

   @NonNull
   protected CatchType getCatchType() {
      String typeStr = this.getConfig().getString("catch-type");
      CatchType type = FishUtils.getEnumValue(CatchType.class, typeStr);
      return type == null ? CatchType.BOTH : type;
   }

   @NonNull
   public List<String> getInteractRewards() {
      return this.getConfig().getStringList("interact-event");
   }

   @NonNull
   public List<String> getEatRewards() {
      return this.getConfig().getStringList("eat-event");
   }

   @NonNull
   public List<String> getCatchRewards() {
      return this.getConfig().getStringList("catch-event");
   }

   @NonNull
   public List<String> getSellRewards() {
      return this.getConfig().getStringList("sell-event");
   }

   @Override
   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else {
         return other instanceof Rarity rarity ? this.getId().equals(rarity.getId()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getId());
   }

   @Override
   public int getIndex() {
      return this.getConfig().getInt("sort-index");
   }
}

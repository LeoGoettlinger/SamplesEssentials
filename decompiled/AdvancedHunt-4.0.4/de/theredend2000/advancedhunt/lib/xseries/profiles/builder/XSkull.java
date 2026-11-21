package de.theredend2000.advancedhunt.lib.xseries.profiles.builder;

import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.profiles.PlayerProfiles;
import de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile.MojangGameProfile;
import de.theredend2000.advancedhunt.lib.xseries.profiles.objects.ProfileContainer;
import de.theredend2000.advancedhunt.lib.xseries.profiles.objects.ProfileInputType;
import de.theredend2000.advancedhunt.lib.xseries.profiles.objects.Profileable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

public final class XSkull {
   private static final MojangGameProfile DEFAULT_PROFILE = PlayerProfiles.signXSeries(
      ProfileInputType.BASE64
         .getProfile(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0="
         )
   );

   @NotNull
   @Contract(
      value = "-> new",
      pure = true
   )
   public static ProfileInstruction<ItemStack> createItem() {
      return of(XMaterial.PLAYER_HEAD.parseItem());
   }

   @NotNull
   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static ProfileInstruction<ItemStack> of(@NotNull ItemStack item) {
      return new ProfileInstruction<>(new ProfileContainer.ItemStackProfileContainer(item));
   }

   @NotNull
   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static ProfileInstruction<ItemMeta> of(@NotNull ItemMeta meta) {
      return new ProfileInstruction<>(new ProfileContainer.ItemMetaProfileContainer((SkullMeta)meta));
   }

   @NotNull
   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static ProfileInstruction<Block> of(@NotNull Block block) {
      return new ProfileInstruction<>(new ProfileContainer.BlockProfileContainer(block));
   }

   @NotNull
   @Contract(
      value = "_ -> new",
      pure = true
   )
   public static ProfileInstruction<Skull> of(@NotNull BlockState state) {
      return new ProfileInstruction<>(new ProfileContainer.BlockStateProfileContainer((Skull)state));
   }

   @NotNull
   @Contract(
      value = "_ -> new",
      pure = true
   )
   @Experimental
   static ProfileInstruction<Player> of(@NotNull Player player) {
      return new ProfileInstruction<>(new ProfileContainer.PlayerContainer(player));
   }

   @NotNull
   @Contract(
      value = "-> new",
      pure = true
   )
   static Profileable getDefaultProfile() {
      return Profileable.of(DEFAULT_PROFILE.copy(), false);
   }
}

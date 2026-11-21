package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_R1;

import com.mojang.authlib.GameProfile;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;

class PaperweightFakePlayer extends ServerPlayer {
   private static final GameProfile FAKE_WORLDEDIT_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("worldedit".getBytes()), "[WorldEdit]");
   private static final Vec3 ORIGIN = new Vec3(0.0, 0.0, 0.0);
   private static final ClientInformation FAKE_CLIENT_INFO = new ClientInformation("en_US", 16, ChatVisiblity.FULL, true, 0, HumanoidArm.LEFT, false, false);

   PaperweightFakePlayer(ServerLevel world) {
      super(world.getServer(), world, FAKE_WORLDEDIT_PROFILE, FAKE_CLIENT_INFO);
   }

   public Vec3 position() {
      return ORIGIN;
   }

   public void tick() {
   }

   public void die(DamageSource damagesource) {
   }

   public OptionalInt openMenu(MenuProvider factory) {
      return OptionalInt.empty();
   }

   public void updateOptions(ClientInformation clientOptions) {
   }

   public void displayClientMessage(Component message, boolean actionBar) {
   }

   public void awardStat(Stat<?> stat, int amount) {
   }

   public void awardStat(Stat<?> stat) {
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return true;
   }

   public void openTextEdit(SignBlockEntity sign, boolean front) {
   }
}

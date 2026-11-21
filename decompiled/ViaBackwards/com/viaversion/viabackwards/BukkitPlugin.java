package com.viaversion.viabackwards;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import com.viaversion.viabackwards.listener.DurabilitySync1_11;
import com.viaversion.viabackwards.listener.FireExtinguish1_16;
import com.viaversion.viabackwards.listener.ItemDropSync1_17;
import com.viaversion.viabackwards.listener.LecternInteract1_14;
import com.viaversion.viabackwards.listener.PlayerHurtSound1_12;
import com.viaversion.viabackwards.listener.SpearAttack1_21_11;
import com.viaversion.viabackwards.protocol.v1_20_2to1_20.provider.AdvancementCriteriaProvider;
import com.viaversion.viabackwards.provider.BukkitAdvancementCriteriaProvider;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements ViaBackwardsPlatform {
   public BukkitPlugin() {
      Via.getManager().addEnableListener(() -> this.init(new File(this.getDataFolder(), "config.yml")));
   }

   public void onEnable() {
      if (Via.getManager().getInjector().lateProtocolVersionSetting()) {
         Via.getPlatform().runSync(this::enable, 1L);
      } else {
         this.enable();
      }
   }

   @Override
   public void enable() {
      ViaBackwardsPlatform.super.enable();
      ProtocolVersion protocolVersion = Via.getAPI().getServerVersion().highestSupportedProtocolVersion();
      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_21_11)) {
         new SpearAttack1_21_11(this).register();
      }

      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_17)) {
         new ItemDropSync1_17(this).register();
      }

      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_16)) {
         new FireExtinguish1_16(this).register();
      }

      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_14)) {
         new LecternInteract1_14(this).register();
      }

      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_12)) {
         new PlayerHurtSound1_12(this).register();
      }

      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_11)) {
         new DurabilitySync1_11(this).register();
      }

      ViaProviders providers = Via.getManager().getProviders();
      if (protocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_20_2)) {
         providers.use(AdvancementCriteriaProvider.class, new BukkitAdvancementCriteriaProvider());
      }
   }

   @Override
   public void disable() {
      this.getPluginLoader().disablePlugin(this);
   }
}

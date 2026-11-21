package ac.grim.grimac.checks;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.Generated;

public class Check extends GrimProcessor implements AbstractCheck {
   private static final FlagEvent.Channel FLAG_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(FlagEvent.class);
   @NotNull
   protected final GrimPlayer player;
   public double violations;
   private double decay;
   private double setbackVL;
   private final VerboseBuf verbose = new VerboseBuf();
   private String checkName;
   private String configName;
   private String alternativeName;
   private String displayName;
   private String description;
   private String stableKey = "";
   private boolean experimental;
   private boolean isEnabled;
   private boolean exemptPermission;
   private boolean noSetbackPermission;
   private boolean noModifyPacketPermission;
   private long lastViolationTime;
   private boolean lastFlagStoredBinaryVerbose;

   public Check(@NotNull GrimPlayer player) {
      this.player = Objects.requireNonNull(player);
      CheckData checkData = this.getClass().getAnnotation(CheckData.class);
      if (checkData != null) {
         this.checkName = checkData.name();
         this.configName = checkData.configName();
         if (this.configName.equals("DEFAULT")) {
            this.configName = this.checkName;
         }

         this.decay = checkData.decay();
         this.setbackVL = checkData.setback();
         this.alternativeName = checkData.alternativeName();
         this.experimental = checkData.experimental();
         this.description = checkData.description();
         this.stableKey = checkData.stableKey();
         this.displayName = this.checkName;
      }

      this.reload();
   }

   public boolean shouldModifyPackets() {
      return this.isEnabled && !this.player.disableGrim && !this.player.noModifyPacketPermission && !this.noModifyPacketPermission && !this.exemptPermission;
   }

   public final void updatePermissions() {
      if (this.configName != null) {
         String id = this.configName.toLowerCase();
         this.exemptPermission = this.player.hasPermission("grim.exempt." + id);
         this.noSetbackPermission = this.player.hasPermission("grim.nosetback." + id);
         this.noModifyPacketPermission = this.player.hasPermission("grim.nomodifypacket." + id);
      }
   }

   public final boolean flag() {
      return this.flag("");
   }

   public final boolean flag(String verbose) {
      Supplier<String> alertText = constant(verbose);
      if (this.recordFlag(alertText)) {
         this.alert(alertText);
         return true;
      } else {
         return false;
      }
   }

   public final boolean flag(@NotNull Verbose.Writer verbose) {
      Check.BinaryVerbose binary = this.lazyVerbose(verbose);
      if (this.recordFlag(binary)) {
         this.alert(binary.rendered());
         return true;
      } else {
         return false;
      }
   }

   public final boolean flag(@NotNull Verbose.Writer verbose, @NotNull Supplier<String> alertText) {
      Check.BinaryVerbose binary = this.lazyVerbose(verbose);
      if (this.recordFlag(binary)) {
         this.alert(memoize(Objects.requireNonNull(alertText, "alertText")));
         return true;
      } else {
         return false;
      }
   }

   private boolean recordFlag(@NotNull Supplier<String> verbose) {
      if (this.player.disableGrim || this.experimental && !this.player.isExperimentalChecks() || this.exemptPermission) {
         return false;
      } else if (FLAG_CHANNEL.fire(this.player, this, verbose)) {
         return false;
      } else {
         this.lastFlagStoredBinaryVerbose = false;
         this.player.punishmentManager.handleViolation(this);
         this.lastViolationTime = System.currentTimeMillis();
         this.violations++;
         return true;
      }
   }

   private boolean recordFlag(@NotNull Check.BinaryVerbose verbose) {
      Supplier<String> rendered = verbose.rendered();
      byte[] verboseData = verbose.data();
      if (this.player.disableGrim || this.experimental && !this.player.isExperimentalChecks() || this.exemptPermission) {
         return false;
      } else if (FLAG_CHANNEL.fire(this.player, this, rendered)) {
         return false;
      } else {
         this.lastFlagStoredBinaryVerbose = true;
         this.player.punishmentManager.handleViolation(this);
         this.lastViolationTime = System.currentTimeMillis();
         this.violations++;
         GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks().recordFlagDataFromCheck(this.player, this, this.violations, verboseData);
         return true;
      }
   }

   @NotNull
   private Check.BinaryVerbose lazyVerbose(@NotNull Verbose.Writer writer) {
      Objects.requireNonNull(writer, "writer");
      byte[] verboseData = writer.end().toByteArray();
      Verbose template = writer.verbose();
      Supplier<String> rendered = memoize(
         () -> template.render(
            verboseData,
            new VerboseRenderContext(
               this.player.getClientVersion().getProtocolVersion(), GrimAPI.INSTANCE.getPlatformServer().getPlatformImplementationString()
            )
         )
      );
      return new Check.BinaryVerbose(verboseData, rendered);
   }

   public final void registerVerboseTemplates(@Nullable VerboseRegistry registry) {
      if (registry != null && !this.stableKey.isEmpty()) {
         String pluginVersion = safePluginVersion();

         for (Verbose template : Verbose.declaredBy(this.getClass(), Check.class)) {
            registry.registerTemplate(this.stableKey, this.checkName, this.description, pluginVersion, template);
         }
      }
   }

   @Nullable
   private static String safePluginVersion() {
      try {
         return GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
      } catch (RuntimeException var1) {
         return null;
      }
   }

   @NotNull
   protected final VerboseBuf verbose() {
      return this.verbose;
   }

   public final boolean flagWithSetback() {
      return this.flagWithSetback("");
   }

   public final boolean flagWithSetback(String verbose) {
      if (this.flag(verbose)) {
         this.setbackIfAboveSetbackVL();
         return true;
      } else {
         return false;
      }
   }

   public final boolean flagWithSetback(@NotNull Verbose.Writer verbose) {
      if (this.flag(verbose)) {
         this.setbackIfAboveSetbackVL();
         return true;
      } else {
         return false;
      }
   }

   public final boolean flagWithSetback(@NotNull Verbose.Writer verbose, @NotNull Supplier<String> alertText) {
      if (this.flag(verbose, alertText)) {
         this.setbackIfAboveSetbackVL();
         return true;
      } else {
         return false;
      }
   }

   public final void reward() {
      this.violations = Math.max(0.0, this.violations - this.decay);
   }

   public final void reload(ConfigManager configuration) {
      this.decay = configuration.getDoubleElse(this.configName + ".decay", this.decay);
      this.setbackVL = configuration.getDoubleElse(this.configName + ".setbackvl", this.setbackVL);
      this.displayName = configuration.getStringElse(this.configName + ".displayname", this.checkName);
      this.description = configuration.getStringElse(this.configName + ".description", this.description);
      if (this.setbackVL == -1.0) {
         this.setbackVL = Double.MAX_VALUE;
      }

      this.onReload(configuration);
   }

   @Override
   public void onReload(ConfigManager config) {
   }

   public boolean alert(String verbose) {
      return this.alert(constant(verbose));
   }

   public boolean alert(@NotNull Supplier<String> verbose) {
      return this.player.punishmentManager.handleAlert(this.player, memoize(Objects.requireNonNull(verbose, "verbose")), this);
   }

   public boolean setbackIfAboveSetbackVL() {
      return this.shouldSetback() ? this.player.getSetbackTeleportUtil().executeViolationSetback() : false;
   }

   public boolean shouldSetback() {
      return !this.noSetbackPermission && this.violations > this.setbackVL;
   }

   public boolean executeViolationSetback() {
      return !this.noSetbackPermission && this.player.getSetbackTeleportUtil().executeViolationSetback();
   }

   public String formatOffset(double offset) {
      return offset > 0.001 ? String.format("%.5f", offset) : String.format("%.2E", offset);
   }

   public static boolean isTransaction(PacketTypeCommon packetType) {
      return packetType == PacketType.Play.Client.PONG || packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
   }

   public static boolean isAsync(PacketTypeCommon packetType) {
      return packetType == PacketType.Play.Client.KEEP_ALIVE
         || packetType == PacketType.Play.Client.CHUNK_BATCH_ACK
         || packetType == PacketType.Play.Client.RESOURCE_PACK_STATUS;
   }

   public boolean isUpdate(PacketTypeCommon packetType) {
      return WrapperPlayClientPlayerFlying.isFlying(packetType) || packetType == PacketType.Play.Client.CLIENT_TICK_END || isTransaction(packetType);
   }

   public boolean isTickPacket(PacketTypeCommon packetType) {
      if (this.isTickPacketIncludingNonMovement(packetType)) {
         return !WrapperPlayClientPlayerFlying.isFlying(packetType)
            ? true
            : !this.player.packetStateData.lastPacketWasTeleport && !this.player.packetStateData.lastPacketWasOnePointSeventeenDuplicate;
      } else {
         return false;
      }
   }

   public boolean isTickPacketIncludingNonMovement(PacketTypeCommon packetType) {
      return this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
            && !this.player.packetStateData.didSendMovementBeforeTickEnd
            && packetType == PacketType.Play.Client.CLIENT_TICK_END
         ? true
         : WrapperPlayClientPlayerFlying.isFlying(packetType);
   }

   public boolean canCancel(DiggingAction action) {
      return action != DiggingAction.RELEASE_USE_ITEM
         && (
            action != DiggingAction.DROP_ITEM && action != DiggingAction.DROP_ITEM_STACK
               || this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)
         );
   }

   @NotNull
   private static Supplier<String> constant(String verbose) {
      String value = verbose == null ? "" : verbose;
      return () -> value;
   }

   @NotNull
   private static Supplier<String> memoize(@NotNull final Supplier<String> supplier) {
      return new Supplier<String>() {
         private String value;
         private boolean computed;

         public synchronized String get() {
            if (!this.computed) {
               try {
                  this.value = supplier.get();
                  if (this.value == null) {
                     this.value = "";
                  }
               } catch (Throwable var2) {
                  this.value = "";
               }

               this.computed = true;
            }

            return this.value;
         }
      };
   }

   @NotNull
   @Generated
   public GrimPlayer getPlayer() {
      return this.player;
   }

   @Generated
   @Override
   public double getViolations() {
      return this.violations;
   }

   @Generated
   @Override
   public double getDecay() {
      return this.decay;
   }

   @Generated
   @Override
   public double getSetbackVL() {
      return this.setbackVL;
   }

   @Generated
   public VerboseBuf getVerbose() {
      return this.verbose;
   }

   @Generated
   @Override
   public String getCheckName() {
      return this.checkName;
   }

   @Generated
   @Override
   public String getConfigName() {
      return this.configName;
   }

   @Generated
   @Override
   public String getAlternativeName() {
      return this.alternativeName;
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }

   @Generated
   @Override
   public String getDescription() {
      return this.description;
   }

   @Generated
   @Override
   public String getStableKey() {
      return this.stableKey;
   }

   @Generated
   @Override
   public boolean isExperimental() {
      return this.experimental;
   }

   @Generated
   @Override
   public boolean isEnabled() {
      return this.isEnabled;
   }

   @Generated
   public boolean isExemptPermission() {
      return this.exemptPermission;
   }

   @Generated
   public boolean isNoSetbackPermission() {
      return this.noSetbackPermission;
   }

   @Generated
   public boolean isNoModifyPacketPermission() {
      return this.noModifyPacketPermission;
   }

   @Generated
   @Override
   public long getLastViolationTime() {
      return this.lastViolationTime;
   }

   @Generated
   public boolean isLastFlagStoredBinaryVerbose() {
      return this.lastFlagStoredBinaryVerbose;
   }

   @Generated
   @Override
   public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
   }

   private record BinaryVerbose(@NotNull byte[] data, @NotNull Supplier<String> rendered) {
   }
}

package ac.grim.grimac.checks.impl.verbose;

import ac.grim.grimac.api.storage.verbose.VerboseSchema;
import ac.grim.grimac.api.storage.verbose.VerboseTags;
import ac.grim.grimac.checks.impl.prediction.OffsetHandler;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;

public final class VerboseCodecs {
   public static final int PACKET_NONE = Integer.MIN_VALUE;
   public static final int PACKET_TRANSACTION = -2147483647;

   private VerboseCodecs() {
   }

   public static void ensureRegistered() {
   }

   public static int enumId(@Nullable Enum<?> value) {
      return VerboseTags.enumId(value);
   }

   public static int block(@NotNull StateType type, @NotNull ClientVersion version) {
      try {
         StateType.Mapped mapped = type.getMapped();
         return mapped == null ? -1 : mapped.getId(version);
      } catch (RuntimeException var3) {
         return -1;
      }
   }

   public static int item(@NotNull ItemType type, @NotNull ClientVersion version) {
      return type.getId(version);
   }

   public static int packet(@NotNull PacketTypeCommon type, @NotNull ClientVersion version) {
      return type.getId(version);
   }

   public static int entity(@NotNull EntityType type, @NotNull ClientVersion version) {
      return type.getId(version);
   }

   @NotNull
   private static String blockName(int clientVersionPvn, int id) {
      if (id < 0) {
         return "unknown";
      } else {
         StateType type = StateTypes.getById(ClientVersion.getById(clientVersionPvn), id);
         return type == null ? "unknown(" + id + ")" : type.getName();
      }
   }

   @NotNull
   private static String itemTypeName(int clientVersionPvn, int id) {
      if (id < 0) {
         return "";
      } else {
         ItemType type = ItemTypes.getById(ClientVersion.getById(clientVersionPvn), id);
         return type == null ? "unknown(" + id + ")" : type.getName().getKey();
      }
   }

   @NotNull
   private static String packetName(int clientVersionPvn, int packetId) {
      if (packetId == Integer.MIN_VALUE) {
         return "";
      } else if (packetId == -2147483647) {
         return "TRANSACTION";
      } else {
         PacketTypeCommon type = PacketType.Play.Client.getById(ClientVersion.getById(clientVersionPvn), packetId);
         return type == null ? "unknown(" + packetId + ")" : type.getName();
      }
   }

   @NotNull
   private static String entityTypeName(int clientVersionPvn, int entityId) {
      EntityType entityType = EntityTypes.getById(ClientVersion.getById(clientVersionPvn), entityId);
      return entityType == null ? "unknown" : entityType.getName().getKey();
   }

   @NotNull
   private static String formatNumberStandard(double value) {
      double abs = Math.abs(value);
      if (abs < 1.0E-7) {
         return "0";
      } else if (abs < 0.001) {
         String formatted = String.format("%.4E", value);
         return formatted.replace("E-0", "E-");
      } else {
         String formatted = String.format("%6f", value);
         return formatted.replace("0.", ".");
      }
   }

   static {
      VerboseTags.registerEnum("face", BlockFace.values());
      VerboseTags.registerEnum("digging", DiggingAction.values());
      VerboseTags.registerEnumLower("digging_lower", DiggingAction.values());
      VerboseTags.registerEnum("clicktype", WrapperPlayClientClickWindow.WindowClickType.values());
      VerboseTags.registerEnumLower("clicktype_lower", WrapperPlayClientClickWindow.WindowClickType.values());
      VerboseTags.registerEnum("entityaction", WrapperPlayClientEntityAction.Action.values());
      VerboseTags.registerEnum("hand", InteractionHand.values());
      VerboseTags.register("block", List.of(VerboseSchema.TypeTag.ZZ), (in, ctx, out, fmt) -> out.append(blockName(ctx.clientVersionPvn(), in.rzz())));
      VerboseTags.register("item", List.of(VerboseSchema.TypeTag.ZZ), (in, ctx, out, fmt) -> out.append(itemTypeName(ctx.clientVersionPvn(), in.rzz())));
      VerboseTags.register("packet", List.of(VerboseSchema.TypeTag.ZZ), (in, ctx, out, fmt) -> out.append(packetName(ctx.clientVersionPvn(), in.rzz())));
      VerboseTags.register("entity", List.of(VerboseSchema.TypeTag.VI), (in, ctx, out, fmt) -> out.append(entityTypeName(ctx.clientVersionPvn(), in.rvi())));
      VerboseTags.register("offset", List.of(VerboseSchema.TypeTag.F64), (in, ctx, out, fmt) -> out.append(OffsetHandler.humanFormattedOffset(in.rf64())));
      VerboseTags.register("stdnum", List.of(VerboseSchema.TypeTag.F64), (in, ctx, out, fmt) -> out.append(formatNumberStandard(in.rf64())));
   }
}

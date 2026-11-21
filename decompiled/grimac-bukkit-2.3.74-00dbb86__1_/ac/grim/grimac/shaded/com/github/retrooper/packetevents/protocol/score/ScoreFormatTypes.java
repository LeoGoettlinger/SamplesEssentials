package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.score;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.VersionedRegistry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ScoreFormatTypes {
   private static final VersionedRegistry<ScoreFormatType<?>> REGISTRY = new VersionedRegistry<>("number_format_type");
   public static final ScoreFormatType<BlankScoreFormat> BLANK = define("blank", BlankScoreFormat::read, BlankScoreFormat::write);
   public static final ScoreFormatType<StyledScoreFormat> STYLED = define("styled", StyledScoreFormat::read, StyledScoreFormat::write);
   public static final ScoreFormatType<FixedScoreFormat> FIXED = define("fixed", FixedScoreFormat::read, FixedScoreFormat::write);

   private ScoreFormatTypes() {
   }

   public static VersionedRegistry<ScoreFormatType<?>> getRegistry() {
      return REGISTRY;
   }

   public static Collection<ScoreFormatType<?>> values() {
      return REGISTRY.getEntries();
   }

   @Deprecated
   public static ScoreFormat read(PacketWrapper<?> wrapper) {
      return ScoreFormat.readTyped(wrapper);
   }

   @Deprecated
   public static void write(PacketWrapper<?> wrapper, ScoreFormat format) {
      ScoreFormat.writeTyped(wrapper, format);
   }

   @Deprecated
   @ApiStatus.Internal
   public static <T extends ScoreFormat> ScoreFormatType<T> define(
      int id, String name, Class<T> formatClass, Function<PacketWrapper<?>, T> reader, BiConsumer<PacketWrapper<?>, T> writer
   ) {
      return define(name, reader::apply, writer::accept);
   }

   @ApiStatus.Internal
   public static <T extends ScoreFormat> ScoreFormatType<T> define(String name, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      return REGISTRY.define(name, data -> new StaticScoreFormatType<>(data, reader, writer));
   }

   @Nullable
   public static ScoreFormatType<?> getById(ClientVersion version, int id) {
      return REGISTRY.getById(version, id);
   }

   @Nullable
   public static ScoreFormatType<?> getByName(String name) {
      return REGISTRY.getByName(name);
   }

   @Nullable
   public static ScoreFormatType<?> getByName(ResourceLocation name) {
      return REGISTRY.getByName(name);
   }

   static {
      REGISTRY.unloadMappings();
   }
}

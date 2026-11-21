package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.score;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

public class StaticScoreFormatType<T extends ScoreFormat> extends AbstractMappedEntity implements ScoreFormatType<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;

   @ApiStatus.Internal
   public StaticScoreFormatType(@Nullable TypesBuilderData data, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      super(data);
      this.reader = reader;
      this.writer = writer;
   }

   @Override
   public int getId() {
      ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
      return this.getId(version.toClientVersion());
   }

   @Override
   public T read(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void write(PacketWrapper<?> wrapper, T format) {
      this.writer.accept(wrapper, format);
   }
}

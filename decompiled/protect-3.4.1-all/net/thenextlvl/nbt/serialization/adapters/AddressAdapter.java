package net.thenextlvl.nbt.serialization.adapters;

import java.net.InetSocketAddress;
import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class AddressAdapter implements TagAdapter<InetSocketAddress> {
   public static final AddressAdapter INSTANCE = new AddressAdapter();

   private AddressAdapter() {
   }

   public InetSocketAddress deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      CompoundTag root = tag.getAsCompound();
      String hostname = root.<Tag>get("hostname").getAsString();
      int port = root.<Tag>get("port").getAsInt();
      return new InetSocketAddress(hostname, port);
   }

   public Tag serialize(InetSocketAddress address, TagSerializationContext context) throws ParserException {
      return CompoundTag.builder().put("hostname", address.getHostName()).put("port", address.getPort()).build();
   }
}

package com.viaversion.viaversion.api.protocol;

import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ProtocolPipeline {
   void add(Protocol var1);

   void add(Collection<Protocol> var1);

   boolean contains(Class<? extends Protocol> var1);

   @Deprecated
   @Nullable
   <P extends Protocol> P getProtocol(Class<P> var1);

   void transform(Direction var1, State var2, PacketWrapper var3) throws InformativeException, CancelException;

   List<Protocol> pipes(@Nullable Class<? extends Protocol> var1, boolean var2, Direction var3);

   List<Protocol> pipes();

   List<Protocol> reversedPipes();

   int baseProtocolCount();

   boolean hasNonBaseProtocols();

   void cleanPipes();
}

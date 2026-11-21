package ac.grim.grimac.utils.functions;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BlockAndPositionConsumer {
   void accept(@NotNull WrappedBlockState var1, int var2, int var3, int var4);
}

package com.github.retrooper.packetevents.netty;

import com.github.retrooper.packetevents.netty.buffer.ByteBufAllocationOperator;
import com.github.retrooper.packetevents.netty.buffer.ByteBufOperator;
import com.github.retrooper.packetevents.netty.channel.ChannelOperator;

public interface NettyManager {
   ChannelOperator getChannelOperator();

   ByteBufOperator getByteBufOperator();

   ByteBufAllocationOperator getByteBufAllocationOperator();
}

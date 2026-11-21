package com.github.retrooper.packetevents.protocol.stats;

import net.kyori.adventure.text.Component;

public interface Statistic {
   String getId();

   Component display();
}

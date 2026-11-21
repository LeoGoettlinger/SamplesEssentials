package com.artillexstudios.axtrade.libs.axapi.metrics;

import com.artillexstudios.axtrade.libs.axapi.config.annotation.Comment;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.ConfigurationPart;
import java.util.UUID;

public final class MetricsConfig implements ConfigurationPart {
   @Comment("If sending anonymous data should be enabled.\nHaving this enabled helps us gain insightful data\nsuch as the server version, the java version you are using,\nand how popular our plugins are.\n\nWant to know more? Check out our metrics at: https://metrics.artillex-studios.com/\n\nWe'd appreciate if you kept this enabled, as this has no impact on your performance.\n")
   public static boolean enabled = true;
   public static UUID serverUuid = UUID.randomUUID();
}

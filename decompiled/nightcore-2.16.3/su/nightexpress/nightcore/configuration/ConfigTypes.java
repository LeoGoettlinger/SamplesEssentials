package su.nightexpress.nightcore.configuration;

import su.nightexpress.nightcore.configuration.codec.ConfigCodecs;

@Deprecated(
   forRemoval = true
)
public class ConfigTypes extends ConfigCodecs {
   public static final ConfigType<int[]> INT_ARRAY = ConfigType.of((c, p) -> ConfigCodecs.INT_ARRAY.read(c, p, new int[0]), ConfigCodecs.INT_ARRAY::write);
}

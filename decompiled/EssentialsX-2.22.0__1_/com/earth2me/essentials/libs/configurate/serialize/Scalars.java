package com.earth2me.essentials.libs.configurate.serialize;

import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

public final class Scalars {
   public static final ScalarSerializer<Boolean> BOOLEAN = new BooleanSerializer();
   public static final ScalarSerializer<String> STRING = new StringSerializer();
   public static final ScalarSerializer<Character> CHAR = new CharSerializer();
   public static final ScalarSerializer<Enum<?>> ENUM = new EnumValueSerializer();
   public static final ScalarSerializer<Pattern> PATTERN = new PatternSerializer();
   public static final ScalarSerializer<URI> URI = new UriSerializer();
   public static final ScalarSerializer<URL> URL = new UrlSerializer();
   public static final ScalarSerializer<UUID> UUID = new UuidSerializer();
   public static final ScalarSerializer<Byte> BYTE = NumericSerializers.BYTE;
   public static final ScalarSerializer<Short> SHORT = NumericSerializers.SHORT;
   public static final ScalarSerializer<Integer> INTEGER = NumericSerializers.INTEGER;
   public static final ScalarSerializer<Long> LONG = NumericSerializers.LONG;
   public static final ScalarSerializer<Float> FLOAT = NumericSerializers.FLOAT;
   public static final ScalarSerializer<Double> DOUBLE = NumericSerializers.DOUBLE;

   private Scalars() {
   }
}

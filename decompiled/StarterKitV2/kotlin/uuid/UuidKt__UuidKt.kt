package kotlin.uuid

import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nUuid.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Uuid.kt\nkotlin/uuid/UuidKt__UuidKt\n+ 2 HexExtensions.kt\nkotlin/text/HexExtensionsKt\n*L\n1#1,1051:1\n893#1,5:1052\n898#1:1066\n885#1,2:1067\n899#1:1069\n900#1:1076\n885#1,2:1077\n901#1:1079\n902#1:1086\n885#1,2:1087\n903#1:1089\n904#1:1096\n885#1,2:1097\n905#1:1099\n907#1,3:1106\n893#1,5:1109\n898#1:1123\n885#1,2:1124\n899#1:1126\n900#1:1133\n885#1,2:1134\n901#1:1136\n902#1:1143\n885#1,2:1144\n903#1:1146\n904#1:1153\n885#1,2:1154\n905#1:1156\n907#1,3:1163\n885#1,2:1175\n885#1,2:1186\n885#1,2:1197\n885#1,2:1208\n939#1:1219\n940#1:1229\n941#1:1236\n939#1:1237\n940#1:1247\n941#1:1254\n1201#2,3:1057\n1229#2,5:1060\n1205#2:1065\n1229#2,5:1070\n1205#2:1075\n1229#2,5:1080\n1205#2:1085\n1229#2,5:1090\n1205#2:1095\n1229#2,5:1100\n1205#2:1105\n1201#2,3:1114\n1229#2,5:1117\n1205#2:1122\n1229#2,5:1127\n1205#2:1132\n1229#2,5:1137\n1205#2:1142\n1229#2,5:1147\n1205#2:1152\n1229#2,5:1157\n1205#2:1162\n1201#2,3:1166\n1229#2,5:1169\n1205#2:1174\n1201#2,3:1177\n1229#2,5:1180\n1205#2:1185\n1201#2,3:1188\n1229#2,5:1191\n1205#2:1196\n1201#2,3:1199\n1229#2,5:1202\n1205#2:1207\n1201#2,3:1210\n1229#2,5:1213\n1205#2:1218\n1201#2,3:1220\n1229#2,5:1223\n1205#2:1228\n1229#2,5:1230\n1205#2:1235\n1201#2,3:1238\n1229#2,5:1241\n1205#2:1246\n1229#2,5:1248\n1205#2:1253\n1201#2,3:1255\n1229#2,5:1258\n1205#2:1263\n1201#2,3:1264\n1229#2,5:1267\n1205#2:1272\n*S KotlinDebug\n*F\n+ 1 Uuid.kt\nkotlin/uuid/UuidKt__UuidKt\n*L\n869#1:1052,5\n869#1:1066\n869#1:1067,2\n869#1:1069\n869#1:1076\n869#1:1077,2\n869#1:1079\n869#1:1086\n869#1:1087,2\n869#1:1089\n869#1:1096\n869#1:1097,2\n869#1:1099\n869#1:1106,3\n876#1:1109,5\n876#1:1123\n876#1:1124,2\n876#1:1126\n876#1:1133\n876#1:1134,2\n876#1:1136\n876#1:1143\n876#1:1144,2\n876#1:1146\n876#1:1153\n876#1:1154,2\n876#1:1156\n876#1:1163,3\n898#1:1175,2\n900#1:1186,2\n902#1:1197,2\n904#1:1208,2\n921#1:1219\n921#1:1229\n921#1:1236\n928#1:1237\n928#1:1247\n928#1:1254\n869#1:1057,3\n869#1:1060,5\n869#1:1065\n869#1:1070,5\n869#1:1075\n869#1:1080,5\n869#1:1085\n869#1:1090,5\n869#1:1095\n869#1:1100,5\n869#1:1105\n876#1:1114,3\n876#1:1117,5\n876#1:1122\n876#1:1127,5\n876#1:1132\n876#1:1137,5\n876#1:1142\n876#1:1147,5\n876#1:1152\n876#1:1157,5\n876#1:1162\n897#1:1166,3\n897#1:1169,5\n897#1:1174\n899#1:1177,3\n899#1:1180,5\n899#1:1185\n901#1:1188,3\n901#1:1191,5\n901#1:1196\n903#1:1199,3\n903#1:1202,5\n903#1:1207\n905#1:1210,3\n905#1:1213,5\n905#1:1218\n921#1:1220,3\n921#1:1223,5\n921#1:1228\n921#1:1230,5\n921#1:1235\n928#1:1238,3\n928#1:1241,5\n928#1:1246\n928#1:1248,5\n928#1:1253\n939#1:1255,3\n939#1:1258,5\n939#1:1263\n940#1:1264,3\n940#1:1267,5\n940#1:1272\n*E\n"])
internal class UuidKt__UuidKt : UuidKt__UuidJVMKt {
   private const val UUID_HEX_LENGTH: Int = 32
   private const val UUID_HEX_DASH_LENGTH: Int = 36

   @ExperimentalUuidApi
   @JvmStatic
   internal fun secureRandomUuid(): Uuid {
      val var0: ByteArray = new byte[16];
      UuidKt.secureRandomBytes(var0);
      return UuidKt.uuidFromRandomBytes(var0);
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun uuidFromRandomBytes(randomBytes: ByteArray): Uuid {
      randomBytes[6] = (byte)(randomBytes[6] and 15);
      randomBytes[6] = (byte)(randomBytes[6] or 64);
      randomBytes[8] = (byte)(randomBytes[8] and 63);
      randomBytes[8] = (byte)(randomBytes[8] or 128);
      return Uuid.Companion.fromByteArray(randomBytes);
   }

   @JvmStatic
   internal fun ByteArray.getLongAtCommonImpl(index: Int): Long {
      return (`$this$getLongAtCommonImpl`[index + 0] and 255L) shl 56 or (`$this$getLongAtCommonImpl`[index + 1] and 255L) shl 48 or (
         `$this$getLongAtCommonImpl`[index + 2] and 255L
      ) shl 40 or (`$this$getLongAtCommonImpl`[index + 3] and 255L) shl 32 or (`$this$getLongAtCommonImpl`[index + 4] and 255L) shl 24 or (
         `$this$getLongAtCommonImpl`[index + 5] and 255L
      ) shl 16 or (`$this$getLongAtCommonImpl`[index + 6] and 255L) shl 8 or `$this$getLongAtCommonImpl`[index + 7] and 255L;
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun Long.formatBytesIntoCommonImpl(dst: ByteArray, dstOffset: Int, startIndex: Int, endIndex: Int) {
      var dstIndex: Int = dstOffset;
      var reversedIndex: Int = 7 - startIndex;
      val var8: Int = 8 - endIndex;
      if (8 - endIndex <= reversedIndex) {
         while (true) {
            val byteDigits: Int = HexExtensionsKt.getBYTE_TO_LOWER_CASE_HEX_DIGITS()[(int)(`$this$formatBytesIntoCommonImpl` shr (reversedIndex shl 3) and 255L)];
            dst[dstIndex++] = (byte)(byteDigits shr 8);
            dst[dstIndex++] = (byte)byteDigits;
            if (reversedIndex == var8) {
               break;
            }

            reversedIndex--;
         }
      }
   }

   @JvmStatic
   internal fun ByteArray.setLongAtCommonImpl(index: Int, value: Long) {
      var i: Int = index;

      for (int reversedIndex = 7; -1 < reversedIndex; reversedIndex--) {
         `$this$setLongAtCommonImpl`[i++] = (byte)(value shr (reversedIndex shl 3));
      }
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun uuidParseHexDashCommonImpl(hexDashString: String): Uuid {
      val `$this$uuidCheckHyphenAt$iv$iv`: java.lang.String = hexDashString;
      val `$this$uuidCheckHyphenAt$iv$ivx`: Int = 8;
      var `part4$iv`: Long = 0L;

      for (int index$iv$iv = 0; index$iv$iv < $this$uuidCheckHyphenAt$iv$ivx; index$iv$iv++) {
         val var10000: Long = `part4$iv` shl 4;
         val `endIndex$iv$iv`: Int = `$this$uuidCheckHyphenAt$iv$iv`.charAt(`$this$uuidCheckHyphenAt$iv$iv`);
         if (`endIndex$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`endIndex$iv$iv`] < 0L) {
            UuidKt.uuidThrowUnexpectedCharacterException(`$this$uuidCheckHyphenAt$iv$iv`, "a hexadecimal digit", `$this$uuidCheckHyphenAt$iv$iv`);
            throw new KotlinNothingValueException();
         }

         `part4$iv` = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`endIndex$iv$iv`];
      }

      if (hexDashString.charAt(8) != '-') {
         UuidKt.uuidThrowUnexpectedCharacterException(hexDashString, "'-' (hyphen)", 8);
         throw new KotlinNothingValueException();
      } else {
         val var51: java.lang.String = hexDashString;
         val `$this$uuidCheckHyphenAt$iv$ivxx`: Int = 13;
         var `result$iv$ivx`: Long = 0L;

         for (int index$iv$iv = 9; index$iv$iv < $this$uuidCheckHyphenAt$iv$ivxx; index$iv$iv++) {
            val var91: Long = `result$iv$ivx` shl 4;
            val `index$iv$ivx`: Int = var51.charAt(`$this$parseHexToLong$iv$iv`);
            if (`index$iv$ivx` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`] < 0L) {
               UuidKt.uuidThrowUnexpectedCharacterException(var51, "a hexadecimal digit", `$this$parseHexToLong$iv$iv`);
               throw new KotlinNothingValueException();
            }

            `result$iv$ivx` = var91 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`];
         }

         if (hexDashString.charAt(13) != '-') {
            UuidKt.uuidThrowUnexpectedCharacterException(hexDashString, "'-' (hyphen)", 13);
            throw new KotlinNothingValueException();
         } else {
            val var84: java.lang.String = hexDashString;
            val `endIndex$iv$ivxx`: Int = 18;
            var `result$iv$ivxx`: Long = 0L;

            for (int index$iv$iv = 14; index$iv$iv < endIndex$iv$ivxx; index$iv$iv++) {
               val var92: Long = `result$iv$ivxx` shl 4;
               val `index$iv$ivx`: Int = var84.charAt(var64);
               if (`index$iv$ivx` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`] < 0L) {
                  UuidKt.uuidThrowUnexpectedCharacterException(var84, "a hexadecimal digit", var64);
                  throw new KotlinNothingValueException();
               }

               `result$iv$ivxx` = var92 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`];
            }

            if (hexDashString.charAt(18) != '-') {
               UuidKt.uuidThrowUnexpectedCharacterException(hexDashString, "'-' (hyphen)", 18);
               throw new KotlinNothingValueException();
            } else {
               val var56: java.lang.String = hexDashString;
               val `endIndex$iv$ivxxx`: Int = 23;
               var `result$iv$ivxxx`: Long = 0L;

               for (int index$iv$iv = 19; index$iv$iv < endIndex$iv$ivxxx; index$iv$iv++) {
                  val var93: Long = `result$iv$ivxxx` shl 4;
                  val var71: Int = var56.charAt(var67);
                  if (var71 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var71] < 0L) {
                     UuidKt.uuidThrowUnexpectedCharacterException(var56, "a hexadecimal digit", var67);
                     throw new KotlinNothingValueException();
                  }

                  `result$iv$ivxxx` = var93 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var71];
               }

               if (hexDashString.charAt(23) != '-') {
                  UuidKt.uuidThrowUnexpectedCharacterException(hexDashString, "'-' (hyphen)", 23);
                  throw new KotlinNothingValueException();
               } else {
                  val var61: java.lang.String = hexDashString;
                  val `endIndex$iv$ivxxxx`: Int = 36;
                  var `result$iv$ivxxxx`: Long = 0L;

                  for (int index$iv$iv = 24; index$iv$iv < endIndex$iv$ivxxxx; index$iv$iv++) {
                     val var94: Long = `result$iv$ivxxxx` shl 4;
                     val var88: Int = var61.charAt(var69);
                     if (var88 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var88] < 0L) {
                        UuidKt.uuidThrowUnexpectedCharacterException(var61, "a hexadecimal digit", var69);
                        throw new KotlinNothingValueException();
                     }

                     `result$iv$ivxxxx` = var94 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var88];
                  }

                  return Uuid.Companion
                     .fromLongs(`part4$iv` shl 32 or `result$iv$ivx` shl 16 or `result$iv$ivxx`, `result$iv$ivxxx` shl 48 or `result$iv$ivxxxx`);
               }
            }
         }
      }
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun uuidParseHexDashOrNullCommonImpl(hexDashString: String): Uuid? {
      val `$this$uuidCheckHyphenAt$iv$iv`: java.lang.String = hexDashString;
      val `$this$uuidCheckHyphenAt$iv$ivx`: Int = 8;
      var `part4$iv`: Long = 0L;

      for (int index$iv$iv = 0; index$iv$iv < $this$uuidCheckHyphenAt$iv$ivx; index$iv$iv++) {
         val var10000: Long = `part4$iv` shl 4;
         val `endIndex$iv$iv`: Int = `$this$uuidCheckHyphenAt$iv$iv`.charAt(`$this$uuidCheckHyphenAt$iv$iv`);
         if (`endIndex$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`endIndex$iv$iv`] < 0L) {
            return null;
         }

         `part4$iv` = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`endIndex$iv$iv`];
      }

      if (hexDashString.charAt(8) != '-') {
         return null;
      } else {
         val var48: java.lang.String = hexDashString;
         val `$this$uuidCheckHyphenAt$iv$ivxx`: Int = 13;
         var `result$iv$ivx`: Long = 0L;

         for (int index$iv$iv = 9; index$iv$iv < $this$uuidCheckHyphenAt$iv$ivxx; index$iv$iv++) {
            val var85: Long = `result$iv$ivx` shl 4;
            val `index$iv$ivx`: Int = var48.charAt(`$this$parseHexToLong$iv$iv`);
            if (`index$iv$ivx` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`] < 0L) {
               return null;
            }

            `result$iv$ivx` = var85 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`];
         }

         if (hexDashString.charAt(13) != '-') {
            return null;
         } else {
            val var78: java.lang.String = hexDashString;
            val `endIndex$iv$ivxx`: Int = 18;
            var `result$iv$ivxx`: Long = 0L;

            for (int index$iv$iv = 14; index$iv$iv < endIndex$iv$ivxx; index$iv$iv++) {
               val var86: Long = `result$iv$ivxx` shl 4;
               val `index$iv$ivx`: Int = var78.charAt(var61);
               if (`index$iv$ivx` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`] < 0L) {
                  return null;
               }

               `result$iv$ivxx` = var86 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$ivx`];
            }

            if (hexDashString.charAt(18) != '-') {
               return null;
            } else {
               val var53: java.lang.String = hexDashString;
               val `endIndex$iv$ivxxx`: Int = 23;
               var `result$iv$ivxxx`: Long = 0L;

               for (int index$iv$iv = 19; index$iv$iv < endIndex$iv$ivxxx; index$iv$iv++) {
                  val var87: Long = `result$iv$ivxxx` shl 4;
                  val var68: Int = var53.charAt(var64);
                  if (var68 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var68] < 0L) {
                     return null;
                  }

                  `result$iv$ivxxx` = var87 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var68];
               }

               if (hexDashString.charAt(23) != '-') {
                  return null;
               } else {
                  val var58: java.lang.String = hexDashString;
                  val `endIndex$iv$ivxxxx`: Int = 36;
                  var `result$iv$ivxxxx`: Long = 0L;

                  for (int index$iv$iv = 24; index$iv$iv < endIndex$iv$ivxxxx; index$iv$iv++) {
                     val var88: Long = `result$iv$ivxxxx` shl 4;
                     val var82: Int = var58.charAt(var66);
                     if (var82 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var82] < 0L) {
                        return null;
                     }

                     `result$iv$ivxxxx` = var88 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var82];
                  }

                  return Uuid.Companion
                     .fromLongs(`part4$iv` shl 32 or `result$iv$ivx` shl 16 or `result$iv$ivxx`, `result$iv$ivxxx` shl 48 or `result$iv$ivxxxx`);
               }
            }
         }
      }
   }

   @JvmStatic
   internal inline fun String.uuidCheckHyphenAt(index: Int, onError: (String, String, Int) -> Unit) {
      if (`$this$uuidCheckHyphenAt`.charAt(index) != '-') {
         onError.invoke(`$this$uuidCheckHyphenAt`, "'-' (hyphen)", index);
      }
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal inline fun uuidParseHexDashCommonImpl(hexDashString: String, onError: (String, String, Int) -> Nothing): Uuid {
      val part2: java.lang.String = hexDashString;
      val part3: Int = 8;
      var part4: Long = 0L;

      for (int index$iv = 0; index$iv < endIndex$iv; index$iv++) {
         val var10000: Long = part4 shl 4;
         val lsb: Int = part2.charAt(part5);
         if (lsb ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[lsb] < 0L) {
            onError.invoke(part2, "a hexadecimal digit", part5);
            throw new KotlinNothingValueException();
         }

         part4 = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[lsb];
      }

      if (hexDashString.charAt(8) != '-') {
         onError.invoke(hexDashString, "'-' (hyphen)", 8);
      }

      val var34: java.lang.String = hexDashString;
      val `endIndex$ivx`: Int = 13;
      var `result$ivx`: Long = 0L;

      for (int index$iv = 9; index$iv < endIndex$ivx; index$iv++) {
         val var70: Long = `result$ivx` shl 4;
         val `result$iv`: Int = var34.charAt(msb);
         if (`result$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`result$iv`] < 0L) {
            onError.invoke(var34, "a hexadecimal digit", msb);
            throw new KotlinNothingValueException();
         }

         `result$ivx` = var70 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`result$iv`];
      }

      if (hexDashString.charAt(13) != '-') {
         onError.invoke(hexDashString, "'-' (hyphen)", 13);
      }

      val var40: java.lang.String = hexDashString;
      val `endIndex$ivxx`: Int = 18;
      var `result$ivxx`: Long = 0L;

      for (int index$iv = 14; index$iv < endIndex$ivxx; index$iv++) {
         val var71: Long = `result$ivxx` shl 4;
         val `index$ivx`: Int = var40.charAt(var57);
         if (`index$ivx` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$ivx`] < 0L) {
            onError.invoke(var40, "a hexadecimal digit", var57);
            throw new KotlinNothingValueException();
         }

         `result$ivxx` = var71 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$ivx`];
      }

      if (hexDashString.charAt(18) != '-') {
         onError.invoke(hexDashString, "'-' (hyphen)", 18);
      }

      val var47: java.lang.String = hexDashString;
      val `endIndex$ivxxx`: Int = 23;
      var `result$ivxxx`: Long = 0L;

      for (int index$iv = 19; index$iv < endIndex$ivxxx; index$iv++) {
         val var72: Long = `result$ivxxx` shl 4;
         val var67: Int = var47.charAt(var62);
         if (var67 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var67] < 0L) {
            onError.invoke(var47, "a hexadecimal digit", var62);
            throw new KotlinNothingValueException();
         }

         `result$ivxxx` = var72 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var67];
      }

      if (hexDashString.charAt(23) != '-') {
         onError.invoke(hexDashString, "'-' (hyphen)", 23);
      }

      val var53: java.lang.String = hexDashString;
      val `endIndex$ivxxxx`: Int = 36;
      var `result$ivxxxx`: Long = 0L;

      for (int index$iv = 24; index$iv < endIndex$ivxxxx; index$iv++) {
         val var73: Long = `result$ivxxxx` shl 4;
         val var69: Int = var53.charAt(var65);
         if (var69 ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var69] < 0L) {
            onError.invoke(var53, "a hexadecimal digit", var65);
            throw new KotlinNothingValueException();
         }

         `result$ivxxxx` = var73 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[var69];
      }

      return Uuid.Companion.fromLongs(part4 shl 32 or `result$ivx` shl 16 or `result$ivxx`, `result$ivxxx` shl 48 or `result$ivxxxx`);
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun uuidParseHexCommonImpl(hexString: String): Uuid {
      val `$this$parseHexToLong$iv$iv`: java.lang.String = hexString;
      val `$this$parseHexToLong$iv$ivx`: Int = 16;
      var `result$iv$iv`: Long = 0L;

      for (int index$iv$iv = 0; index$iv$iv < $this$parseHexToLong$iv$ivx; index$iv$iv++) {
         val var10000: Long = `result$iv$iv` shl 4;
         val `index$iv$iv$iv`: Int = `$this$parseHexToLong$iv$iv`.charAt(`index$iv$iv`);
         if (`index$iv$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv$iv`] < 0L) {
            UuidKt.uuidThrowUnexpectedCharacterException(`$this$parseHexToLong$iv$iv`, "a hexadecimal digit", `index$iv$iv`);
            throw new KotlinNothingValueException();
         }

         `result$iv$iv` = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv$iv`];
      }

      val var34: java.lang.String = hexString;
      val `endIndex$iv$ivx`: Int = 32;
      var `result$iv$ivx`: Long = 0L;

      for (int index$iv$iv = 16; index$iv$iv < endIndex$iv$ivx; index$iv$iv++) {
         val var38: Long = `result$iv$ivx` shl 4;
         val `code$iv$iv$iv`: Int = var34.charAt(`index$iv$iv`);
         if (`code$iv$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv$iv`] < 0L) {
            UuidKt.uuidThrowUnexpectedCharacterException(var34, "a hexadecimal digit", `index$iv$iv`);
            throw new KotlinNothingValueException();
         }

         `result$iv$ivx` = var38 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv$iv`];
      }

      return Uuid.Companion.fromLongs(`result$iv$iv`, `result$iv$ivx`);
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal fun uuidParseHexOrNullCommonImpl(hexString: String): Uuid? {
      val `$this$parseHexToLong$iv$iv`: java.lang.String = hexString;
      val `$this$parseHexToLong$iv$ivx`: Int = 16;
      var `result$iv$iv`: Long = 0L;

      for (int index$iv$iv = 0; index$iv$iv < $this$parseHexToLong$iv$ivx; index$iv$iv++) {
         val var10000: Long = `result$iv$iv` shl 4;
         val `index$iv$iv$iv`: Int = `$this$parseHexToLong$iv$iv`.charAt(`index$iv$iv`);
         if (`index$iv$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv$iv`] < 0L) {
            return null;
         }

         `result$iv$iv` = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv$iv`];
      }

      val var31: java.lang.String = hexString;
      val `endIndex$iv$ivx`: Int = 32;
      var `result$iv$ivx`: Long = 0L;

      for (int index$iv$iv = 16; index$iv$iv < endIndex$iv$ivx; index$iv$iv++) {
         val var34: Long = `result$iv$ivx` shl 4;
         val `code$iv$iv$iv`: Int = var31.charAt(`index$iv$iv`);
         if (`code$iv$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv$iv`] < 0L) {
            return null;
         }

         `result$iv$ivx` = var34 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv$iv`];
      }

      return Uuid.Companion.fromLongs(`result$iv$iv`, `result$iv$ivx`);
   }

   @ExperimentalUuidApi
   @JvmStatic
   internal inline fun uuidParseHexCommonImpl(hexString: String, onError: (String, String, Int) -> Nothing): Uuid {
      val lsb: java.lang.String = hexString;
      val `$this$parseHexToLong$iv`: Int = 16;
      var `endIndex$iv`: Long = 0L;

      for (int index$iv = 0; index$iv < endIndex$iv; index$iv++) {
         val var10000: Long = `endIndex$iv` shl 4;
         val `index$iv$iv`: Int = lsb.charAt(`result$iv`);
         if (`index$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv`] < 0L) {
            onError.invoke(lsb, "a hexadecimal digit", `result$iv`);
            throw new KotlinNothingValueException();
         }

         `endIndex$iv` = var10000 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`index$iv$iv`];
      }

      val var24: java.lang.String = hexString;
      val `endIndex$ivx`: Int = 32;
      var `result$ivx`: Long = 0L;

      for (int index$iv = 16; index$iv < endIndex$ivx; index$iv++) {
         val var28: Long = `result$ivx` shl 4;
         val `code$iv$iv`: Int = var24.charAt(`index$iv`);
         if (`code$iv$iv` ushr 8 != 0 || HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv`] < 0L) {
            onError.invoke(var24, "a hexadecimal digit", `index$iv`);
            throw new KotlinNothingValueException();
         }

         `result$ivx` = var28 or HexExtensionsKt.access$getHEX_DIGITS_TO_LONG_DECIMAL$p()[`code$iv$iv`];
      }

      return Uuid.Companion.fromLongs(`endIndex$iv`, `result$ivx`);
   }

   @JvmStatic
   private fun String.truncateForErrorMessage(maxLength: Int): String {
      val var10000: java.lang.String;
      if (`$this$truncateForErrorMessage`.length() <= maxLength) {
         var10000 = `$this$truncateForErrorMessage`;
      } else {
         val var4: StringBuilder = new StringBuilder();
         val var10001: java.lang.String = `$this$truncateForErrorMessage`.substring(0, maxLength);
         var10000 = var4.append(var10001).append("...").toString();
      }

      return var10000;
   }

   @JvmStatic
   private fun ByteArray.truncateForErrorMessage(maxSize: Int): String {
      return ArraysKt.joinToString$default(`$this$truncateForErrorMessage`, null, "[", "]", maxSize, null, null, 49, null);
   }

   @JvmStatic
   internal fun uuidThrowUnexpectedCharacterException(inputString: String, errorDescription: String, errorIndex: Int): Nothing {
      throw new IllegalArgumentException("Expected $errorDescription at index $errorIndex, but was '${inputString.charAt(errorIndex)}'");
   }

   open fun UuidKt__UuidKt() {
   }
}

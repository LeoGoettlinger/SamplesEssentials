package com.github.retrooper.packetevents.util.crypto;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.UUID;
import net.kyori.adventure.text.Component;

@Deprecated
class MessageVerifierHelper {
   public static boolean verify(UUID uuid, MessageSignData signData, PublicKey publicKey, Component component) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      return verify(uuid, signData, publicKey, AdventureSerializer.serializer().asJson(component));
   }

   public static boolean verify(UUID uuid, MessageSignData signData, PublicKey publicKey, String jsonMessage) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initVerify(publicKey);
      byte[] data = new byte[32];
      ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
      buffer.putLong(signData.getSaltSignature().getSalt());
      buffer.putLong(uuid.getMostSignificantBits());
      buffer.putLong(uuid.getLeastSignificantBits());
      buffer.putLong(signData.getTimestamp().getEpochSecond());
      signature.update(data);
      signature.update(jsonMessage.getBytes(StandardCharsets.UTF_8));
      return signature.verify(signData.getSaltSignature().getSignature());
   }
}

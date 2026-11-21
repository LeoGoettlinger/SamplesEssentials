package org.postgresql.shaded.com.ongres.scram.client;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanism;
import org.postgresql.shaded.com.ongres.scram.common.ScramMechanisms;
import org.postgresql.shaded.com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.postgresql.shaded.com.ongres.scram.common.stringprep.StringPreparation;
import org.postgresql.shaded.com.ongres.scram.common.util.CryptoUtil;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public class ScramClient {
   public static final int DEFAULT_NONCE_LENGTH = 24;
   private final ScramClient.ChannelBinding channelBinding;
   private final StringPreparation stringPreparation;
   private final ScramMechanism scramMechanism;
   private final SecureRandom secureRandom;
   private final NonceSupplier nonceSupplier;

   private ScramClient(
      ScramClient.ChannelBinding channelBinding,
      StringPreparation stringPreparation,
      ScramMechanism nonChannelBindingMechanism,
      ScramMechanism channelBindingMechanism,
      SecureRandom secureRandom,
      NonceSupplier nonceSupplier
   ) {
      assert null != channelBinding : "channelBinding";

      assert null != stringPreparation : "stringPreparation";

      assert null != nonChannelBindingMechanism || null != channelBindingMechanism : "Either a channel-binding or a non-binding mechanism must be present";

      assert null != secureRandom : "secureRandom";

      assert null != nonceSupplier : "nonceSupplier";

      this.channelBinding = channelBinding;
      this.stringPreparation = stringPreparation;
      this.scramMechanism = null != nonChannelBindingMechanism ? nonChannelBindingMechanism : channelBindingMechanism;
      this.secureRandom = secureRandom;
      this.nonceSupplier = nonceSupplier;
   }

   public static ScramClient.PreBuilder1 channelBinding(ScramClient.ChannelBinding channelBinding) throws IllegalArgumentException {
      return new ScramClient.PreBuilder1(Preconditions.checkNotNull(channelBinding, "channelBinding"));
   }

   public StringPreparation getStringPreparation() {
      return this.stringPreparation;
   }

   public ScramMechanism getScramMechanism() {
      return this.scramMechanism;
   }

   public static List<String> supportedMechanisms() {
      List<String> supportedMechanisms = new ArrayList<>();

      for (ScramMechanisms scramMechanisms : ScramMechanisms.values()) {
         supportedMechanisms.add(scramMechanisms.getName());
      }

      return supportedMechanisms;
   }

   public ScramSession scramSession(String user) {
      return new ScramSession(this.scramMechanism, this.stringPreparation, Preconditions.checkNotEmpty(user, "user"), this.nonceSupplier.get());
   }

   public static class Builder extends ScramClient.PreBuilder2 {
      private final ScramMechanism nonChannelBindingMechanism;
      private final ScramMechanism channelBindingMechanism;
      private SecureRandom secureRandom = new SecureRandom();
      private NonceSupplier nonceSupplier;
      private int nonceLength = 24;

      private Builder(
         ScramClient.ChannelBinding channelBinding,
         StringPreparation stringPreparation,
         ScramMechanism nonChannelBindingMechanism,
         ScramMechanism channelBindingMechanism
      ) {
         super(channelBinding, stringPreparation);
         this.nonChannelBindingMechanism = nonChannelBindingMechanism;
         this.channelBindingMechanism = channelBindingMechanism;
      }

      public ScramClient.Builder secureRandomAlgorithmProvider(String algorithm, String provider) throws IllegalArgumentException {
         Preconditions.checkNotNull(algorithm, "algorithm");

         try {
            this.secureRandom = null == provider ? SecureRandom.getInstance(algorithm) : SecureRandom.getInstance(algorithm, provider);
            return this;
         } catch (NoSuchProviderException | NoSuchAlgorithmException var4) {
            throw new IllegalArgumentException("Invalid algorithm or provider", var4);
         }
      }

      public ScramClient.Builder nonceSupplier(NonceSupplier nonceSupplier) throws IllegalArgumentException {
         this.nonceSupplier = Preconditions.checkNotNull(nonceSupplier, "nonceSupplier");
         return this;
      }

      public ScramClient.Builder nonceLength(int length) throws IllegalArgumentException {
         this.nonceLength = Preconditions.gt0(length, "length");
         return this;
      }

      public ScramClient setup() {
         return new ScramClient(
            this.channelBinding,
            this.stringPreparation,
            this.nonChannelBindingMechanism,
            this.channelBindingMechanism,
            this.secureRandom,
            this.nonceSupplier != null ? this.nonceSupplier : new NonceSupplier() {
               @Override
               public String get() {
                  return CryptoUtil.nonce(Builder.this.nonceLength, Builder.this.secureRandom);
               }
            }
         );
      }
   }

   public static enum ChannelBinding {
      NO(Gs2CbindFlag.CLIENT_NOT),
      YES(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED),
      IF_SERVER_SUPPORTS_IT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT);

      private final Gs2CbindFlag gs2CbindFlag;

      private ChannelBinding(Gs2CbindFlag gs2CbindFlag) {
         this.gs2CbindFlag = gs2CbindFlag;
      }

      public Gs2CbindFlag gs2CbindFlag() {
         return this.gs2CbindFlag;
      }
   }

   public static class PreBuilder1 {
      protected final ScramClient.ChannelBinding channelBinding;

      private PreBuilder1(ScramClient.ChannelBinding channelBinding) {
         this.channelBinding = channelBinding;
      }

      public ScramClient.PreBuilder2 stringPreparation(StringPreparation stringPreparation) throws IllegalArgumentException {
         return new ScramClient.PreBuilder2(this.channelBinding, Preconditions.checkNotNull(stringPreparation, "stringPreparation"));
      }
   }

   public static class PreBuilder2 extends ScramClient.PreBuilder1 {
      protected final StringPreparation stringPreparation;
      protected ScramMechanism nonChannelBindingMechanism = null;
      protected ScramMechanism channelBindingMechanism = null;

      private PreBuilder2(ScramClient.ChannelBinding channelBinding, StringPreparation stringPreparation) {
         super(channelBinding);
         this.stringPreparation = stringPreparation;
      }

      public ScramClient.Builder selectMechanismBasedOnServerAdvertised(String... serverMechanisms) {
         Preconditions.checkArgument(null != serverMechanisms && serverMechanisms.length > 0, "serverMechanisms");
         this.nonChannelBindingMechanism = ScramMechanisms.selectMatchingMechanism(false, serverMechanisms);
         if (this.channelBinding == ScramClient.ChannelBinding.NO && null == this.nonChannelBindingMechanism) {
            throw new IllegalArgumentException("Server does not support non channel binding mechanisms");
         } else {
            this.channelBindingMechanism = ScramMechanisms.selectMatchingMechanism(true, serverMechanisms);
            if (this.channelBinding == ScramClient.ChannelBinding.YES && null == this.channelBindingMechanism) {
               throw new IllegalArgumentException("Server does not support channel binding mechanisms");
            } else if (null == this.channelBindingMechanism && null == this.nonChannelBindingMechanism) {
               throw new IllegalArgumentException("There are no matching mechanisms between client and server");
            } else {
               return new ScramClient.Builder(this.channelBinding, this.stringPreparation, this.nonChannelBindingMechanism, this.channelBindingMechanism);
            }
         }
      }

      public ScramClient.Builder selectMechanismBasedOnServerAdvertisedCsv(String serverMechanismsCsv) throws IllegalArgumentException {
         return this.selectMechanismBasedOnServerAdvertised(Preconditions.checkNotNull(serverMechanismsCsv, "serverMechanismsCsv").split(","));
      }

      public ScramClient.Builder selectClientMechanism(ScramMechanism scramMechanism) {
         Preconditions.checkNotNull(scramMechanism, "scramMechanism");
         if (this.channelBinding == ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT) {
            throw new IllegalArgumentException("If server selection is considered, no direct client selection should be performed");
         } else if ((this.channelBinding != ScramClient.ChannelBinding.YES || scramMechanism.supportsChannelBinding())
            && (this.channelBinding != ScramClient.ChannelBinding.NO || !scramMechanism.supportsChannelBinding())) {
            return scramMechanism.supportsChannelBinding()
               ? new ScramClient.Builder(this.channelBinding, this.stringPreparation, null, scramMechanism)
               : new ScramClient.Builder(this.channelBinding, this.stringPreparation, scramMechanism, null);
         } else {
            throw new IllegalArgumentException("Incompatible selection of mechanism and channel binding");
         }
      }
   }
}

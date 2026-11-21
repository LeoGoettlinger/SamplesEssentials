package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.authentication;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.List;
import org.lushplugins.lushrewards.libraries.mysql.cj.Messages;
import org.lushplugins.lushrewards.libraries.mysql.cj.callback.MysqlCallbackHandler;
import org.lushplugins.lushrewards.libraries.mysql.cj.callback.UsernameCallback;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.RSAException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.AuthenticationPlugin;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ExportControlled;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Protocol;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.NativeConstants;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.a.NativePacketPayload;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.StringUtils;

public class AuthenticationOciClient implements AuthenticationPlugin<NativePacketPayload> {
   public static String PLUGIN_NAME = "authentication_oci_client";
   private String sourceOfAuthData = PLUGIN_NAME;
   protected Protocol<NativePacketPayload> protocol = null;
   private MysqlCallbackHandler usernameCallbackHandler = null;
   private String configFingerprint = null;
   private String configKeyFile = null;
   private String configSecurityTokenFile = null;
   private RSAPrivateKey privateKey = null;
   private byte[] token = null;

   @Override
   public void init(Protocol<NativePacketPayload> prot, MysqlCallbackHandler cbh) {
      this.protocol = prot;
      this.usernameCallbackHandler = cbh;
   }

   @Override
   public void reset() {
      this.configFingerprint = null;
      this.privateKey = null;
   }

   @Override
   public void destroy() {
      this.reset();
      this.protocol = null;
      this.usernameCallbackHandler = null;
   }

   @Override
   public String getProtocolPluginName() {
      return PLUGIN_NAME;
   }

   @Override
   public boolean requiresConfidentiality() {
      return false;
   }

   @Override
   public boolean isReusable() {
      return false;
   }

   @Override
   public void setAuthenticationParameters(String user, String password) {
      if (user == null && this.usernameCallbackHandler != null) {
         this.usernameCallbackHandler.handle(new UsernameCallback(System.getProperty("user.name")));
      }
   }

   @Override
   public void setSourceOfAuthData(String sourceOfAuthData) {
      this.sourceOfAuthData = sourceOfAuthData;
   }

   public boolean nextAuthenticationStep(NativePacketPayload fromServer, List<NativePacketPayload> toServer) {
      toServer.clear();
      if (this.sourceOfAuthData.equals(PLUGIN_NAME) && fromServer.getPayloadLength() != 0) {
         this.loadOciConfig();
         this.initializePrivateKey();
         this.initializeToken();
         byte[] nonce = fromServer.readBytes(NativeConstants.StringSelfDataType.STRING_EOF);
         byte[] signature = ExportControlled.sign(nonce, this.privateKey);
         if (signature == null) {
            signature = new byte[0];
         }

         String payload = String.format(
            "{\"fingerprint\":\"%s\", \"signature\":\"%s\", \"token\":\"%s\"}",
            this.configFingerprint,
            Base64.getEncoder().encodeToString(signature),
            new String(this.token)
         );
         toServer.add(new NativePacketPayload(payload.getBytes(Charset.defaultCharset())));
         return true;
      } else {
         toServer.add(new NativePacketPayload(0));
         return true;
      }
   }

   private void loadOciConfig() {
      ConfigFile configFile;
      try {
         String configFilePath = this.protocol.getPropertySet().getStringProperty(PropertyKey.ociConfigFile.getKeyName()).getStringValue();
         String configProfile = this.protocol.getPropertySet().getStringProperty(PropertyKey.ociConfigProfile.getKeyName()).getStringValue();
         if (StringUtils.isNullOrEmpty(configFilePath)) {
            configFile = ConfigFileReader.parseDefault(configProfile);
         } else {
            if (!Files.exists(Paths.get(configFilePath))) {
               throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.ConfigFileNotFound"));
            }

            configFile = ConfigFileReader.parse(configFilePath, configProfile);
         }
      } catch (NoClassDefFoundError var4) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.OciSdkNotFound"), var4);
      } catch (IOException var5) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.OciConfigFileError"), var5);
      } catch (IllegalArgumentException var6) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.ProfileNotFound"), var6);
      }

      this.configFingerprint = configFile.get("fingerprint");
      if (StringUtils.isNullOrEmpty(this.configFingerprint)) {
         throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.OciConfigFileMissingEntry"));
      } else {
         this.configKeyFile = configFile.get("key_file");
         if (StringUtils.isNullOrEmpty(this.configKeyFile)) {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.OciConfigFileMissingEntry"));
         } else {
            this.configSecurityTokenFile = configFile.get("security_token_file");
         }
      }
   }

   private void initializePrivateKey() {
      if (this.privateKey == null) {
         try {
            Path keyFilePath = Paths.get(this.configKeyFile);
            if (Files.notExists(keyFilePath)) {
               throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.PrivateKeyNotFound"));
            } else {
               String key = new String(Files.readAllBytes(keyFilePath));
               this.privateKey = ExportControlled.decodeRSAPrivateKey(key);
            }
         } catch (IOException var3) {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.FailedReadingPrivateKey"), var3);
         } catch (IllegalArgumentException | RSAException var4) {
            throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.PrivateKeyNotValid"), var4);
         }
      }
   }

   private void initializeToken() {
      if (this.token == null) {
         if (StringUtils.isNullOrEmpty(this.configSecurityTokenFile)) {
            this.token = new byte[0];
         } else {
            try {
               Path securityTokenFilePath = Paths.get(this.configSecurityTokenFile);
               if (Files.notExists(securityTokenFilePath)) {
                  throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.SecurityTokenFileNotFound"));
               } else {
                  long size = Files.size(securityTokenFilePath);
                  if (size > 10240L) {
                     throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.SecurityTokenTooBig"));
                  } else {
                     this.token = Files.readAllBytes(Paths.get(this.configSecurityTokenFile));
                  }
               }
            } catch (IOException var4) {
               throw ExceptionFactory.createException(Messages.getString("AuthenticationOciClientPlugin.FailedReadingSecurityTokenFile"), var4);
            }
         }
      }
   }
}

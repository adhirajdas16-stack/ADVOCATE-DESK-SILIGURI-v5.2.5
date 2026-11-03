package in.advocatedesk.siliguri;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class DatabaseKey {
 private static final String STORE="AndroidKeyStore", ALIAS="advocate_desk_db_key";
 static String get(Context c) throws Exception {
  SharedPreferences p=c.getSharedPreferences("db_key_store",Context.MODE_PRIVATE);
  String encrypted=p.getString("cipher",null), ivText=p.getString("iv",null);
  SecretKey key=getOrCreateKey();
  if(encrypted==null){
   byte[] raw=new byte[32];new java.security.SecureRandom().nextBytes(raw);
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key);
   byte[] enc=cipher.doFinal(raw);
   p.edit().putString("cipher",Base64.encodeToString(enc,Base64.NO_WRAP))
    .putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP)).commit();
   return Base64.encodeToString(raw,Base64.NO_WRAP);
  }
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Base64.decode(ivText,Base64.NO_WRAP)));
  byte[] raw=cipher.doFinal(Base64.decode(encrypted,Base64.NO_WRAP));
  return Base64.encodeToString(raw,Base64.NO_WRAP);
 }
 private static SecretKey getOrCreateKey() throws Exception {
  KeyStore ks=KeyStore.getInstance(STORE);ks.load(null);
  if(ks.containsAlias(ALIAS))return (SecretKey)ks.getKey(ALIAS,null);
  KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,STORE);
  kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
   .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
   .setRandomizedEncryptionRequired(true).build());
  return kg.generateKey();
 }
}

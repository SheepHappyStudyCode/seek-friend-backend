package com.yupi.friend;

import com.yupi.friend.utils.HashUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class UserTest {

    @Test
    void testHash() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
        SecretKey key = keyGen.generateKey();

        byte[] skey = key.getEncoded();
        System.out.println(HashUtils.hexToStr(skey));

        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(key);
        mac.update("HelloWorld".getBytes("UTF-8"));
        byte[] result = mac.doFinal();
        System.out.println(HashUtils.hexToStr(result));
    }

    @Test
    void testDecodeStr() throws Exception{
        byte[] hkey = HashUtils.strToHex("1435cc910505744122ff30424ed4315ed258272d8d86b23273fbc6b8f356592b53582c70619c303ba189b943f87a820cf7196322d2087fae6597316918f41479");
        SecretKey key = new SecretKeySpec(hkey, "HmacMD5");
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(key);
        mac.update("HelloWorld".getBytes("UTF-8"));
        byte[] result = mac.doFinal();
        Assertions.assertEquals("e85465f1be16f57839e757bdb02f00c0", HashUtils.hexToStr(result));


    }

    @Test
    void commonTest(){
        long time = 10L;
        TimeUnit unit = TimeUnit.MINUTES;
        System.out.println(unit.toSeconds(time));
        System.out.println(unit.toSeconds(time));
        System.out.println(unit.toSeconds(time));
    }
}

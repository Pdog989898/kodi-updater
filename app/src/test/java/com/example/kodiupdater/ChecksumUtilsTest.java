package com.example.kodiupdater;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class ChecksumUtilsTest {

    @Test
    public void calculatesExpectedSha256() {
        String hash = ChecksumUtils.sha256Hex("kodi".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("48740fb4ea903f67f12358d50f406b8f03d088f7b1ab06a964fd266a45e79f52", hash);
    }
}

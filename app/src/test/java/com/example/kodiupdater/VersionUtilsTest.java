package com.example.kodiupdater;

import org.junit.Assert;
import org.junit.Test;

public class VersionUtilsTest {

    @Test
    public void detectsNewerVersion() {
        Assert.assertTrue(VersionUtils.isNewer("1.2.0", "1.1.9"));
    }

    @Test
    public void detectsSameVersion() {
        Assert.assertFalse(VersionUtils.isNewer("1.0.0", "1.0.0"));
    }

    @Test
    public void handlesEmptyLocalVersion() {
        Assert.assertTrue(VersionUtils.isNewer("1.0.0", ""));
    }
}

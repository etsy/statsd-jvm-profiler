package com.etsy.statsd.profiler.util;

import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class TagUtilTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoTagMapping() {
        String prefix = "i.am.a.prefix";

        Map<String, String> expected = Maps.newHashMap();
        expected.put(TagUtil.PREFIX_TAG, prefix);

        assertEquals(expected, TagUtil.getTags(null, prefix, false));
    }

    @Test
    public void testInvalidMapping() {
        String prefix = "one.two.three";
        String tagMapping = "tagOne.tagTwo";

        expectedException.expect(RuntimeException.class);
        TagUtil.getTags(tagMapping, prefix, false);
    }
    
    @Test
    public void testMapping() {
        String prefix = "one.two.three";
        String tagMapping = "tagOne.tagTwo.tagThree";

        Map<String, String> expected = Maps.newHashMap();
        expected.put("tagOne", "one");
        expected.put("tagTwo", "two");
        expected.put("tagThree", "three");

        assertEquals(expected, TagUtil.getTags(tagMapping, prefix, false));
    }

    @Test
    public void testGetGlobalTags() {
        Map<String, String> globalTags = new HashMap<>();
        TagUtil.getGlobalTags(globalTags);
        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add(TagUtil.PID_TAG);
        expectedKeys.add(TagUtil.HOSTNAME_TAG);
        expectedKeys.add(TagUtil.JVM_NAME_TAG);

        assertEquals(expectedKeys, globalTags.keySet());
    }

    @Test
    public void testIncludeGlobalTags() {
        String prefix = "one.two.three";
        String tagMapping = "tagOne.tagTwo.tagThree";

        Map<String, String> expected = Maps.newHashMap();
        expected.put("tagOne", "one");
        expected.put("tagTwo", "two");
        expected.put("tagThree", "three");
        Map<String, String> globalTags = new HashMap<>();
        TagUtil.getGlobalTags(globalTags);
        expected.putAll(globalTags);

        assertEquals(expected, TagUtil.getTags(tagMapping, prefix, true));
    }
}

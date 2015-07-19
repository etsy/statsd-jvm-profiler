package com.etsy.statsd.profiler.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Utility class for tagging metrics from the metric prefix
 * This is used to support InfluxDB 0.9's tag feature
 *
 * @author Andrew Johnson
 */
public class TagUtil {
    public static final String SKIP_TAG = "SKIP";
    public static final String PREFIX_TAG = "prefix";

    /**
     * Gets all the tag values from the prefix and the tag mapping
     *
     * @return A map of tag name to value
     */
    public static Map<String, String> getTags(String tagMapping, String prefix) {
        Map<String, String> mapping = Maps.newHashMap();
        if (tagMapping != null) {
            String[] tagNames = tagMapping.split("\\.");
            String[] prefixComponents = prefix.split("\\.");
            if (tagNames.length != prefixComponents.length) {
                throw new RuntimeException(String.format("Invalid tag mapping: %s", tagMapping));
            }

            for (int i = 0; i < tagNames.length; i++) {
                String tag = tagNames[i];
                String value = prefixComponents[i];
                if (!tag.equals(SKIP_TAG)) {
                    mapping.put(tag, value);
                }
            }
        } else {
            mapping.put(PREFIX_TAG, prefix);
        }

        return mapping;
    }
}

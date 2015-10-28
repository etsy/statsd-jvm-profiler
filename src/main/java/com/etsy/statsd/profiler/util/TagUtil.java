package com.etsy.statsd.profiler.util;

import com.google.common.collect.Maps;

import java.lang.management.ManagementFactory;
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

    public static final String UNKNOWN = "unknown";
    public static final String PID_TAG = "pid";
    public static final String HOSTNAME_TAG = "hostname";
    public static final String JVM_NAME_TAG = "jvmName";

    public static Map<String, String> getGlobalTags(Map<String, String> tags) {
        // Add the jvm name, pid, hostname as tags to help identify different processes
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        tags.put(JVM_NAME_TAG, jvmName);
        int atIndex = jvmName.indexOf("@");
        if (atIndex > 0) {
            tags.put(PID_TAG, jvmName.substring(0, atIndex));
            tags.put(HOSTNAME_TAG, jvmName.substring(atIndex + 1));
        } else {
            tags.put(PID_TAG, UNKNOWN);
            tags.put(HOSTNAME_TAG, UNKNOWN);
        }

        return tags;
    }

    /**
     * Gets all the tag values from the prefix and the tag mapping
     *
     * @param tagMapping The mapping of tag names from the metric prefix
     * @param prefix The metric prefix
     * @param includeGlobalTags Whether or not to include the global tags
     * @return A map of tag name to value
     */
    public static Map<String, String> getTags(String tagMapping, String prefix, boolean includeGlobalTags) {
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

        if (includeGlobalTags) {
            mapping.putAll(getGlobalTags(mapping));
        }

        return mapping;
    }
}

package org.rhq.core.domain.provisioning;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.rhq.core.domain.util.StringUtils;

/**
 * A helper class to parse a set of media types from and to strings.
 *
 * @author Lukas Krejci
 */
final class MediaTypes {

    private static final StringUtils.ObjectFromStringFactory<MediaType> FROM_STRING_FACTORY = new StringUtils.ObjectFromStringFactory<MediaType>() {
        @Override
        public MediaType fromString(String s) {
            return MediaType.valueOf(s);
        }
    };

    private MediaTypes() {

    }

    /**
     * Converts a whitespace separated list of media type strings into a set
     * of media type instances.
     *
     * @param value the string to parse
     * @return parsed media types
     */
    public static Set<MediaType> parse(String value) {
        HashSet<MediaType> ret = new HashSet<MediaType>();

        StringUtils.fill(ret, value, " ", true, FROM_STRING_FACTORY);

        return ret;
    }

    /**
     * Converts the provided set of media types into a space-separated list of
     * their string representations.
     *
     * @param mediaTypes the media types
     * @return the string representing the set of media types
     */
    public static String toString(Set<MediaType> mediaTypes) {
        return StringUtils.join(mediaTypes, " ", false);
    }
}

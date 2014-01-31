/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.core.domain.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 */
public class StringUtils {
    private static final Set<String> LOWERCASE_WORDS = new HashSet<String>();

    static {
        // conjunctions
        LOWERCASE_WORDS.add("And");
        LOWERCASE_WORDS.add("Or");

        // articles
        LOWERCASE_WORDS.add("A");
        LOWERCASE_WORDS.add("An");
        LOWERCASE_WORDS.add("The");
    }

    public interface ObjectFromStringFactory<T> {
        T fromString(String s);
    }

    private static final ObjectFromStringFactory<String> STRING_FROM_STRING_FACTORY = new ObjectFromStringFactory<String>() {
        @Override
        public String fromString(String s) {
            return s;
        }
    };

    /*
     * Take something that is camel-cased, add spaces between the words, and capitalize each word.
     */
    public static String deCamelCase(String target) {
        if (target == null) {
            return null;
        }

        if (target.length() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        if (target.startsWith("-")) {
            target = target.substring(1);
        }

        if (target.endsWith("-")) {
            target = target.substring(0, target.length() - 1);
        }

        int nextDash;
        while ((nextDash = target.indexOf('-')) > 0) {
            target = target.substring(0, nextDash) + Character.toUpperCase(target.charAt(nextDash + 1))
                + target.substring(nextDash + 2);
        }

        char currentChar;
        // Always make the first char upper case.
        char previousChar = Character.toUpperCase(target.charAt(0));
        StringBuilder currentWord = new StringBuilder();
        currentWord.append(previousChar);
        for (int i = 1; i < target.length(); i++) {
            currentChar = target.charAt(i);

            // Make sure to not insert spaces in the middle of acronyms or multi-digit numbers.
            if ((previousChar == ' ' && currentChar != ' ')
                || (Character.isDigit(currentChar) && !Character.isDigit(previousChar))
                || (Character.isUpperCase(currentChar) && (i < (target.length() - 1))
                    && ((i + 1) < target.length() - 1) && Character.isLowerCase(target.charAt(i + 1)))
                || (Character.isUpperCase(currentChar) && Character.isLowerCase(previousChar))) {
                // We're at the start of a new word.
                appendWord(result, currentWord.toString());
                currentWord = new StringBuilder();
                // Append a space before the next word.
                result.append(' ');
            }

            if (currentChar != ' ') {
                currentWord.append(currentChar);
            }
            previousChar = currentChar;
        }
        // Append the final word.
        appendWord(result, currentWord.toString());

        return result.toString();
    }

    private static void appendWord(StringBuilder result, String word) {
        if (word.length() >= 1) {
            if (LOWERCASE_WORDS.contains(word)) {
                result.append(word.toLowerCase());
            } else {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
    }

    public static List<String> getStringAsList(String input, String regexSplitter, boolean ignoreEmptyTokens) {
        List<String> results = new ArrayList<String>();
        fill(results, input, regexSplitter, ignoreEmptyTokens);

        return results;
    }

    public static void fill(Collection<String> collection, String input, String splitRegex, boolean ignoreEmptyTokens) {
        fill(collection, input, splitRegex, ignoreEmptyTokens, STRING_FROM_STRING_FACTORY);
    }

    public static <T> void fill(Collection<T> collection, String input, String splitRegex, boolean ignoreEmptyTokens, ObjectFromStringFactory<T> factory) {
        if (input == null) {
            // gracefully return a 0-element list if the input is null
            return;
        }

        for (String lineItem : input.split(splitRegex)) {
            // allow user to visual separate data, but ignore blank lines
            if (ignoreEmptyTokens && lineItem.trim().equals("")) {
                continue;
            }

            T object = factory.fromString(lineItem);

            collection.add(object);
        }
    }

    public static String getListAsString(List<?> stringList, String seperatorFragment) {
        return join(stringList, seperatorFragment, true);
    }

    public static String join(Iterable<?> strings, String separator, boolean includeNullElements) {
        Iterator<?> it = strings.iterator();
        if (!it.hasNext()) {
            return "";
        }

        StringBuilder bld = new StringBuilder();
        Object next = it.next();

        if (next != null || includeNullElements) {
            bld.append(next);
        }

        while(it.hasNext()) {
            next = it.next();
            if (next != null || includeNullElements) {
                bld.append(separator).append(next);
            }
        }

        return bld.toString();
    }

    /**
     * Ensure that the path uses only forward slash.
     * @param path
     * @return forward-slashed path, or null if path is null
     */
    public static String useForwardSlash(String path) {

        return (null != path) ? path.replace('\\', '/') : null;
    }

}

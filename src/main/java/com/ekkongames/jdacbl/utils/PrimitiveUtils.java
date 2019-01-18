package com.ekkongames.jdacbl.utils;

import java.util.List;

/**
 * Created by Dolphish on 2016-10-29.
 */
public class PrimitiveUtils {
    public static <T> T get(List<T> items) {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(0);
    }
}

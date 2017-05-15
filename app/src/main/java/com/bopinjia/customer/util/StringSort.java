package com.bopinjia.customer.util;

import java.util.Comparator;

public class StringSort implements Comparator<String> {
    @Override
    public int compare(String lhs, String rhs) {
        return lhs.compareTo(rhs);
    }
}

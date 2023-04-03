

package com.parmet.buf.test.v1;

import com.google.type.DateTime;

public class Foo {
    public static void test() {
        Test.BasicMessage.newBuilder().setDateTime(DateTime.newBuilder().build()).build();
    }
}

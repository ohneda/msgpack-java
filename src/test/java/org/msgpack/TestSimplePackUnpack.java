package org.msgpack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.value.Value;

import org.junit.Test;

public class TestSimplePackUnpack {
    @Test
    public void testSimplePackUnpack() throws IOException {
        MessagePack msgpack = new MessagePack();
        byte[] raw = msgpack.write(new int[] {1,2,3});

        Value v = msgpack.read(raw);
        int[] a = msgpack.read(raw, new int[3]);

        Value vb = msgpack.read(ByteBuffer.wrap(raw));
        int[] ab = msgpack.read(ByteBuffer.wrap(raw), new int[3]);
    }
}


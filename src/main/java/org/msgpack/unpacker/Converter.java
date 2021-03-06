//
// MessagePack for Java
//
// Copyright (C) 2009-2011 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.unpacker;

import java.io.EOFException;
import java.math.BigInteger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Unconverter;
import org.msgpack.value.Value;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;

public class Converter extends Unpacker {
    private final UnpackerStack stack;
    private Object[] values;
    private Value value;

    public Converter(Value value) {
        this.stack = new UnpackerStack();
        this.values = new Object[UnpackerStack.MAX_STACK_SIZE];
        this.values[0] = value;
        this.value = value;
    }

    @Override
    public boolean tryReadNil() {
        stack.checkCount();
        if(getTop().isNil()) {
            stack.reduceCount();
            return true;
        }
        return false;
    }

    @Override
    public boolean trySkipNil() {
        if(stack.getDepth() > 0 && stack.getTopCount() <= 0) {
            // end of array or map
            return true;
        }

        if(getTop().isNil()) {
            stack.reduceCount();
            return true;
        }
        return false;
    }

    @Override
    public void readNil() {
        if(!getTop().isNil()) {
            throw new MessageTypeException("Expected nil but got not nil value");
        }
        stack.reduceCount();
    }

    @Override
    public boolean readBoolean() {
        boolean v = getTop().asBooleanValue().getBoolean();
        stack.reduceCount();
        return v;
    }

    @Override
    public byte readByte() {
        byte v = getTop().asIntegerValue().getByte();
        stack.reduceCount();
        return v;
    }

    @Override
    public short readShort() {
        short v = getTop().asIntegerValue().getShort();
        stack.reduceCount();
        return v;
    }

    @Override
    public int readInt() {
        int v = getTop().asIntegerValue().getInt();
        stack.reduceCount();
        return v;
    }

    @Override
    public long readLong() {
        long v = getTop().asIntegerValue().getLong();
        stack.reduceCount();
        return v;
    }

    @Override
    public BigInteger readBigInteger() {
        BigInteger v = getTop().asIntegerValue().getBigInteger();
        stack.reduceCount();
        return v;
    }

    @Override
    public float readFloat() {
        float v = getTop().asFloatValue().getFloat();
        stack.reduceCount();
        return v;
    }

    @Override
    public double readDouble() {
        double v = getTop().asFloatValue().getDouble();
        stack.reduceCount();
        return v;
    }

    @Override
    public byte[] readByteArray() {
        byte[] raw = getTop().asRawValue().getByteArray();
        stack.reduceCount();
        return raw;
    }

    @Override
    public int readArrayBegin() {
        Value v = getTop();
        if(!v.isArray()) {
            throw new MessageTypeException("Expected array but got not array value");
        }
        ArrayValue a = v.asArrayValue();
        stack.pushArray(a.size());
        values[stack.getDepth()] = a.getElementArray();
        return a.size();
    }

    @Override
    public void readArrayEnd(boolean check) {
        if(!stack.topIsArray()) {
            throw new MessageTypeException("readArrayEnd() is called but readArrayBegin() is not called");
        }

        int remain = stack.getTopCount();
        if(remain > 0) {
            if(check) {
                throw new MessageTypeException("readArrayEnd(check=true) is called but the array is not end");
            }
            for(int i=0; i < remain; i++) {
                skip();
            }
        }
        stack.pop();
    }

    @Override
    public int readMapBegin() {
        Value v = getTop();
        if(!v.isArray()) {
            throw new MessageTypeException("Expected array but got not array value");
        }
        MapValue m = v.asMapValue();
        stack.pushMap(m.size());
        values[stack.getDepth()] = m.getKeyValueArray();
        return m.size();
    }

    @Override
    public void readMapEnd(boolean check) {
        if(!stack.topIsMap()) {
            throw new MessageTypeException("readMapEnd() is called but readMapBegin() is not called");
        }

        int remain = stack.getTopCount();
        if(remain > 0) {
            if(check) {
                throw new MessageTypeException("readMapEnd(check=true) is called but the map is not end");
            }
            for(int i=0; i < remain; i++) {
                skip();
            }
        }
        stack.pop();
    }

    private Value getTop() {
        stack.checkCount();
        if(stack.getDepth() == 0) {
            if(stack.getTopCount() < 0) {
                //throw new EOFException();  // TODO
                throw new RuntimeException(new EOFException());
            }
            return (Value) values[0];
        }
        Value[] array = (Value[]) values[stack.getDepth()];
        return array[array.length - stack.getTopCount()];
    }

    @Override
    protected void readValue(Unconverter uc) {
        if(uc.getResult() != null) {
            uc.resetResult();
        }

        stack.checkCount();
        Value v = getTop();
        if(!v.isArray() && !v.isMap()) {
            stack.reduceCount();
            if(uc.getResult() != null) {
                return;
            }
        }

        while(true) {
            while(stack.getTopCount() == 0) {
                if(stack.topIsArray()) {
                    uc.writeArrayEnd(true);
                    stack.pop();
                } else if(stack.topIsMap()) {
                    uc.writeMapEnd(true);
                    stack.pop();
                } else {
                    throw new RuntimeException("invalid stack"); // FIXME error?
                }
                if(uc.getResult() != null) {
                    return;
                }
            }

            stack.checkCount();
            v = getTop();
            if(v.isArray()) {
                ArrayValue a = v.asArrayValue();
                uc.writeArrayBegin(a.size());
                stack.reduceCount();
                stack.pushArray(a.size());
                values[stack.getDepth()] = a.getElementArray();

            } else if(v.isMap()) {
                MapValue m = v.asMapValue();
                uc.writeMapBegin(m.size());
                stack.reduceCount();
                stack.pushMap(m.size());
                values[stack.getDepth()] = m.getKeyValueArray();

            } else {
                uc.write(v);
                stack.reduceCount();
            }
        }
    }

    @Override
    public void skip() {
        stack.checkCount();
        Value v = getTop();
        if(!v.isArray() && !v.isMap()) {
            stack.reduceCount();
            return;
        }
        int targetDepth = stack.getDepth();
        while(true) {
            while(stack.getTopCount() == 0) {
                stack.pop();
                if(stack.getDepth() <= targetDepth) {
                    return;
                }
            }

            stack.checkCount();
            v = getTop();
            if(v.isArray()) {
                ArrayValue a = v.asArrayValue();
                stack.reduceCount();
                stack.pushArray(a.size());
                values[stack.getDepth()] = a.getElementArray();

            } else if(v.isMap()) {
                MapValue m = v.asMapValue();
                stack.reduceCount();
                stack.pushMap(m.size());
                values[stack.getDepth()] = m.getKeyValueArray();

            } else {
                stack.reduceCount();
            }
        }
    }
}


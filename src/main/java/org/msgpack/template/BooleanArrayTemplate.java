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
package org.msgpack.template;

import java.io.IOException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.msgpack.MessageTypeException;


public class BooleanArrayTemplate implements Template {
    private BooleanArrayTemplate() { }

    public void write(Packer pk, Object target) throws IOException {
        if(target == null) {
            throw new MessageTypeException("Attempted to write null");
        }
        boolean[] array = (boolean[]) target;
        pk.writeArrayBegin(array.length);
        for(boolean a : array) {
            pk.writeBoolean(a);
        }
        pk.writeArrayEnd();
    }

    public Object read(Unpacker u, Object to) throws IOException {
        int n = u.readArrayBegin();
        boolean[] array;
        if(to != null && ((boolean[]) to).length == n) {
            array = (boolean[]) to;
        } else {
            array = new boolean[n];
        }
        for(int i=0; i < n; i++) {
            array[i] = u.readBoolean();
        }
        u.readArrayEnd();
        return array;
    }

    static public BooleanArrayTemplate getInstance() {
        return instance;
    }

    static final BooleanArrayTemplate instance = new BooleanArrayTemplate();
}


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
package org.msgpack.packer;

import org.msgpack.io.LinkedBufferOutput;

public class BufferPacker extends AbstractMessagePackPacker {
    public BufferPacker() {
        this(512);  // TODO default buffer size
    }

    public BufferPacker(int bufferSize) {
        super(new LinkedBufferOutput(bufferSize));
    }

    public byte[] toByteArray() {
        LinkedBufferOutput bo = (LinkedBufferOutput) out;
        return ((LinkedBufferOutput) bo).toByteArray();
    }
}


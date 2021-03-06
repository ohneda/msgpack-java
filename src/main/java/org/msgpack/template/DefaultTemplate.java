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
import java.lang.reflect.Type;

import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.TemplateRegistry;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;


public class DefaultTemplate implements Template {
    private TemplateRegistry registry;

    private Class<?> targetClass;

    private Type lookupType;

    private boolean messagePackable;

    public DefaultTemplate(TemplateRegistry registry, Class<?> targetClass) {
	this(registry, targetClass, (Type) targetClass);
    }

    public DefaultTemplate(TemplateRegistry registry, Class<?> targetClass, Type lookupType) {
	this.registry = registry;
	this.targetClass = targetClass;
	this.lookupType = lookupType;
	this.messagePackable = MessagePackable.class.isAssignableFrom(targetClass);
    }

    public void write(Packer pk, Object target) throws IOException {
	if (messagePackable) {
	    if (target == null) {
		throw new NullPointerException("target is null");
	    }
	    ((MessagePackable) target).writeTo(pk);
	    return;
	}
	Template tmpl = registry.tryLookup(lookupType);
	if (tmpl == this || tmpl == null) {
	    throw new MessageTypeException("Template lookup fail: " + lookupType);
	}
	tmpl.write(pk, target);
    }

    public Object read(Unpacker pac, Object to) throws IOException {
	// TODO #MN
	Template tmpl = registry.tryLookup(lookupType);
	if (tmpl == this || tmpl == null) {
	    throw new MessageTypeException("Template lookup fail: " + lookupType);
	}
	return tmpl.read(pac, to);
    }
}

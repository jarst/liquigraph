/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.liquigraph.core.documentation.xml;

import org.liquigraph.core.model.Changeset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.Writer;

public class XmlMarshaller<T> {

    public String serialize(T object, String rootName, boolean includeProlog) throws Exception {
        try (Writer writer = new StringWriter()) {
            marshaller(includeProlog).marshal(
                itsAWrap(object, rootName),
                writer
            );
            return writer.toString();
        }
    }

    private static Marshaller marshaller(boolean includeProlog) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Changeset.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, !includeProlog);
        return marshaller;
    }

    private JAXBElement<T> itsAWrap(T object, String rootName) {
        return new JAXBElement<T>(new QName(null, rootName), (Class<T>) object.getClass(), object);
    }
}

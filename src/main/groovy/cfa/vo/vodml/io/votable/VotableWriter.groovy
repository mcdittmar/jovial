/*
 * #%L
 * jovial
 * %%
 * Copyright (C) 2016 - 2017 Smithsonian Astrophysical Observatory
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Smithsonian Astrophysical Observatory nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package cfa.vo.vodml.io.votable

import cfa.vo.vodml.instance.*
import groovy.xml.StreamingMarkupBuilder

class VotableWriter extends AbstractMarkupInstanceWriter {
    @Override
    void build(DataModelInstance instance, Object builder) {
        def elem = {
            VOTABLE() {
                VODML() {
                    instance.models.each {
                        out << buildModel(it, delegate)
                    }
                    if (instance.objectTypes) {
                        GLOBALS() {
                            instance.objectTypes.each {
                                out << buildObject(it, delegate)
                            }
                        }
                    }
                    if (instance.tables) {
                        TEMPLATES() {
                            instance.tables.each {
                                out << buildTable(it, delegate)
                            }
                        }
                    }
                }
            }
        }
        elem.delegate = builder
        elem()
    }

    void buildModel(ModelImportInstance modelImportInstance, builder) {
        def elem = {
            MODEL() {
                NAME(modelImportInstance.name)
                URL(modelImportInstance.vodmlURL)
                if (modelImportInstance.identifier) {
                    IDENTIFIER(modelImportInstance.identifier)
                }
            }
        }
        elem.delegate = builder
        elem()
    }

    void buildObject(objectInstance, builder) {
        def elem = {
            INSTANCE(dmtype:objectInstance.type.toString(), ID: objectInstance.id) {
                if (objectInstance.fullId) {
                    IDENTIFIER() {
                        IDFIELD() {
                            LITERAL(dmtype: "ivoa:string", value: objectInstance.id)
                        }
                    }
                }
                objectInstance.objectTypes.each {
                    out << buildAttribute(it, builder)
                }
                objectInstance.references.each {
                    REFERENCE(dmrole: it.role, it.value)
                }
                if (objectInstance.hasProperty("compositions")) {
                    objectInstance.compositions.each {
                        out << buildComposition(it, builder)
                    }
                }
                objectInstance.columns.each { column ->
                    ATTRIBUTE(dmrole: column.role) {
                        COLUMN(ref: column.value)
                    }
                }
            }
        }
        elem.delegate = builder
        elem()
    }

    void buildComposition(CompositionInstance compositionInstance, builder) {
        def elem = {
            COMPOSITION(dmrole:compositionInstance.role.toString()) {
                for (instance in compositionInstance.objectTypes) {
                    out << buildObject(instance, builder)
                }
            }
        }
        elem.delegate = builder
        elem()
    }

    void buildTable(TableInstance tableInstance, builder) {
        def elem = {
            for (instance in tableInstance.objectTypes) {
              out << buildObject(instance, builder)
            }
        }
        elem.delegate = builder
        elem()
    }

    void buildAttribute(ObjectInstance attributeInstance, builder) {
        def elem = {
            ATTRIBUTE(dmrole: attributeInstance.role) {
                LITERAL(value: attributeInstance.value, dmtype: attributeInstance.type.toString())
            }
        }
        elem.delegate = builder
        elem()
    }

    @Override
    String getNameSpace() {
        return "http://www.ivoa.net/xml/VOTable/v1.4"
    }

    @Override
    String getPrefix() {
        return ""
    }

    @Override
    StreamingMarkupBuilder getMarkupBuilder() {
        return new StreamingMarkupBuilder()
    }
}

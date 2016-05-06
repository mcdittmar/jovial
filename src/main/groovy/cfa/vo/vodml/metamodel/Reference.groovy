package cfa.vo.vodml.metamodel

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode


@Bindable
@EqualsAndHashCode
class Reference extends Relation implements Buildable {
    @Override
    void build(GroovyObject builder) {
        def elem = {
            reference() {
                "vodml-id"(this.vodmlid)
                name(this.name)
                description(this.description)
                datatype() {
                    out << this.dataType
                }
                out << this.multiplicity
            }
        }
        elem.delegate = builder
        elem()
    }
}

package cfa.vo.vodml.io

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class VodmlWriter {

    def write(model, OutputStream os) {
        def writer = new OutputStreamWriter(os)
        def builder = new StreamingMarkupBuilder().bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace("${model.prefix}": model.ns, xsi: "http://www.w3.org/2001/XMLSchema-instance")
            out << model
        }
        XmlUtil.serialize builder, writer
    }
}
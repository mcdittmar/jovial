package cfa.vo.vodml.io

import ca.odell.glazedlists.BasicEventList
import cfa.vo.vodml.*
import groovy.util.slurpersupport.GPathResult
import org.joda.time.DateTime

class VodmlReader {
    private slurper = new XmlSlurper()

    Model read(InputStream is) {
        def parsed = slurper.parse(is)
        return modelFrom(parsed)
    }

    Model read(File file) {
        read(new FileInputStream(file))
    }

    private Model modelFrom(GPathResult xml) {
        new Model(
                name: xml.name,
                description: xml.description,
                title: xml.title,
                version: xml.version,
                lastModified: new DateTime(xml.lastModified.toString()),
                authors: xml.author?.collect {
                    it.text()
                } as BasicEventList,
                previousVersions: xml.previousVersions?.collect {
                    it.text()
                },
                imports: xml.import?.collect {
                    importFrom(it)
                } as BasicEventList,
                packages: xml.package?.collect {
                    packageFrom(it)
                },
                objectTypes: xml.objectType?.collect {
                    objectTypeFrom(it)
                },
                dataTypes: xml.dataType?.collect {
                    dataTypeFrom(it)
                },
                enumerations: xml.enumeration?.collect {
                    enumerationFrom(it)
                },
                primitiveTypes: xml.primitiveType.collect {
                    primitiveTypeFrom(it)
                }
        )
    }

    private ModelImport importFrom(GPathResult xml) {
        new ModelImport(
                name: xml.name,
                version: xml.version,
                url: new URL(xml.url.text()),
                documentationURL: new URL(xml.documentationURL.text())
        )
    }

    private Package packageFrom(GPathResult xml) {
        new Package(
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                objectTypes: xml.objectType?.collect {
                    objectTypeFrom(it)
                },
                dataTypes: xml.dataType?.collect {
                    dataTypeFrom(it)
                },
                enumerations: xml.enumeration?.collect {
                    enumerationFrom(it)
                },
                primitiveTypes: xml.primitiveType.collect {
                    primitiveTypeFrom(it)
                },
                packages: xml.package?.collect {
                    packageFrom(it)
                },
        )
    }

    private ObjectType objectTypeFrom(GPathResult xml) {
        new ObjectType(
                abstract_ : xml.@abstract.text(),
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                extends_: elementRefFrom(xml.extends),
                attributes: xml.attribute.collect {
                    attributeFrom(it)
                },
                collections: xml.collection.collect {
                    compositionFrom(it)
                },
                references: xml.reference.collect {
                    referenceFrom(it)
                },
                constraints: xml.constraint.collect {
                    constraintFrom(it)
                },
        )
    }

    private DataType dataTypeFrom(GPathResult xml) {
        new DataType(
                abstract_ : xml.@abstract.text(),
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                extends_: elementRefFrom(xml.extends),
                attributes: xml.attribute?.collect {
                    attributeFrom(it)
                },
                references: xml.reference.collect {
                    referenceFrom(it)
                },
                constraints: xml.constraint.collect {
                    constraintFrom(it)
                }
        )
    }

    private Enumeration_ enumerationFrom(GPathResult xml) {
        new Enumeration_(
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                extends_: elementRefFrom(xml.extends),
                literals: xml.literal?.collect {
                    new EnumLiteral(
                            name: it.name,
                            vodmlid: it."vodml-id",
                            description: it.description,
                    )
                }
        )
    }

    private PrimitiveType primitiveTypeFrom(GPathResult xml) {
        new PrimitiveType(
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                extends_: elementRefFrom(xml.extends),
        )
    }

    private ElementRef elementRefFrom(GPathResult xml) {
        if(xml.size()) {
            new ElementRef(
                    vodmlref: xml."vodml-ref"
            )
        }
    }

    private SemanticConcept semanticConceptFrom(GPathResult xml) {
        new SemanticConcept(
                topConcept: xml.topConcept.text(),
                vocabularyURI: xml.vocabularyURI.text()
        )
    }

    private Attribute attributeFrom(GPathResult xml) {
        new Attribute(
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                dataType: elementRefFrom(xml.datatype),
                semanticConcepts: xml.semanticconcept?.collect {
                    semanticConceptFrom(it)
                },
                multiplicity: multiplicityFrom(xml.multiplicity)
        )
    }

    private Composition compositionFrom(GPathResult xml) {
        new Composition(
                name: xml.name,
                vodmlid: xml."vodml-id",
                description: xml.description,
                dataType: elementRefFrom(xml.datatype),
                multiplicity: multiplicityFrom(xml.multiplicity)
        )
    }

    private Multiplicity multiplicityFrom(GPathResult xml) {
        new Multiplicity(
                minOccurs: Integer.parseInt(xml.minOccurs.text()),
                maxOccurs: Integer.parseInt(xml.maxOccurs.text())
        )
    }

    private Reference referenceFrom(GPathResult xml) {
        new Reference(
            name: xml.name,
            vodmlid: xml."vodml-id",
            description: xml.description,
            dataType: elementRefFrom(xml.datatype),
            multiplicity: multiplicityFrom(xml.multiplicity)
        )
    }

    private Constraint constraintFrom(GPathResult xml) {
        if (xml."@xsi:type"?.equals("vo-dml:SubsettedRole")) {
            new SubsettedRole(
                    role: elementRefFrom(xml.role),
                    dataType: elementRefFrom(xml.datatype),
                    semanticConcept: semanticConceptFrom(xml.semanticconcept)
            )
        } else {
            new Constraint(
                    description: xml.description
            )
        }
    }
}
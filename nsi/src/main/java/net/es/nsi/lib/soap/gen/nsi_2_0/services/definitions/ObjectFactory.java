//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.19 at 01:27:41 PM PDT 
//


package net.es.nsi.lib.soap.gen.nsi_2_0.services.definitions;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.nsi.lib.soap.gen.nsi_2_0.services.definitions package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceDefinition_QNAME = new QName("http://schemas.ogf.org/nsi/2013/12/services/definition", "serviceDefinition");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.nsi.lib.soap.gen.nsi_2_0.services.definitions
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NsiServiceDefinitionType }
     * 
     */
    public NsiServiceDefinitionType createNsiServiceDefinitionType() {
        return new NsiServiceDefinitionType();
    }

    /**
     * Create an instance of {@link NsiAdaptationType }
     * 
     */
    public NsiAdaptationType createNsiAdaptationType() {
        return new NsiAdaptationType();
    }

    /**
     * Create an instance of {@link NsiSchemaType }
     * 
     */
    public NsiSchemaType createNsiSchemaType() {
        return new NsiSchemaType();
    }

    /**
     * Create an instance of {@link NsiParameterType }
     * 
     */
    public NsiParameterType createNsiParameterType() {
        return new NsiParameterType();
    }

    /**
     * Create an instance of {@link NsiAttributeType }
     * 
     */
    public NsiAttributeType createNsiAttributeType() {
        return new NsiAttributeType();
    }

    /**
     * Create an instance of {@link NsiErrorType }
     * 
     */
    public NsiErrorType createNsiErrorType() {
        return new NsiErrorType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NsiServiceDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/12/services/definition", name = "serviceDefinition")
    public JAXBElement<NsiServiceDefinitionType> createServiceDefinition(NsiServiceDefinitionType value) {
        return new JAXBElement<NsiServiceDefinitionType>(_ServiceDefinition_QNAME, NsiServiceDefinitionType.class, null, value);
    }

}

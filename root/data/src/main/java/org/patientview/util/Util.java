package org.patientview.util;

import generated.Patientview;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class Util {


    private final static Logger LOG = LoggerFactory.getLogger(Util.class);
    private static JsonParser jsonParser;

    static {
        jsonParser = new JsonParser();
    }

    /**
     * Convert the Iterable<T> type passed for Spring DAO interface into a more useful typed List.
     * @param iterable Iterable to convert to typed List
     * @param <T> Type of List to create
     * @return Typed List
     */
    public static <T> List<T> convertIterable(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;
    }

    public static FhirLink getFhirLink(Group group, String identifierText, Set<FhirLink> fhirLinks) {
        if (CollectionUtils.isEmpty(fhirLinks)) {
            return null;
        }

        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getGroup().equals(group) && fhirLink.getIdentifier().getIdentifier().equals(identifierText)) {
                return fhirLink;
            }
        }

        return null;
    }

    public static StringWriter marshallPatientRecord(Patientview patientview) throws ImportResourceException {

        StringWriter stringWriter = null;

        try {
            JAXBContext context = JAXBContext.newInstance(Patientview.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientview, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

        return stringWriter;
    }

    public static Patientview unmarshallPatientRecord(String content) throws ImportResourceException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Patientview.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Patientview) unmarshaller.unmarshal(new StringReader(content));
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }
    }

    public static String marshallFhirRecord(Resource resource) throws FhirResourceException {
        JsonComposer jsonComposer = new JsonComposer();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            jsonComposer.compose(outputStream, resource, false);
        } catch (Exception e) {
            LOG.error("Unable to handle Fhir resource record", e);
            throw new FhirResourceException("Cannot build JSON", e);
        }
        return outputStream.toString();
    }

    public static UUID getVersionId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        JSONArray links = (JSONArray) resource.get("link");
        JSONObject link = (JSONObject)  links.get(0);
        String[] href = link.getString("href").split("/");
        return UUID.fromString(href[href.length - 1]);
    }

    public static UUID getResourceId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.get("id").toString());
    }

    public static ResourceReference createResourceReference(UUID uuid) {
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setDisplaySimple(uuid.toString());
        resourceReference.setReferenceSimple("uuid");
        return resourceReference;
    }

    // check string appears in enum
    public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equals(value)) { return true; }
        }
        return false;
    }
}

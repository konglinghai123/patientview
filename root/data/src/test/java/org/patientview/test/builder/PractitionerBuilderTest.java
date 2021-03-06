package org.patientview.test.builder;

import generated.Patientview;
import org.hl7.fhir.instance.model.Practitioner;
import org.junit.Assert;
import org.junit.Test;
import org.patientview.builder.PractitionerBuilder;
import org.patientview.test.BaseTest;
import org.patientview.util.Util;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
public class PractitionerBuilderTest extends BaseTest {

    /**
     * Test: To create the practitioner without error
     *
     * @throws Exception
     */
    @Test
    public void testPractitionerBuilder() throws Exception {

        Patientview patientview = Util.unmarshallPatientRecord(getTestFile());
        PractitionerBuilder practitionerBuilder = new PractitionerBuilder(patientview);
        Practitioner practitioner =  practitionerBuilder.build();

        Assert.assertTrue("The practitioner is not empty", practitioner != null);
    }
}

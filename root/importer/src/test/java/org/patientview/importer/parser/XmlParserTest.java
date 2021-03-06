package org.patientview.importer.parser;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
public class XmlParserTest {

    @Test
    public void testXmlLoading() throws IOException, URISyntaxException {
        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_14251692001.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        try {
            Patientview patientRecord = XmlParser.parse(file);
            Assert.assertNotNull("A Patientview record should have been returned for the parser", patientRecord);
        } catch(JAXBException je) {
            Assert.fail("JAXBException: " + je.getMessage());
        }
    }

    @Test
    public void testDamagedXmlLoading() throws IOException, URISyntaxException {
        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/errors/1111111111_damaged.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        try {
            XmlParser.parse(file);
            Assert.fail("Should throw JAXBException");
        } catch(JAXBException je) {
            Assert.assertNotNull(je);
        }
    }

    @Test
    public void testSurveyXml() throws IOException, URISyntaxException {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource("data/xml/survey/survey_1.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        try {
            Survey survey = XmlParser.parseAny(file, Survey.class);
            Assert.assertNotNull("A Survey record should have been returned for the parser", survey);
        } catch(JAXBException je) {
            Assert.fail("JAXBException: " + je.getMessage());
        }
    }

    @Test
    public void testSurveyResponseXml() throws IOException, URISyntaxException {
        URL xmlPath = Thread.currentThread().getContextClassLoader()
                .getResource("data/xml/survey_response/survey_response_1.xml");
        File file = new File(xmlPath.toURI());
        Assert.assertTrue("Test file not loaded", file.exists());

        try {
            SurveyResponse surveyResponse = XmlParser.parseAny(file, SurveyResponse.class);
            Assert.assertNotNull("A SurveyResponse record should have been returned for the parser", surveyResponse);
        } catch(JAXBException je) {
            Assert.fail("JAXBException: " + je.getMessage());
        }
    }
}

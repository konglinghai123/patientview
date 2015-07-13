package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.SurveyResponseServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public class SurveyResponseServiceTest {

    User creator;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    QuestionOptionRepository questionOptionRepository;

    @Mock
    SurveyRepository surveyRepository;

    @Mock
    SurveyResponseRepository surveyResponseRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    SurveyResponseService surveyResponseService = new SurveyResponseServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAdd() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);
        survey.setId(1L);

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setUser(user);
        surveyResponse.setDate(new Date());
        surveyResponse.setSurvey(survey);

        Question question = new Question();
        question.setId(1L);

        QuestionOption questionOption = new QuestionOption();
        questionOption.setId(1L);

        QuestionAnswer questionAnswer = new QuestionAnswer();
        questionAnswer.setQuestionOption(questionOption);
        questionAnswer.setQuestion(question);
        surveyResponse.getQuestionAnswers().add(questionAnswer);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(questionOptionRepository.findOne(eq(questionOption.getId()))).thenReturn(questionOption);
        when(questionRepository.findOne(eq(question.getId()))).thenReturn(question);
        when(surveyRepository.findOne(eq(survey.getId()))).thenReturn(survey);

        surveyResponseService.add(user.getId(), surveyResponse);

        verify(surveyResponseRepository, Mockito.times(1)).save(any(SurveyResponse.class));
    }

    @Test
    public void testGetByUserIdAndType() throws ResourceNotFoundException {

        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Survey survey = new Survey();
        survey.setType(SurveyTypes.CROHNS_SYMPTOM_SCORE);

        SurveyResponse surveyResponse
                = new SurveyResponse(user, 1, ScoreSeverity.LOW, new Date(), SurveyResponseScoreTypes.SYMPTOM_SCORE);
        List<SurveyResponse> surveyResponses = new ArrayList<>();
        surveyResponses.add(surveyResponse);
        surveyResponse.setSurvey(survey);

        when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
        when(surveyResponseRepository.findByUserAndSurveyType(eq(user), eq(survey.getType())))
                .thenReturn(surveyResponses);
        List<SurveyResponse> returned = surveyResponseService.getByUserIdAndSurveyType(user.getId(), survey.getType());

        verify(surveyResponseRepository, Mockito.times(1)).findByUserAndSurveyType(eq(user), eq(survey.getType()));
        Assert.assertEquals("Should return 1 symptom score", 1, returned.size());
    }
}

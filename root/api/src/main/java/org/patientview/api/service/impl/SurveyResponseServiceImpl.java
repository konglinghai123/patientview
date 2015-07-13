package org.patientview.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.SurveyResponseService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Question;
import org.patientview.persistence.model.QuestionAnswer;
import org.patientview.persistence.model.QuestionOption;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.SurveyResponseScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.QuestionElementTypes;
import org.patientview.persistence.model.enums.QuestionTypes;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.model.enums.SurveyResponseScoreTypes;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.persistence.repository.QuestionOptionRepository;
import org.patientview.persistence.repository.QuestionRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.SurveyResponseRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@Service
public class SurveyResponseServiceImpl extends AbstractServiceImpl<SurveyResponseServiceImpl>
        implements SurveyResponseService {

    @Inject
    private QuestionRepository questionRepository;

    @Inject
    private QuestionOptionRepository questionOptionRepository;

    @Inject
    private SurveyRepository surveyRepository;

    @Inject
    private SurveyResponseRepository surveyResponseRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, SurveyResponse surveyResponse) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Survey survey = surveyRepository.findOne(surveyResponse.getSurvey().getId());
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        if (surveyResponse.getDate() == null) {
            throw new ResourceNotFoundException("Must include symptom score date");
        }

        SurveyResponse newSurveyResponse = new SurveyResponse();
        newSurveyResponse.setSurvey(survey);
        newSurveyResponse.setUser(user);
        newSurveyResponse.setDate(surveyResponse.getDate());

        for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
            QuestionAnswer newQuestionAnswer = new QuestionAnswer();
            newQuestionAnswer.setSurveyResponse(newSurveyResponse);
            boolean answer = false;

            if (questionAnswer.getQuestionOption() != null) {
                // if QuestionTypes.SINGLE_SELECT, will have question option
                QuestionOption questionOption
                        = questionOptionRepository.findOne(questionAnswer.getQuestionOption().getId());

                if (questionOption == null) {
                    throw new ResourceNotFoundException("Question option not found");
                }

                newQuestionAnswer.setQuestionOption(questionOption);
                answer = true;
            } else if (StringUtils.isNotEmpty(questionAnswer.getValue())) {
                // if QuestionTypes.SINGLE_SELECT_RANGE, will have value
                newQuestionAnswer.setValue(questionAnswer.getValue());
                answer = true;
            }

            if (answer) {
                Question question = questionRepository.findOne(questionAnswer.getQuestion().getId());
                if (question == null) {
                    throw new ResourceNotFoundException("Invalid question");
                }

                newQuestionAnswer.setQuestion(question);
                newSurveyResponse.getQuestionAnswers().add(newQuestionAnswer);
            }
        }

        if (newSurveyResponse.getQuestionAnswers().isEmpty()) {
            throw new ResourceNotFoundException("No valid answers");
        }

        if (survey.getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)
                || survey.getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.SYMPTOM_SCORE;
            Integer score = calculateScore(newSurveyResponse, type);

            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));
        } else if (survey.getType().equals(SurveyTypes.IBD_CONTROL)) {
            SurveyResponseScoreTypes type = SurveyResponseScoreTypes.IBD_CONTROL_EIGHT;
            Integer score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));

            type = SurveyResponseScoreTypes.IBD_CONTROL_VAS;
            score = calculateScore(newSurveyResponse, type);
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, type, score, calculateSeverity(newSurveyResponse, score)));
        } else {
            newSurveyResponse.getSurveyResponseScores().add(
                new SurveyResponseScore(newSurveyResponse, SurveyResponseScoreTypes.UNKNOWN, 0, ScoreSeverity.UNKNOWN));
        }

        surveyResponseRepository.save(newSurveyResponse);
    }

    private Integer calculateScore(SurveyResponse surveyResponse, SurveyResponseScoreTypes type) {
        Map<QuestionTypes, Integer> questionTypeScoreMap = new HashMap<>();
        for (QuestionAnswer questionAnswer : surveyResponse.getQuestionAnswers()) {
            if (questionAnswer.getQuestionOption() != null
                    && questionAnswer.getQuestionOption().getScore() != null
                    && questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null) {
                questionTypeScoreMap.put(
                    questionAnswer.getQuestion().getType(), questionAnswer.getQuestionOption().getScore());
            }

            // add scoring for ranged values
            if (questionAnswer.getQuestion() != null
                    && questionAnswer.getQuestion().getType() != null
                    && questionAnswer.getQuestion().getElementType().equals(QuestionElementTypes.SINGLE_SELECT_RANGE)
                    && questionAnswer.getValue() != null) {
                try {
                    questionTypeScoreMap.put(
                            questionAnswer.getQuestion().getType(), Integer.valueOf(questionAnswer.getValue()));
                } catch (NumberFormatException e) {
                    questionTypeScoreMap.put(questionAnswer.getQuestion().getType(), 0);
                }
            }
        }

        Integer score = 0;

        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            if (questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.OPEN_BOWELS);
            }
            if (questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.ABDOMINAL_PAIN);
            }
            if (questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.MASS_IN_TUMMY);
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION);
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING);
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_DAYTIME);
            }
            if (questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.NUMBER_OF_STOOLS_NIGHTTIME);
            }
            if (questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.TOILET_TIMING);
            }
            if (questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.PRESENT_BLOOD);
            }
            if (questionTypeScoreMap.get(QuestionTypes.COMPLICATION) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.COMPLICATION);
            }
            if (questionTypeScoreMap.get(QuestionTypes.FEELING) != null) {
                score += questionTypeScoreMap.get(QuestionTypes.FEELING);
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.IBD_CONTROL)) {
            if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_EIGHT)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_TWO_WEEKS);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_CONTROLLED_CURRENT_TREATMENT);
                } else {
                    if (questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT) != null) {
                        score += questionTypeScoreMap.get(QuestionTypes.IBD_NO_TREATMENT);
                    }
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_MISS_PLANNED_ACTIVITIES);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_WAKE_UP);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_SIGNIFICANT_PAIN);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_LACKING_ENERGY);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_FEEL_ANXIOUS);
                }
                if (questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_NEED_CHANGE);
                }
            } else if (type.equals(SurveyResponseScoreTypes.IBD_CONTROL_VAS)) {
                if (questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL) != null) {
                    score += questionTypeScoreMap.get(QuestionTypes.IBD_OVERALL_CONTROL);
                }
            }
        }

        return score;
    }

    // note: these are hardcoded
    private ScoreSeverity calculateSeverity(SurveyResponse surveyResponse, Integer score) {
        if (surveyResponse.getSurvey().getType().equals(SurveyTypes.CROHNS_SYMPTOM_SCORE)) {
            if (score != null) {
                if (score >= 16) {
                    return ScoreSeverity.HIGH;
                }
                if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                }
                if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        } else if (surveyResponse.getSurvey().getType().equals(SurveyTypes.COLITIS_SYMPTOM_SCORE)) {
            if (score != null) {
                if (score >= 10) {
                    return ScoreSeverity.HIGH;
                }
                if (score >= 4) {
                    return ScoreSeverity.MEDIUM;
                }
                if (score < 4) {
                    return ScoreSeverity.LOW;
                }
            }
        }

        return ScoreSeverity.UNKNOWN;
    }

    @Override
    public List<SurveyResponse> getByUserIdAndSurveyType(Long userId, SurveyTypes surveyType)
            throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }
        if (surveyType == null) {
            throw new ResourceNotFoundException("Must set survey type");
        }

        return surveyResponseRepository.findByUserAndSurveyType(user, surveyType);
    }

    @Override
    public SurveyResponse getSurveyResponse(Long userId, Long surveyResponseId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return surveyResponseRepository.findOne(surveyResponseId);
    }
}

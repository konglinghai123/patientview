alter table pv_question
  add column custom_question bool;
update pv_question
set custom_question = false;
alter table pv_question_answer
  add column question_text varchar(500);

insert into pv_survey (id, type, description) values (14917703, 'POS_S', '');
insert into pv_question_group (id, survey_id, text, display_order) values (14917710, 14917703, 'POS-S Questions', 0);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917730, 14917710, 'SINGLE_SELECT', 'RADIO', 'Pain', 'YSQ1', 0, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917791, 14917730, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917792, 14917730, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917793, 14917730, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917794, 14917730, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917795, 14917730, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917731, 14917710, 'SINGLE_SELECT', 'RADIO', 'Shortness of breath', 'YSQ2', 1, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917796, 14917731, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917797, 14917731, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917798, 14917731, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917799, 14917731, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917800, 14917731, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917732, 14917710, 'SINGLE_SELECT', 'RADIO', 'Weakness or lack of energy', 'YSQ3', 2, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917801, 14917732, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917802, 14917732, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917803, 14917732, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917804, 14917732, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917805, 14917732, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917733, 14917710, 'SINGLE_SELECT', 'RADIO', 'Nausea (feeling like you are about to be sick)', 'YSQ4', 3, true,
        false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917806, 14917733, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917807, 14917733, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917808, 14917733, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917809, 14917733, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917810, 14917733, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917734, 14917710, 'SINGLE_SELECT', 'RADIO', 'Vomiting (being sick)', 'YSQ5', 4, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917811, 14917734, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917812, 14917734, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917813, 14917734, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917814, 14917734, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917815, 14917734, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917735, 14917710, 'SINGLE_SELECT', 'RADIO', 'Poor appetite', 'YSQ6', 6, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917816, 14917735, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917817, 14917735, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917818, 14917735, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917819, 14917735, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917820, 14917735, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917736, 14917710, 'SINGLE_SELECT', 'RADIO', 'Constipation', 'YSQ7', 6, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917821, 14917736, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917822, 14917736, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917823, 14917736, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917824, 14917736, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917825, 14917736, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917737, 14917710, 'SINGLE_SELECT', 'RADIO', 'Mouth problems', 'YSQ8', 7, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917826, 14917737, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917827, 14917737, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917828, 14917737, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917829, 14917737, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917830, 14917737, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917738, 14917710, 'SINGLE_SELECT', 'RADIO', 'Drowsiness', 'YSQ9', 8, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917831, 14917738, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917832, 14917738, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917833, 14917738, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917834, 14917738, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917835, 14917738, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917739, 14917710, 'SINGLE_SELECT', 'RADIO', 'Poor mobility', 'YSQ10', 9, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917836, 14917739, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917837, 14917739, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917838, 14917739, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917839, 14917739, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917840, 14917739, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917740, 14917710, 'SINGLE_SELECT', 'RADIO', 'Itching', 'YSQ11', 10, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917841, 14917740, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917842, 14917740, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917843, 14917740, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917844, 14917740, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917845, 14917740, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917741, 14917710, 'SINGLE_SELECT', 'RADIO', 'Difficulty sleeping', 'YSQ12', 11, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917846, 14917741, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917847, 14917741, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917848, 14917741, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917849, 14917741, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917850, 14917741, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values
  (14917742, 14917710, 'SINGLE_SELECT', 'RADIO', 'Restless legs or difficulty keeping legs still', 'YSQ13', 12, true,
   false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917851, 14917742, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917852, 14917742, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917853, 14917742, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917854, 14917742, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917855, 14917742, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917743, 14917710, 'SINGLE_SELECT', 'RADIO', 'Feeling anxious', 'YSQ14', 13, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917856, 14917743, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917857, 14917743, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917858, 14917743, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917859, 14917743, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917860, 14917743, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917744, 14917710, 'SINGLE_SELECT', 'RADIO', 'Feeling depressed', 'YSQ15', 14, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917861, 14917744, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917862, 14917744, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917863, 14917744, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917864, 14917744, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917865, 14917744, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917745, 14917710, 'SINGLE_SELECT', 'RADIO', 'Changes in skin', 'YSQ16', 15, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917866, 14917745, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917867, 14917745, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917868, 14917745, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917869, 14917745, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917870, 14917745, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917746, 14917710, 'SINGLE_SELECT', 'RADIO', 'Diarrhoea', 'YSQ17', 16, true, false);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917871, 14917746, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917872, 14917746, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917873, 14917746, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917874, 14917746, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917875, 14917746, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917747, 14917710, 'SINGLE_SELECT', 'RADIO', '', 'YSQ18', 17, false, true);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917876, 14917747, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917877, 14917747, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917878, 14917747, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917879, 14917747, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917880, 14917747, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917748, 14917710, 'SINGLE_SELECT', 'RADIO', '', 'YSQ19', 18, false, true);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917881, 14917748, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917882, 14917748, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917883, 14917748, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917884, 14917748, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917885, 14917748, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917749, 14917710, 'SINGLE_SELECT', 'RADIO', '', 'YSQ20', 19, false, true);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917886, 14917749, 'Not at all', 0, 'No effect', 0, 0);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917887, 14917749, 'Slightly', 1, 'but not bothered to be rid of it', 1, 1);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917888, 14917749, 'Moderately', 2, 'limits on some activity or concentration', 2, 2);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917889, 14917749, 'Severely', 3, 'activities or concentration markedly affected', 3, 3);
insert into pv_question_option (id, question_id, text, type, description, display_order, score)
values (14917890, 14917749, 'Overwhelmingly', 4, 'unable to think of anything else', 4, 4);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values
  (14917750, 14917710, 'SINGLE_SELECT', 'TEXT', 'Which symptom has affected you the most?', 'YSQ21', 20, false, false);
insert into pv_question (id, question_group_id, element_type, html_type, text, type, display_order, required, custom_question)
values (14917751, 14917710, 'SINGLE_SELECT', 'TEXT', 'Which symptom has improved the most?', 'YSQ22', 21, false, false);

insert into public.pv_survey (id, type, description) values (14917704, 'EQ5D5L', '');
insert into public.pv_question_group (id, survey_id, text, display_order)
values (14917711, 14917704, 'EQ-5D-5L Questions', 0);
INSERT INTO public.pv_question VALUES
  (14917752, 14917711, 'SINGLE_SELECT', 'RADIO', 'Mobility', 'YOHQ1', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, true,
   false);
INSERT INTO public.pv_question_option
VALUES (14917891, 14917752, 'I have no problems in walking about', '1', NULL, 0, 1);
INSERT INTO public.pv_question_option
VALUES (14917892, 14917752, 'I have slight problems in walking about', '2', NULL, 1, 2);
INSERT INTO public.pv_question_option
VALUES (14917893, 14917752, 'I have moderate problems in walking about', '3', NULL, 2, 3);
INSERT INTO public.pv_question_option
VALUES (14917894, 14917752, 'I have severe problems in walking about', '4', NULL, 3, 4);
INSERT INTO public.pv_question_option VALUES (14917895, 14917752, 'I am unable to walk about', '5', NULL, 4, 5);
INSERT INTO public.pv_question VALUES
  (14917753, 14917711, 'SINGLE_SELECT', 'RADIO', 'Self-Care', 'YOHQ2', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL,
   true, false);
INSERT INTO public.pv_question_option
VALUES (14917896, 14917753, 'I have no problems washing or dressing myself', '1', NULL, 0, 1);
INSERT INTO public.pv_question_option
VALUES (14917897, 14917753, 'I have slight problems washing or dressing myself', '2', NULL, 1, 2);
INSERT INTO public.pv_question_option
VALUES (14917898, 14917753, 'I have moderate problems washing or dressing myself', '3', NULL, 2, 3);
INSERT INTO public.pv_question_option
VALUES (14917899, 14917753, 'I have severe problems washing or dressing myself', '4', NULL, 3, 4);
INSERT INTO public.pv_question_option
VALUES (14917900, 14917753, 'I am unable to wash or dress myself', '5', NULL, 4, 5);
INSERT INTO public.pv_question VALUES
  (14917754, 14917711, 'SINGLE_SELECT', 'RADIO', 'Usual Activities (e.g. work, study, housework, family or leisure activities)', 'YOHQ3', NULL, NULL, 2, NULL, NULL, NULL, NULL,
   NULL, true, false);
INSERT INTO public.pv_question_option
VALUES (14917901, 14917754, 'I have no problems doing my usual activities', '1', NULL, 0, 1);
INSERT INTO public.pv_question_option
VALUES (14917902, 14917754, 'I have slight problems doing my usual activities', '2', NULL, 1, 2);
INSERT INTO public.pv_question_option
VALUES (14917903, 14917754, 'I have moderate problems doing my usual activities', '3', NULL, 2, 3);
INSERT INTO public.pv_question_option
VALUES (14917904, 14917754, 'I have severe problems doing my usual activities', '4', NULL, 3, 4);
INSERT INTO public.pv_question_option
VALUES (14917905, 14917754, 'I am unable to do my usual activities', '5', NULL, 4, 5);
INSERT INTO public.pv_question VALUES
  (14917755, 14917711, 'SINGLE_SELECT', 'RADIO', 'Pain / Discomfort', 'YOHQ4', NULL, NULL, 3, NULL, NULL, NULL, NULL,
   NULL, true, false);
INSERT INTO public.pv_question_option VALUES (14917906, 14917755, 'I have no pain or discomfort', '1', NULL, 0, 1);
INSERT INTO public.pv_question_option VALUES (14917907, 14917755, 'I have slight pain or discomfort', '2', NULL, 1, 2);
INSERT INTO public.pv_question_option
VALUES (14917908, 14917755, 'I have moderate pain or discomfort', '3', NULL, 2, 3);
INSERT INTO public.pv_question_option VALUES (14917909, 14917755, 'I have severe pain or discomfort', '4', NULL, 3, 4);
INSERT INTO public.pv_question_option VALUES (14917910, 14917755, 'I have extreme pain or discomfort', '5', NULL, 4, 5);
INSERT INTO public.pv_question VALUES
  (14917756, 14917711, 'SINGLE_SELECT', 'RADIO', 'Anxiety / Depression', 'YOHQ5', NULL, NULL, 4, NULL, NULL, NULL, NULL,
   NULL, true, false);
INSERT INTO public.pv_question_option VALUES (14917911, 14917756, 'I am not anxious or depressed', '1', NULL, 0, 1);
INSERT INTO public.pv_question_option
VALUES (14917912, 14917756, 'I am slightly anxious or depressed', '2', NULL, 1, 2);
INSERT INTO public.pv_question_option
VALUES (14917913, 14917756, 'I am moderately anxious or depressed', '3', NULL, 2, 3);
INSERT INTO public.pv_question_option
VALUES (14917914, 14917756, 'I am severely anxious or depressed', '4', NULL, 3, 4);
INSERT INTO public.pv_question_option
VALUES (14917915, 14917756, 'I am extremely anxious or depressed', '5', NULL, 4, 5);
INSERT INTO public.pv_question VALUES
  (14917757, 14917711, 'SINGLE_SELECT_RANGE', 'SLIDER', 'Your Health from 0 (worst) to 100 (best)', 'YOHQ6', NULL, NULL,
             5, 0, 100, 'The worst health you can imagine', 'The best health you can imagine', NULL, false, false);

INSERT INTO pv_feature (id, feature_name, description, creation_date)
values (15, 'OPT_EPRO', 'OPT ePRO Health Surveys', '2018-12-07 10:15:00.000');
INSERT INTO pv_feature_feature_type (id, feature_id, type_id) values (21, 15, 14);

CREATE TABLE IF NOT EXISTS public.pv_survey_unit (
  Id              BIGINT                                      NOT NULL,
  unit_id         BIGINT REFERENCES public.pv_group (id)      NOT NULL,
  survey_group_id BIGINT REFERENCES public.pv_group (id)      NOT NULL,
  PRIMARY KEY (Id)
);

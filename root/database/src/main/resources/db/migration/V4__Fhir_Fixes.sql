ALTER TABLE observation_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE observation_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE condition_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE condition_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE encounter_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE encounter_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE practitioner_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE practitioner_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE patient_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE patient_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE medication_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE medication_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE medicationstatement_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE medicationstatement_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE diagnosticreport_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE diagnosticreport_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE documentreference_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE documentreference_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE allergyintolerance_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE allergyintolerance_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE adversereaction_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE adversereaction_sort ALTER COLUMN upper DROP NOT NULL;
ALTER TABLE substance_sort ALTER COLUMN lower DROP NOT NULL;
ALTER TABLE substance_sort ALTER COLUMN upper DROP NOT NULL;

create INDEX content_subject_display on observation (( content ->'subject' ->>'display'));
create INDEX content_name_text on observation (( content-> 'name' ->> 'text'));
create INDEX documentreference_content_subject_display on documentreference (( content ->'subject' ->>'display'));
create INDEX medicationstatement_content_patient_display on medicationstatement (( content ->'patient' ->>'display'));


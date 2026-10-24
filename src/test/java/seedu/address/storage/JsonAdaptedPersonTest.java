package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Attendance;
import seedu.address.model.person.ClassName;
import seedu.address.model.person.Email;
import seedu.address.model.person.Grade;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R,achel";
    private static final String INVALID_CLASS_NAME = "A1&";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String MESSAGE_INVALID_ATTENDANCE = "Attendance record has an invalid date or status.";
    private static final String MESSAGE_INVALID_GRADE = "Grade assessment name or score is invalid.";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_CLASS_NAME = BENSON.getClassName().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().get().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_personWithoutEmail_returnsPersonWithoutEmail() throws Exception {
        Person personWithoutEmail = new PersonBuilder(BENSON).withoutEmail().build();
        JsonAdaptedPerson person = new JsonAdaptedPerson(personWithoutEmail);
        assertEquals(personWithoutEmail, person.toModelType());
        assertTrue(person.toModelType().getEmail().isEmpty());
    }

    @Test
    public void toModelType_nameWithAccentsAndSymbols_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson("Jose\u0301 Müller (Joe) #2", "T11/A", null,
                List.of(), List.of(), List.of());
        Person modelPerson = person.toModelType();
        assertEquals("José Müller (Joe) #2", modelPerson.getName().fullName);
        assertEquals("T11/A", modelPerson.getClassName().value);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(INVALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_tooLongName_throwsIllegalValueException() {
        String tooLongName = "a".repeat(Name.MAX_LENGTH + 1);
        JsonAdaptedPerson person = new JsonAdaptedPerson(tooLongName, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, Name.MESSAGE_TOO_LONG, person::toModelType);
    }

    @Test
    public void toModelType_emptyName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson("", VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, Name.MESSAGE_EMPTY, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
            new JsonAdaptedPerson(null, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidClassName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, INVALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, ClassName.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_tooLongClassName_throwsIllegalValueException() {
        String tooLongClassName = "a".repeat(ClassName.MAX_LENGTH + 1);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, tooLongClassName, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, ClassName.MESSAGE_TOO_LONG, person::toModelType);
    }

    @Test
    public void toModelType_emptyClassName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
            new JsonAdaptedPerson(VALID_NAME, "", VALID_EMAIL, VALID_TAGS, List.of(), List.of());
        assertThrows(IllegalValueException.class, ClassName.MESSAGE_EMPTY, person::toModelType);
    }

    @Test
    public void toModelType_nullClassName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ClassName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, INVALID_EMAIL, VALID_TAGS,
            List.of(), List.of());
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_tooLongEmail_throwsIllegalValueExceptionWithTooLongMessage() {
        String tooLongEmail = "a".repeat(Email.MAX_LENGTH + 1);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, tooLongEmail, VALID_TAGS,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, Email.MESSAGE_TOO_LONG, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_returnsPersonWithoutEmail() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, null, VALID_TAGS,
            List.of(), List.of());
        assertTrue(person.toModelType().getEmail().isEmpty());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, invalidTags,
            List.of(), List.of());
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_tooLongTag_throwsIllegalValueException() {
        List<JsonAdaptedTag> tooLongTags = List.of(new JsonAdaptedTag("a".repeat(Tag.MAX_LENGTH + 1)));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, tooLongTags, List.of(), List.of());
        assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_tagsWithSpacesAndMixedCasing_returnsPersonWithTagsAsTyped() throws Exception {
        Person personWithGroups =
                new PersonBuilder(BENSON).withTags("Group A", "team-1_Alpha", "gROUP b").build();

        Person restoredPerson = new JsonAdaptedPerson(personWithGroups).toModelType();

        assertEquals(personWithGroups, restoredPerson);
        Set<String> restoredTagNames = restoredPerson.getTags().stream()
                .map(tag -> tag.tagName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("Group A", "team-1_Alpha", "gROUP b"), restoredTagNames);
    }

    @Test
    public void toModelType_tagWithExtraSpacesInFile_returnsNormalizedTag() throws Exception {
        List<JsonAdaptedTag> tagsWithExtraSpaces = List.of(new JsonAdaptedTag("  Group    A "));
        JsonAdaptedPerson person =
            new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL,
            tagsWithExtraSpaces, List.of(), List.of());

        Set<Tag> tags = person.toModelType().getTags();

        assertEquals(1, tags.size());
        assertEquals("Group A", tags.iterator().next().tagName);
    }

    @Test
    public void toModelType_personWithRecords_roundTripKeepsRecords() throws Exception {
        Attendance record = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Grade grade = new Grade("Assignment 1", new BigDecimal("97.5"));

        // EP: a contact with attendance records only
        Person withAttendance = BENSON.withAttendance(record);
        assertEquals(withAttendance, new JsonAdaptedPerson(withAttendance).toModelType());

        // EP: a contact with grades only
        Person withGrade = BENSON.withGrade(grade);
        assertEquals(withGrade, new JsonAdaptedPerson(withGrade).toModelType());

        // EP: a contact with both, where the records are kept in the order they were recorded
        Attendance laterRecord = new Attendance(LocalDate.of(2026, 9, 10), Attendance.Status.LATE);
        Grade otherGrade = new Grade("Exam", new BigDecimal("60"));
        Person withBoth = BENSON.withAttendance(record).withAttendance(laterRecord).withGrade(grade)
                .withGrade(otherGrade);
        Person restored = new JsonAdaptedPerson(withBoth).toModelType();
        assertEquals(withBoth, restored);
        assertEquals(List.of(record, laterRecord), restored.getAttendanceRecords());
        assertEquals(List.of(grade, otherGrade), restored.getGradeRecords());
    }

    @Test
    public void toModelType_noRecords_returnsPersonWithEmptyRecords() throws Exception {
        // EP: the lists are missing from the file
        JsonAdaptedPerson missingLists = new JsonAdaptedPerson(
                VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, null, null);
        assertTrue(missingLists.toModelType().getAttendanceRecords().isEmpty());
        assertTrue(missingLists.toModelType().getGradeRecords().isEmpty());

        // EP: the lists are empty
        JsonAdaptedPerson emptyLists = new JsonAdaptedPerson(
                VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(), List.of());
        assertTrue(emptyLists.toModelType().getAttendanceRecords().isEmpty());
        assertTrue(emptyLists.toModelType().getGradeRecords().isEmpty());
    }

    @Test
    public void toModelType_duplicateAttendanceDates_throwsIllegalValueException() {
        // EP: two records on the same date with different statuses
        assertDuplicateAttendanceDate(List.of(new JsonAdaptedAttendance("2026-09-16", "present"),
                new JsonAdaptedAttendance("2026-09-16", "absent")));

        // EP: two records on the same date with the same status
        assertDuplicateAttendanceDate(List.of(new JsonAdaptedAttendance("2026-09-16", "present"),
                new JsonAdaptedAttendance("2026-09-16", "present")));

        // EP: the repeated date is not next to the first record with that date
        assertDuplicateAttendanceDate(List.of(new JsonAdaptedAttendance("2026-09-16", "present"),
                new JsonAdaptedAttendance("2026-09-17", "late"),
                new JsonAdaptedAttendance("2026-09-16", "absent")));
    }

    @Test
    public void toModelType_duplicateAssessments_throwsIllegalValueException() {
        // EP: two grades for the same assessment with different scores
        assertDuplicateAssessment(List.of(new JsonAdaptedGrade("Quiz 1", BigDecimal.valueOf(80)),
                new JsonAdaptedGrade("Quiz 1", BigDecimal.valueOf(20))));

        // EP: two grades for the same assessment in another case
        assertDuplicateAssessment(List.of(new JsonAdaptedGrade("Quiz 1", BigDecimal.valueOf(80)),
                new JsonAdaptedGrade("quiz 1", BigDecimal.valueOf(20))));

        // EP: two grades for the same assessment with extra spaces
        assertDuplicateAssessment(List.of(new JsonAdaptedGrade("Quiz 1", BigDecimal.valueOf(80)),
                new JsonAdaptedGrade("  Quiz    1 ", BigDecimal.valueOf(20))));

        // EP: the repeated assessment is not next to the first grade for it
        assertDuplicateAssessment(List.of(new JsonAdaptedGrade("Quiz 1", BigDecimal.valueOf(80)),
                new JsonAdaptedGrade("Exam", BigDecimal.valueOf(50)),
                new JsonAdaptedGrade("QUIZ 1", BigDecimal.valueOf(20))));
    }

    @Test
    public void toModelType_nullTag_throwsIllegalValueException() {
        // EP: a null entry in the tags
        List<JsonAdaptedTag> tagsWithNull = new ArrayList<>(VALID_TAGS);
        tagsWithNull.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, tagsWithNull,
                List.of(), List.of());
        String expectedMessage = String.format(JsonAdaptedPerson.EMPTY_ENTRY_MESSAGE_FORMAT, "tags");
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullRecord_throwsIllegalValueException() {
        // EP: a null entry in the attendance records
        List<JsonAdaptedAttendance> attendanceWithNull = new ArrayList<>();
        attendanceWithNull.add(null);
        JsonAdaptedPerson withNullAttendance = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL,
                VALID_TAGS, attendanceWithNull, List.of());
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedPerson.EMPTY_ENTRY_MESSAGE_FORMAT, "attendanceRecords"),
                withNullAttendance::toModelType);

        // EP: a null entry in the grade records
        List<JsonAdaptedGrade> gradesWithNull = new ArrayList<>();
        gradesWithNull.add(null);
        JsonAdaptedPerson withNullGrade = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL,
                VALID_TAGS, List.of(), gradesWithNull);
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedPerson.EMPTY_ENTRY_MESSAGE_FORMAT, "gradeRecords"),
                withNullGrade::toModelType);

        // EP: a null entry after a valid record
        List<JsonAdaptedAttendance> validThenNull = new ArrayList<>();
        validThenNull.add(new JsonAdaptedAttendance("2026-09-16", "present"));
        validThenNull.add(null);
        assertThrows(IllegalValueException.class,
                String.format(JsonAdaptedPerson.EMPTY_ENTRY_MESSAGE_FORMAT, "attendanceRecords"),
                new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, validThenNull,
                        List.of())::toModelType);
    }

    @Test
    public void toModelType_invalidRecord_throwsIllegalValueException() {
        // EP: an attendance record with a date that does not exist
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_ATTENDANCE, personWithAttendance(
                new JsonAdaptedAttendance("2026-02-31", "present"))::toModelType);

        // EP: an attendance record with a status that does not exist
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_ATTENDANCE, personWithAttendance(
                new JsonAdaptedAttendance("2026-09-16", "sick"))::toModelType);

        // EP: an attendance record without a date or without a status
        assertThrows(IllegalValueException.class, "Attendance date is missing.", personWithAttendance(
                new JsonAdaptedAttendance(null, "present"))::toModelType);
        assertThrows(IllegalValueException.class, "Attendance status is missing.", personWithAttendance(
                new JsonAdaptedAttendance("2026-09-16", null))::toModelType);

        // EP: a grade with an assessment name that is not valid
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_GRADE, personWithGrade(
                new JsonAdaptedGrade("Exam,1", BigDecimal.TEN))::toModelType);

        // EP: a grade with a score that is not valid
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_GRADE, personWithGrade(
                new JsonAdaptedGrade("Exam", new BigDecimal("101")))::toModelType);

        // EP: a grade without an assessment name or without a score
        assertThrows(IllegalValueException.class, "Grade assessment name is missing.", personWithGrade(
                new JsonAdaptedGrade(null, BigDecimal.TEN))::toModelType);
        assertThrows(IllegalValueException.class, "Grade score is missing.", personWithGrade(
                new JsonAdaptedGrade("Exam", null))::toModelType);
    }

    @Test
    public void toModelType_validRecordTogetherWithInvalidRecord_throwsIllegalValueException() {
        // EP: a valid attendance record followed by an invalid one -> the invalid one is reported
        List<JsonAdaptedAttendance> validThenInvalidAttendance = List.of(
                new JsonAdaptedAttendance("2026-09-16", "present"),
                new JsonAdaptedAttendance("2026-02-31", "present"));
        JsonAdaptedPerson attendance = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL,
                VALID_TAGS, validThenInvalidAttendance, List.of());
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_ATTENDANCE, attendance::toModelType);

        // EP: a valid grade followed by an invalid one -> the invalid one is reported
        List<JsonAdaptedGrade> validThenInvalidGrade = List.of(
                new JsonAdaptedGrade("Quiz 1", BigDecimal.TEN),
                new JsonAdaptedGrade("Exam,1", BigDecimal.TEN));
        JsonAdaptedPerson grade = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
                List.of(), validThenInvalidGrade);
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_GRADE, grade::toModelType);
    }

    @Test
    public void toModelType_twoProblems_reportsTheOneThatIsCheckedFirst() {
        JsonAdaptedAttendance invalidAttendance = new JsonAdaptedAttendance("2026-02-31", "present");
        JsonAdaptedGrade invalidGrade = new JsonAdaptedGrade("Exam,1", BigDecimal.TEN);

        // EP: an invalid attendance record and an invalid grade -> the attendance record
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_ATTENDANCE, new JsonAdaptedPerson(VALID_NAME,
                VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(invalidAttendance),
                List.of(invalidGrade))::toModelType);

        // EP: an invalid grade and an invalid name -> the grade, as the records are read before the name
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_GRADE, new JsonAdaptedPerson(INVALID_NAME,
                VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(), List.of(invalidGrade))::toModelType);

        // EP: a duplicate date and an invalid grade -> the invalid grade, as each record is read first
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_GRADE, new JsonAdaptedPerson(VALID_NAME,
                VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
                List.of(new JsonAdaptedAttendance("2026-09-16", "present"),
                        new JsonAdaptedAttendance("2026-09-16", "absent")),
                List.of(invalidGrade))::toModelType);
    }

    private static JsonAdaptedPerson personWithAttendance(JsonAdaptedAttendance record) {
        return new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(record),
                List.of());
    }

    private static JsonAdaptedPerson personWithGrade(JsonAdaptedGrade grade) {
        return new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS, List.of(),
                List.of(grade));
    }

    private static void assertDuplicateAttendanceDate(List<JsonAdaptedAttendance> records) {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
                records, List.of());
        assertThrows(IllegalValueException.class, Person.MESSAGE_DUPLICATE_ATTENDANCE_DATE, person::toModelType);
    }

    private static void assertDuplicateAssessment(List<JsonAdaptedGrade> records) {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_CLASS_NAME, VALID_EMAIL, VALID_TAGS,
                List.of(), records);
        assertThrows(IllegalValueException.class, Person.MESSAGE_DUPLICATE_ASSESSMENT, person::toModelType);
    }

}

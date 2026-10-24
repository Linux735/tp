package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_CLASS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_CLASS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_validAttendanceRecords_keepsRecordsInOrder() {
        Person person = new PersonBuilder().build();
        Attendance first = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Attendance second = new Attendance(LocalDate.of(2026, 9, 17), Attendance.Status.ABSENT);
        Attendance third = new Attendance(LocalDate.of(2026, 9, 18), Attendance.Status.LATE);

        // EP: no record
        assertTrue(new Person(person.getName(), person.getClassName(), person.getEmail(), person.getTags(),
                List.of()).getAttendanceRecords().isEmpty());

        // EP: one record (boundary)
        assertEquals(List.of(first), new Person(person.getName(), person.getClassName(), person.getEmail(),
                person.getTags(), List.of(first)).getAttendanceRecords());

        // EP: several records on different dates, which are kept in the order they were given
        assertEquals(List.of(second, third, first), new Person(person.getName(), person.getClassName(),
                person.getEmail(), person.getTags(), List.of(second, third, first)).getAttendanceRecords());
    }

    @Test
    public void constructor_duplicateAttendanceDates_throwsIllegalArgumentException() {
        Person person = new PersonBuilder().build();
        LocalDate date = LocalDate.of(2026, 9, 16);
        LocalDate otherDate = LocalDate.of(2026, 9, 17);

        // EP: two records on the same date with different statuses
        assertDuplicateAttendanceDate(person, List.of(
                new Attendance(date, Attendance.Status.PRESENT), new Attendance(date, Attendance.Status.ABSENT)));

        // EP: two records on the same date with the same status
        assertDuplicateAttendanceDate(person, List.of(
                new Attendance(date, Attendance.Status.PRESENT), new Attendance(date, Attendance.Status.PRESENT)));

        // EP: the repeated date is the last record, after a record on another date
        assertDuplicateAttendanceDate(person, List.of(
                new Attendance(date, Attendance.Status.PRESENT),
                new Attendance(otherDate, Attendance.Status.PRESENT),
                new Attendance(date, Attendance.Status.LATE)));
    }

    @Test
    public void constructor_validGradeRecords_keepsRecordsInOrder() {
        Person person = new PersonBuilder().build();
        Grade first = new Grade("Quiz 1", BigDecimal.valueOf(80));
        Grade second = new Grade("Quiz 2", BigDecimal.valueOf(80));
        Grade third = new Grade("Exam", BigDecimal.valueOf(60));

        // EP: no record
        assertTrue(new Person(person.getName(), person.getClassName(), person.getEmail(), person.getTags(),
                List.of(), List.of()).getGradeRecords().isEmpty());

        // EP: one record (boundary)
        assertEquals(List.of(first), new Person(person.getName(), person.getClassName(), person.getEmail(),
                person.getTags(), List.of(), List.of(first)).getGradeRecords());

        // EP: several records for different assessments, which are kept in the order they were given
        assertEquals(List.of(second, third, first), new Person(person.getName(), person.getClassName(),
                person.getEmail(), person.getTags(), List.of(), List.of(second, third, first)).getGradeRecords());
    }

    @Test
    public void constructor_duplicateAssessments_throwsIllegalArgumentException() {
        Person person = new PersonBuilder().build();
        Grade quiz = new Grade("Quiz 1", BigDecimal.valueOf(80));

        // EP: the same assessment with a different score
        assertDuplicateAssessment(person, List.of(quiz, new Grade("Quiz 1", BigDecimal.valueOf(20))));

        // EP: the same assessment with the same score
        assertDuplicateAssessment(person, List.of(quiz, new Grade("Quiz 1", BigDecimal.valueOf(80))));

        // EP: the same assessment in another case
        assertDuplicateAssessment(person, List.of(quiz, new Grade("quiz 1", BigDecimal.valueOf(20))));

        // EP: the same assessment with extra spaces
        assertDuplicateAssessment(person, List.of(quiz, new Grade("  Quiz    1 ", BigDecimal.valueOf(20))));

        // EP: the same assessment with a letter written in full width
        assertDuplicateAssessment(person, List.of(quiz, new Grade("Ｑuiz 1", BigDecimal.valueOf(20))));

        // EP: the repeated assessment is the last record, after a different assessment
        assertDuplicateAssessment(person, List.of(quiz, new Grade("Exam", BigDecimal.TEN),
                new Grade("QUIZ 1", BigDecimal.ONE)));
    }

    @Test
    public void constructor_nullRecords_throwsNullPointerException() {
        Person person = new PersonBuilder().build();

        // EP: null attendance records
        assertThrows(NullPointerException.class, () -> new Person(person.getName(), person.getClassName(),
                person.getEmail(), person.getTags(), null, List.of()));

        // EP: null grade records
        assertThrows(NullPointerException.class, () -> new Person(person.getName(), person.getClassName(),
                person.getEmail(), person.getTags(), List.of(), null));
    }

    @Test
    public void constructor_recordListsChangedAfterwards_personIsNotAffected() {
        Person person = new PersonBuilder().build();
        List<Attendance> attendanceRecords = new ArrayList<>();
        List<Grade> gradeRecords = new ArrayList<>();

        Person withRecords = new Person(person.getName(), person.getClassName(), person.getEmail(),
                person.getTags(), attendanceRecords, gradeRecords);
        attendanceRecords.add(new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT));
        gradeRecords.add(new Grade("Quiz 1", BigDecimal.TEN));

        // EP: the lists that were given are changed after the person is created
        assertTrue(withRecords.getAttendanceRecords().isEmpty());
        assertTrue(withRecords.getGradeRecords().isEmpty());
    }

    @Test
    public void hasUniqueAttendanceDates_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Person.hasUniqueAttendanceDates(null));
    }

    @Test
    public void hasUniqueAttendanceDates_variousLists_returnsExpected() {
        Attendance first = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Attendance second = new Attendance(LocalDate.of(2026, 9, 17), Attendance.Status.PRESENT);

        // EP: no record, and one record (boundaries)
        assertTrue(Person.hasUniqueAttendanceDates(List.of()));
        assertTrue(Person.hasUniqueAttendanceDates(List.of(first)));

        // EP: records on different dates
        assertTrue(Person.hasUniqueAttendanceDates(List.of(first, second)));

        // EP: two records on the same date, with the same or a different status
        assertFalse(Person.hasUniqueAttendanceDates(List.of(first, first)));
        assertFalse(Person.hasUniqueAttendanceDates(List.of(first,
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT))));

        // EP: the repeated date is not next to the first record with that date
        assertFalse(Person.hasUniqueAttendanceDates(List.of(first, second, first)));
    }

    @Test
    public void hasUniqueAssessments_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Person.hasUniqueAssessments(null));
    }

    @Test
    public void hasUniqueAssessments_variousLists_returnsExpected() {
        Grade quiz = new Grade("Quiz 1", BigDecimal.TEN);
        Grade exam = new Grade("Exam", BigDecimal.TEN);

        // EP: no record, and one record (boundaries)
        assertTrue(Person.hasUniqueAssessments(List.of()));
        assertTrue(Person.hasUniqueAssessments(List.of(quiz)));

        // EP: records for different assessments, including names that only differ by a space
        assertTrue(Person.hasUniqueAssessments(List.of(quiz, exam)));
        assertTrue(Person.hasUniqueAssessments(List.of(quiz, new Grade("Quiz1", BigDecimal.TEN))));

        // EP: two records for the same assessment, with the same score or a different score
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, quiz)));
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, new Grade("Quiz 1", BigDecimal.ONE))));

        // EP: two records for the same assessment in another case, with extra spaces, or with a full-width letter
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, new Grade("QUIZ 1", BigDecimal.ONE))));
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, new Grade(" quiz   1 ", BigDecimal.ONE))));
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, new Grade("Ｑuiz 1", BigDecimal.ONE))));

        // EP: the repeated assessment is not next to the first record for it
        assertFalse(Person.hasUniqueAssessments(List.of(quiz, exam, new Grade("quiz 1", BigDecimal.ONE))));
    }

    private static void assertDuplicateAttendanceDate(Person person, List<Attendance> records) {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_DUPLICATE_ATTENDANCE_DATE, () ->
                new Person(person.getName(), person.getClassName(), person.getEmail(), person.getTags(), records));
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_DUPLICATE_ATTENDANCE_DATE, () ->
                new Person(person.getName(), person.getClassName(), person.getEmail(), person.getTags(), records,
                        List.of()));
    }

    private static void assertDuplicateAssessment(Person person, List<Grade> records) {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_DUPLICATE_ASSESSMENT, () ->
                new Person(person.getName(), person.getClassName(), person.getEmail(), person.getTags(),
                        List.of(), records));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void hasTag_nullTag_throwsNullPointerException() {
        Person person = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () -> person.hasTag(null));
    }

    @Test
    public void hasTag_tagPresent_returnsTrue() {
        Person person = new PersonBuilder().withTags("Group A", "team-1").build();
        assertTrue(person.hasTag(new Tag("team-1")));
    }

    @Test
    public void hasTag_tagPresentWithDifferentCasingAndSpacing_returnsTrue() {
        Person person = new PersonBuilder().withTags("Group A", "team-1").build();
        assertTrue(person.hasTag(new Tag("  gROUP   a ")));
    }

    @Test
    public void hasTag_tagAbsent_returnsFalse() {
        Person person = new PersonBuilder().withTags("Group A", "team-1").build();
        assertFalse(person.hasTag(new Tag("Group B")));
    }

    @Test
    public void hasTag_personWithoutTags_returnsFalse() {
        Person person = new PersonBuilder().withTags().build();
        assertFalse(person.hasTag(new Tag("Group A")));
    }

    @Test
    public void withoutTag_nullTag_throwsNullPointerException() {
        Person person = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () -> person.withoutTag(null));
    }

    @Test
    public void withoutTag_tagPresent_removesOnlyThatTag() {
        Person person = new PersonBuilder().withTags("Group A", "Group B").build();

        Person untagged = person.withoutTag(new Tag("Group B"));

        assertEquals(new PersonBuilder(person).withTags("Group A").build(), untagged);
    }

    @Test
    public void withoutTag_tagPresentWithDifferentCasingAndSpacing_removesTag() {
        Person person = new PersonBuilder().withTags("Group A").build();

        Person untagged = person.withoutTag(new Tag("  gROUP   a "));

        assertTrue(untagged.getTags().isEmpty());
    }

    @Test
    public void withoutTag_tagAbsent_keepsTheSameTags() {
        Person person = new PersonBuilder().withTags("Group A").build();

        Person untagged = person.withoutTag(new Tag("Group B"));

        assertEquals(person.getTags(), untagged.getTags());
    }

    @Test
    public void withoutTag_tagPresent_doesNotModifyOriginalPerson() {
        Person person = new PersonBuilder().withTags("Group A").build();

        person.withoutTag(new Tag("Group A"));

        assertEquals(Set.of(new Tag("Group A")), person.getTags());
    }

    @Test
    public void withoutTag_personWithoutEmail_returnsPersonWithoutEmail() {
        Person person = new PersonBuilder().withoutEmail().withTags("Group A").build();

        Person untagged = person.withoutTag(new Tag("Group A"));

        assertEquals(new PersonBuilder(person).withTags().build(), untagged);
        assertTrue(untagged.getEmail().isEmpty());
    }

    @Test
    public void withoutTag_personWithRecords_keepsAttendanceAndGradeRecords() {
        Attendance attendance = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Grade grade = new Grade("Assignment 1", new BigDecimal("97.5"));
        Person person = new PersonBuilder().withTags("Group A").build().withAttendance(attendance).withGrade(grade);

        Person untagged = person.withoutTag(new Tag("Group A"));

        assertEquals(List.of(attendance), untagged.getAttendanceRecords());
        assertEquals(List.of(grade), untagged.getGradeRecords());
    }

    @Test
    public void withTag_nullTag_throwsNullPointerException() {
        Person person = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () -> person.withTag(null));
    }

    @Test
    public void withTag_newTag_keepsExistingTagsAndOtherFields() {
        Person person = new PersonBuilder().withTags("Group A").build();

        Person tagged = person.withTag(new Tag("Group B"));

        assertEquals(new PersonBuilder(person).withTags("Group A", "Group B").build(), tagged);
    }

    @Test
    public void withTag_newTag_doesNotModifyOriginalPerson() {
        Person person = new PersonBuilder().withTags("Group A").build();

        person.withTag(new Tag("Group B"));

        assertEquals(Set.of(new Tag("Group A")), person.getTags());
    }

    @Test
    public void withTag_personWithoutEmail_returnsPersonWithoutEmail() {
        Person person = new PersonBuilder().withoutEmail().withTags().build();

        Person tagged = person.withTag(new Tag("Group A"));

        assertEquals(new PersonBuilder(person).withTags("Group A").build(), tagged);
        assertTrue(tagged.getEmail().isEmpty());
    }

    @Test
    public void withTag_tagAlreadyPresent_keepsExistingTag() {
        Person person = new PersonBuilder().withTags("Group A").build();

        Person tagged = person.withTag(new Tag("group a"));

        assertEquals(1, tagged.getTags().size());
        assertEquals("Group A", tagged.getTags().iterator().next().tagName);
    }

    @Test
    public void withTag_personWithRecords_keepsAttendanceAndGradeRecords() {
        Attendance attendance = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Grade grade = new Grade("Assignment 1", new BigDecimal("97.5"));
        Person person = new PersonBuilder().build().withAttendance(attendance).withGrade(grade);

        Person tagged = person.withTag(new Tag("Group A"));

        assertEquals(List.of(attendance), tagged.getAttendanceRecords());
        assertEquals(List.of(grade), tagged.getGradeRecords());
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and class, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has repeated spaces, all other attributes same -> returns true
        String nameWithRepeatedSpaces = VALID_NAME_BOB.replace(" ", "   ");
        editedBob = new PersonBuilder(BOB).withName(nameWithRepeatedSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // same name, different class, all other attributes same -> returns false
        editedBob = new PersonBuilder(BOB).withClassName(VALID_CLASS_AMY).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // same name, class differs in case, all other attributes same -> returns true
        editedBob = new PersonBuilder(BOB).withClassName(VALID_CLASS_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // same name and class, no email, all other attributes same -> returns true
        editedBob = new PersonBuilder(BOB).withoutEmail().build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void hasNameAndClass_variousInputs_matchesIgnoringCaseAndExtraSpaces() {
        // EP: the name and the class as they are stored
        assertTrue(BOB.hasNameAndClass(VALID_NAME_BOB, VALID_CLASS_BOB));

        // EP: the name in upper case and the class in lower case
        assertTrue(BOB.hasNameAndClass(VALID_NAME_BOB.toUpperCase(), VALID_CLASS_BOB.toLowerCase()));

        // EP: spaces around the name and the class, and repeated spaces inside the name
        String nameWithExtraSpaces = "  " + VALID_NAME_BOB.replace(" ", "   ") + " ";
        assertTrue(BOB.hasNameAndClass(nameWithExtraSpaces, " " + VALID_CLASS_BOB));

        // EP: a letter written in full width, which looks the same
        char fullWidthFirstLetter = (char) (VALID_NAME_BOB.charAt(0) - 'A' + 'Ａ');
        assertTrue(BOB.hasNameAndClass(fullWidthFirstLetter + VALID_NAME_BOB.substring(1), VALID_CLASS_BOB));

        // EP: a contact without an email is matched in the same way
        Person bobWithoutEmail = new PersonBuilder(BOB).withoutEmail().build();
        assertTrue(bobWithoutEmail.hasNameAndClass(VALID_NAME_BOB, VALID_CLASS_BOB));

        // EP: the right name and the wrong class
        assertFalse(BOB.hasNameAndClass(VALID_NAME_BOB, VALID_CLASS_AMY));

        // EP: the wrong name and the right class
        assertFalse(BOB.hasNameAndClass(VALID_NAME_AMY, VALID_CLASS_BOB));

        // EP: the name and the class swapped
        assertFalse(BOB.hasNameAndClass(VALID_CLASS_BOB, VALID_NAME_BOB));

        // EP: a name that is only part of the name
        assertFalse(BOB.hasNameAndClass(VALID_NAME_BOB.substring(0, VALID_NAME_BOB.length() - 1), VALID_CLASS_BOB));

        // EP: empty text, and values that no contact can have, which do not match and do not throw
        assertFalse(BOB.hasNameAndClass("", ""));
        assertFalse(BOB.hasNameAndClass("R@chel", "A1/B2"));
    }

    @Test
    public void hasNameAndClass_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> BOB.hasNameAndClass(null, VALID_CLASS_BOB));

        // EP: null class
        assertThrows(NullPointerException.class, () -> BOB.hasNameAndClass(VALID_NAME_BOB, null));
    }

    @Test
    public void getEmail_personWithoutEmail_returnsEmptyOptional() {
        Person personWithoutEmail = new PersonBuilder().withoutEmail().build();
        assertTrue(personWithoutEmail.getEmail().isEmpty());
        assertTrue(new PersonBuilder().build().getEmail().isPresent());
    }

    @Test
    public void constructor_nullEmailOptional_throwsNullPointerException() {
        Person person = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () ->
                new Person(person.getName(), person.getClassName(), null, person.getTags()));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different class -> returns false
        editedAlice = new PersonBuilder(ALICE).withClassName(VALID_CLASS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // email removed -> returns false
        editedAlice = new PersonBuilder(ALICE).withoutEmail().build();
        assertFalse(ALICE.equals(editedAlice));

        // both without email -> returns true
        Person aliceWithoutEmail = new PersonBuilder(ALICE).withoutEmail().build();
        assertTrue(aliceWithoutEmail.equals(new PersonBuilder(ALICE).withoutEmail().build()));
        assertEquals(aliceWithoutEmail.hashCode(), new PersonBuilder(ALICE).withoutEmail().build().hashCode());

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", className="
                + ALICE.getClassName() + ", email=" + ALICE.getEmail().get() + ", tags=" + ALICE.getTags()
                + ", attendanceRecords=" + ALICE.getAttendanceRecords() + ", gradeRecords="
                + ALICE.getGradeRecords() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void hasAttendanceOn_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().hasAttendanceOn(null));
    }

    @Test
    public void hasAttendanceOn_variousDates_returnsExpected() {
        LocalDate date = LocalDate.of(2026, 9, 16);
        Person person = new PersonBuilder().build().withAttendance(new Attendance(date, Attendance.Status.LATE))
                .withAttendance(new Attendance(date.plusDays(5), Attendance.Status.PRESENT));

        // EP: a date that has a record, whichever the status is
        assertTrue(person.hasAttendanceOn(date));
        assertTrue(person.hasAttendanceOn(date.plusDays(5)));

        // EP: the days just before and just after a date with a record (boundaries)
        assertFalse(person.hasAttendanceOn(date.minusDays(1)));
        assertFalse(person.hasAttendanceOn(date.plusDays(1)));

        // EP: a contact with no record
        assertFalse(new PersonBuilder().build().hasAttendanceOn(date));
    }

    @Test
    public void hasGradeFor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().hasGradeFor(null));
    }

    @Test
    public void hasGradeFor_variousAssessments_returnsExpected() {
        Person person = new PersonBuilder().build().withGrade(new Grade("Quiz 1", BigDecimal.TEN))
                .withGrade(new Grade("Exam", BigDecimal.TEN));

        // EP: an assessment that has a grade, with the same score or a different score
        assertTrue(person.hasGradeFor(new Grade("Quiz 1", BigDecimal.TEN)));
        assertTrue(person.hasGradeFor(new Grade("Quiz 1", BigDecimal.ONE)));

        // EP: an assessment that has a grade, written in another case, with extra spaces, or with a full-width
        // letter
        assertTrue(person.hasGradeFor(new Grade("QUIZ 1", BigDecimal.ONE)));
        assertTrue(person.hasGradeFor(new Grade("  quiz   1 ", BigDecimal.ONE)));
        assertTrue(person.hasGradeFor(new Grade("Ｑuiz 1", BigDecimal.ONE)));

        // EP: an assessment with no grade, including names that only differ by a space or a character
        assertFalse(person.hasGradeFor(new Grade("Quiz 2", BigDecimal.TEN)));
        assertFalse(person.hasGradeFor(new Grade("Quiz1", BigDecimal.TEN)));
        assertFalse(person.hasGradeFor(new Grade("Quiz 10", BigDecimal.TEN)));

        // EP: a contact with no grade
        assertFalse(new PersonBuilder().build().hasGradeFor(new Grade("Quiz 1", BigDecimal.TEN)));
    }

    @Test
    public void withAttendance_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().withAttendance(null));
    }

    @Test
    public void withAttendance_newDate_returnsPersonWithRecordAtTheEnd() {
        Attendance first = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Attendance second = new Attendance(LocalDate.of(2026, 9, 10), Attendance.Status.ABSENT);
        Person original = new PersonBuilder().build();

        // EP: a contact with no record (boundary)
        Person withFirst = original.withAttendance(first);
        assertEquals(List.of(first), withFirst.getAttendanceRecords());

        // EP: a contact with a record, and a date earlier than that record -> the new record is last
        assertEquals(List.of(first, second), withFirst.withAttendance(second).getAttendanceRecords());

        // EP: the contact that was used is not changed
        assertTrue(original.getAttendanceRecords().isEmpty());
        assertEquals(List.of(first), withFirst.getAttendanceRecords());
    }

    @Test
    public void withAttendance_eachStatus_keepsStatus() {
        LocalDate date = LocalDate.of(2026, 9, 16);

        // EP: each status is its own partition
        for (Attendance.Status status : Attendance.Status.values()) {
            Person updated = new PersonBuilder().build().withAttendance(new Attendance(date, status));

            assertEquals(status, updated.getAttendanceRecords().get(0).getStatus());
        }
    }

    @Test
    public void withAttendance_otherFields_areKept() {
        Person original = ALICE.withGrade(new Grade("Quiz 1", BigDecimal.TEN));
        Person updated = original.withAttendance(new Attendance(LocalDate.of(2026, 9, 16),
                Attendance.Status.PRESENT));

        // EP: the name, the class, the email, the tags and the grades are not changed
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getClassName(), updated.getClassName());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getTags(), updated.getTags());
        assertEquals(original.getGradeRecords(), updated.getGradeRecords());
    }

    @Test
    public void withAttendance_duplicateDate_throwsIllegalArgumentException() {
        LocalDate date = LocalDate.of(2026, 9, 16);
        Person person = new PersonBuilder().build().withAttendance(new Attendance(date, Attendance.Status.PRESENT));

        // EP: a record on the same date with a different status
        assertThrows(IllegalArgumentException.class, () ->
                person.withAttendance(new Attendance(date, Attendance.Status.ABSENT)));

        // EP: a record on the same date with the same status
        assertThrows(IllegalArgumentException.class, () ->
                person.withAttendance(new Attendance(date, Attendance.Status.PRESENT)));
    }

    @Test
    public void withoutAttendance_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().withoutAttendance(null));
    }

    @Test
    public void withoutAttendance_existingRecord_removesOnlyThatRecord() {
        Attendance first = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Attendance second = new Attendance(LocalDate.of(2026, 9, 17), Attendance.Status.ABSENT);
        Attendance third = new Attendance(LocalDate.of(2026, 9, 18), Attendance.Status.LATE);
        Person original = new PersonBuilder().build().withAttendance(first).withAttendance(second)
                .withAttendance(third);

        // EP: the first record
        assertEquals(List.of(second, third), original.withoutAttendance(first).getAttendanceRecords());

        // EP: a record in the middle
        assertEquals(List.of(first, third), original.withoutAttendance(second).getAttendanceRecords());

        // EP: the last record
        assertEquals(List.of(first, second), original.withoutAttendance(third).getAttendanceRecords());

        // EP: the contact that was used is not changed
        assertEquals(List.of(first, second, third), original.getAttendanceRecords());
    }

    @Test
    public void withoutAttendance_onlyRecord_leavesNoRecord() {
        // EP: a contact with exactly one record (boundary)
        Attendance record = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);

        assertTrue(new PersonBuilder().build().withAttendance(record).withoutAttendance(record)
                .getAttendanceRecords().isEmpty());
    }

    @Test
    public void withoutAttendance_otherFields_areKept() {
        Attendance record = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Person original = ALICE.withAttendance(record).withGrade(new Grade("Quiz 1", BigDecimal.TEN));
        Person updated = original.withoutAttendance(record);

        // EP: the name, the class, the email, the tags and the grades are not changed
        assertEquals(ALICE.getName(), updated.getName());
        assertEquals(ALICE.getClassName(), updated.getClassName());
        assertEquals(ALICE.getEmail(), updated.getEmail());
        assertEquals(ALICE.getTags(), updated.getTags());
        assertEquals(original.getGradeRecords(), updated.getGradeRecords());
    }

    @Test
    public void withoutAttendance_missingRecord_throwsIllegalArgumentException() {
        Person person = new PersonBuilder().build().withAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT));

        // EP: a record on a date that has no record
        assertThrows(IllegalArgumentException.class, () -> person.withoutAttendance(
                new Attendance(LocalDate.of(2026, 9, 17), Attendance.Status.PRESENT)));

        // EP: a record on the same date with a different status, which is not the record that is stored
        assertThrows(IllegalArgumentException.class, () -> person.withoutAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT)));

        // EP: a contact with no record
        assertThrows(IllegalArgumentException.class, () -> new PersonBuilder().build().withoutAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT)));
    }

    @Test
    public void getAttendanceRecords_modifyList_throwsUnsupportedOperationException() {
        Attendance record = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Person person = new PersonBuilder().build().withAttendance(record);

        // EP: adding a record
        assertThrows(UnsupportedOperationException.class, () -> person.getAttendanceRecords().add(
                new Attendance(LocalDate.of(2026, 9, 17), Attendance.Status.ABSENT)));

        // EP: removing a record
        assertThrows(UnsupportedOperationException.class, () -> person.getAttendanceRecords().remove(record));
    }

    @Test
    public void withGrade_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().withGrade(null));
    }

    @Test
    public void withGrade_newAssessment_returnsPersonWithGradeAtTheEnd() {
        Grade first = new Grade("Assignment 1", new BigDecimal("97.5"));
        Grade second = new Grade("Assignment 0", new BigDecimal("50"));
        Person original = new PersonBuilder().build();

        // EP: a contact with no grade (boundary)
        Person withFirst = original.withGrade(first);
        assertEquals(List.of(first), withFirst.getGradeRecords());

        // EP: a contact with a grade, and a name that comes before it in the alphabet -> the new grade is last
        assertEquals(List.of(first, second), withFirst.withGrade(second).getGradeRecords());

        // EP: the contact that was used is not changed
        assertTrue(original.getGradeRecords().isEmpty());
        assertEquals(List.of(first), withFirst.getGradeRecords());
    }

    @Test
    public void withGrade_otherFields_areKept() {
        Person original = ALICE.withAttendance(new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.LATE));
        Person updated = original.withGrade(new Grade("Quiz 1", BigDecimal.TEN));

        // EP: the name, the class, the email, the tags and the attendance records are not changed
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getClassName(), updated.getClassName());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getTags(), updated.getTags());
        assertEquals(original.getAttendanceRecords(), updated.getAttendanceRecords());
    }

    @Test
    public void withGrade_duplicateAssessment_throwsIllegalArgumentException() {
        Person person = new PersonBuilder().build().withGrade(new Grade("Quiz 1", new BigDecimal("90")));

        // EP: the same assessment with the same score, and with a different score
        assertThrows(IllegalArgumentException.class, () -> person.withGrade(new Grade("Quiz 1", new BigDecimal("90"))));
        assertThrows(IllegalArgumentException.class, () -> person.withGrade(new Grade("Quiz 1", new BigDecimal("80"))));

        // EP: the same assessment in another case
        assertThrows(IllegalArgumentException.class, () -> person.withGrade(new Grade("quiz 1", new BigDecimal("80"))));

        // EP: the same assessment with extra spaces
        assertThrows(IllegalArgumentException.class, () ->
                person.withGrade(new Grade(" quiz   1 ", new BigDecimal("80"))));

        // EP: the same assessment with a letter written in full width
        assertThrows(IllegalArgumentException.class, () ->
                person.withGrade(new Grade("Ｑuiz 1", new BigDecimal("80"))));
    }

    @Test
    public void withoutGrade_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().build().withoutGrade(null));
    }

    @Test
    public void withoutGrade_existingGrade_removesOnlyThatGrade() {
        Grade quiz = new Grade("Quiz 1", new BigDecimal("90"));
        Grade exam = new Grade("Exam", new BigDecimal("80"));
        Grade project = new Grade("Project", new BigDecimal("70"));
        Person original = new PersonBuilder().build().withGrade(quiz).withGrade(exam).withGrade(project);

        // EP: the first grade
        assertEquals(List.of(exam, project), original.withoutGrade(quiz).getGradeRecords());

        // EP: a grade in the middle
        assertEquals(List.of(quiz, project), original.withoutGrade(exam).getGradeRecords());

        // EP: the last grade
        assertEquals(List.of(quiz, exam), original.withoutGrade(project).getGradeRecords());

        // EP: the contact that was used is not changed
        assertEquals(List.of(quiz, exam, project), original.getGradeRecords());
    }

    @Test
    public void withoutGrade_onlyGrade_leavesNoGrade() {
        // EP: a contact with exactly one grade (boundary)
        Grade grade = new Grade("Quiz 1", new BigDecimal("90"));

        assertTrue(new PersonBuilder().build().withGrade(grade).withoutGrade(grade).getGradeRecords().isEmpty());
    }

    @Test
    public void withoutGrade_otherFields_areKept() {
        Grade grade = new Grade("Quiz 1", BigDecimal.TEN);
        Person original = ALICE.withGrade(grade).withAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.LATE));
        Person updated = original.withoutGrade(grade);

        // EP: the name, the class, the email, the tags and the attendance records are not changed
        assertEquals(ALICE.getName(), updated.getName());
        assertEquals(ALICE.getClassName(), updated.getClassName());
        assertEquals(ALICE.getEmail(), updated.getEmail());
        assertEquals(ALICE.getTags(), updated.getTags());
        assertEquals(original.getAttendanceRecords(), updated.getAttendanceRecords());
    }

    @Test
    public void withoutGrade_missingGrade_throwsIllegalArgumentException() {
        Person person = new PersonBuilder().build().withGrade(new Grade("Quiz 1", new BigDecimal("90")));

        // EP: an assessment that has no grade
        assertThrows(IllegalArgumentException.class, () ->
                person.withoutGrade(new Grade("Exam", new BigDecimal("80"))));

        // EP: the same assessment with a different score, which is not the grade that is stored
        assertThrows(IllegalArgumentException.class, () ->
                person.withoutGrade(new Grade("Quiz 1", new BigDecimal("80"))));

        // EP: a contact with no grade
        assertThrows(IllegalArgumentException.class, () ->
                new PersonBuilder().build().withoutGrade(new Grade("Quiz 1", new BigDecimal("90"))));
    }

    @Test
    public void getGradeRecords_modifyList_throwsUnsupportedOperationException() {
        Grade grade = new Grade("Quiz 1", new BigDecimal("90"));
        Person person = new PersonBuilder().build().withGrade(grade);

        // EP: adding a grade
        assertThrows(UnsupportedOperationException.class, () ->
                person.getGradeRecords().add(new Grade("Exam", new BigDecimal("80"))));

        // EP: removing a grade, and clearing the grades
        assertThrows(UnsupportedOperationException.class, () -> person.getGradeRecords().remove(grade));
        assertThrows(UnsupportedOperationException.class, () -> person.getGradeRecords().clear());
    }

    @Test
    public void equals_differentRecords_returnsFalse() {
        Person person = new PersonBuilder().build();

        // EP: the same contact with an attendance record that the other contact does not have
        assertFalse(person.equals(person.withAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT))));

        // EP: the same contact with a grade that the other contact does not have
        assertFalse(person.equals(person.withGrade(new Grade("Quiz 1", new BigDecimal("90")))));

        // EP: the same records
        Attendance record = new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.PRESENT);
        Grade grade = new Grade("Quiz 1", new BigDecimal("90"));
        assertTrue(person.withAttendance(record).withGrade(grade).equals(person.withAttendance(record)
                .withGrade(grade)));

        // EP: the same attendance record with a different status, and the same grade with a different score
        assertFalse(person.withAttendance(record).equals(person.withAttendance(
                new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT))));
        assertFalse(person.withGrade(grade).equals(person.withGrade(new Grade("Quiz 1", new BigDecimal("91")))));
    }

}

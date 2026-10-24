package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Attendance;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteAttendanceByNameAndClassCommand}.
 *
 * <p>The inputs are the name, the class and the date, and the state is the contacts of the address book and
 * the attendance records that the contact has. The first contact has a late record on {@link #DATE} and a
 * present record on {@link #OTHER_DATE}. Each invalid input is tested alone before it is tested together
 * with another invalid input.
 */
public class DeleteAttendanceByNameAndClassCommandTest {

    private static final String ALICE_NAME = ALICE.getName().toString();
    private static final String ALICE_CLASS = ALICE.getClassName().toString();
    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);
    private static final LocalDate OTHER_DATE = LocalDate.of(2026, 9, 17);
    private static final Attendance ATTENDANCE = new Attendance(DATE, Attendance.Status.LATE);
    private static final Attendance OTHER_ATTENDANCE = new Attendance(OTHER_DATE, Attendance.Status.PRESENT);

    private Model model;
    private Person aliceWithAttendance;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        aliceWithAttendance = ALICE.withAttendance(ATTENDANCE).withAttendance(OTHER_ATTENDANCE);
        model.setPerson(ALICE, aliceWithAttendance);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () ->
                new DeleteAttendanceByNameAndClassCommand(null, ALICE_CLASS, DATE));

        // EP: null class
        assertThrows(NullPointerException.class, () ->
                new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, null, DATE));

        // EP: null date
        assertThrows(NullPointerException.class, () ->
                new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, null));
    }

    @Test
    public void execute_nameAndClassMatchExactly_deletesOnlyThatRecord() {
        // EP: a contact with several records -> the other record is kept, and so are the other contacts
        DeleteAttendanceByNameAndClassCommand command =
                new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, DATE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithAttendance, ALICE.withAttendance(OTHER_ATTENDANCE));

        assertCommandSuccess(command, model,
                "Attendance of late for Alice Pauline on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_eachStatus_showsStoredStatus() {
        // EP: a record with the status late
        assertRecordDeleted(ALICE_NAME, ALICE_CLASS, DATE, "late");

        // EP: a record with the status present
        assertRecordDeleted(ALICE_NAME, ALICE_CLASS, OTHER_DATE, "present");

        // EP: a record with the status absent
        Person person = model.getAddressBook().getPersonList().get(0);
        model.setPerson(person, person.withAttendance(new Attendance(DATE.plusDays(5), Attendance.Status.ABSENT)));
        assertRecordDeleted(ALICE_NAME, ALICE_CLASS, DATE.plusDays(5), "absent");
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_deletesRecord() {
        // EP: the name with different case and extra spaces, and the class in lower case
        assertRecordDeleted("  aLiCe   Pauline ", "a1", DATE, "late");
    }

    @Test
    public void execute_nameWithFullWidthLetter_deletesRecord() {
        // EP: a letter written in full width, which looks the same as the letter in the name
        assertRecordDeleted("Ａlice Pauline", ALICE_CLASS, DATE, "late");
    }

    @Test
    public void execute_onlyRecordOfContact_leavesNoRecord() {
        // EP: a contact with exactly one record (boundary) -> no record is left
        Person bensonWithRecord = BENSON.withAttendance(ATTENDANCE);
        model.setPerson(BENSON, bensonWithRecord);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(bensonWithRecord, BENSON);

        assertCommandSuccess(new DeleteAttendanceByNameAndClassCommand(BENSON.getName().toString(),
                BENSON.getClassName().toString(), DATE), model,
                "Attendance of late for Benson Meier on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_personHiddenByFilter_deletesRecord() {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person bensonWithAttendance = BENSON.withAttendance(ATTENDANCE);
        model.setPerson(BENSON, bensonWithAttendance);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertFalse(model.getFilteredPersonList().contains(bensonWithAttendance));

        DeleteAttendanceByNameAndClassCommand command = new DeleteAttendanceByNameAndClassCommand(
                BENSON.getName().toString(), BENSON.getClassName().toString(), DATE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(bensonWithAttendance, BENSON);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(command, model,
                "Attendance of late for Benson Meier on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_sameNameInAnotherClass_deletesRecordOfRequestedClass() {
        // EP: two contacts have the same name and a record on the same date, and the class tells them apart
        Person sameNameOtherClass = new PersonBuilder(ALICE).withClassName("B2").build().withAttendance(ATTENDANCE);
        model.addPerson(sameNameOtherClass);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(sameNameOtherClass, new PersonBuilder(ALICE).withClassName("B2").build());

        assertCommandSuccess(new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, "b2", DATE), model,
                "Attendance of late for Alice Pauline on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_attendanceNotFound_throwsCommandException() {
        // EP: the day before a record (boundary)
        assertCommandFailure(new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, DATE.minusDays(1)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: the day after the last record (boundary)
        assertCommandFailure(
                new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, OTHER_DATE.plusDays(1)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: a date far from the records, including a date in the future
        assertCommandFailure(
                new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, LocalDate.of(9999, 12, 31)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: a contact with no record at all
        assertCommandFailure(new DeleteAttendanceByNameAndClassCommand(BENSON.getName().toString(),
                BENSON.getClassName().toString(), DATE), model,
                DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);
    }

    @Test
    public void execute_noMatchingContact_throwsCommandException() {
        // EP: the name matches a contact but the class does not
        assertFailsWithContactNotFound(ALICE_NAME, "Z9");

        // EP: the class matches a contact but the name does not
        assertFailsWithContactNotFound("Nobody Here", ALICE_CLASS);

        // EP: neither the name nor the class matches
        assertFailsWithContactNotFound("Nobody Here", "Z9");

        // EP: a name that is only part of a name of a contact
        assertFailsWithContactNotFound(ALICE_NAME.substring(0, ALICE_NAME.length() - 1), ALICE_CLASS);

        // EP: a name and a class that are not valid for a contact
        assertFailsWithContactNotFound("Not, A Name", "!!!");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        // EP: there is no contact at all
        assertCommandFailure(new DeleteAttendanceByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, DATE),
                new ModelManager(), Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void execute_noMatchingContactAndAttendanceNotFound_reportsContactNotFound() {
        // EP: a contact that does not exist and a date without a record -> the contact is reported first
        assertCommandFailure(
                new DeleteAttendanceByNameAndClassCommand("Nobody Here", ALICE_CLASS, DATE.minusDays(1)),
                model, Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        DeleteAttendanceByNameAndClassCommand command =
                new DeleteAttendanceByNameAndClassCommand("Alice", "A1", DATE);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new DeleteAttendanceByNameAndClassCommand("Alice", "A1", DATE)));

        // EP: a different name
        assertFalse(command.equals(new DeleteAttendanceByNameAndClassCommand("Bob", "A1", DATE)));

        // EP: a different class
        assertFalse(command.equals(new DeleteAttendanceByNameAndClassCommand("Alice", "B2", DATE)));

        // EP: a different date
        assertFalse(command.equals(new DeleteAttendanceByNameAndClassCommand("Alice", "A1", OTHER_DATE)));

        // EP: the command that uses an index
        assertFalse(command.equals(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE)));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals(1));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        DeleteAttendanceByNameAndClassCommand command =
                new DeleteAttendanceByNameAndClassCommand("Alice", "A1", DATE);
        String expected = DeleteAttendanceByNameAndClassCommand.class.getCanonicalName()
                + "{name=Alice, className=A1, date=" + DATE + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Deletes the record on {@code date} of the contact identified by {@code name} and {@code className},
     * which has to be the first contact, and checks that the message shows the stored status as
     * {@code expectedStatusText} and that the other records are kept.
     */
    private void assertRecordDeleted(String name, String className, LocalDate date, String expectedStatusText) {
        Person person = model.getAddressBook().getPersonList().get(0);
        Attendance recordToDelete = person.getAttendanceRecords().stream()
                .filter(record -> record.getDate().equals(date))
                .findFirst().orElseThrow();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withoutAttendance(recordToDelete));

        String expectedMessage = "Attendance of " + expectedStatusText + " for " + person.getName() + " on "
                + Attendance.formatDate(date) + " has been deleted";

        assertCommandSuccess(new DeleteAttendanceByNameAndClassCommand(name, className, date), model,
                expectedMessage, expectedModel);
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        assertCommandFailure(new DeleteAttendanceByNameAndClassCommand(name, className, DATE), model,
                Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

}

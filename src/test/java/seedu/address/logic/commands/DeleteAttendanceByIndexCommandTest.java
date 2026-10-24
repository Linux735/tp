package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Attendance;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteAttendanceByIndexCommand}.
 *
 * <p>The inputs are the index and the date, and the state is the list that is displayed and the attendance
 * records that the contact has. The first contact has an absent record on {@link #DATE} and a present record
 * on {@link #OTHER_DATE}. Each invalid input is tested alone before it is tested together with another
 * invalid input.
 */
public class DeleteAttendanceByIndexCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);
    private static final LocalDate OTHER_DATE = LocalDate.of(2026, 9, 17);
    private static final Attendance ATTENDANCE = new Attendance(DATE, Attendance.Status.ABSENT);
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
        // EP: null index
        assertThrows(NullPointerException.class, () -> new DeleteAttendanceByIndexCommand(null, DATE));

        // EP: null date
        assertThrows(NullPointerException.class, () ->
                new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_eachStatus_deletesRecordAndShowsStoredStatus() {
        // EP: a record with the status absent
        assertRecordDeleted(INDEX_FIRST_PERSON, DATE, "absent");

        // EP: a record with the status present
        assertRecordDeleted(INDEX_FIRST_PERSON, OTHER_DATE, "present");

        // EP: a record with the status late
        Person person = model.getFilteredPersonList().get(0);
        Person personWithLateRecord = person.withAttendance(new Attendance(DATE.plusDays(5),
                Attendance.Status.LATE));
        model.setPerson(person, personWithLateRecord);
        assertRecordDeleted(INDEX_FIRST_PERSON, DATE.plusDays(5), "late");
    }

    @Test
    public void execute_validIndexUnfilteredList_deletesOnlyThatRecord() {
        // EP: a contact with several records -> the other record is kept, and so are the other contacts
        DeleteAttendanceByIndexCommand command =
                new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithAttendance, ALICE.withAttendance(OTHER_ATTENDANCE));

        assertCommandSuccess(command, model,
                "Attendance of absent for Alice Pauline on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_onlyRecordOfContact_leavesNoRecord() {
        // EP: a contact with exactly one record (boundary) -> no record is left
        Person lastPerson = model.getFilteredPersonList().get(model.getFilteredPersonList().size() - 1);
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPersonWithRecord = lastPerson.withAttendance(ATTENDANCE);
        model.setPerson(lastPerson, lastPersonWithRecord);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(lastPersonWithRecord, lastPerson);

        assertCommandSuccess(new DeleteAttendanceByIndexCommand(lastIndex, DATE), model,
                "Attendance of absent for " + lastPerson.getName() + " on 16-09-2026 has been deleted",
                expectedModel);
    }

    @Test
    public void execute_yearZeroDate_messageShowsYearZero() {
        // EP: a date with a year that has fewer than four digits (boundary) -> zeros are added in the message
        Attendance yearZeroAttendance = new Attendance(LocalDate.of(0, 1, 1), Attendance.Status.LATE);
        Person personWithYearZero = aliceWithAttendance.withAttendance(yearZeroAttendance);
        model.setPerson(aliceWithAttendance, personWithYearZero);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithYearZero, aliceWithAttendance);

        assertCommandSuccess(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, LocalDate.of(0, 1, 1)), model,
                "Attendance of late for " + ALICE.getName() + " on 01-01-0000 has been deleted", expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_deletesRecordOfDisplayedContact() {
        // EP: an index of the filtered list
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        DeleteAttendanceByIndexCommand command =
                new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithAttendance, ALICE.withAttendance(OTHER_ATTENDANCE));
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(command, model,
                "Attendance of absent for Alice Pauline on 16-09-2026 has been deleted", expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // EP: the index just after the last index (boundary)
        Index justOutOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new DeleteAttendanceByIndexCommand(justOutOfBounds, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // EP: an index far after the last index
        assertCommandFailure(new DeleteAttendanceByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), DATE),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        // EP: an index that is in the full list but not in the filtered list (boundary)
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_SECOND_PERSON, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_attendanceNotFound_throwsCommandException() {
        // EP: the day before a record (boundary)
        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE.minusDays(1)), model,
                DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: the day after the last record (boundary)
        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, OTHER_DATE.plusDays(1)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: a date far from the records, including a date in the future
        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, LocalDate.of(9999, 12, 31)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);

        // EP: a contact with no record at all
        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_SECOND_PERSON, DATE), model,
                DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);
    }

    @Test
    public void execute_dateOfAnotherContact_throwsCommandException() {
        // EP: another contact has a record on that date, which does not count
        Person other = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(other, other.withAttendance(new Attendance(LocalDate.of(2026, 1, 5),
                Attendance.Status.LATE)));

        assertCommandFailure(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, LocalDate.of(2026, 1, 5)),
                model, DeleteAttendanceCommand.MESSAGE_ATTENDANCE_NOT_FOUND);
    }

    @Test
    public void execute_invalidIndexAndAttendanceNotFound_reportsInvalidIndex() {
        // EP: an index that is not in the list and a date without a record -> the index is reported first
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DeleteAttendanceByIndexCommand(outOfBounds, DATE.minusDays(1)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        DeleteAttendanceByIndexCommand command = new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE)));

        // EP: a different index
        assertFalse(command.equals(new DeleteAttendanceByIndexCommand(INDEX_SECOND_PERSON, DATE)));

        // EP: a different date
        assertFalse(command.equals(new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, OTHER_DATE)));

        // EP: the command that uses a name and a class
        assertFalse(command.equals(new DeleteAttendanceByNameAndClassCommand("Alice", "A1", DATE)));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals(1));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        DeleteAttendanceByIndexCommand command =
                new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE);
        String expected = DeleteAttendanceByIndexCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", date=" + DATE + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Deletes the record of the contact at {@code index} on {@code date}, and checks that the message shows
     * the stored status as {@code expectedStatusText} and that the other records are kept.
     */
    private void assertRecordDeleted(Index index, LocalDate date, String expectedStatusText) {
        Person person = model.getFilteredPersonList().get(index.getZeroBased());
        Attendance recordToDelete = person.getAttendanceRecords().stream()
                .filter(record -> record.getDate().equals(date))
                .findFirst().orElseThrow();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withoutAttendance(recordToDelete));

        String expectedMessage = "Attendance of " + expectedStatusText + " for " + person.getName() + " on "
                + Attendance.formatDate(date) + " has been deleted";

        assertCommandSuccess(new DeleteAttendanceByIndexCommand(index, date), model, expectedMessage,
                expectedModel);
    }

}

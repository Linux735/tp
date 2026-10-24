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
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

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
 * {@code AttendanceByIndexCommand}.
 *
 * <p>The inputs are the index, the date and the status, and the state is the list that is displayed and the
 * attendance records that the contact already has. Each invalid input is tested alone before it is tested
 * together with another invalid input.
 */
public class AttendanceByIndexCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null index
        assertThrows(NullPointerException.class, () ->
                new AttendanceByIndexCommand(null, DATE, Attendance.Status.PRESENT));

        // EP: null date
        assertThrows(NullPointerException.class, () ->
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, null, Attendance.Status.PRESENT));

        // EP: null status
        assertThrows(NullPointerException.class, () ->
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, null));
    }

    @Test
    public void execute_eachStatus_recordsAttendance() {
        // EP: status present, shown in lower case in the message
        assertAttendanceRecorded(Index.fromOneBased(1), Attendance.Status.PRESENT, "present");

        // EP: status absent
        assertAttendanceRecorded(Index.fromOneBased(2), Attendance.Status.ABSENT, "absent");

        // EP: status late
        assertAttendanceRecorded(Index.fromOneBased(3), Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_indexInUnfilteredList_recordsAttendanceOfThatContact() {
        // EP: the first index (boundary)
        assertAttendanceRecorded(INDEX_FIRST_PERSON, Attendance.Status.LATE, "late");

        // EP: the last index of the list (boundary)
        assertAttendanceRecorded(Index.fromOneBased(model.getFilteredPersonList().size()),
                Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_indexInFilteredList_recordsAttendanceOfDisplayedContact() {
        // EP: an index of the filtered list; the contact is not the one at that index of the full list
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person displayedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased()),
                displayedPerson);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(displayedPerson,
                displayedPerson.withAttendance(new Attendance(DATE, Attendance.Status.LATE)));
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        String expectedMessage = "Attendance for " + displayedPerson.getName() + " on 16-09-2026 recorded as late.";
        assertCommandSuccess(new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.LATE), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_datesThatAreNotInTheFuture_recordsAttendance() {
        // EP: a date in the past
        assertAttendanceRecordedOn(Index.fromOneBased(1), LocalDate.of(2026, 9, 16), "16-09-2026");

        // EP: today, the latest date that is allowed (boundary)
        LocalDate today = LocalDate.now();
        assertAttendanceRecordedOn(Index.fromOneBased(2), today, Attendance.formatDate(today));

        // EP: a date that only exists in a leap year
        assertAttendanceRecordedOn(Index.fromOneBased(3), LocalDate.of(2024, 2, 29), "29-02-2024");

        // EP: a year with fewer than four digits, which is shown with zeros in front (boundary)
        assertAttendanceRecordedOn(Index.fromOneBased(4), LocalDate.of(0, 1, 1), "01-01-0000");
    }

    @Test
    public void execute_contactWithRecordOnAnotherDate_recordsAttendance() {
        // EP: a contact that already has a record on a different date
        Person person = model.getFilteredPersonList().get(0);
        Person personWithRecord = person.withAttendance(new Attendance(DATE.minusDays(1), Attendance.Status.PRESENT));
        model.setPerson(person, personWithRecord);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithRecord,
                personWithRecord.withAttendance(new Attendance(DATE, Attendance.Status.ABSENT)));

        AttendanceByIndexCommand command =
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.ABSENT);
        String expectedMessage = "Attendance for " + person.getName() + " on 16-09-2026 recorded as absent.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // EP: the index just after the last index (boundary)
        Index justOutOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new AttendanceByIndexCommand(justOutOfBounds, DATE, Attendance.Status.PRESENT),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // EP: an index far after the last index
        assertCommandFailure(
                new AttendanceByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), DATE,
                        Attendance.Status.PRESENT),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        // EP: an index that is in the full list but not in the filtered list (boundary)
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        AttendanceByIndexCommand command =
                new AttendanceByIndexCommand(INDEX_SECOND_PERSON, DATE, Attendance.Status.PRESENT);

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_futureDate_throwsCommandException() {
        // EP: tomorrow, the earliest date that is not allowed (boundary)
        assertCommandFailure(new AttendanceByIndexCommand(INDEX_FIRST_PERSON, LocalDate.now().plusDays(1),
                Attendance.Status.PRESENT), model, AttendanceCommand.MESSAGE_FUTURE_DATE);

        // EP: a date far in the future
        assertCommandFailure(new AttendanceByIndexCommand(INDEX_FIRST_PERSON, LocalDate.of(9999, 12, 31),
                Attendance.Status.PRESENT), model, AttendanceCommand.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void execute_futureDateAndInvalidIndex_reportsFutureDate() {
        // EP: a date in the future and an index that is not in the list -> the date is reported first
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        AttendanceByIndexCommand command = new AttendanceByIndexCommand(
                outOfBounds, LocalDate.now().plusDays(1), Attendance.Status.PRESENT);

        assertCommandFailure(command, model, AttendanceCommand.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void execute_duplicateDate_throwsCommandException() {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(person, person.withAttendance(new Attendance(DATE, Attendance.Status.PRESENT)));

        // EP: a record on the same date with a different status
        assertCommandFailure(new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.ABSENT),
                model, AttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE);

        // EP: a record on the same date with the same status
        assertCommandFailure(new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.PRESENT),
                model, AttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE);
    }

    @Test
    public void execute_sameDateForAnotherContact_recordsAttendance() {
        // EP: another contact has a record on the same date, which does not matter
        Person other = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(other, other.withAttendance(new Attendance(DATE, Attendance.Status.PRESENT)));

        assertAttendanceRecorded(INDEX_FIRST_PERSON, Attendance.Status.ABSENT, "absent");
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        AttendanceByIndexCommand command =
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.PRESENT);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.PRESENT)));

        // EP: a different index
        assertFalse(command.equals(
                new AttendanceByIndexCommand(INDEX_SECOND_PERSON, DATE, Attendance.Status.PRESENT)));

        // EP: a different date
        assertFalse(command.equals(
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE.plusDays(1), Attendance.Status.PRESENT)));

        // EP: a different status
        assertFalse(command.equals(
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.ABSENT)));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not an AttendanceByIndexCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        AttendanceByIndexCommand command =
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.PRESENT);
        String expected = AttendanceByIndexCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", date=" + DATE + ", status=present}";

        assertEquals(expected, command.toString());
    }

    /**
     * Records the attendance of the contact at {@code index} on {@link #DATE} and checks the result.
     */
    private void assertAttendanceRecorded(Index index, Attendance.Status status, String expectedStatusText) {
        assertAttendanceRecordedWithDetails(index, DATE, status, "16-09-2026", expectedStatusText);
    }

    /**
     * Records the attendance of the contact at {@code index} on {@code date} with the status late, and checks
     * that the message shows the date as {@code expectedDateText}.
     */
    private void assertAttendanceRecordedOn(Index index, LocalDate date, String expectedDateText) {
        assertAttendanceRecordedWithDetails(index, date, Attendance.Status.LATE, expectedDateText, "late");
    }

    private void assertAttendanceRecordedWithDetails(Index index, LocalDate date, Attendance.Status status,
            String expectedDateText, String expectedStatusText) {
        Person person = model.getFilteredPersonList().get(index.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withAttendance(new Attendance(date, status)));

        String expectedMessage = "Attendance for " + person.getName() + " on " + expectedDateText
                + " recorded as " + expectedStatusText + ".";

        assertCommandSuccess(new AttendanceByIndexCommand(index, date, status), model, expectedMessage,
                expectedModel);
    }

}

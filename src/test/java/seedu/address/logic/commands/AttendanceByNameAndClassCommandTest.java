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

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Attendance;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code AttendanceByNameAndClassCommand}.
 *
 * <p>The inputs are the name, the class, the date and the status, and the state is the contacts of the
 * address book and the attendance records that the contact already has. Each invalid input is tested alone
 * before it is tested together with another invalid input.
 */
public class AttendanceByNameAndClassCommandTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () ->
                new AttendanceByNameAndClassCommand(null, "A1", DATE, Attendance.Status.PRESENT));

        // EP: null class
        assertThrows(NullPointerException.class, () ->
                new AttendanceByNameAndClassCommand("Amy", null, DATE, Attendance.Status.PRESENT));

        // EP: null date
        assertThrows(NullPointerException.class, () ->
                new AttendanceByNameAndClassCommand("Amy", "A1", null, Attendance.Status.PRESENT));

        // EP: null status
        assertThrows(NullPointerException.class, () ->
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE, null));
    }

    @Test
    public void execute_eachStatus_recordsAttendance() {
        // EP: status present, shown in lower case in the message
        assertAttendanceRecorded(contact(0), contact(0).getName().toString(), contact(0).getClassName().toString(),
                Attendance.Status.PRESENT, "present");

        // EP: status absent
        assertAttendanceRecorded(contact(1), contact(1).getName().toString(), contact(1).getClassName().toString(),
                Attendance.Status.ABSENT, "absent");

        // EP: status late
        assertAttendanceRecorded(contact(2), contact(2).getName().toString(), contact(2).getClassName().toString(),
                Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_recordsAttendance() {
        Person person = contact(0);
        String name = person.getName().toString();
        String className = person.getClassName().toString();

        // EP: the name in upper case and the class in lower case
        assertAttendanceRecorded(person, name.toUpperCase(), className.toLowerCase(), Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_nameWithExtraSpaces_recordsAttendance() {
        Person person = contact(1);

        // EP: spaces around the name and the class, and repeated spaces inside the name
        assertAttendanceRecorded(person, "  " + person.getName().toString().replace(" ", "   ") + " ",
                "  " + person.getClassName() + " ", Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_nameWithFullWidthLetter_recordsAttendance() {
        Person person = contact(2);

        // EP: a letter written in full width, which looks the same as the letter in the name
        String actualName = person.getName().toString();
        char fullWidthFirstLetter = (char) (actualName.charAt(0) - 'A' + '\uff21');
        String name = fullWidthFirstLetter + actualName.substring(1);
        assertAttendanceRecorded(person, name, person.getClassName().toString(), Attendance.Status.LATE, "late");
    }

    @Test
    public void execute_contactNotDisplayed_recordsAttendance() {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person hiddenPerson = contact(0);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(hiddenPerson,
                hiddenPerson.withAttendance(new Attendance(DATE, Attendance.Status.LATE)));
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        AttendanceByNameAndClassCommand command = new AttendanceByNameAndClassCommand(
                hiddenPerson.getName().toString(), hiddenPerson.getClassName().toString(), DATE,
                Attendance.Status.LATE);
        String expectedMessage = "Attendance for " + hiddenPerson.getName() + " on 16-09-2026 recorded as late.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameInAnotherClass_recordsAttendanceOfRequestedClass() {
        // EP: two contacts have the same name, and the class tells them apart
        Person person = contact(0);
        Person sameNameOtherClass = new PersonBuilder(person).withClassName("B2").build();
        model.addPerson(sameNameOtherClass);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(sameNameOtherClass,
                sameNameOtherClass.withAttendance(new Attendance(DATE, Attendance.Status.LATE)));

        AttendanceByNameAndClassCommand command = new AttendanceByNameAndClassCommand(
                person.getName().toString(), "B2", DATE, Attendance.Status.LATE);
        String expectedMessage = "Attendance for " + person.getName() + " on 16-09-2026 recorded as late.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_datesThatAreNotInTheFuture_recordsAttendance() {
        // EP: today, the latest date that is allowed (boundary)
        LocalDate today = LocalDate.now();
        Person person = contact(0);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withAttendance(new Attendance(today, Attendance.Status.LATE)));

        AttendanceByNameAndClassCommand command = new AttendanceByNameAndClassCommand(
                person.getName().toString(), person.getClassName().toString(), today, Attendance.Status.LATE);
        String expectedMessage = "Attendance for " + person.getName() + " on " + Attendance.formatDate(today)
                + " recorded as late.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_contactWithRecordOnAnotherDate_recordsAttendance() {
        // EP: a contact that already has a record on a different date
        Person person = contact(0);
        Person personWithRecord = person.withAttendance(new Attendance(DATE.minusDays(1), Attendance.Status.PRESENT));
        model.setPerson(person, personWithRecord);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithRecord,
                personWithRecord.withAttendance(new Attendance(DATE, Attendance.Status.ABSENT)));

        AttendanceByNameAndClassCommand command = new AttendanceByNameAndClassCommand(
                person.getName().toString(), person.getClassName().toString(), DATE, Attendance.Status.ABSENT);
        String expectedMessage = "Attendance for " + person.getName() + " on 16-09-2026 recorded as absent.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noMatchingContact_throwsCommandException() {
        Person person = contact(0);
        String name = person.getName().toString();
        String className = person.getClassName().toString();

        // EP: the name matches a contact but the class does not
        assertFailsWithContactNotFound(name, "Z9");

        // EP: the class matches a contact but the name does not
        assertFailsWithContactNotFound("Unknown Student", className);

        // EP: neither the name nor the class matches
        assertFailsWithContactNotFound("Unknown Student", "Z9");

        // EP: a name that is only part of a name of a contact
        assertFailsWithContactNotFound(name.substring(0, name.length() - 1), className);

        // EP: a name and a class that are not valid for a contact
        assertFailsWithContactNotFound("Not, A Name", "!!!");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        // EP: there is no contact at all
        AttendanceByNameAndClassCommand command =
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE, Attendance.Status.PRESENT);

        assertCommandFailure(command, new ModelManager(), Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void execute_futureDate_throwsCommandException() {
        Person person = contact(0);

        // EP: tomorrow, the earliest date that is not allowed (boundary)
        assertCommandFailure(new AttendanceByNameAndClassCommand(person.getName().toString(),
                person.getClassName().toString(), LocalDate.now().plusDays(1), Attendance.Status.PRESENT),
                model, AttendanceCommand.MESSAGE_FUTURE_DATE);

        // EP: a date far in the future
        assertCommandFailure(new AttendanceByNameAndClassCommand(person.getName().toString(),
                person.getClassName().toString(), LocalDate.of(9999, 12, 31), Attendance.Status.PRESENT),
                model, AttendanceCommand.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void execute_futureDateAndUnknownContact_reportsFutureDate() {
        // EP: a date in the future and a contact that does not exist -> the date is reported first
        AttendanceByNameAndClassCommand command = new AttendanceByNameAndClassCommand(
                "Unknown Student", "Z9", LocalDate.now().plusDays(1), Attendance.Status.PRESENT);

        assertCommandFailure(command, model, AttendanceCommand.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void execute_duplicateDate_throwsCommandException() {
        Person person = contact(0);
        model.setPerson(person, person.withAttendance(new Attendance(DATE, Attendance.Status.PRESENT)));
        String name = person.getName().toString();
        String className = person.getClassName().toString();

        // EP: a record on the same date with a different status
        assertCommandFailure(new AttendanceByNameAndClassCommand(name, className, DATE, Attendance.Status.ABSENT),
                model, AttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE);

        // EP: a record on the same date with the same status
        assertCommandFailure(new AttendanceByNameAndClassCommand(name, className, DATE, Attendance.Status.PRESENT),
                model, AttendanceCommand.MESSAGE_DUPLICATE_ATTENDANCE);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        AttendanceByNameAndClassCommand command =
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE, Attendance.Status.PRESENT);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE, Attendance.Status.PRESENT)));

        // EP: a different name
        assertFalse(command.equals(
                new AttendanceByNameAndClassCommand("Benson", "A1", DATE, Attendance.Status.PRESENT)));

        // EP: a different class
        assertFalse(command.equals(
                new AttendanceByNameAndClassCommand("Amy", "A2", DATE, Attendance.Status.PRESENT)));

        // EP: a different date
        assertFalse(command.equals(
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE.plusDays(1), Attendance.Status.PRESENT)));

        // EP: a different status
        assertFalse(command.equals(
                new AttendanceByNameAndClassCommand("Amy", "A1", DATE, Attendance.Status.ABSENT)));

        // EP: the command that uses an index
        assertFalse(command.equals(
                new AttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE, Attendance.Status.PRESENT)));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not an AttendanceByNameAndClassCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        AttendanceByNameAndClassCommand command =
                new AttendanceByNameAndClassCommand("Alex Yeoh", "A1", DATE, Attendance.Status.PRESENT);
        String expected = AttendanceByNameAndClassCommand.class.getCanonicalName()
                + "{name=Alex Yeoh, className=A1, date=" + DATE + ", status=present}";

        assertEquals(expected, command.toString());
    }

    private Person contact(int zeroBasedIndex) {
        return model.getFilteredPersonList().get(zeroBasedIndex);
    }

    /**
     * Records the attendance of {@code person} on {@link #DATE}, identifying the contact by {@code name} and
     * {@code className}, and checks the result.
     */
    private void assertAttendanceRecorded(Person person, String name, String className,
            Attendance.Status status, String expectedStatusText) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withAttendance(new Attendance(DATE, status)));

        String expectedMessage = "Attendance for " + person.getName() + " on 16-09-2026 recorded as "
                + expectedStatusText + ".";

        assertCommandSuccess(new AttendanceByNameAndClassCommand(name, className, DATE, status), model,
                expectedMessage, expectedModel);
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        assertCommandFailure(
                new AttendanceByNameAndClassCommand(name, className, DATE, Attendance.Status.PRESENT),
                model, Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

}

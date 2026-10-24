package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Attendance;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains unit tests for {@code ViewAttendanceCommand}.
 *
 * <p>The inputs are the name and the class, and the state is the contacts of the address book and the
 * attendance records of the contact. The command only reads the model, so every test also checks that the
 * model is not changed.
 */
public class ViewAttendanceCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> new ViewAttendanceCommand(null, "A1"));

        // EP: null class
        assertThrows(NullPointerException.class, () -> new ViewAttendanceCommand("Alex Tan", null));
    }

    @Test
    public void execute_studentWithoutRecords_showsEmptyMessage() throws Exception {
        // EP: a contact with no attendance record
        Person person = contact(0);

        assertEquals(header(person) + ViewAttendanceCommand.MESSAGE_NO_ATTENDANCE, viewAttendanceOf(person));
    }

    @Test
    public void execute_studentWithOneRecord_showsTheRecord() throws Exception {
        // EP: a contact with one attendance record (boundary)
        Person person = contact(0);
        model.setPerson(person, person.withAttendance(new Attendance(LocalDate.of(2026, 9, 16),
                Attendance.Status.ABSENT)));

        assertEquals(header(person) + "16-Sep-2026  absent", viewAttendanceOf(contact(0)));
    }

    @Test
    public void execute_studentWithSeveralRecords_showsLatestDateFirst() throws Exception {
        // EP: a contact with records that were recorded from the oldest date to the newest date, and one
        // record for each status
        Person person = contact(0);
        model.setPerson(person, person
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 9), Attendance.Status.LATE))
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT))
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 23), Attendance.Status.PRESENT)));

        assertEquals(header(person) + "23-Sep-2026  present\n16-Sep-2026  absent\n09-Sep-2026  late",
                viewAttendanceOf(contact(0)));
    }

    @Test
    public void execute_recordsRecordedOutOfOrder_showsLatestDateFirst() throws Exception {
        // EP: a contact with records that were not recorded in the order of the dates
        Person person = contact(0);
        model.setPerson(person, person
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT))
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 23), Attendance.Status.PRESENT))
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 9), Attendance.Status.LATE)));

        assertEquals(header(person) + "23-Sep-2026  present\n16-Sep-2026  absent\n09-Sep-2026  late",
                viewAttendanceOf(contact(0)));
    }

    @Test
    public void execute_recordsInDifferentYears_showsLatestDateFirst() throws Exception {
        // EP: records in different years and months, where the order of the text would be wrong
        Person person = contact(0);
        model.setPerson(person, person
                .withAttendance(new Attendance(LocalDate.of(2025, 12, 31), Attendance.Status.PRESENT))
                .withAttendance(new Attendance(LocalDate.of(2026, 1, 1), Attendance.Status.ABSENT)));

        assertEquals(header(person) + "01-Jan-2026  absent\n31-Dec-2025  present", viewAttendanceOf(contact(0)));
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_findsStudent() throws Exception {
        Person person = new PersonBuilder().withName("John Tan").withClassName("A1").build();
        Model testModel = new ModelManager();
        testModel.addPerson(person);
        String expected = "Here is the attendance for John Tan from class A1\n"
                + ViewAttendanceCommand.MESSAGE_NO_ATTENDANCE;

        // EP: the name in another case, with extra spaces, and the class in another case
        assertEquals(expected, new ViewAttendanceCommand("  john   TAN ", "a1").execute(testModel)
                .getFeedbackToUser());

        // EP: a letter of the name written in full width, which looks the same
        assertEquals(expected, new ViewAttendanceCommand("Ｊohn Tan", "A1").execute(testModel)
                .getFeedbackToUser());
    }

    @Test
    public void execute_studentNotDisplayed_findsStudent() throws Exception {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person hiddenPerson = contact(0);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));

        assertEquals(header(hiddenPerson) + ViewAttendanceCommand.MESSAGE_NO_ATTENDANCE,
                viewAttendanceOf(hiddenPerson));
    }

    @Test
    public void execute_sameNameDifferentClass_selectsRequestedClass() throws Exception {
        // EP: two contacts have the same name, and the class tells them apart
        Person otherClassPerson = new PersonBuilder().withName("Alex Tan").withClassName("A2").build()
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 16), Attendance.Status.ABSENT));
        Person targetPerson = new PersonBuilder().withName("Alex Tan").withClassName("A1").build()
                .withAttendance(new Attendance(LocalDate.of(2026, 9, 23), Attendance.Status.PRESENT));
        Model testModel = new ModelManager();
        testModel.addPerson(otherClassPerson);
        testModel.addPerson(targetPerson);

        String output = new ViewAttendanceCommand("Alex Tan", "A1").execute(testModel).getFeedbackToUser();

        assertEquals("Here is the attendance for Alex Tan from class A1\n23-Sep-2026  present", output);
    }

    @Test
    public void execute_noMatchingStudent_throwsCommandException() {
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
        assertFailsWithContactNotFound("@@@", "!!!");
        assertFailsWithContactNotFound("Tan, John", "A1&");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        // EP: there is no contact at all
        CommandException exception = assertThrows(CommandException.class, () ->
                new ViewAttendanceCommand("Alex Tan", "A1").execute(new ModelManager()));
        assertEquals(Messages.MESSAGE_CONTACT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        ViewAttendanceCommand command = new ViewAttendanceCommand("Alex Tan", "A1");

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new ViewAttendanceCommand("Alex Tan", "A1")));

        // EP: a different name
        assertFalse(command.equals(new ViewAttendanceCommand("Jamie Tan", "A1")));

        // EP: a different class
        assertFalse(command.equals(new ViewAttendanceCommand("Alex Tan", "A2")));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not a ViewAttendanceCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        ViewAttendanceCommand command = new ViewAttendanceCommand("Alex Tan", "A1");
        String expected = ViewAttendanceCommand.class.getCanonicalName() + "{name=Alex Tan, className=A1}";

        assertEquals(expected, command.toString());
    }

    private Person contact(int zeroBasedIndex) {
        return model.getAddressBook().getPersonList().get(zeroBasedIndex);
    }

    private static String header(Person person) {
        return "Here is the attendance for " + person.getName() + " from class " + person.getClassName() + "\n";
    }

    /**
     * Runs the command for {@code person}, checks that the model is not changed, and returns the message.
     */
    private String viewAttendanceOf(Person person) throws Exception {
        Model modelBefore = new ModelManager(model.getAddressBook(), new UserPrefs());
        String message = new ViewAttendanceCommand(person.getName().toString(), person.getClassName().toString())
                .execute(model).getFeedbackToUser();
        assertEquals(modelBefore.getAddressBook(), model.getAddressBook());
        return message;
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        CommandException exception = assertThrows(CommandException.class, () ->
                new ViewAttendanceCommand(name, className).execute(model));
        assertEquals(Messages.MESSAGE_CONTACT_NOT_FOUND, exception.getMessage());
    }

}

package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Grade;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains unit tests for {@code ViewGradeCommand}.
 *
 * <p>The inputs are the name and the class, and the state is the contacts of the address book and the grades
 * of the contact. The command only reads the model, so every test also checks that the model is not changed.
 */
public class ViewGradeCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> new ViewGradeCommand(null, "A1"));

        // EP: null class
        assertThrows(NullPointerException.class, () -> new ViewGradeCommand("Alex Tan", null));
    }

    @Test
    public void execute_studentWithoutGrades_showsEmptyMessage() throws Exception {
        // EP: a contact with no grade
        Person person = contact(0);

        assertEquals(header(person) + ViewGradeCommand.MESSAGE_NO_GRADES, viewGradeOf(person));
    }

    @Test
    public void execute_studentWithOneGrade_showsTheGrade() throws Exception {
        // EP: a contact with one grade (boundary)
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("Quiz 1", new BigDecimal("80"))));

        assertEquals(header(person) + "Quiz 1: 80", viewGradeOf(contact(0)));
    }

    @Test
    public void execute_studentWithSeveralGrades_showsLastRecordedFirst() throws Exception {
        // EP: a contact with several grades -> the grade that was recorded last is shown first
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("Quiz 1", new BigDecimal("80")))
                .withGrade(new Grade("Quiz 2", new BigDecimal("85")))
                .withGrade(new Grade("Quiz 3", new BigDecimal("90"))));

        assertEquals(header(person) + "Quiz 3: 90\nQuiz 2: 85\nQuiz 1: 80", viewGradeOf(contact(0)));
    }

    @Test
    public void execute_gradesRecordedOutOfAlphabeticalOrder_keepsRecordingOrder() throws Exception {
        // EP: grades whose names and scores are not in order -> the order of recording decides, not the name
        // or the score
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("Final", new BigDecimal("60")))
                .withGrade(new Grade("Assignment 1", new BigDecimal("95")))
                .withGrade(new Grade("Midterm", new BigDecimal("75"))));

        assertEquals(header(person) + "Midterm: 75\nAssignment 1: 95\nFinal: 60", viewGradeOf(contact(0)));
    }

    @Test
    public void execute_scoresInTheOutput_showsScoreWithoutZerosAtTheEnd() throws Exception {
        // EP: the smallest score and the largest score (boundaries)
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("Low", new BigDecimal("0")))
                .withGrade(new Grade("High", new BigDecimal("100"))));
        assertEquals(header(person) + "High: 100\nLow: 0", viewGradeOf(contact(0)));

        // EP: scores with a decimal part, and scores with zeros at the end -> the zeros are not shown
        Person other = contact(1);
        model.setPerson(other, other.withGrade(new Grade("Decimal", new BigDecimal("97.5")))
                .withGrade(new Grade("Zeros", new BigDecimal("90.50")))
                .withGrade(new Grade("Whole", new BigDecimal("80.00"))));
        assertEquals(header(other) + "Whole: 80\nZeros: 90.5\nDecimal: 97.5", viewGradeOf(contact(1)));
    }

    @Test
    public void execute_assessmentNameWithExtraSpaces_showsNameAsStored() throws Exception {
        // EP: a name that was typed with extra spaces and in a different case -> the name is shown as stored
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("  quiz   1 ", new BigDecimal("80"))));

        assertEquals(header(person) + "quiz   1: 80", viewGradeOf(contact(0)));
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_findsStudent() throws Exception {
        Person person = new PersonBuilder().withName("Alex Tan").withClassName("A1").build()
                .withGrade(new Grade("Quiz 1", new BigDecimal("80")));
        Model testModel = new ModelManager();
        testModel.addPerson(person);
        String expected = "Here are the grades for Alex Tan from class A1\nQuiz 1: 80";

        // EP: the name in another case, with extra spaces, and the class in another case
        assertEquals(expected, new ViewGradeCommand("  aLEX    tan ", "a1").execute(testModel)
                .getFeedbackToUser());

        // EP: a letter of the name written in full width, which looks the same
        assertEquals(expected, new ViewGradeCommand("Ａlex Tan", "A1").execute(testModel)
                .getFeedbackToUser());
    }

    @Test
    public void execute_studentNotDisplayed_findsStudent() throws Exception {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person hiddenPerson = contact(0);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));

        assertEquals(header(hiddenPerson) + ViewGradeCommand.MESSAGE_NO_GRADES, viewGradeOf(hiddenPerson));
    }

    @Test
    public void execute_sameNameDifferentClass_selectsRequestedClass() throws Exception {
        // EP: two contacts have the same name, and the class tells them apart
        Person otherClassPerson = new PersonBuilder().withName("Alex Tan").withClassName("A2").build()
                .withGrade(new Grade("Quiz 1", new BigDecimal("50")));
        Person targetPerson = new PersonBuilder().withName("Alex Tan").withClassName("A1").build()
                .withGrade(new Grade("Assignment 1", new BigDecimal("97")));
        Model testModel = new ModelManager();
        testModel.addPerson(otherClassPerson);
        testModel.addPerson(targetPerson);

        String output = new ViewGradeCommand("Alex Tan", "A1").execute(testModel).getFeedbackToUser();

        assertEquals("Here are the grades for Alex Tan from class A1\nAssignment 1: 97", output);
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
                new ViewGradeCommand("Alex Tan", "A1").execute(new ModelManager()));
        assertEquals(Messages.MESSAGE_CONTACT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        ViewGradeCommand command = new ViewGradeCommand("Alex Tan", "A1");

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new ViewGradeCommand("Alex Tan", "A1")));

        // EP: a different name
        assertFalse(command.equals(new ViewGradeCommand("Jamie Tan", "A1")));

        // EP: a different class
        assertFalse(command.equals(new ViewGradeCommand("Alex Tan", "A2")));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not a ViewGradeCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        ViewGradeCommand command = new ViewGradeCommand("Alex Tan", "A1");
        String expected = ViewGradeCommand.class.getCanonicalName() + "{name=Alex Tan, className=A1}";

        assertEquals(expected, command.toString());
    }

    private Person contact(int zeroBasedIndex) {
        return model.getAddressBook().getPersonList().get(zeroBasedIndex);
    }

    private static String header(Person person) {
        return "Here are the grades for " + person.getName() + " from class " + person.getClassName() + "\n";
    }

    /**
     * Runs the command for {@code person}, checks that the model is not changed, and returns the message.
     */
    private String viewGradeOf(Person person) throws Exception {
        Model modelBefore = new ModelManager(model.getAddressBook(), new UserPrefs());
        String message = new ViewGradeCommand(person.getName().toString(), person.getClassName().toString())
                .execute(model).getFeedbackToUser();
        assertEquals(modelBefore.getAddressBook(), model.getAddressBook());
        return message;
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        CommandException exception = assertThrows(CommandException.class, () ->
                new ViewGradeCommand(name, className).execute(model));
        assertEquals(Messages.MESSAGE_CONTACT_NOT_FOUND, exception.getMessage());
    }

}

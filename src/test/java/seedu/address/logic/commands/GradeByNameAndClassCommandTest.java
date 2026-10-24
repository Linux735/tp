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

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Grade;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code GradeByNameAndClassCommand}.
 *
 * <p>The inputs are the name, the class and the grade, and the state is the contacts of the address book and
 * the grades that the contact already has. The assessment name and the score have their own tests in
 * {@code GradeTest}. Each invalid input is tested alone before it is tested together with another invalid
 * input.
 */
public class GradeByNameAndClassCommandTest {

    private static final Grade GRADE = new Grade("Assignment 1", new BigDecimal("97.5"));

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> new GradeByNameAndClassCommand(null, "A1", GRADE));

        // EP: null class
        assertThrows(NullPointerException.class, () -> new GradeByNameAndClassCommand("Amy", null, GRADE));

        // EP: null grade
        assertThrows(NullPointerException.class, () -> new GradeByNameAndClassCommand("Amy", "A1", null));
    }

    @Test
    public void execute_nameAndClassMatchExactly_recordsGrade() {
        Person person = contact(0);

        // EP: the name and the class as they are stored
        assertGradeRecorded(person, person.getName().toString(), person.getClassName().toString(), GRADE, "97.5");
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_recordsGrade() {
        Person person = contact(0);

        // EP: the name in upper case and the class in lower case
        assertGradeRecorded(person, person.getName().toString().toUpperCase(),
                person.getClassName().toString().toLowerCase(), GRADE, "97.5");

        // EP: spaces around the name and the class, and repeated spaces inside the name
        Person other = contact(1);
        assertGradeRecorded(other, "  " + other.getName().toString().replace(" ", "   ") + " ",
                "  " + other.getClassName() + " ", GRADE, "97.5");
    }

    @Test
    public void execute_nameWithFullWidthLetter_recordsGrade() {
        Person person = contact(2);

        // EP: a letter written in full width, which looks the same as the letter in the name
        String actualName = person.getName().toString();
        char fullWidthFirstLetter = (char) (actualName.charAt(0) - 'A' + 'Ａ');
        String name = fullWidthFirstLetter + actualName.substring(1);
        assertGradeRecorded(person, name, person.getClassName().toString(), GRADE, "97.5");
    }

    @Test
    public void execute_contactNotDisplayed_recordsGrade() {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person hiddenPerson = contact(0);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertFalse(model.getFilteredPersonList().contains(hiddenPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(hiddenPerson, hiddenPerson.withGrade(GRADE));
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        GradeByNameAndClassCommand command = new GradeByNameAndClassCommand(
                hiddenPerson.getName().toString(), hiddenPerson.getClassName().toString(), GRADE);
        String expectedMessage = "Grade of 97.5 recorded for Assignment 1 for student "
                + hiddenPerson.getName() + " in class " + hiddenPerson.getClassName() + ".";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameInAnotherClass_recordsGradeOfRequestedClass() {
        // EP: two contacts have the same name, and the class tells them apart
        Person person = contact(0);
        Person sameNameOtherClass = new PersonBuilder(person).withClassName("B2").build();
        model.addPerson(sameNameOtherClass);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(sameNameOtherClass, sameNameOtherClass.withGrade(GRADE));

        GradeByNameAndClassCommand command =
                new GradeByNameAndClassCommand(person.getName().toString(), "B2", GRADE);
        String expectedMessage = "Grade of 97.5 recorded for Assignment 1 for student "
                + person.getName() + " in class B2.";

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_scoresInTheMessage_showsScoreWithoutZerosAtTheEnd() {
        // EP: the smallest score (boundary)
        Person first = contact(0);
        assertGradeRecorded(first, first.getName().toString(), first.getClassName().toString(),
                new Grade("Quiz", new BigDecimal("0")), "0");

        // EP: the largest score (boundary)
        Person second = contact(1);
        assertGradeRecorded(second, second.getName().toString(), second.getClassName().toString(),
                new Grade("Quiz", new BigDecimal("100")), "100");

        // EP: a score with zeros at the end -> the zeros are not shown
        Person third = contact(2);
        assertGradeRecorded(third, third.getName().toString(), third.getClassName().toString(),
                new Grade("Quiz", new BigDecimal("90.50")), "90.5");
    }

    @Test
    public void execute_contactWithAnotherGrade_recordsGrade() {
        // EP: a contact that already has a grade for a different assessment
        Person person = contact(0);
        Person personWithGrade = person.withGrade(new Grade("Quiz 1", BigDecimal.TEN));
        model.setPerson(person, personWithGrade);

        assertGradeRecorded(personWithGrade, person.getName().toString(), person.getClassName().toString(),
                GRADE, "97.5");
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

        // EP: a name that is not valid for a contact, with the class of a contact
        assertFailsWithContactNotFound("Not, A Name", className);

        // EP: a class that is not valid for a contact, with the name of a contact
        assertFailsWithContactNotFound(name, "!!!");

        // EP: a name and a class that are both not valid for a contact
        assertFailsWithContactNotFound("Not, A Name", "!!!");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        // EP: there is no contact at all
        assertCommandFailure(new GradeByNameAndClassCommand("Amy", "A1", GRADE), new ModelManager(),
                Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void execute_duplicateAssessment_throwsCommandException() {
        Person person = contact(0);
        model.setPerson(person, person.withGrade(new Grade("Assignment 1", BigDecimal.TEN)));
        String name = person.getName().toString();
        String className = person.getClassName().toString();

        // EP: the same assessment with a different score
        assertCommandFailure(new GradeByNameAndClassCommand(name, className, GRADE), model,
                GradeCommand.MESSAGE_DUPLICATE_GRADE);

        // EP: the same assessment with the same score
        assertCommandFailure(new GradeByNameAndClassCommand(name, className,
                new Grade("Assignment 1", BigDecimal.TEN)), model, GradeCommand.MESSAGE_DUPLICATE_GRADE);

        // EP: the same assessment written in another case or with extra spaces
        assertCommandFailure(new GradeByNameAndClassCommand(name, className,
                new Grade("  ASSIGNMENT   1 ", BigDecimal.ONE)), model, GradeCommand.MESSAGE_DUPLICATE_GRADE);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        GradeByNameAndClassCommand command = new GradeByNameAndClassCommand("Amy", "A1", GRADE);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values, with the score at a different scale
        assertTrue(command.equals(new GradeByNameAndClassCommand("Amy", "A1",
                new Grade("Assignment 1", new BigDecimal("97.50")))));

        // EP: a different name
        assertFalse(command.equals(new GradeByNameAndClassCommand("Benson", "A1", GRADE)));

        // EP: a different class
        assertFalse(command.equals(new GradeByNameAndClassCommand("Amy", "A2", GRADE)));

        // EP: a different grade
        assertFalse(command.equals(new GradeByNameAndClassCommand("Amy", "A1",
                new Grade("Assignment 1", BigDecimal.ONE))));

        // EP: the command that uses an index
        assertFalse(command.equals(new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE)));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not a GradeByNameAndClassCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        GradeByNameAndClassCommand command = new GradeByNameAndClassCommand("Alex Yeoh", "A1", GRADE);
        String expected = GradeByNameAndClassCommand.class.getCanonicalName()
                + "{name=Alex Yeoh, className=A1, grade=" + GRADE + "}";

        assertEquals(expected, command.toString());
    }

    private Person contact(int zeroBasedIndex) {
        return model.getFilteredPersonList().get(zeroBasedIndex);
    }

    /**
     * Records {@code grade} for {@code person}, identifying the contact by {@code name} and
     * {@code className}, and checks that the message shows the score as {@code expectedScoreText}.
     */
    private void assertGradeRecorded(Person person, String name, String className, Grade grade,
            String expectedScoreText) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withGrade(grade));

        String expectedMessage = "Grade of " + expectedScoreText + " recorded for " + grade.getAssessmentName()
                + " for student " + person.getName() + " in class " + person.getClassName() + ".";

        assertCommandSuccess(new GradeByNameAndClassCommand(name, className, grade), model, expectedMessage,
                expectedModel);
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        assertCommandFailure(new GradeByNameAndClassCommand(name, className, GRADE), model,
                Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

}

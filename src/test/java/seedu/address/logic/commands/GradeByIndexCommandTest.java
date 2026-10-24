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

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Grade;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code GradeByIndexCommand}.
 *
 * <p>The inputs are the index and the grade, and the state is the list that is displayed and the grades that
 * the contact already has. The assessment name and the score have their own tests in {@code GradeTest}. Each
 * invalid input is tested alone before it is tested together with another invalid input.
 */
public class GradeByIndexCommandTest {

    private static final Grade GRADE = new Grade("Assignment 1", new BigDecimal("97.5"));

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null index
        assertThrows(NullPointerException.class, () -> new GradeByIndexCommand(null, GRADE));

        // EP: null grade
        assertThrows(NullPointerException.class, () -> new GradeByIndexCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_indexInUnfilteredList_recordsGradeOfThatContact() {
        // EP: the first index (boundary)
        assertGradeRecorded(INDEX_FIRST_PERSON, GRADE, "97.5");

        // EP: the last index of the list (boundary)
        assertGradeRecorded(Index.fromOneBased(model.getFilteredPersonList().size()), GRADE, "97.5");
    }

    @Test
    public void execute_indexInFilteredList_recordsGradeOfDisplayedContact() {
        // EP: an index of the filtered list; the contact is not the one at that index of the full list
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person displayedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased()),
                displayedPerson);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(displayedPerson, displayedPerson.withGrade(GRADE));
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        String expectedMessage = "Grade of 97.5 recorded for Assignment 1 for student "
                + displayedPerson.getName() + " in class " + displayedPerson.getClassName() + ".";

        assertCommandSuccess(new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_scoresInTheMessage_showsScoreWithoutZerosAtTheEnd() {
        // EP: the smallest score (boundary)
        assertGradeRecorded(Index.fromOneBased(1), new Grade("Quiz", new BigDecimal("0")), "0");

        // EP: the largest score (boundary)
        assertGradeRecorded(Index.fromOneBased(2), new Grade("Quiz", new BigDecimal("100")), "100");

        // EP: a score with a decimal part
        assertGradeRecorded(Index.fromOneBased(3), new Grade("Quiz", new BigDecimal("97.5")), "97.5");

        // EP: a score with zeros at the end -> the zeros are not shown
        assertGradeRecorded(Index.fromOneBased(4), new Grade("Quiz", new BigDecimal("100.00")), "100");
        assertGradeRecorded(Index.fromOneBased(5), new Grade("Quiz", new BigDecimal("90.50")), "90.5");
        assertGradeRecorded(Index.fromOneBased(6), new Grade("Quiz", new BigDecimal("0.0")), "0");
    }

    @Test
    public void execute_contactWithAnotherGrade_recordsGrade() {
        // EP: a contact that already has a grade for a different assessment
        Person person = model.getFilteredPersonList().get(0);
        Person personWithGrade = person.withGrade(new Grade("Quiz 1", BigDecimal.TEN));
        model.setPerson(person, personWithGrade);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithGrade, personWithGrade.withGrade(GRADE));

        String expectedMessage = "Grade of 97.5 recorded for Assignment 1 for student "
                + person.getName() + " in class " + person.getClassName() + ".";

        assertCommandSuccess(new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_sameAssessmentForAnotherContact_recordsGrade() {
        // EP: another contact has a grade for the same assessment, which does not matter
        Person other = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(other, other.withGrade(GRADE));

        assertGradeRecorded(INDEX_FIRST_PERSON, GRADE, "97.5");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // EP: the index just after the last index (boundary)
        Index justOutOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new GradeByIndexCommand(justOutOfBounds, GRADE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // EP: an index far after the last index
        assertCommandFailure(new GradeByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), GRADE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        // EP: an index that is in the full list but not in the filtered list (boundary)
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new GradeByIndexCommand(INDEX_SECOND_PERSON, GRADE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateAssessment_throwsCommandException() {
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(person, person.withGrade(new Grade("Assignment 1", BigDecimal.TEN)));

        // EP: the same assessment with a different score
        assertCommandFailure(new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE), model,
                GradeCommand.MESSAGE_DUPLICATE_GRADE);

        // EP: the same assessment with the same score
        assertCommandFailure(new GradeByIndexCommand(INDEX_FIRST_PERSON,
                new Grade("Assignment 1", BigDecimal.TEN)), model, GradeCommand.MESSAGE_DUPLICATE_GRADE);

        // EP: the same assessment written in another case or with extra spaces
        assertCommandFailure(new GradeByIndexCommand(INDEX_FIRST_PERSON,
                new Grade("  ASSIGNMENT   1 ", BigDecimal.ONE)), model, GradeCommand.MESSAGE_DUPLICATE_GRADE);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        GradeByIndexCommand command = new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE);

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values, with the score at a different scale
        assertTrue(command.equals(new GradeByIndexCommand(INDEX_FIRST_PERSON,
                new Grade("Assignment 1", new BigDecimal("97.50")))));

        // EP: a different index
        assertFalse(command.equals(new GradeByIndexCommand(INDEX_SECOND_PERSON, GRADE)));

        // EP: a different assessment name
        assertFalse(command.equals(new GradeByIndexCommand(INDEX_FIRST_PERSON,
                new Grade("Assignment 2", new BigDecimal("97.5")))));

        // EP: a different score
        assertFalse(command.equals(new GradeByIndexCommand(INDEX_FIRST_PERSON,
                new Grade("Assignment 1", BigDecimal.ONE))));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals("not a GradeByIndexCommand"));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        GradeByIndexCommand command = new GradeByIndexCommand(INDEX_FIRST_PERSON, GRADE);
        String expected = GradeByIndexCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", grade=" + GRADE + "}";

        assertEquals(expected, command.toString());
    }

    /**
     * Records {@code grade} for the contact at {@code index} and checks that the message shows the score as
     * {@code expectedScoreText}.
     */
    private void assertGradeRecorded(Index index, Grade grade, String expectedScoreText) {
        Person person = model.getFilteredPersonList().get(index.getZeroBased());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withGrade(grade));

        String expectedMessage = "Grade of " + expectedScoreText + " recorded for " + grade.getAssessmentName()
                + " for student " + person.getName() + " in class " + person.getClassName() + ".";

        assertCommandSuccess(new GradeByIndexCommand(index, grade), model, expectedMessage, expectedModel);
    }

}

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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Grade;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteGradeByIndexCommand}.
 *
 * <p>The inputs are the index and the assessment name, and the state is the list that is displayed and the
 * grades that the contact has. The first contact has a grade of 90.50 for "Quiz 1" and a grade of 80 for
 * "Exam". Each invalid input is tested alone before it is tested together with another invalid input.
 */
public class DeleteGradeByIndexCommandTest {

    private static final Grade GRADE = new Grade("Quiz 1", new BigDecimal("90.50"));
    private static final Grade OTHER_GRADE = new Grade("Exam", new BigDecimal("80"));

    private Model model;
    private Person aliceWithGrades;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        aliceWithGrades = ALICE.withGrade(GRADE).withGrade(OTHER_GRADE);
        model.setPerson(ALICE, aliceWithGrades);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null index
        assertThrows(NullPointerException.class, () -> new DeleteGradeByIndexCommand(null, "Quiz 1"));

        // EP: null assessment name
        assertThrows(NullPointerException.class, () ->
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void constructor_invalidAssessment_throwsAssertionError() {
        // EP: an assessment name that the parser would have rejected, which is a mistake of the caller
        assertThrows(AssertionError.class, () ->
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz,1"));

        // EP: an assessment name that only has spaces
        assertThrows(AssertionError.class, () ->
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "   "));
    }

    @Test
    public void execute_validIndexUnfilteredList_deletesOnlyThatGrade() {
        // EP: a contact with several grades -> the other grade is kept, and so are the other contacts
        DeleteGradeByIndexCommand command = new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithGrades, ALICE.withGrade(OTHER_GRADE));

        assertCommandSuccess(command, model,
                "Grade of 90.5 for Quiz 1 for student Alice Pauline in class A1 has been deleted", expectedModel);
    }

    @Test
    public void execute_assessmentNameMatchIgnoringCaseAndSpaces_deletesGradeAndShowsStoredName() {
        // EP: the assessment name in upper case
        assertGradeDeleted("QUIZ 1", "Quiz 1", "90.5");

        // EP: the assessment name with extra spaces inside and around it
        Person person = model.getFilteredPersonList().get(0);
        model.setPerson(person, person.withGrade(GRADE));
        assertGradeDeleted("  quiz    1 ", "Quiz 1", "90.5");
    }

    @Test
    public void execute_assessmentNameWithFullWidthLetter_deletesGrade() {
        // EP: a letter written in full width, which looks the same as the letter in the name
        assertGradeDeleted("Ｑuiz 1", "Quiz 1", "90.5");
    }

    @Test
    public void execute_scoresInTheMessage_showsStoredScoreWithoutZerosAtTheEnd() {
        Person person = model.getFilteredPersonList().get(0);
        model.setPerson(person, ALICE.withGrade(new Grade("Low", new BigDecimal("0")))
                .withGrade(new Grade("High", new BigDecimal("100.00")))
                .withGrade(new Grade("Decimal", new BigDecimal("97.5"))));

        // EP: the smallest score (boundary)
        assertGradeDeleted("Low", "Low", "0");

        // EP: the largest score written with zeros at the end -> the zeros are not shown (boundary)
        assertGradeDeleted("High", "High", "100");

        // EP: a score with a decimal part
        assertGradeDeleted("Decimal", "Decimal", "97.5");
    }

    @Test
    public void execute_onlyGradeOfContact_leavesNoGrade() {
        // EP: a contact with exactly one grade (boundary) -> no grade is left
        Person lastPerson = model.getFilteredPersonList().get(model.getFilteredPersonList().size() - 1);
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPersonWithGrade = lastPerson.withGrade(GRADE);
        model.setPerson(lastPerson, lastPersonWithGrade);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(lastPersonWithGrade, lastPerson);

        assertCommandSuccess(new DeleteGradeByIndexCommand(lastIndex, "Quiz 1"), model,
                "Grade of 90.5 for Quiz 1 for student " + lastPerson.getName() + " in class "
                        + lastPerson.getClassName() + " has been deleted", expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_deletesGradeOfDisplayedContact() {
        // EP: an index of the filtered list
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        DeleteGradeByIndexCommand command = new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithGrades, ALICE.withGrade(OTHER_GRADE));
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(command, model,
                "Grade of 90.5 for Quiz 1 for student Alice Pauline in class A1 has been deleted", expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // EP: the index just after the last index (boundary)
        Index justOutOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new DeleteGradeByIndexCommand(justOutOfBounds, "Quiz 1"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // EP: an index far after the last index
        assertCommandFailure(new DeleteGradeByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), "Quiz 1"),
                model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        // EP: an index that is in the full list but not in the filtered list (boundary)
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_SECOND_PERSON, "Quiz 1"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_gradeNotFound_throwsCommandException() {
        // EP: an assessment that the contact has no grade for
        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Assignment 1"), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);

        // EP: a name that is almost the name of a grade, with a space missing or with a character more
        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz1"), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);
        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 10"), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);

        // EP: a contact with no grade at all
        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_SECOND_PERSON, "Quiz 1"), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);
    }

    @Test
    public void execute_gradeOfAnotherContact_throwsCommandException() {
        // EP: another contact has a grade for that assessment, which does not count
        Person other = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(other, other.withGrade(new Grade("Project", BigDecimal.TEN)));

        assertCommandFailure(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Project"), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);
    }

    @Test
    public void execute_invalidIndexAndGradeNotFound_reportsInvalidIndex() {
        // EP: an index that is not in the list and an assessment without a grade -> the index is reported first
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DeleteGradeByIndexCommand(outOfBounds, "Assignment 1"), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        DeleteGradeByIndexCommand command = new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1");

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1")));

        // EP: a different index
        assertFalse(command.equals(new DeleteGradeByIndexCommand(INDEX_SECOND_PERSON, "Quiz 1")));

        // EP: a different assessment name
        assertFalse(command.equals(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Exam")));

        // EP: the command that uses a name and a class
        assertFalse(command.equals(new DeleteGradeByNameAndClassCommand("Alice", "A1", "Quiz 1")));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals(1));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        DeleteGradeByIndexCommand command = new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1");
        String expected = DeleteGradeByIndexCommand.class.getCanonicalName()
                + "{targetIndex=" + INDEX_FIRST_PERSON + ", assessmentName=Quiz 1}";

        assertEquals(expected, command.toString());
    }

    /**
     * Deletes the grade for {@code typedAssessmentName} of the first contact, and checks that the message
     * shows the stored name and the score as {@code expectedStoredName} and {@code expectedScoreText}, and
     * that the other grades are kept.
     */
    private void assertGradeDeleted(String typedAssessmentName, String expectedStoredName,
            String expectedScoreText) {
        Person person = model.getFilteredPersonList().get(0);
        Grade gradeToDelete = person.getGradeRecords().stream()
                .filter(grade -> grade.getAssessmentName().equals(expectedStoredName))
                .findFirst().orElseThrow();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withoutGrade(gradeToDelete));

        String expectedMessage = "Grade of " + expectedScoreText + " for " + expectedStoredName + " for student "
                + person.getName() + " in class " + person.getClassName() + " has been deleted";

        assertCommandSuccess(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, typedAssessmentName), model,
                expectedMessage, expectedModel);
    }

}

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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
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
 * {@code DeleteGradeByNameAndClassCommand}.
 *
 * <p>The inputs are the name, the class and the assessment name, and the state is the contacts of the address
 * book and the grades that the contact has. The first contact has a grade of 90.50 for "Quiz 1" and a grade
 * of 80 for "Exam". Each invalid input is tested alone before it is tested together with another invalid
 * input.
 */
public class DeleteGradeByNameAndClassCommandTest {

    private static final String ALICE_NAME = ALICE.getName().toString();
    private static final String ALICE_CLASS = ALICE.getClassName().toString();
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
        // EP: null name
        assertThrows(NullPointerException.class, () ->
                new DeleteGradeByNameAndClassCommand(null, ALICE_CLASS, "Quiz 1"));

        // EP: null class
        assertThrows(NullPointerException.class, () ->
                new DeleteGradeByNameAndClassCommand(ALICE_NAME, null, "Quiz 1"));

        // EP: null assessment name
        assertThrows(NullPointerException.class, () ->
                new DeleteGradeByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, null));
    }

    @Test
    public void constructor_invalidAssessment_throwsAssertionError() {
        // EP: an assessment name that the parser would have rejected, which is a mistake of the caller
        assertThrows(AssertionError.class, () ->
                new DeleteGradeByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, "Quiz,1"));

        // EP: an assessment name that only has spaces
        assertThrows(AssertionError.class, () ->
                new DeleteGradeByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, "   "));
    }

    @Test
    public void execute_nameAndClassMatchExactly_deletesOnlyThatGrade() {
        // EP: a contact with several grades -> the other grade is kept, and so are the other contacts
        DeleteGradeByNameAndClassCommand command =
                new DeleteGradeByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, "Quiz 1");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceWithGrades, ALICE.withGrade(OTHER_GRADE));

        assertCommandSuccess(command, model,
                "Grade of 90.5 for Quiz 1 for student Alice Pauline in class A1 has been deleted", expectedModel);
    }

    @Test
    public void execute_nameAndClassMatchIgnoringCaseAndSpaces_deletesGrade() {
        // EP: the name with different case and extra spaces, the class in lower case, and the assessment name
        // in upper case with extra spaces
        assertGradeDeleted("  aLiCe   Pauline ", "a1", "QUIZ    1", "Quiz 1", "90.5");
    }

    @Test
    public void execute_nameWithFullWidthLetter_deletesGrade() {
        // EP: a letter of the name and a letter of the assessment name written in full width, which look the same
        assertGradeDeleted("Ａlice Pauline", ALICE_CLASS, "Ｑuiz 1", "Quiz 1", "90.5");
    }

    @Test
    public void execute_scoresInTheMessage_showsStoredScoreWithoutZerosAtTheEnd() {
        model.setPerson(aliceWithGrades, ALICE.withGrade(new Grade("Low", new BigDecimal("0")))
                .withGrade(new Grade("High", new BigDecimal("100.00")))
                .withGrade(new Grade("Decimal", new BigDecimal("97.5"))));

        // EP: the smallest score (boundary)
        assertGradeDeleted(ALICE_NAME, ALICE_CLASS, "Low", "Low", "0");

        // EP: the largest score written with zeros at the end -> the zeros are not shown (boundary)
        assertGradeDeleted(ALICE_NAME, ALICE_CLASS, "High", "High", "100");

        // EP: a score with a decimal part
        assertGradeDeleted(ALICE_NAME, ALICE_CLASS, "Decimal", "Decimal", "97.5");
    }

    @Test
    public void execute_onlyGradeOfContact_leavesNoGrade() {
        // EP: a contact with exactly one grade (boundary) -> no grade is left
        Person bensonWithGrade = BENSON.withGrade(GRADE);
        model.setPerson(BENSON, bensonWithGrade);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(bensonWithGrade, BENSON);

        assertCommandSuccess(new DeleteGradeByNameAndClassCommand(BENSON.getName().toString(),
                BENSON.getClassName().toString(), "Quiz 1"), model,
                "Grade of 90.5 for Quiz 1 for student Benson Meier in class A1 has been deleted", expectedModel);
    }

    @Test
    public void execute_sameNameInOtherClass_deletesOnlyMatchingStudentGrade() {
        // EP: two contacts have the same name and a grade for the same assessment, and the class tells them apart
        Person aliceInOtherClass = new PersonBuilder(ALICE).withClassName("B2").build().withGrade(GRADE);
        model.addPerson(aliceInOtherClass);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(aliceInOtherClass, new PersonBuilder(ALICE).withClassName("B2").build());

        assertCommandSuccess(new DeleteGradeByNameAndClassCommand(ALICE_NAME, "b2", "Quiz 1"), model,
                "Grade of 90.5 for Quiz 1 for student Alice Pauline in class B2 has been deleted", expectedModel);

        assertTrue(model.getAddressBook().getPersonList().contains(aliceWithGrades));
    }

    @Test
    public void execute_personHiddenByFilter_deletesGrade() {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        Person bensonWithGrade = BENSON.withGrade(GRADE);
        model.setPerson(BENSON, bensonWithGrade);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertFalse(model.getFilteredPersonList().contains(bensonWithGrade));

        DeleteGradeByNameAndClassCommand command = new DeleteGradeByNameAndClassCommand(
                BENSON.getName().toString(), BENSON.getClassName().toString(), "Quiz 1");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(bensonWithGrade, BENSON);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(command, model,
                "Grade of 90.5 for Quiz 1 for student Benson Meier in class A1 has been deleted", expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().contains(aliceWithGrades));
    }

    @Test
    public void execute_gradeNotFound_throwsCommandException() {
        // EP: an assessment that the contact has no grade for
        assertFailsWithGradeNotFound(ALICE_NAME, ALICE_CLASS, "Assignment 1");

        // EP: a name that is almost the name of a grade, with a space missing or with a character more
        assertFailsWithGradeNotFound(ALICE_NAME, ALICE_CLASS, "Quiz1");
        assertFailsWithGradeNotFound(ALICE_NAME, ALICE_CLASS, "Quiz 10");

        // EP: a contact with no grade at all
        assertFailsWithGradeNotFound(BENSON.getName().toString(), BENSON.getClassName().toString(), "Quiz 1");
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
        assertFailsWithContactNotFound("R@chel", "A1/B2");
    }

    @Test
    public void execute_emptyAddressBook_throwsCommandException() {
        // EP: there is no contact at all
        assertCommandFailure(new DeleteGradeByNameAndClassCommand(ALICE_NAME, ALICE_CLASS, "Quiz 1"),
                new ModelManager(), Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    @Test
    public void execute_noMatchingContactAndGradeNotFound_reportsContactNotFound() {
        // EP: a contact that does not exist and an assessment without a grade -> the contact is reported first
        assertFailsWithContactNotFound("Nobody Here", ALICE_CLASS, "Assignment 1");
    }

    @Test
    public void equals_variousCommands_followsEqualityContract() {
        DeleteGradeByNameAndClassCommand command =
                new DeleteGradeByNameAndClassCommand("Alice", "A1", "Quiz 1");

        // EP: the same object
        assertTrue(command.equals(command));

        // EP: the same values
        assertTrue(command.equals(new DeleteGradeByNameAndClassCommand("Alice", "A1", "Quiz 1")));

        // EP: a different name
        assertFalse(command.equals(new DeleteGradeByNameAndClassCommand("Bob", "A1", "Quiz 1")));

        // EP: a different class
        assertFalse(command.equals(new DeleteGradeByNameAndClassCommand("Alice", "B2", "Quiz 1")));

        // EP: a different assessment name
        assertFalse(command.equals(new DeleteGradeByNameAndClassCommand("Alice", "A1", "Exam")));

        // EP: the command that uses an index
        assertFalse(command.equals(new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1")));

        // EP: null
        assertFalse(command.equals(null));

        // EP: an object of another type
        assertFalse(command.equals(1));
    }

    @Test
    public void toString_validCommand_returnsFieldsRepresentation() {
        DeleteGradeByNameAndClassCommand command =
                new DeleteGradeByNameAndClassCommand("Alice", "A1", "Quiz 1");
        String expected = DeleteGradeByNameAndClassCommand.class.getCanonicalName()
                + "{name=Alice, className=A1, assessmentName=Quiz 1}";

        assertEquals(expected, command.toString());
    }

    /**
     * Deletes the grade for {@code typedAssessmentName} of the first contact, identified by {@code name} and
     * {@code className}, and checks that the message shows the stored name and the score as
     * {@code expectedStoredName} and {@code expectedScoreText}, and that the other grades are kept.
     */
    private void assertGradeDeleted(String name, String className, String typedAssessmentName,
            String expectedStoredName, String expectedScoreText) {
        Person person = model.getAddressBook().getPersonList().get(0);
        Grade gradeToDelete = person.getGradeRecords().stream()
                .filter(grade -> grade.getAssessmentName().equals(expectedStoredName))
                .findFirst().orElseThrow();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(person, person.withoutGrade(gradeToDelete));

        String expectedMessage = "Grade of " + expectedScoreText + " for " + expectedStoredName + " for student "
                + person.getName() + " in class " + person.getClassName() + " has been deleted";

        assertCommandSuccess(new DeleteGradeByNameAndClassCommand(name, className, typedAssessmentName), model,
                expectedMessage, expectedModel);
    }

    private void assertFailsWithContactNotFound(String name, String className) {
        assertFailsWithContactNotFound(name, className, "Quiz 1");
    }

    private void assertFailsWithContactNotFound(String name, String className, String assessmentName) {
        assertCommandFailure(new DeleteGradeByNameAndClassCommand(name, className, assessmentName), model,
                Messages.MESSAGE_CONTACT_NOT_FOUND);
    }

    private void assertFailsWithGradeNotFound(String name, String className, String assessmentName) {
        assertCommandFailure(new DeleteGradeByNameAndClassCommand(name, className, assessmentName), model,
                DeleteGradeCommand.MESSAGE_GRADE_NOT_FOUND);
    }

}

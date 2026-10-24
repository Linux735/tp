package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Grade;

public class JsonAdaptedGradeTest {

    private static final String MESSAGE_MISSING_NAME = "Grade assessment name is missing.";
    private static final String MESSAGE_MISSING_SCORE = "Grade score is missing.";
    private static final String MESSAGE_INVALID = "Grade assessment name or score is invalid.";

    @Test
    public void constructor_fromModel_roundTripKeepsNameAndScore() throws Exception {
        // EP: a score with a decimal part
        Grade decimal = new Grade("Assignment 1", new BigDecimal("97.5"));
        assertEquals(decimal, new JsonAdaptedGrade(decimal).toModelType());

        // EP: the smallest and the largest score (boundaries)
        Grade lowest = new Grade("Quiz", new BigDecimal("0"));
        Grade highest = new Grade("Quiz", new BigDecimal("100"));
        assertEquals(lowest, new JsonAdaptedGrade(lowest).toModelType());
        assertEquals(highest, new JsonAdaptedGrade(highest).toModelType());

        // EP: a score with many decimals
        Grade manyDecimals = new Grade("Quiz", new BigDecimal("0.000000000000000000000001"));
        assertEquals(manyDecimals, new JsonAdaptedGrade(manyDecimals).toModelType());

        // EP: an assessment name with symbols and accented letters
        Grade symbols = new Grade("Quiz #1 (Examen Finé)", new BigDecimal("50"));
        assertEquals(symbols, new JsonAdaptedGrade(symbols).toModelType());
    }

    @Test
    public void toModelType_validGrade_returnsGrade() throws Exception {
        // EP: a name and a score in the middle of the range
        assertEquals(new Grade("Assignment 1", new BigDecimal("97.5")), adapted("Assignment 1", "97.5"));

        // EP: the smallest and the largest score (boundaries)
        assertEquals(new Grade("Quiz", new BigDecimal("0")), adapted("Quiz", "0"));
        assertEquals(new Grade("Quiz", new BigDecimal("100")), adapted("Quiz", "100"));

        // EP: the shortest and the longest name (boundaries)
        assertEquals(new Grade("Q", new BigDecimal("50")), adapted("Q", "50"));
        String longestName = "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH);
        assertEquals(new Grade(longestName, new BigDecimal("50")), adapted(longestName, "50"));

        // EP: a name with spaces at the start and end -> the spaces are removed
        assertEquals("Quiz 1", new JsonAdaptedGrade("  Quiz 1  ", new BigDecimal("50")).toModelType()
                .getAssessmentName());
    }

    @Test
    public void toModelType_missingAssessmentName_throwsIllegalValueException() {
        // EP: no assessment name
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_NAME,
                new JsonAdaptedGrade(null, BigDecimal.TEN)::toModelType);
    }

    @Test
    public void toModelType_missingScore_throwsIllegalValueException() {
        // EP: no score
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_SCORE,
                new JsonAdaptedGrade("Quiz", null)::toModelType);
    }

    @Test
    public void toModelType_invalidAssessmentName_throwsIllegalValueException() {
        // EP: an empty name or only spaces (boundary, shorter than the shortest name)
        assertInvalid("", "50");
        assertInvalid("   ", "50");

        // EP: a name longer than the longest name (boundary)
        assertInvalid("a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH + 1), "50");

        // EP: a name with a comma
        assertInvalid("Exam,1", "50");

        // EP: a name with letters of another language
        assertInvalid("期中考试", "50");

        // EP: a name with a line break
        assertInvalid("Quiz\n1", "50");
    }

    @Test
    public void toModelType_invalidScore_throwsIllegalValueException() {
        // EP: a score just below the smallest score (boundary), and far below it
        assertInvalid("Quiz", "-0.1");
        assertInvalid("Quiz", "-50");

        // EP: a score just above the largest score (boundary), and far above it
        assertInvalid("Quiz", "100.1");
        assertInvalid("Quiz", "1000");
    }

    @Test
    public void toModelType_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: a missing name and a missing score -> the name
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_NAME,
                new JsonAdaptedGrade(null, null)::toModelType);

        // EP: a missing name and an invalid score -> the name
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_NAME,
                new JsonAdaptedGrade(null, new BigDecimal("101"))::toModelType);

        // EP: an invalid name and a missing score -> the score
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_SCORE,
                new JsonAdaptedGrade("Exam,1", null)::toModelType);

        // EP: an invalid name and an invalid score -> one message for both
        assertInvalid("Exam,1", "101");
    }

    private static Grade adapted(String assessmentName, String score) throws IllegalValueException {
        return new JsonAdaptedGrade(assessmentName, new BigDecimal(score)).toModelType();
    }

    private static void assertInvalid(String assessmentName, String score) {
        assertThrows(IllegalValueException.class, MESSAGE_INVALID,
                new JsonAdaptedGrade(assessmentName, new BigDecimal(score))::toModelType);
    }

}

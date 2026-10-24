package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class GradeTest {

    private static final BigDecimal VALID_SCORE = new BigDecimal("97");

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null assessment name
        assertThrows(NullPointerException.class, () -> new Grade(null, VALID_SCORE));

        // EP: null score
        assertThrows(NullPointerException.class, () -> new Grade("Quiz", null));
    }

    @Test
    public void constructor_validAssessmentName_success() {
        // EP: name with letters, digits and spaces
        assertEquals("Assignment 1", new Grade("Assignment 1", VALID_SCORE).getAssessmentName());

        // EP: name with symbols from a standard keyboard
        assertEquals("Quiz #1 (Part A)", new Grade("Quiz #1 (Part A)", VALID_SCORE).getAssessmentName());
        assertEquals("Lab 1/2 - Mid-term", new Grade("Lab 1/2 - Mid-term", VALID_SCORE).getAssessmentName());
        assertEquals("Exam_1", new Grade("Exam_1", VALID_SCORE).getAssessmentName());

        // EP: name with accented letters, typed as a letter followed by an accent -> stored as one character
        assertEquals("Examen Finé", new Grade("Examen Finé", VALID_SCORE).getAssessmentName());

        // EP: name with spaces at the start and end -> spaces are removed
        assertEquals("Quiz 1", new Grade("  Quiz 1  ", VALID_SCORE).getAssessmentName());

        // EP: name with the minimum length (boundary)
        assertEquals("Q", new Grade("Q", VALID_SCORE).getAssessmentName());

        // EP: name with the maximum length (boundary)
        String maxLengthName = "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH);
        assertEquals(maxLengthName, new Grade(maxLengthName, VALID_SCORE).getAssessmentName());
    }

    @Test
    public void constructor_invalidAssessmentName_throwsIllegalArgumentException() {
        // EP: empty name (boundary, shorter than the minimum length)
        assertThrows(IllegalArgumentException.class, () -> new Grade("", VALID_SCORE));

        // EP: name with only spaces
        assertThrows(IllegalArgumentException.class, () -> new Grade("   ", VALID_SCORE));

        // EP: name longer than the maximum length (boundary)
        assertThrows(IllegalArgumentException.class, () ->
                new Grade("a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH + 1), VALID_SCORE));

        // EP: name with a comma, the one symbol on a standard keyboard that is not allowed
        assertThrows(IllegalArgumentException.class, () -> new Grade("Exam,1", VALID_SCORE));

        // EP: name with letters of another language
        assertThrows(IllegalArgumentException.class, () -> new Grade("期中考试", VALID_SCORE));

        // EP: name with a line break
        assertThrows(IllegalArgumentException.class, () -> new Grade("Quiz\n1", VALID_SCORE));
    }

    @Test
    public void constructor_validScore_success() {
        // EP: score at the minimum (boundary)
        assertEquals(0, new Grade("Quiz", Grade.MIN_SCORE).getScore().compareTo(BigDecimal.ZERO));

        // EP: score just above the minimum (boundary)
        assertEquals(0, new Grade("Quiz", new BigDecimal("0.1")).getScore().compareTo(new BigDecimal("0.1")));

        // EP: score between the minimum and the maximum, with a decimal part
        assertEquals(0, new Grade("Quiz", new BigDecimal("97.5")).getScore().compareTo(new BigDecimal("97.5")));

        // EP: score just below the maximum (boundary)
        assertEquals(0, new Grade("Quiz", new BigDecimal("99.9")).getScore().compareTo(new BigDecimal("99.9")));

        // EP: score at the maximum (boundary)
        assertEquals(0, new Grade("Quiz", Grade.MAX_SCORE).getScore().compareTo(Grade.MAX_SCORE));
    }

    @Test
    public void constructor_scoreOutsideRange_throwsIllegalArgumentException() {
        // EP: score below the minimum (boundary and far below)
        assertThrows(IllegalArgumentException.class, () -> new Grade("Quiz", new BigDecimal("-0.1")));
        assertThrows(IllegalArgumentException.class, () -> new Grade("Quiz", new BigDecimal("-50")));

        // EP: score above the maximum (boundary and far above)
        assertThrows(IllegalArgumentException.class, () -> new Grade("Quiz", new BigDecimal("100.1")));
        assertThrows(IllegalArgumentException.class, () -> new Grade("Quiz", new BigDecimal("1000")));
    }

    @Test
    public void isValidScore_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Grade.isValidScore(null));
    }

    @Test
    public void isValidScore_variousScores_returnsExpected() {
        // EP: score below the minimum
        assertFalse(Grade.isValidScore(new BigDecimal("-0.001"))); // boundary
        assertFalse(Grade.isValidScore(new BigDecimal("-100")));

        // EP: score from the minimum to the maximum
        assertTrue(Grade.isValidScore(Grade.MIN_SCORE)); // boundary
        assertTrue(Grade.isValidScore(new BigDecimal("0.001"))); // boundary
        assertTrue(Grade.isValidScore(new BigDecimal("50")));
        assertTrue(Grade.isValidScore(new BigDecimal("99.999"))); // boundary
        assertTrue(Grade.isValidScore(Grade.MAX_SCORE)); // boundary

        // EP: score written with extra zeros has the same validity as the same number
        assertTrue(Grade.isValidScore(new BigDecimal("100.000")));
        assertTrue(Grade.isValidScore(new BigDecimal("0.0")));

        // EP: score above the maximum
        assertFalse(Grade.isValidScore(new BigDecimal("100.001"))); // boundary
        assertFalse(Grade.isValidScore(new BigDecimal("1000")));
    }

    @Test
    public void getAssessmentNameConstraintViolation_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Grade.getAssessmentNameConstraintViolation(null));
    }

    @Test
    public void getAssessmentNameConstraintViolation_variousNames_returnsExpected() {
        // EP: valid names, including the minimum and the maximum length (boundaries)
        assertEquals(Optional.empty(), Grade.getAssessmentNameConstraintViolation("Quiz 1"));
        assertEquals(Optional.empty(), Grade.getAssessmentNameConstraintViolation("Q"));
        assertEquals(Optional.empty(),
                Grade.getAssessmentNameConstraintViolation("a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH)));

        // EP: empty name
        assertEquals(Optional.of(Grade.MESSAGE_INVALID_ASSESSMENT_NAME),
                Grade.getAssessmentNameConstraintViolation(""));

        // EP: name longer than the maximum length (boundary)
        assertEquals(Optional.of(Grade.MESSAGE_INVALID_ASSESSMENT_NAME), Grade.getAssessmentNameConstraintViolation(
                "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH + 1)));

        // EP: name with a character that is not allowed
        assertEquals(Optional.of(Grade.MESSAGE_INVALID_ASSESSMENT_NAME),
                Grade.getAssessmentNameConstraintViolation("Exam,1"));
    }

    @Test
    public void hasAssessmentName_variousNames_returnsExpected() {
        Grade grade = new Grade("Quiz 1", VALID_SCORE);

        // EP: the same name
        assertTrue(grade.hasAssessmentName("Quiz 1"));

        // EP: the same name in a different case
        assertTrue(grade.hasAssessmentName("QUIZ 1"));

        // EP: the same name with extra spaces
        assertTrue(grade.hasAssessmentName("  quiz   1 "));

        // EP: the same name with a letter written in full width (looks the same)
        assertTrue(grade.hasAssessmentName("ＱＵＩＺ 1"));

        // EP: a different name, including one that only lacks a space
        assertFalse(grade.hasAssessmentName("Quiz 2"));
        assertFalse(grade.hasAssessmentName("Quiz1"));

        // EP: null
        assertFalse(grade.hasAssessmentName(null));
    }

    @Test
    public void isSameAssessment_variousGrades_returnsExpected() {
        Grade grade = new Grade("Quiz 1", VALID_SCORE);

        // EP: the same object
        assertTrue(grade.isSameAssessment(grade));

        // EP: the same assessment with a different score
        assertTrue(grade.isSameAssessment(new Grade("Quiz 1", new BigDecimal("80"))));

        // EP: the same assessment in a different case or with extra spaces
        assertTrue(grade.isSameAssessment(new Grade("quiz   1", new BigDecimal("80"))));
        assertTrue(grade.isSameAssessment(new Grade("  QUIZ 1 ", new BigDecimal("80"))));

        // EP: the same assessment with a letter written in full width (looks the same)
        assertTrue(grade.isSameAssessment(new Grade("Ｑuiz 1", BigDecimal.TEN)));

        // EP: a different assessment with the same score, including one that only lacks a space
        assertFalse(grade.isSameAssessment(new Grade("Quiz 2", VALID_SCORE)));
        assertFalse(grade.isSameAssessment(new Grade("Quiz1", VALID_SCORE)));

        // EP: null
        assertFalse(grade.isSameAssessment(null));
    }

    @Test
    public void equals_variousObjects_returnsExpected() {
        Grade grade = new Grade("Assignment 1", new BigDecimal("97.0"));

        // EP: the same object
        assertTrue(grade.equals(grade));

        // EP: the same values, with the assessment name in a different case and the score at a different scale
        assertTrue(grade.equals(new Grade("assignment 1", new BigDecimal("97"))));

        // EP: a different assessment name
        assertFalse(grade.equals(new Grade("Assignment 2", new BigDecimal("97"))));

        // EP: a different score
        assertFalse(grade.equals(new Grade("Assignment 1", new BigDecimal("96"))));

        // EP: null
        assertFalse(grade.equals(null));

        // EP: an object of another type
        assertFalse(grade.equals("not a Grade"));
    }

    @Test
    public void hashCode_variousGrades_equalGradesHaveEqualHashCodes() {
        // EP: the assessment name in a different case or with extra spaces, and the score at a different scale
        Grade grade = new Grade("Quiz 1", new BigDecimal("97"));
        assertEquals(grade.hashCode(), new Grade("quiz    1", new BigDecimal("97.0")).hashCode());
    }

    @Test
    public void toString_variousGrades_returnsNameAndScore() {
        // EP: score without a decimal part
        assertEquals("Quiz: 100", new Grade("Quiz", new BigDecimal("100")).toString());

        // EP: score with a decimal part
        assertEquals("Quiz: 97.5", new Grade("Quiz", new BigDecimal("97.5")).toString());

        // EP: score with zeros at the end -> the zeros are omitted
        assertEquals("Assignment 1: 97.5", new Grade("Assignment 1", new BigDecimal("97.50")).toString());
        assertEquals("Quiz: 100", new Grade("Quiz", new BigDecimal("100.00")).toString());

        // EP: assessment name with extra spaces -> the name is shown as given
        assertEquals("quiz   1: 97", new Grade("quiz   1", new BigDecimal("97")).toString());
    }

}

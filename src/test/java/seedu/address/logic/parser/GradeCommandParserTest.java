package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_ASSESSMENT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CLASS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.GradeCommandParser.MESSAGE_UNKNOWN_PARAMETER;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_BOTH_CONTACT_IDENTIFIERS;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_MISSING_CONTACT_IDENTIFIER;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_PREFIX_WITHOUT_SPACE;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.GradeByIndexCommand;
import seedu.address.logic.commands.GradeByNameAndClassCommand;
import seedu.address.logic.commands.GradeCommand;
import seedu.address.model.person.Grade;

/**
 * The inputs of the command are the contact (an index, or a name and a class), the assessment name and the
 * score. The assessment name and the score have their own tests in {@code ParserUtilTest}, so here they are
 * only tested for how they combine with the other inputs. Each invalid input is tested alone before it is
 * tested together with another invalid input.
 */
public class GradeCommandParserTest {

    private static final String MESSAGE_MISSING_SCORE = "Score missing";

    private final GradeCommandParser parser = new GradeCommandParser();

    @Test
    public void parse_validIndexArguments_success() {
        // EP: contact by index
        assertParseSuccess(parser, "5 /assessment Assignment 1 /score 97", byIndex(5, "Assignment 1", "97"));
    }

    @Test
    public void parse_validNameAndClassArguments_success() {
        // EP: contact by name and class
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /assessment Assignment 1 /score 97",
                byNameAndClass("Alex Yeoh", "A1", "Assignment 1", "97"));
    }

    @Test
    public void parse_indexBoundaries_success() {
        // EP: the smallest index (boundary)
        assertParseSuccess(parser, "1 /assessment Quiz /score 90", byIndex(1, "Quiz", "90"));

        // EP: the largest index that fits in an integer (boundary)
        assertParseSuccess(parser, "2147483647 /assessment Quiz /score 90",
                byIndex(Integer.MAX_VALUE, "Quiz", "90"));
    }

    @Test
    public void parse_scoreBoundariesAndDecimals_success() {
        // EP: the smallest score (boundary)
        assertParseSuccess(parser, "1 /assessment Quiz /score 0", byIndex(1, "Quiz", "0"));

        // EP: the largest score (boundary)
        assertParseSuccess(parser, "1 /assessment Quiz /score 100", byIndex(1, "Quiz", "100"));

        // EP: a score with a decimal part
        assertParseSuccess(parser, "1 /assessment Quiz /score 97.5", byIndex(1, "Quiz", "97.5"));

        // EP: the score is used with the contact in either form
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /assessment Quiz /score 0",
                byNameAndClass("Alex Yeoh", "A1", "Quiz", "0"));
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /assessment Quiz /score 100",
                byNameAndClass("Alex Yeoh", "A1", "Quiz", "100"));
    }

    @Test
    public void parse_assessmentNameVariations_success() {
        // EP: a name with symbols from a standard keyboard
        assertParseSuccess(parser, "1 /assessment Quiz #1 (Part A) /score 90", byIndex(1, "Quiz #1 (Part A)", "90"));

        // EP: a name with a slash that is not a parameter
        assertParseSuccess(parser, "1 /assessment Lab 1/2 /score 90", byIndex(1, "Lab 1/2", "90"));

        // EP: a name with accented letters
        assertParseSuccess(parser, "1 /assessment Examen Finalé /score 90", byIndex(1, "Examen Finalé", "90"));

        // EP: a name with the longest length (boundary)
        String longestName = "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH);
        assertParseSuccess(parser, "1 /assessment " + longestName + " /score 90", byIndex(1, longestName, "90"));
    }

    @Test
    public void parse_parametersInAnyOrder_success() {
        // EP: the score before the assessment name
        assertParseSuccess(parser, "2 /score 97.5 /assessment Quiz-1", byIndex(2, "Quiz-1", "97.5"));

        // EP: the assessment name and the score before the name and the class, and the class before the name
        assertParseSuccess(parser, " /score 97 /assessment Quiz /class A1 /name Alex Yeoh",
                byNameAndClass("Alex Yeoh", "A1", "Quiz", "97"));
    }

    @Test
    public void parse_prefixesInAnyCase_success() {
        // EP: prefixes in upper case or in mixed case
        assertParseSuccess(parser, "5 /ASSESSMENT Assignment 1 /Score 97", byIndex(5, "Assignment 1", "97"));
        assertParseSuccess(parser, " /NAME Alex Yeoh /Class A1 /assessment Quiz /score 97",
                byNameAndClass("Alex Yeoh", "A1", "Quiz", "97"));
    }

    @Test
    public void parse_extraSpacesInNameAndClass_success() {
        // EP: spaces around a value are removed, but the spaces inside a value are kept as typed
        assertParseSuccess(parser, " /name   alex   yeoh  /class  a1 /assessment Assignment 1 /score 97",
                byNameAndClass("alex   yeoh", "a1", "Assignment 1", "97"));
    }

    @Test
    public void parse_nameAndClassThatNoContactCanHave_success() {
        // EP: a name and a class that are not valid for a contact are accepted, and are not found when the
        // command runs
        assertParseSuccess(parser, " /name @@@, /class !!! /assessment Assignment 1 /score 97",
                byNameAndClass("@@@,", "!!!", "Assignment 1", "97"));
    }

    @Test
    public void parse_noContact_failure() {
        // EP: neither an index, nor a name, nor a class
        assertParseFailure(parser, " /assessment Assignment 1 /score 97", MESSAGE_MISSING_CONTACT_IDENTIFIER);

        // EP: a name and a class that only have spaces after them
        assertParseFailure(parser, " /name  /class  /assessment Assignment 1 /score 97",
                MESSAGE_MISSING_CONTACT_IDENTIFIER);
    }

    @Test
    public void parse_indexTogetherWithNameOrClass_failure() {
        // EP: an index with a name and a class
        assertParseFailure(parser, "1 /name Alex Yeoh /class A1 /assessment Assignment 1 /score 97",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: an index with only a name
        assertParseFailure(parser, "1 /name Alex Yeoh /assessment Assignment 1 /score 97",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: an index with only a class
        assertParseFailure(parser, "1 /class A1 /assessment Assignment 1 /score 97",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);
    }

    @Test
    public void parse_missingNameOrClass_failure() {
        // EP: a class without a name
        assertParseFailure(parser, " /class A1 /assessment Assignment 1 /score 97", MESSAGE_MISSING_NAME);

        // EP: a name without a class
        assertParseFailure(parser, " /name Alex Yeoh /assessment Assignment 1 /score 97", MESSAGE_MISSING_CLASS);

        // EP: a class and a name that only has spaces after it
        assertParseFailure(parser, " /name   /class A1 /assessment Assignment 1 /score 97", MESSAGE_MISSING_NAME);

        // EP: a name and a class that only has spaces after it
        assertParseFailure(parser, " /name Alex Yeoh /class   /assessment Assignment 1 /score 97",
                MESSAGE_MISSING_CLASS);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // EP: zero, the number just below the smallest index (boundary)
        assertParseFailure(parser, "0 /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);

        // EP: a negative number
        assertParseFailure(parser, "-1 /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);

        // EP: a number larger than the largest integer (boundary)
        assertParseFailure(parser, "2147483648 /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);

        // EP: text that is not a whole number
        assertParseFailure(parser, "abc /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1.5 /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);

        // EP: two numbers
        assertParseFailure(parser, "1 2 /assessment Quiz /score 90", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_missingAssessmentName_failure() {
        // EP: no assessment name, contact by index
        assertParseFailure(parser, "1 /score 97", MESSAGE_MISSING_ASSESSMENT);

        // EP: no assessment name, contact by name and class
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /score 97", MESSAGE_MISSING_ASSESSMENT);

        // EP: an assessment name that only has spaces after it
        assertParseFailure(parser, "1 /assessment   /score 97", MESSAGE_MISSING_ASSESSMENT);
    }

    @Test
    public void parse_invalidAssessmentName_failure() {
        // EP: a name with a comma
        assertParseFailure(parser, "1 /assessment Exam,1 /score 90", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: a name that is longer than the longest length (boundary)
        assertParseFailure(parser,
                "1 /assessment " + "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH + 1) + " /score 90",
                Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: a name with letters of another language
        assertParseFailure(parser, "1 /assessment 期中考试 /score 90", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);
    }

    @Test
    public void parse_prefixStuckToAssessmentName_failure() {
        // EP: a parameter stuck to the end of the name, with and without text after it
        assertParseFailure(parser, "1 /assessment Quiz/score 90 /score 80",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/score"));
        assertParseFailure(parser, "1 /assessment Quiz/name /score 80",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/name"));
    }

    @Test
    public void parse_missingScore_failure() {
        // EP: no score, contact by index
        assertParseFailure(parser, "1 /assessment Assignment 1", MESSAGE_MISSING_SCORE);

        // EP: no score, contact by name and class
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /assessment Assignment 1", MESSAGE_MISSING_SCORE);

        // EP: a score that only has spaces after it
        assertParseFailure(parser, "1 /assessment Assignment 1 /score   ", MESSAGE_MISSING_SCORE);
    }

    @Test
    public void parse_invalidScore_failure() {
        // EP: text that is not a number
        assertParseFailure(parser, "1 /assessment Assignment 1 /score A", Grade.MESSAGE_INVALID_SCORE);

        // EP: a score below the smallest score
        assertParseFailure(parser, "1 /assessment Assignment 1 /score -1", Grade.MESSAGE_INVALID_SCORE);

        // EP: a score just above the largest score (boundary)
        assertParseFailure(parser, "1 /assessment Assignment 1 /score 100.1", Grade.MESSAGE_INVALID_SCORE);

        // EP: a score far above the largest score
        assertParseFailure(parser, "1 /assessment Assignment 1 /score 1000", Grade.MESSAGE_INVALID_SCORE);
    }

    @Test
    public void parse_unknownParameter_failure() {
        // EP: a parameter that no command accepts
        assertParseFailure(parser, "1 /assessment Quiz 1 /score 90 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a parameter that another command accepts
        assertParseFailure(parser, "1 /assessment Quiz 1 /score 90 /date 16-09-2026", MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 /assessment Quiz 1 /score 90 /email a@b.com", MESSAGE_UNKNOWN_PARAMETER);

        // EP: only an unknown parameter
        assertParseFailure(parser, "1 /foo bar", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_unknownParameterMessage_listsEveryAcceptedParameter() {
        assertEquals("Unknown parameter. Use /name, /class, /assessment or /score", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        // EP: the assessment name is repeated
        assertParseFailure(parser, "1 /assessment Quiz /assessment Exam /score 90", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the score is repeated
        assertParseFailure(parser, "1 /assessment Quiz /score 90 /score 91", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the name is repeated
        assertParseFailure(parser, " /name Alex Yeoh /name John Tan /class A1 /assessment Quiz /score 90",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the class is repeated
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /class A2 /assessment Quiz /score 90",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: two parameters are repeated
        assertParseFailure(parser, "1 /assessment Quiz /assessment Exam /score 90 /score 95",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: a parameter repeated in a different case
        assertParseFailure(parser, "1 /assessment Quiz /ASSESSMENT Exam /score 90", MESSAGE_DUPLICATE_PARAMETERS);
    }

    @Test
    public void parse_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: an unknown parameter and a repeated parameter -> the unknown parameter
        assertParseFailure(parser, "1 /assessment A /assessment B /score 90 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a repeated parameter and an index with a name and a class -> the repeated parameter
        assertParseFailure(parser, "1 /name A /name B /class A1 /assessment Quiz /score 90",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: an index with a name and a class, and a missing assessment name -> the contact
        assertParseFailure(parser, "1 /name Alex Yeoh /class A1 /score 90", MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: no contact and a missing assessment name -> the contact
        assertParseFailure(parser, " /score 90", MESSAGE_MISSING_CONTACT_IDENTIFIER);

        // EP: a missing name and a missing assessment name -> the name
        assertParseFailure(parser, " /class A1 /score 90", MESSAGE_MISSING_NAME);

        // EP: a missing assessment name and a missing score -> the assessment name
        assertParseFailure(parser, "1", MESSAGE_MISSING_ASSESSMENT);

        // EP: a missing assessment name and an invalid score -> the assessment name
        assertParseFailure(parser, "1 /score abc", MESSAGE_MISSING_ASSESSMENT);

        // EP: a missing score and an invalid assessment name -> the score
        assertParseFailure(parser, "1 /assessment Exam,1", MESSAGE_MISSING_SCORE);

        // EP: a stuck parameter and an invalid score -> the stuck parameter
        assertParseFailure(parser, "1 /assessment Quiz/score /score abc",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/score"));

        // EP: an invalid assessment name and an invalid score -> the assessment name
        assertParseFailure(parser, "1 /assessment Exam,1 /score abc", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: an invalid score and an invalid index -> the score
        assertParseFailure(parser, "0 /assessment Quiz /score abc", Grade.MESSAGE_INVALID_SCORE);

        // EP: an invalid assessment name and an invalid index -> the assessment name
        assertParseFailure(parser, "0 /assessment Exam,1 /score 90", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);
    }

    private static GradeCommand byIndex(int oneBasedIndex, String assessmentName, String score) {
        return new GradeByIndexCommand(Index.fromOneBased(oneBasedIndex),
                new Grade(assessmentName, new BigDecimal(score)));
    }

    private static GradeCommand byNameAndClass(String name, String className, String assessmentName,
            String score) {
        return new GradeByNameAndClassCommand(name, className, new Grade(assessmentName, new BigDecimal(score)));
    }

}

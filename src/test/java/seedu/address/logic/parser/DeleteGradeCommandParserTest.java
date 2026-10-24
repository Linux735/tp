package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_ASSESSMENT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CLASS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DeleteGradeCommandParser.MESSAGE_UNKNOWN_PARAMETER;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_PREFIX_WITHOUT_SPACE;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteGradeByIndexCommand;
import seedu.address.logic.commands.DeleteGradeByNameAndClassCommand;
import seedu.address.logic.commands.DeleteGradeCommand;
import seedu.address.model.person.Grade;

/**
 * The inputs of the command are the contact (an index, or a name and a class) and the assessment name. The
 * assessment name has its own tests in {@code ParserUtilTest}, so here it is only tested for how it combines
 * with the contact. Each invalid input is tested alone before it is tested together with another invalid
 * input.
 */
public class DeleteGradeCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteGradeCommand.MESSAGE_USAGE);

    private final DeleteGradeCommandParser parser = new DeleteGradeCommandParser();

    @Test
    public void parse_validIndex_success() {
        // EP: contact by index
        assertParseSuccess(parser, "1 /assessment Quiz 1",
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1"));
    }

    @Test
    public void parse_indexBoundaries_success() {
        // EP: an index with several digits
        assertParseSuccess(parser, "25 /assessment Quiz 1",
                new DeleteGradeByIndexCommand(Index.fromOneBased(25), "Quiz 1"));

        // EP: the largest index that fits in an integer (boundary)
        assertParseSuccess(parser, "2147483647 /assessment Quiz 1",
                new DeleteGradeByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), "Quiz 1"));
    }

    @Test
    public void parse_validNameAndClass_success() {
        DeleteGradeByNameAndClassCommand expected =
                new DeleteGradeByNameAndClassCommand("John Tan", "A1", "Quiz 1");

        // EP: contact by name and class
        assertParseSuccess(parser, " /name John Tan /class A1 /assessment Quiz 1", expected);

        // EP: the assessment name before the class and the name
        assertParseSuccess(parser, " /assessment Quiz 1 /class A1 /name John Tan", expected);
    }

    @Test
    public void parse_assessmentNameVariations_success() {
        // EP: a name with symbols from a standard keyboard
        assertParseSuccess(parser, "1 /assessment Quiz #1 (Part A)",
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz #1 (Part A)"));

        // EP: a name with a slash that is not a parameter
        assertParseSuccess(parser, "1 /assessment Lab 1/2",
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Lab 1/2"));

        // EP: a name with accented letters
        assertParseSuccess(parser, "1 /assessment Examen Finalé",
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Examen Finalé"));

        // EP: a name with the longest length (boundary)
        String longestName = "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH);
        assertParseSuccess(parser, "1 /assessment " + longestName,
                new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, longestName));
    }

    @Test
    public void parse_prefixesInAnyCase_success() {
        // EP: prefixes in upper case or in mixed case
        assertParseSuccess(parser, " /Name John Tan /CLASS A1 /ASSESSMENT Quiz #1 (Part A)",
                new DeleteGradeByNameAndClassCommand("John Tan", "A1", "Quiz #1 (Part A)"));
        assertParseSuccess(parser, "1 /Assessment Quiz 1", new DeleteGradeByIndexCommand(INDEX_FIRST_PERSON, "Quiz 1"));
    }

    @Test
    public void parse_extraSpacesInNameAndClass_success() {
        // EP: spaces around a value are removed, but the spaces inside a value are kept as typed
        assertParseSuccess(parser, " /name   john   tan  /class  a1 /assessment Quiz 1",
                new DeleteGradeByNameAndClassCommand("john   tan", "a1", "Quiz 1"));
    }

    @Test
    public void parse_nameAndClassThatNoContactCanHave_success() {
        // EP: a name and a class that are not valid for a contact are accepted, and are not found when the
        // command runs
        assertParseSuccess(parser, " /name @@@, /class !!! /assessment Quiz 1",
                new DeleteGradeByNameAndClassCommand("@@@,", "!!!", "Quiz 1"));
    }

    @Test
    public void parse_invalidIndex_failure() {
        // EP: zero, the number just below the smallest index (boundary)
        assertParseFailure(parser, "0 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: a negative number
        assertParseFailure(parser, "-1 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: a number larger than the largest integer (boundary)
        assertParseFailure(parser, "2147483648 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: text that is not a whole number
        assertParseFailure(parser, "abc /assessment Quiz 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: two numbers
        assertParseFailure(parser, "1 2 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: no index and no name and class
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " /assessment Quiz 1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexTogetherWithNameOrClass_failure() {
        // EP: an index with a name and a class
        assertParseFailure(parser, "1 /name John Tan /class A1 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: an index with only a name
        assertParseFailure(parser, "1 /name John Tan /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: an index with only a class
        assertParseFailure(parser, "1 /class A1 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);

        // EP: text that is not an index before a name and a class
        assertParseFailure(parser, "John /name John Tan /class A1 /assessment Quiz 1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingNameOrClass_failure() {
        // EP: a class without a name
        assertParseFailure(parser, " /class A1 /assessment Quiz 1", MESSAGE_MISSING_NAME);

        // EP: a name without a class
        assertParseFailure(parser, " /name John Tan /assessment Quiz 1", MESSAGE_MISSING_CLASS);

        // EP: a class and a name that only has spaces after it
        assertParseFailure(parser, " /name   /class A1 /assessment Quiz 1", MESSAGE_MISSING_NAME);

        // EP: a name and a class that only has spaces after it
        assertParseFailure(parser, " /name John Tan /class   /assessment Quiz 1", MESSAGE_MISSING_CLASS);
    }

    @Test
    public void parse_missingAssessmentName_failure() {
        // EP: no assessment name, contact by index
        assertParseFailure(parser, "1", MESSAGE_MISSING_ASSESSMENT);

        // EP: no assessment name, contact by name and class
        assertParseFailure(parser, " /name John Tan /class A1", MESSAGE_MISSING_ASSESSMENT);

        // EP: an assessment name that only has spaces after it
        assertParseFailure(parser, "1 /assessment   ", MESSAGE_MISSING_ASSESSMENT);
        assertParseFailure(parser, " /name John Tan /class A1 /assessment   ", MESSAGE_MISSING_ASSESSMENT);
    }

    @Test
    public void parse_invalidAssessmentName_failure() {
        // EP: a name with a comma
        assertParseFailure(parser, "1 /assessment Quiz,1", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: a name that is longer than the longest length (boundary)
        assertParseFailure(parser, "1 /assessment " + "a".repeat(Grade.MAX_ASSESSMENT_NAME_LENGTH + 1),
                Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: a name with letters of another language
        assertParseFailure(parser, "1 /assessment 期中考试", Grade.MESSAGE_INVALID_ASSESSMENT_NAME);

        // EP: an invalid name, contact by name and class
        assertParseFailure(parser, " /name John Tan /class A1 /assessment Quiz,1",
                Grade.MESSAGE_INVALID_ASSESSMENT_NAME);
    }

    @Test
    public void parse_prefixStuckToAssessmentName_failure() {
        // EP: a parameter stuck to the name, with and without text after it
        assertParseFailure(parser, "1 /assessment Quiz/name John",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/name"));
        assertParseFailure(parser, "1 /assessment Quiz/class",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/class"));
    }

    @Test
    public void parse_unknownParameter_failure() {
        // EP: a parameter that no command accepts
        assertParseFailure(parser, "1 /assessment Quiz /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a parameter that another command accepts
        assertParseFailure(parser, "1 /assessment Quiz /score 90", MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 /assessment Quiz /date 16-09-2026", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_unknownParameterMessage_listsEveryAcceptedParameter() {
        assertEquals("Unknown parameter. Use /name, /class or /assessment", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        // EP: the assessment name is repeated
        assertParseFailure(parser, "1 /assessment Quiz /assessment Exam", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the name is repeated
        assertParseFailure(parser, " /name John Tan /name Jane Doe /class A1 /assessment Quiz",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the class is repeated
        assertParseFailure(parser, " /name John Tan /class A1 /class A2 /assessment Quiz",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: a parameter repeated in a different case
        assertParseFailure(parser, "1 /assessment Quiz /ASSESSMENT Exam", MESSAGE_DUPLICATE_PARAMETERS);
    }

    @Test
    public void parse_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: an unknown parameter and a repeated parameter -> the unknown parameter
        assertParseFailure(parser, "1 /assessment Quiz /assessment Exam /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a repeated parameter and an index with a name -> the repeated parameter
        assertParseFailure(parser, "1 /name A /name B /assessment Quiz", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: an invalid index and a missing assessment name -> the index
        assertParseFailure(parser, "0", MESSAGE_INVALID_FORMAT);

        // EP: an invalid index and an invalid assessment name -> the index
        assertParseFailure(parser, "0 /assessment Quiz,1", MESSAGE_INVALID_FORMAT);

        // EP: text before a name and a class, and a missing name -> the text before
        assertParseFailure(parser, "John /class A1 /assessment Quiz", MESSAGE_INVALID_FORMAT);

        // EP: a missing name and a missing assessment name -> the name
        assertParseFailure(parser, " /class A1", MESSAGE_MISSING_NAME);

        // EP: a missing class and a missing assessment name -> the class
        assertParseFailure(parser, " /name John Tan", MESSAGE_MISSING_CLASS);

        // EP: a missing name and an invalid assessment name -> the name
        assertParseFailure(parser, " /class A1 /assessment Quiz,1", MESSAGE_MISSING_NAME);

        // EP: a stuck parameter and an invalid character in the name -> the stuck parameter
        assertParseFailure(parser, "1 /assessment Quiz,/name",
                String.format(MESSAGE_PREFIX_WITHOUT_SPACE, "assessment name", "/name"));
    }

}

package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CLASS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_DATE;
import static seedu.address.logic.Messages.MESSAGE_MISSING_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DeleteAttendanceCommandParser.MESSAGE_UNKNOWN_PARAMETER;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_DATE;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteAttendanceByIndexCommand;
import seedu.address.logic.commands.DeleteAttendanceByNameAndClassCommand;
import seedu.address.logic.commands.DeleteAttendanceCommand;

/**
 * The inputs of the command are the contact (an index, or a name and a class) and the date. The date has its
 * own tests in {@code ParserUtilTest}, so here it is only tested for how it combines with the contact. Each
 * invalid input is tested alone before it is tested together with another invalid input.
 */
public class DeleteAttendanceCommandParserTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteAttendanceCommand.MESSAGE_USAGE);

    private final DeleteAttendanceCommandParser parser = new DeleteAttendanceCommandParser();

    @Test
    public void parse_validIndex_success() {
        // EP: contact by index
        assertParseSuccess(parser, "1 /date 16-09-2026",
                new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE));
    }

    @Test
    public void parse_indexBoundaries_success() {
        // EP: an index with several digits
        assertParseSuccess(parser, "25 /date 16-09-2026",
                new DeleteAttendanceByIndexCommand(Index.fromOneBased(25), DATE));

        // EP: the largest index that fits in an integer (boundary)
        assertParseSuccess(parser, "2147483647 /date 16-09-2026",
                new DeleteAttendanceByIndexCommand(Index.fromOneBased(Integer.MAX_VALUE), DATE));
    }

    @Test
    public void parse_validNameAndClass_success() {
        DeleteAttendanceByNameAndClassCommand expected =
                new DeleteAttendanceByNameAndClassCommand("John Tan", "A1", DATE);

        // EP: contact by name and class
        assertParseSuccess(parser, " /name John Tan /class A1 /date 16-09-2026", expected);

        // EP: the date before the class and the name
        assertParseSuccess(parser, " /date 16-09-2026 /class A1 /name John Tan", expected);
    }

    @Test
    public void parse_prefixesInAnyCase_success() {
        // EP: prefixes in upper case or in mixed case
        assertParseSuccess(parser, " /Name John Tan /CLASS A1 /DATE 16-09-2026",
                new DeleteAttendanceByNameAndClassCommand("John Tan", "A1", DATE));
        assertParseSuccess(parser, "1 /Date 16-09-2026", new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON, DATE));
    }

    @Test
    public void parse_extraSpacesInNameAndClass_success() {
        // EP: spaces around a value are removed, but the spaces inside a value are kept as typed
        assertParseSuccess(parser, " /name   john   tan  /class  a1 /date 16-09-2026",
                new DeleteAttendanceByNameAndClassCommand("john   tan", "a1", DATE));
    }

    @Test
    public void parse_nameAndClassThatNoContactCanHave_success() {
        // EP: a name and a class that are not valid for a contact are accepted, and are not found when the
        // command runs
        assertParseSuccess(parser, " /name @@@, /class !!! /date 16-09-2026",
                new DeleteAttendanceByNameAndClassCommand("@@@,", "!!!", DATE));
    }

    @Test
    public void parse_datesThatOtherCommandsRefuse_success() {
        // EP: a date in the future, which only the command that records attendance refuses
        assertParseSuccess(parser, "1 /date 31-12-9999", new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON,
                LocalDate.of(9999, 12, 31)));

        // EP: a date that only exists in a leap year
        assertParseSuccess(parser, "1 /date 29-02-2024", new DeleteAttendanceByIndexCommand(INDEX_FIRST_PERSON,
                LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void parse_invalidIndex_failure() {
        // EP: zero, the number just below the smallest index (boundary)
        assertParseFailure(parser, "0 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: a negative number
        assertParseFailure(parser, "-1 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: a number larger than the largest integer (boundary)
        assertParseFailure(parser, "2147483648 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: text that is not a whole number
        assertParseFailure(parser, "abc /date 16-09-2026", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: two numbers
        assertParseFailure(parser, "1 2 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: no index and no name and class
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " /date 16-09-2026", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_indexTogetherWithNameOrClass_failure() {
        // EP: an index with a name and a class
        assertParseFailure(parser, "1 /name John Tan /class A1 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: an index with only a name
        assertParseFailure(parser, "1 /name John Tan /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: an index with only a class
        assertParseFailure(parser, "1 /class A1 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: text that is not an index before a name and a class
        assertParseFailure(parser, "John /name John Tan /class A1 /date 16-09-2026", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingNameOrClass_failure() {
        // EP: a class without a name
        assertParseFailure(parser, " /class A1 /date 16-09-2026", MESSAGE_MISSING_NAME);

        // EP: a name without a class
        assertParseFailure(parser, " /name John Tan /date 16-09-2026", MESSAGE_MISSING_CLASS);

        // EP: a class and a name that only has spaces after it
        assertParseFailure(parser, " /name   /class A1 /date 16-09-2026", MESSAGE_MISSING_NAME);

        // EP: a name and a class that only has spaces after it
        assertParseFailure(parser, " /name John Tan /class   /date 16-09-2026", MESSAGE_MISSING_CLASS);
    }

    @Test
    public void parse_missingDate_failure() {
        // EP: no date, contact by index
        assertParseFailure(parser, "1", MESSAGE_MISSING_DATE);

        // EP: no date, contact by name and class
        assertParseFailure(parser, " /name John Tan /class A1", MESSAGE_MISSING_DATE);

        // EP: a date that only has spaces after it
        assertParseFailure(parser, "1 /date   ", MESSAGE_MISSING_DATE);
        assertParseFailure(parser, " /name John Tan /class A1 /date   ", MESSAGE_MISSING_DATE);
    }

    @Test
    public void parse_invalidDate_failure() {
        // EP: a date that does not exist, a date in another layout, and a day with one digit; the other
        // dates are tested in ParserUtilTest
        assertParseFailure(parser, "1 /date 31-02-2026", MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 /date 2026-09-16", MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 /date 16-9-2026", MESSAGE_INVALID_DATE);

        // EP: an invalid date, contact by name and class
        assertParseFailure(parser, " /name John Tan /class A1 /date 31-02-2026", MESSAGE_INVALID_DATE);
    }

    @Test
    public void parse_unknownParameter_failure() {
        // EP: a parameter that no command accepts
        assertParseFailure(parser, "1 /date 16-09-2026 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a parameter that another command accepts
        assertParseFailure(parser, "1 /date 16-09-2026 /status present", MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 /date 16-09-2026 /assessment Quiz", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_unknownParameterMessage_listsEveryAcceptedParameter() {
        assertEquals("Unknown parameter. Use /name, /class or /date", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        // EP: the date is repeated
        assertParseFailure(parser, "1 /date 16-09-2026 /date 17-09-2026", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the name is repeated
        assertParseFailure(parser, " /name John Tan /name Jane Doe /class A1 /date 16-09-2026",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the class is repeated
        assertParseFailure(parser, " /name John Tan /class A1 /class A2 /date 16-09-2026",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: a parameter repeated in a different case
        assertParseFailure(parser, "1 /date 16-09-2026 /DATE 17-09-2026", MESSAGE_DUPLICATE_PARAMETERS);
    }

    @Test
    public void parse_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: an unknown parameter and a repeated parameter -> the unknown parameter
        assertParseFailure(parser, "1 /date 16-09-2026 /date 17-09-2026 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a repeated parameter and an index with a name -> the repeated parameter
        assertParseFailure(parser, "1 /name A /name B /date 16-09-2026", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: an invalid index and a missing date -> the index
        assertParseFailure(parser, "0", MESSAGE_INVALID_FORMAT);

        // EP: an invalid index and an invalid date -> the index
        assertParseFailure(parser, "0 /date 31-02-2026", MESSAGE_INVALID_FORMAT);

        // EP: text before a name and a class, and a missing name -> the text before
        assertParseFailure(parser, "John /class A1 /date 16-09-2026", MESSAGE_INVALID_FORMAT);

        // EP: a missing name and a missing date -> the name
        assertParseFailure(parser, " /class A1", MESSAGE_MISSING_NAME);

        // EP: a missing class and a missing date -> the class
        assertParseFailure(parser, " /name John Tan", MESSAGE_MISSING_CLASS);

        // EP: a missing name and an invalid date -> the name
        assertParseFailure(parser, " /class A1 /date 31-02-2026", MESSAGE_MISSING_NAME);
    }

}

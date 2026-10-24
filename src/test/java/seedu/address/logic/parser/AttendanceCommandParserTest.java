package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CLASS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_DATE;
import static seedu.address.logic.Messages.MESSAGE_MISSING_NAME;
import static seedu.address.logic.parser.AttendanceCommandParser.MESSAGE_UNKNOWN_PARAMETER;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_BOTH_CONTACT_IDENTIFIERS;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_DATE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_MISSING_CONTACT_IDENTIFIER;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AttendanceByIndexCommand;
import seedu.address.logic.commands.AttendanceByNameAndClassCommand;
import seedu.address.logic.commands.AttendanceCommand;
import seedu.address.model.person.Attendance;

/**
 * The inputs of the command are the contact (an index, or a name and a class), the date and the status. The
 * date has its own tests in {@code ParserUtilTest}, so here it is only tested for how it combines with the
 * other inputs. Each invalid input is tested alone before it is tested together with another invalid input.
 */
public class AttendanceCommandParserTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);
    private static final String MESSAGE_MISSING_STATUS = "Missing status. Please provide the attendance status.";
    private static final String MESSAGE_INVALID_STATUS =
            "Invalid status. Please use 'present', 'absent' or 'late'.";

    private final AttendanceCommandParser parser = new AttendanceCommandParser();

    @Test
    public void parse_indexAndEachStatus_success() {
        // EP: contact by index, status present
        assertParseSuccess(parser, "1 /date 16-09-2026 /status present", byIndex(1, Attendance.Status.PRESENT));

        // EP: contact by index, status absent
        assertParseSuccess(parser, "1 /date 16-09-2026 /status absent", byIndex(1, Attendance.Status.ABSENT));

        // EP: contact by index, status late
        assertParseSuccess(parser, "1 /date 16-09-2026 /status late", byIndex(1, Attendance.Status.LATE));
    }

    @Test
    public void parse_nameAndClassAndEachStatus_success() {
        // EP: contact by name and class, status present
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /date 16-09-2026 /status present",
                byNameAndClass("Alex Yeoh", "A1", Attendance.Status.PRESENT));

        // EP: contact by name and class, status absent
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /date 16-09-2026 /status absent",
                byNameAndClass("Alex Yeoh", "A1", Attendance.Status.ABSENT));

        // EP: contact by name and class, status late
        assertParseSuccess(parser, " /name Alex Yeoh /class A1 /date 16-09-2026 /status late",
                byNameAndClass("Alex Yeoh", "A1", Attendance.Status.LATE));
    }

    @Test
    public void parse_indexBoundaries_success() {
        // EP: the smallest index (boundary)
        assertParseSuccess(parser, "1 /date 16-09-2026 /status late", byIndex(1, Attendance.Status.LATE));

        // EP: an index with several digits
        assertParseSuccess(parser, "25 /date 16-09-2026 /status late", byIndex(25, Attendance.Status.LATE));

        // EP: the largest index that fits in an integer (boundary)
        assertParseSuccess(parser, "2147483647 /date 16-09-2026 /status late",
                byIndex(Integer.MAX_VALUE, Attendance.Status.LATE));
    }

    @Test
    public void parse_statusInAnyCaseOrWithSpaces_success() {
        // EP: a status in upper case or in mixed case
        assertParseSuccess(parser, "1 /date 16-09-2026 /status ABSENT", byIndex(1, Attendance.Status.ABSENT));
        assertParseSuccess(parser, "1 /date 16-09-2026 /status LaTe", byIndex(1, Attendance.Status.LATE));

        // EP: a status with extra spaces around it
        assertParseSuccess(parser, "1 /date 16-09-2026 /status   present   ",
                byIndex(1, Attendance.Status.PRESENT));
    }

    @Test
    public void parse_dateThatIsOnlyCheckedWhenTheCommandRuns_success() {
        // EP: a date in the future is accepted here, because the command is the one that rejects it
        assertParseSuccess(parser, "1 /date 31-12-9999 /status late",
                new AttendanceByIndexCommand(Index.fromOneBased(1), LocalDate.of(9999, 12, 31),
                        Attendance.Status.LATE));

        // EP: a date that only exists in a leap year
        assertParseSuccess(parser, "1 /date 29-02-2024 /status late",
                new AttendanceByIndexCommand(Index.fromOneBased(1), LocalDate.of(2024, 2, 29),
                        Attendance.Status.LATE));
    }

    @Test
    public void parse_parametersInAnyOrder_success() {
        AttendanceCommand expectedByIndex = byIndex(1, Attendance.Status.ABSENT);
        AttendanceCommand expectedByNameAndClass = byNameAndClass("Alex Yeoh", "A1", Attendance.Status.ABSENT);

        // EP: the status before the date
        assertParseSuccess(parser, "1 /status absent /date 16-09-2026", expectedByIndex);

        // EP: the date and the status before the name and class
        assertParseSuccess(parser, " /date 16-09-2026 /status absent /name Alex Yeoh /class A1",
                expectedByNameAndClass);

        // EP: the class before the name
        assertParseSuccess(parser, " /class A1 /name Alex Yeoh /status absent /date 16-09-2026",
                expectedByNameAndClass);
    }

    @Test
    public void parse_prefixesInAnyCase_success() {
        // EP: prefixes in upper case or in mixed case
        assertParseSuccess(parser, "1 /DATE 16-09-2026 /Status absent", byIndex(1, Attendance.Status.ABSENT));
        assertParseSuccess(parser, " /NAME Alex Yeoh /Class A1 /date 16-09-2026 /status absent",
                byNameAndClass("Alex Yeoh", "A1", Attendance.Status.ABSENT));
    }

    @Test
    public void parse_extraSpacesInNameAndClass_success() {
        // EP: spaces around a value are removed, but the spaces inside a value are kept as typed
        assertParseSuccess(parser, " /name   alex   yeoh  /class  a1 /date 16-09-2026 /status late",
                byNameAndClass("alex   yeoh", "a1", Attendance.Status.LATE));
    }

    @Test
    public void parse_nameAndClassThatNoContactCanHave_success() {
        // EP: a name and a class that are not valid for a contact are accepted, and are not found when the
        // command runs
        assertParseSuccess(parser, " /name @@@, /class !!! /date 16-09-2026 /status absent",
                byNameAndClass("@@@,", "!!!", Attendance.Status.ABSENT));
    }

    @Test
    public void parse_noContact_failure() {
        // EP: neither an index, nor a name, nor a class
        assertParseFailure(parser, " /date 16-09-2026 /status absent", MESSAGE_MISSING_CONTACT_IDENTIFIER);

        // EP: a name and a class that only have spaces after them
        assertParseFailure(parser, " /name  /class  /date 16-09-2026 /status absent",
                MESSAGE_MISSING_CONTACT_IDENTIFIER);
    }

    @Test
    public void parse_indexTogetherWithNameOrClass_failure() {
        // EP: an index with a name and a class
        assertParseFailure(parser, "1 /name Alex Yeoh /class A1 /date 16-09-2026 /status absent",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: an index with only a name
        assertParseFailure(parser, "1 /name Alex Yeoh /date 16-09-2026 /status absent",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: an index with only a class
        assertParseFailure(parser, "1 /class A1 /date 16-09-2026 /status absent",
                MESSAGE_BOTH_CONTACT_IDENTIFIERS);
    }

    @Test
    public void parse_missingNameOrClass_failure() {
        // EP: a class without a name
        assertParseFailure(parser, " /class A1 /date 16-09-2026 /status absent", MESSAGE_MISSING_NAME);

        // EP: a name without a class
        assertParseFailure(parser, " /name Alex Yeoh /date 16-09-2026 /status absent", MESSAGE_MISSING_CLASS);

        // EP: a class and a name that only has spaces after it
        assertParseFailure(parser, " /name   /class A1 /date 16-09-2026 /status absent", MESSAGE_MISSING_NAME);

        // EP: a name and a class that only has spaces after it
        assertParseFailure(parser, " /name Alex Yeoh /class   /date 16-09-2026 /status absent",
                MESSAGE_MISSING_CLASS);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // EP: zero, the number just below the smallest index (boundary)
        assertParseFailure(parser, "0 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);

        // EP: a negative number
        assertParseFailure(parser, "-1 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);

        // EP: a number larger than the largest integer (boundary)
        assertParseFailure(parser, "2147483648 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);

        // EP: text that is not a whole number
        assertParseFailure(parser, "abc /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1.5 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "+1 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);

        // EP: two numbers
        assertParseFailure(parser, "1 2 /date 16-09-2026 /status absent", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_missingDate_failure() {
        // EP: no date, contact by index
        assertParseFailure(parser, "1 /status absent", MESSAGE_MISSING_DATE);

        // EP: no date, contact by name and class
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /status absent", MESSAGE_MISSING_DATE);

        // EP: a date that only has spaces after it
        assertParseFailure(parser, "1 /date   /status absent", MESSAGE_MISSING_DATE);
    }

    @Test
    public void parse_invalidDate_failure() {
        // EP: a date that does not exist, a date in another layout, and a day with one digit; the other
        // dates are tested in ParserUtilTest
        assertParseFailure(parser, "1 /date 31-02-2026 /status absent", MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 /date 2026-09-16 /status absent", MESSAGE_INVALID_DATE);
        assertParseFailure(parser, "1 /date 16-9-2026 /status absent", MESSAGE_INVALID_DATE);
    }

    @Test
    public void parse_missingStatus_failure() {
        // EP: no status, contact by index
        assertParseFailure(parser, "1 /date 16-09-2026", MESSAGE_MISSING_STATUS);

        // EP: no status, contact by name and class
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /date 16-09-2026", MESSAGE_MISSING_STATUS);

        // EP: a status that only has spaces after it
        assertParseFailure(parser, "1 /date 16-09-2026 /status   ", MESSAGE_MISSING_STATUS);
    }

    @Test
    public void parse_invalidStatus_failure() {
        // EP: a word that is not a status
        assertParseFailure(parser, "1 /date 16-09-2026 /status excused", MESSAGE_INVALID_STATUS);

        // EP: part of a status, or a status with more text
        assertParseFailure(parser, "1 /date 16-09-2026 /status pres", MESSAGE_INVALID_STATUS);
        assertParseFailure(parser, "1 /date 16-09-2026 /status presents", MESSAGE_INVALID_STATUS);

        // EP: two statuses
        assertParseFailure(parser, "1 /date 16-09-2026 /status late absent", MESSAGE_INVALID_STATUS);
    }

    @Test
    public void parse_unknownParameter_failure() {
        // EP: a parameter that no command accepts
        assertParseFailure(parser, "1 /date 16-09-2026 /status absent /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a parameter that another command accepts
        assertParseFailure(parser, "1 /date 16-09-2026 /status absent /email a@b.com", MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, "1 /date 16-09-2026 /status absent /score 5", MESSAGE_UNKNOWN_PARAMETER);

        // EP: only an unknown parameter
        assertParseFailure(parser, "1 /foo bar", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_unknownParameterMessage_listsEveryAcceptedParameter() {
        assertEquals("Unknown parameter. Use /name, /class, /date or /status", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        // EP: the date is repeated
        assertParseFailure(parser, "1 /date 16-09-2026 /date 17-09-2026 /status absent",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the status is repeated
        assertParseFailure(parser, "1 /date 16-09-2026 /status absent /status present",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the name is repeated
        assertParseFailure(parser, " /name Alex Yeoh /name John Tan /class A1 /date 16-09-2026 /status absent",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the class is repeated
        assertParseFailure(parser, " /name Alex Yeoh /class A1 /class A2 /date 16-09-2026 /status absent",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: a parameter repeated in a different case
        assertParseFailure(parser, "1 /date 16-09-2026 /DATE 17-09-2026 /status absent",
                MESSAGE_DUPLICATE_PARAMETERS);
    }

    @Test
    public void parse_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: an unknown parameter and a repeated parameter -> the unknown parameter
        assertParseFailure(parser, "1 /date 16-09-2026 /date 17-09-2026 /status absent /foo bar",
                MESSAGE_UNKNOWN_PARAMETER);

        // EP: a repeated parameter and an index with a name and a class -> the repeated parameter
        assertParseFailure(parser, "1 /name A /name B /class A1 /date 16-09-2026 /status absent",
                MESSAGE_DUPLICATE_PARAMETERS);

        // EP: an index with a name and a class, and a missing date -> the contact
        assertParseFailure(parser, "1 /name Alex Yeoh /class A1 /status absent", MESSAGE_BOTH_CONTACT_IDENTIFIERS);

        // EP: no contact and a missing date -> the contact
        assertParseFailure(parser, " /status absent", MESSAGE_MISSING_CONTACT_IDENTIFIER);

        // EP: a missing name and a missing date -> the name
        assertParseFailure(parser, " /class A1 /status absent", MESSAGE_MISSING_NAME);

        // EP: a missing date and a missing status -> the date
        assertParseFailure(parser, "1", MESSAGE_MISSING_DATE);

        // EP: a missing date and an invalid status -> the date
        assertParseFailure(parser, "1 /status excused", MESSAGE_MISSING_DATE);

        // EP: a missing status and an invalid date -> the status
        assertParseFailure(parser, "1 /date 31-02-2026", MESSAGE_MISSING_STATUS);

        // EP: an invalid date and an invalid status -> the date
        assertParseFailure(parser, "1 /date 31-02-2026 /status excused", MESSAGE_INVALID_DATE);

        // EP: an invalid status and an invalid index -> the status
        assertParseFailure(parser, "0 /date 16-09-2026 /status excused", MESSAGE_INVALID_STATUS);

        // EP: an invalid date and an invalid index -> the date
        assertParseFailure(parser, "0 /date 31-02-2026 /status absent", MESSAGE_INVALID_DATE);
    }

    private static AttendanceCommand byIndex(int oneBasedIndex, Attendance.Status status) {
        return new AttendanceByIndexCommand(Index.fromOneBased(oneBasedIndex), DATE, status);
    }

    private static AttendanceCommand byNameAndClass(String name, String className, Attendance.Status status) {
        return new AttendanceByNameAndClassCommand(name, className, DATE, status);
    }

}

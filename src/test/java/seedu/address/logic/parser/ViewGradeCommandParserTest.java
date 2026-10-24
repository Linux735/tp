package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_CLASS;
import static seedu.address.logic.Messages.MESSAGE_MISSING_NAME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ViewGradeCommandParser.MESSAGE_UNKNOWN_PARAMETER;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewGradeCommand;

/**
 * The inputs of the command are the name and the class of the contact. Each invalid input is tested alone
 * before it is tested together with another invalid input.
 */
public class ViewGradeCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewGradeCommand.MESSAGE_USAGE);

    private final ViewGradeCommandParser parser = new ViewGradeCommandParser();

    @Test
    public void parse_validNameAndClass_success() {
        // EP: a name and a class
        assertParseSuccess(parser, " /name John Tan /class A1", new ViewGradeCommand("John Tan", "A1"));
    }

    @Test
    public void parse_parametersInAnyOrder_success() {
        // EP: the class before the name
        assertParseSuccess(parser, " /class A1 /name John Tan", new ViewGradeCommand("John Tan", "A1"));
    }

    @Test
    public void parse_extraSpaces_success() {
        // EP: spaces around a value are removed, but the spaces inside a value are kept as typed
        assertParseSuccess(parser, " /class  A1  /name  John   Tan  ", new ViewGradeCommand("John   Tan", "A1"));
    }

    @Test
    public void parse_prefixesInAnyCase_success() {
        // EP: prefixes in upper case or in mixed case
        assertParseSuccess(parser, " /NAME John Tan /Class A1", new ViewGradeCommand("John Tan", "A1"));
    }

    @Test
    public void parse_nameAndClassThatNoContactCanHave_success() {
        // EP: a name and a class that are not valid for a contact are accepted, and are not found when the
        // command runs
        assertParseSuccess(parser, " /name @@@ /class !!!", new ViewGradeCommand("@@@", "!!!"));
        assertParseSuccess(parser, " /name John, Tan /class A1!", new ViewGradeCommand("John, Tan", "A1!"));
    }

    @Test
    public void parse_missingName_failure() {
        // EP: a class without a name
        assertParseFailure(parser, " /class A1", MESSAGE_MISSING_NAME);

        // EP: a name that only has spaces after it
        assertParseFailure(parser, " /name   /class A1", MESSAGE_MISSING_NAME);
    }

    @Test
    public void parse_missingClass_failure() {
        // EP: a name without a class
        assertParseFailure(parser, " /name John Tan", MESSAGE_MISSING_CLASS);

        // EP: a class that only has spaces after it
        assertParseFailure(parser, " /name John Tan /class   ", MESSAGE_MISSING_CLASS);
    }

    @Test
    public void parse_noParameters_reportsMissingName() {
        // EP: nothing is given, so both the name and the class are missing
        assertParseFailure(parser, "", MESSAGE_MISSING_NAME);
        assertParseFailure(parser, "   ", MESSAGE_MISSING_NAME);
    }

    @Test
    public void parse_textBeforeFirstParameter_failure() {
        // EP: text before the name and the class
        assertParseFailure(parser, " John /name John Tan /class A1", MESSAGE_INVALID_FORMAT);

        // EP: text before a parameter, and no other parameter
        assertParseFailure(parser, " John", MESSAGE_INVALID_FORMAT);

        // EP: an index before the name and the class
        assertParseFailure(parser, " 1 /name John Tan /class A1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownParameter_failure() {
        // EP: a parameter that no command accepts
        assertParseFailure(parser, " /name John Tan /class A1 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a parameter that another command accepts
        assertParseFailure(parser, " /name John Tan /class A1 /email john@example.com", MESSAGE_UNKNOWN_PARAMETER);
        assertParseFailure(parser, " /name John Tan /class A1 /assessment Quiz", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_unknownParameterMessage_listsEveryAcceptedParameter() {
        assertEquals("Unknown parameter. Use /name or /class", MESSAGE_UNKNOWN_PARAMETER);
    }

    @Test
    public void parse_repeatedParameter_failure() {
        // EP: the name is repeated
        assertParseFailure(parser, " /name John Tan /name Jane Doe /class A1", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: the class is repeated
        assertParseFailure(parser, " /name John Tan /class A1 /class A2", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: a parameter repeated in a different case
        assertParseFailure(parser, " /name John Tan /NAME Jane Doe /class A1", MESSAGE_DUPLICATE_PARAMETERS);
    }

    @Test
    public void parse_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: an unknown parameter and a repeated parameter -> the unknown parameter
        assertParseFailure(parser, " /name John Tan /name Jane Doe /class A1 /foo bar", MESSAGE_UNKNOWN_PARAMETER);

        // EP: a repeated parameter and text before the first parameter -> the repeated parameter
        assertParseFailure(parser, " John /name John Tan /name Jane Doe /class A1", MESSAGE_DUPLICATE_PARAMETERS);

        // EP: text before the first parameter and a missing name -> the text before
        assertParseFailure(parser, " John /class A1", MESSAGE_INVALID_FORMAT);

        // EP: a missing name and a missing class -> the name
        assertParseFailure(parser, " /name   /class   ", MESSAGE_MISSING_NAME);
    }

}

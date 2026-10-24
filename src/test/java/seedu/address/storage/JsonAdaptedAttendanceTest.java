package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Attendance;

public class JsonAdaptedAttendanceTest {

    private static final String MESSAGE_MISSING_DATE = "Attendance date is missing.";
    private static final String MESSAGE_MISSING_STATUS = "Attendance status is missing.";
    private static final String MESSAGE_INVALID = "Attendance record has an invalid date or status.";

    @Test
    public void constructor_fromModel_roundTripKeepsDateAndStatus() throws Exception {
        // EP: each status is its own partition, as the status is written in lower case
        for (Attendance.Status status : Attendance.Status.values()) {
            Attendance source = new Attendance(LocalDate.of(2026, 9, 16), status);

            assertEquals(source, new JsonAdaptedAttendance(source).toModelType());
        }

        // EP: a date with a year of fewer than four digits is written with zeros in front (boundary)
        Attendance yearZero = new Attendance(LocalDate.of(0, 1, 1), Attendance.Status.LATE);
        assertEquals(yearZero, new JsonAdaptedAttendance(yearZero).toModelType());
    }

    @Test
    public void toModelType_validRecord_returnsAttendance() throws Exception {
        // EP: a date in the past, with each status in lower case
        assertEquals(attendance(2026, 9, 16, Attendance.Status.PRESENT), adapted("2026-09-16", "present"));
        assertEquals(attendance(2026, 9, 16, Attendance.Status.ABSENT), adapted("2026-09-16", "absent"));
        assertEquals(attendance(2026, 9, 16, Attendance.Status.LATE), adapted("2026-09-16", "late"));

        // EP: a status in upper case or in mixed case, or with spaces around it
        assertEquals(attendance(2026, 9, 16, Attendance.Status.LATE), adapted("2026-09-16", "LATE"));
        assertEquals(attendance(2026, 9, 16, Attendance.Status.ABSENT), adapted("2026-09-16", "AbSent"));
        assertEquals(attendance(2026, 9, 16, Attendance.Status.PRESENT), adapted("2026-09-16", " present "));

        // EP: a date that only exists in a leap year
        assertEquals(attendance(2024, 2, 29, Attendance.Status.LATE), adapted("2024-02-29", "late"));

        // EP: the smallest and the largest year with four digits (boundaries)
        assertEquals(attendance(0, 1, 1, Attendance.Status.LATE), adapted("0000-01-01", "late"));
        assertEquals(attendance(9999, 12, 31, Attendance.Status.LATE), adapted("9999-12-31", "late"));

        // EP: a date in the future, which is accepted because the file is not checked against the clock
        assertEquals(attendance(9999, 1, 1, Attendance.Status.PRESENT), adapted("9999-01-01", "present"));
    }

    @Test
    public void toModelType_missingDate_throwsIllegalValueException() {
        // EP: no date
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_DATE,
                new JsonAdaptedAttendance(null, "present")::toModelType);
    }

    @Test
    public void toModelType_missingStatus_throwsIllegalValueException() {
        // EP: no status
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_STATUS,
                new JsonAdaptedAttendance("2026-09-16", null)::toModelType);
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        // EP: empty text or only spaces
        assertInvalid("", "present");
        assertInvalid("   ", "present");

        // EP: a date in another layout
        assertInvalid("16-09-2026", "present");
        assertInvalid("2026/09/16", "present");
        assertInvalid("2026-Sep-16", "present");

        // EP: a month or a day with one digit
        assertInvalid("2026-9-16", "present");
        assertInvalid("2026-09-6", "present");

        // EP: a day or a month below the smallest or above the largest (boundaries)
        assertInvalid("2026-09-00", "present");
        assertInvalid("2026-09-32", "present");
        assertInvalid("2026-00-16", "present");
        assertInvalid("2026-13-16", "present");

        // EP: a day that the month does not have, including 29 February in a year that is not a leap year
        assertInvalid("2026-04-31", "present");
        assertInvalid("2026-02-29", "present");

        // EP: a year that does not have exactly four digits, or that has a sign
        assertInvalid("26-09-16", "present");
        assertInvalid("+12026-01-01", "present");
        assertInvalid("12026-01-01", "present");
        assertInvalid("-0001-01-01", "present");
        assertInvalid("+2026-01-01", "present");
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        // EP: empty text or only spaces
        assertInvalid("2026-09-16", "");
        assertInvalid("2026-09-16", "   ");

        // EP: a word that is not a status
        assertInvalid("2026-09-16", "sick");

        // EP: part of a status, or a status with more text
        assertInvalid("2026-09-16", "pres");
        assertInvalid("2026-09-16", "presents");

        // EP: two statuses
        assertInvalid("2026-09-16", "late absent");
    }

    @Test
    public void toModelType_twoProblems_reportsTheOneThatIsCheckedFirst() {
        // EP: a missing date and a missing status -> the date
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_DATE,
                new JsonAdaptedAttendance(null, null)::toModelType);

        // EP: a missing date and an invalid status -> the date
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_DATE,
                new JsonAdaptedAttendance(null, "sick")::toModelType);

        // EP: an invalid date and a missing status -> the status
        assertThrows(IllegalValueException.class, MESSAGE_MISSING_STATUS,
                new JsonAdaptedAttendance("31-02-2026", null)::toModelType);

        // EP: an invalid date and an invalid status -> one message for both
        assertInvalid("31-02-2026", "sick");
    }

    private static Attendance attendance(int year, int month, int day, Attendance.Status status) {
        return new Attendance(LocalDate.of(year, month, day), status);
    }

    private static Attendance adapted(String date, String status) throws IllegalValueException {
        return new JsonAdaptedAttendance(date, status).toModelType();
    }

    private static void assertInvalid(String date, String status) {
        assertThrows(IllegalValueException.class, MESSAGE_INVALID,
                new JsonAdaptedAttendance(date, status)::toModelType);
    }

}

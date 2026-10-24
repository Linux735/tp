package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AttendanceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null date
        assertThrows(NullPointerException.class, () -> new Attendance(null, Attendance.Status.PRESENT));

        // EP: null status
        assertThrows(NullPointerException.class, () -> new Attendance(DATE, null));
    }

    @Test
    public void constructor_validArguments_keepsDateAndStatus() {
        // EP: each status is its own partition, as the status decides what is recorded
        for (Attendance.Status status : Attendance.Status.values()) {
            Attendance attendance = new Attendance(DATE, status);

            assertEquals(DATE, attendance.getDate());
            assertEquals(status, attendance.getStatus());
        }
    }

    @Test
    public void statusFromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Attendance.Status.fromString(null));
    }

    @Test
    public void statusFromString_validStatus_returnsStatus() {
        // EP: each status in lower case
        assertEquals(Attendance.Status.PRESENT, Attendance.Status.fromString("present"));
        assertEquals(Attendance.Status.ABSENT, Attendance.Status.fromString("absent"));
        assertEquals(Attendance.Status.LATE, Attendance.Status.fromString("late"));

        // EP: a status in upper case or in mixed case
        assertEquals(Attendance.Status.ABSENT, Attendance.Status.fromString("ABSENT"));
        assertEquals(Attendance.Status.LATE, Attendance.Status.fromString("LaTe"));

        // EP: a status with spaces at the start and end
        assertEquals(Attendance.Status.PRESENT, Attendance.Status.fromString("  present  "));
    }

    @Test
    public void statusFromString_invalidStatus_throwsIllegalArgumentException() {
        // EP: empty text or only spaces
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString("   "));

        // EP: a word that is not a status
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString("sick"));

        // EP: part of a status, or a status with more text
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString("pres"));
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString("presents"));

        // EP: two statuses
        assertThrows(IllegalArgumentException.class, () -> Attendance.Status.fromString("late absent"));
    }

    @Test
    public void statusToString_eachStatus_returnsLowerCaseName() {
        // EP: each status is its own partition
        assertEquals("present", Attendance.Status.PRESENT.toString());
        assertEquals("absent", Attendance.Status.ABSENT.toString());
        assertEquals("late", Attendance.Status.LATE.toString());
    }

    @Test
    public void formatDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Attendance.formatDate(null));
    }

    @Test
    public void formatDate_variousDates_returnsDayMonthYearWithFixedDigits() {
        // EP: a date whose day and month have two digits
        assertEquals("16-09-2026", Attendance.formatDate(LocalDate.of(2026, 9, 16)));

        // EP: a date whose day and month have one digit -> zeros are added
        assertEquals("05-03-2026", Attendance.formatDate(LocalDate.of(2026, 3, 5)));

        // EP: the first and last day of a month and of a year
        assertEquals("01-01-2026", Attendance.formatDate(LocalDate.of(2026, 1, 1)));
        assertEquals("31-12-2026", Attendance.formatDate(LocalDate.of(2026, 12, 31)));

        // EP: a day that exists only in a leap year
        assertEquals("29-02-2024", Attendance.formatDate(LocalDate.of(2024, 2, 29)));

        // EP: a year with fewer than four digits -> zeros are added (boundaries)
        assertEquals("01-01-0001", Attendance.formatDate(LocalDate.of(1, 1, 1)));
        assertEquals("01-01-0000", Attendance.formatDate(LocalDate.of(0, 1, 1)));

        // EP: the largest year with four digits (boundary)
        assertEquals("31-12-9999", Attendance.formatDate(LocalDate.of(9999, 12, 31)));
    }

    @Test
    public void toDisplayString_variousRecords_returnsDateWithMonthNameAndStatus() {
        // EP: each status is its own partition
        assertEquals("16-Sep-2026  present", new Attendance(DATE, Attendance.Status.PRESENT).toDisplayString());
        assertEquals("16-Sep-2026  absent", new Attendance(DATE, Attendance.Status.ABSENT).toDisplayString());
        assertEquals("16-Sep-2026  late", new Attendance(DATE, Attendance.Status.LATE).toDisplayString());

        // EP: a date whose day has one digit -> a zero is added
        assertEquals("05-Mar-2026  late",
                new Attendance(LocalDate.of(2026, 3, 5), Attendance.Status.LATE).toDisplayString());

        // EP: a year with fewer than four digits -> zeros are added (boundary)
        assertEquals("01-Jan-0000  late",
                new Attendance(LocalDate.of(0, 1, 1), Attendance.Status.LATE).toDisplayString());
    }

    @Test
    public void toString_variousRecords_returnsIsoDateAndStatus() {
        // EP: each status is its own partition
        assertEquals("2026-09-16: present", new Attendance(DATE, Attendance.Status.PRESENT).toString());
        assertEquals("2026-09-16: absent", new Attendance(DATE, Attendance.Status.ABSENT).toString());
        assertEquals("2026-09-16: late", new Attendance(DATE, Attendance.Status.LATE).toString());
    }

    @Test
    public void equals_variousObjects_returnsExpected() {
        Attendance attendance = new Attendance(DATE, Attendance.Status.PRESENT);

        // EP: the same object
        assertTrue(attendance.equals(attendance));

        // EP: the same date and status
        assertTrue(attendance.equals(new Attendance(DATE, Attendance.Status.PRESENT)));

        // EP: a different date
        assertFalse(attendance.equals(new Attendance(DATE.plusDays(1), Attendance.Status.PRESENT)));

        // EP: a different status
        assertFalse(attendance.equals(new Attendance(DATE, Attendance.Status.ABSENT)));

        // EP: null
        assertFalse(attendance.equals(null));

        // EP: an object of another type
        assertFalse(attendance.equals("not an Attendance"));
    }

    @Test
    public void hashCode_equalRecords_haveEqualHashCodes() {
        // EP: the same date and status
        assertEquals(new Attendance(DATE, Attendance.Status.PRESENT).hashCode(),
                new Attendance(DATE, Attendance.Status.PRESENT).hashCode());
    }

}

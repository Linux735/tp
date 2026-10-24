package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesKeyword;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void addPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.addPerson(null));
    }

    @Test
    public void deletePerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.deletePerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void findPerson_nullArguments_throwsNullPointerException() {
        // EP: null name
        assertThrows(NullPointerException.class, () -> modelManager.findPerson(null, "A1"));

        // EP: null class
        assertThrows(NullPointerException.class, () -> modelManager.findPerson("Alice Pauline", null));
    }

    @Test
    public void findPerson_matchingNameAndClass_returnsMatchingPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        String name = ALICE.getName().toString();
        String className = ALICE.getClassName().toString();

        // EP: the name and the class as they are stored
        assertEquals(Optional.of(ALICE), modelManager.findPerson(name, className));

        // EP: the name in upper case and the class in lower case
        assertEquals(Optional.of(ALICE), modelManager.findPerson(name.toUpperCase(), className.toLowerCase()));

        // EP: spaces around the name and the class, and repeated spaces inside the name
        assertEquals(Optional.of(ALICE),
                modelManager.findPerson("  " + name.replace(" ", "   ") + " ", "  " + className + " "));

        // EP: a letter written in full width, which looks the same
        char fullWidthFirstLetter = (char) (name.charAt(0) - 'A' + 'Ａ');
        assertEquals(Optional.of(ALICE), modelManager.findPerson(fullWidthFirstLetter + name.substring(1), className));
    }

    @Test
    public void findPerson_matchingContactNotDisplayed_returnsMatchingPerson() {
        // EP: the contact is not in the filtered list, and the whole address book is searched
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(person -> person.equals(BENSON));
        assertFalse(modelManager.getFilteredPersonList().contains(ALICE));

        assertEquals(Optional.of(ALICE),
                modelManager.findPerson(ALICE.getName().toString(), ALICE.getClassName().toString()));
    }

    @Test
    public void findPerson_sameNameInTwoClasses_returnsContactOfRequestedClass() {
        // EP: two contacts have the same name, and the class tells them apart
        Person sameNameOtherClass = new PersonBuilder(ALICE).withClassName("B999").build();
        modelManager.addPerson(ALICE);
        modelManager.addPerson(sameNameOtherClass);

        assertEquals(Optional.of(sameNameOtherClass), modelManager.findPerson(ALICE.getName().toString(), "B999"));
        assertEquals(Optional.of(ALICE),
                modelManager.findPerson(ALICE.getName().toString(), ALICE.getClassName().toString()));
    }

    @Test
    public void findPerson_noMatchingContact_returnsEmpty() {
        modelManager.addPerson(ALICE);
        String name = ALICE.getName().toString();
        String className = ALICE.getClassName().toString();

        // EP: the name matches a contact but the class does not
        assertTrue(modelManager.findPerson(name, "B999").isEmpty());

        // EP: the class matches a contact but the name does not
        assertTrue(modelManager.findPerson("Unknown Student", className).isEmpty());

        // EP: neither the name nor the class matches
        assertTrue(modelManager.findPerson("Unknown Student", "B999").isEmpty());

        // EP: a name that is only part of a name of a contact
        assertTrue(modelManager.findPerson(name.substring(0, name.length() - 1), className).isEmpty());

        // EP: empty text, and values that no contact can have, which do not match and do not throw
        assertTrue(modelManager.findPerson("", "").isEmpty());
        assertTrue(modelManager.findPerson("@@@", "!!!").isEmpty());
    }

    @Test
    public void findPerson_emptyAddressBook_returnsEmpty() {
        // EP: there is no contact at all
        assertTrue(modelManager.findPerson("Alice Pauline", "A1").isEmpty());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        modelManager.updateFilteredPersonList(new PersonMatchesKeyword(ALICE.getName().fullName));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}

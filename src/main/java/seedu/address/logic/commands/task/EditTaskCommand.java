package seedu.address.logic.commands.task;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADD_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEADLINE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DELETE_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.TaskCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.task.Contact;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.Project;
import seedu.address.model.task.Task;
import seedu.address.model.task.Title;

/**
 * Edits the details of an existing task in the address book.
 */
public class EditTaskCommand extends TaskCommand {
    public static final String COMMAND_WORD = "edit";
    public static final String COMMAND_WORD_FULL = TaskCommand.COMMAND_WORD + " " + COMMAND_WORD;
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the task identified "
            + "by the index number used in the displayed task list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_TITLE + "TITLE] "
            + "[" + PREFIX_DEADLINE + "DEADLINE] "
            + "[" + PREFIX_PROJECT + "PROJECT NAME] "
            + "[" + PREFIX_ADD_CONTACT + "PERSON_INDEX] "
            + "[" + PREFIX_DELETE_CONTACT + "PERSON_INDEX]\n"
            + "Example: " + COMMAND_WORD_FULL + " 1 "
            + PREFIX_TITLE + "Add tasks functionality "
            + PREFIX_DEADLINE + "next Friday"
            + PREFIX_PROJECT + "CS2103T tp "
            + PREFIX_ADD_CONTACT + "1 "
            + PREFIX_ADD_CONTACT + "3 "
            + PREFIX_DELETE_CONTACT + "2 ";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Edited Task: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in the task panel.";

    private final Index targetIndex;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * @param targetIndex of the task in the filtered task list to edit
     * @param editTaskDescriptor details to edit the task with
     */
    public EditTaskCommand(Index targetIndex, EditTaskDescriptor editTaskDescriptor) {
        requireAllNonNull(targetIndex, editTaskDescriptor);

        this.targetIndex = targetIndex;
        this.editTaskDescriptor = new EditTaskDescriptor(editTaskDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Task> lastShownTaskList = model.getFilteredTaskList();

        if (targetIndex.getZeroBased() >= lastShownTaskList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        Task taskToEdit = lastShownTaskList.get(targetIndex.getZeroBased());
        Task editedTask = createEditedTask(taskToEdit, editTaskDescriptor, model.getFilteredPersonList());

        if (!taskToEdit.isSameTask(editedTask) && model.hasTask(editedTask)) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        }

        // Replace task with edited task
        model.setTask(taskToEdit, editedTask);

        // Return with new edited task
        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, editedTask));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit}
     * edited with {@code editTaskDescriptor} and {@code personList}.
     * @throws CommandException
     */
    private static Task createEditedTask(Task taskToEdit, EditTaskDescriptor editTaskDescriptor,
                                         List<Person> personList) throws CommandException {
        assert taskToEdit != null;

        Title updatedTitle = editTaskDescriptor.getTitle().orElse(taskToEdit.getTitle());
        Deadline updatedDeadline = editTaskDescriptor.getDeadline().orElse(taskToEdit.getDeadline());
        Project updatedProject = editTaskDescriptor.getProject().orElse(taskToEdit.getProject());
        Set<Contact> updatedAssignedContacts =
            editTaskDescriptor.buildNewContacts(taskToEdit, personList).orElse(taskToEdit.getAssignedContacts());

        return new Task(updatedTitle, taskToEdit.getCompleted(), updatedDeadline, updatedProject,
                updatedAssignedContacts);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditTaskCommand)) {
            return false;
        }

        // state check
        EditTaskCommand e = (EditTaskCommand) other;
        return targetIndex.equals(e.targetIndex)
                && editTaskDescriptor.equals(e.editTaskDescriptor);
    }

    /**
     * Stores the details that the task will be edited with. Each non-empty field value will replace the
     * corresponding field value of the task.
     */
    public static class EditTaskDescriptor {

        private Title title;
        private boolean isCompleted;
        private Deadline deadline;
        private Project project;
        private Set<Index> assignedContactIndexes;
        private Set<Index> unassignedContactIndexes;

        public EditTaskDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code contacts} is used internally.
         */
        public EditTaskDescriptor(EditTaskDescriptor toCopy) {
            setTitle(toCopy.title);
            setDeadline(toCopy.deadline);
            setProject(toCopy.project);
            setAssignedContactIndexes(toCopy.assignedContactIndexes);
            setUnassignedContactsIndexes(toCopy.unassignedContactIndexes);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(title, deadline, project,
                    assignedContactIndexes, unassignedContactIndexes);
        }

        public void setTitle(Title title) {
            this.title = title;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public Optional<Title> getTitle() {
            return Optional.ofNullable(title);
        }

        public Optional<Project> getProject() {
            return Optional.ofNullable(project);
        }

        public void setDeadline(Deadline deadline) {
            this.deadline = deadline;
        }

        public Optional<Deadline> getDeadline() {
            return Optional.ofNullable(deadline);
        }

        public void setAssignedContactIndexes(Set<Index> assignedContactIndexes) {
            this.assignedContactIndexes = (assignedContactIndexes != null)
                    ? new HashSet<>(assignedContactIndexes) : null;
        }

        public void setUnassignedContactsIndexes(Set<Index> unassignedContactIndexes) {
            this.unassignedContactIndexes = (unassignedContactIndexes != null)
                    ? new HashSet<>(unassignedContactIndexes) : null;
        }

        public Optional<Set<Index>> getAssignedContactIndexes() {
            return (assignedContactIndexes != null)
                    ? Optional.of(Collections.unmodifiableSet(assignedContactIndexes))
                    : Optional.empty();
        }

        public Optional<Set<Index>> getUnassignedContactIndexes() {
            return (unassignedContactIndexes != null)
                    ? Optional.of(Collections.unmodifiableSet(unassignedContactIndexes))
                    : Optional.empty();
        }


        /**
         * Returns an unmodifiable contact set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code contact} is null.
         */
        public Optional<Set<Contact>> buildNewContacts(Task task, List<Person> personList) throws CommandException {
            if (!CollectionUtil.isAnyNonNull(assignedContactIndexes, unassignedContactIndexes)) {
                return Optional.empty();
            }
            Set<Contact> assignedContacts = personIndexesToContacts(assignedContactIndexes, personList);
            Set<Contact> unassignedContacts = personIndexesToContacts(unassignedContactIndexes, personList);
            Set<Contact> contacts = new HashSet<>(task.getAssignedContacts());
            contacts.addAll(assignedContacts);
            contacts.removeAll(unassignedContacts);
            return Optional.of(Collections.unmodifiableSet(contacts));
        }

        private Set<Contact> personIndexesToContacts(Set<Index> personIndexes, List<Person> personList)
                throws CommandException {
            if (personIndexes == null) {
                return new HashSet<>();
            }
            Set<Contact> assignedContacts = new HashSet<>();
            for (Index personIndex : personIndexes) {
                if (personIndex.getZeroBased() >= personList.size()) {
                    throw new CommandException(String.format(Messages.MESSAGE_INVALID_PERSON_INDEX_CUSTOM,
                            personIndex.getOneBased()));
                }
                Contact contactToAssign =
                        new Contact(personList.get(personIndex.getZeroBased()).getName().fullName);
                assignedContacts.add(contactToAssign);
            }
            return assignedContacts;
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditTaskDescriptor)) {
                return false;
            }

            // state check
            EditTaskDescriptor e = (EditTaskDescriptor) other;

            return getTitle().equals(e.getTitle())
                    && getDeadline() == e.getDeadline()
                    && getProject() == e.getProject()
                    && getAssignedContactIndexes().equals(e.getAssignedContactIndexes())
                    && getUnassignedContactIndexes().equals(e.getUnassignedContactIndexes());
        }
    }
}

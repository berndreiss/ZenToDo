package net.berndreiss.zentodo;

/**
 * The mode/view the app is currently in.
 */
public enum Mode {
    /** View to drop new tasks */
    DROP,
    /** Pick tasks to focus on; set reminder date for others or move them to a list */
    PICK,
    /** Tasks to focus on */
    FOCUS,
    /** Show list that has been assigned to tasks */
    LIST,
    /** Show list with all tasks */
    LIST_ALL,
    /** Show list of tasks without a list */
    LIST_NO,
    /** Show list of lists */
    LIST_OF_LISTS
}

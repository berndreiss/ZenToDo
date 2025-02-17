package net.berndreiss.zentodo.Data;

public interface Database {

    /**
     * Add a new entry to the database.
     */
    void post(Entry entry);

    /**
     * Delete entry from database.
     */
    void delete(int id);

    /**
     * Swap entry with id with the entry at position.
     */
    void swap(int id, int position);

    /**
     * Swap entry in list with entry at position.
     */
    void swapList(int id, int position);

    /**
     * Update the field with the value provided.
     */
    void update(int id, String field, String value);
}

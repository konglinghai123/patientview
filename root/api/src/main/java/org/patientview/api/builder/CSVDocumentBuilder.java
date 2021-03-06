package org.patientview.api.builder;

import java.util.ArrayList;

/**
 * CSV document builder
 * Created by tom@solidstategroup.com
 * Created on 08/07/2015
 */
public class CSVDocumentBuilder {

    private int currentRow;
    private int currentCell;
    private ArrayList<ArrayList<String>> document = new ArrayList<ArrayList<String>>();
    private int numberOfColumns;

    /**
     * Creates a document that models as a csv file
     */
    public CSVDocumentBuilder() {
        document = new ArrayList<>();
        currentRow = 0;
        currentCell = 0;
        numberOfColumns = 0;
        createNewRow();
    }

    /**
     * Add a string 'header to line 1
     *
     * @param header String to be displayed in header
     */
    public void addHeader(String header) {
        document.get(0).add(header);
        numberOfColumns++;
    }

    /**
     * Appends the value given to the cell for the last row of the document
     *
     * @param cellNumber - Cell number to insert the value
     * @param value      - The string value to insert
     */
    public void appendUniqueValueToLastRowCell(int cellNumber, String value) {
        appendUnqiueValue(cellNumber, document.size() - 1, value);
    }

    /**
     * Appends the value given to the current row of the document
     *
     * @param cellNumber - Cell number to insert the value
     * @param value      - The string value to insert
     */
    public void appendUnqiueValueToCurrentRowCell(int cellNumber, String value) {
        appendUnqiueValue(cellNumber, currentRow, value);
    }


    /**
     * Move the pointer to the next cell
     * <p/>
     * Used for when no result/value is present for a cell
     */
    public void nextCell() {
        currentCell++;
    }

    /**
     * Create a new row with blank values in each column/cell
     */
    public void createNewRow() {
        ArrayList<String> newRow = new ArrayList<>();
        for (int i = 0; i < numberOfColumns; i++) {
            newRow.add("");
        }
        document.add(newRow);
    }

    /**
     * Get the document structure
     *
     * @return the csv document structure
     */
    public ArrayList<ArrayList<String>> getDocument() {
        return document;
    }

    /**
     * Reset the current points to the last row and first cell
     */
    public void resetCurrentPosition() {
        currentRow = document.size() - 1;
        currentCell = 0;
    }

    /**
     * Adds a value to the current row cell and each
     * row below that (e.g. dates and units)
     *
     * @param cellNumber Cell number to place the value
     * @param value      The value to add
     */
    public void addValueToCellCascade(int cellNumber, String value) {
        for (int i = currentRow; i < document.size(); i++) {
            document.get(i).set(cellNumber, value);
        }
    }

    /**
     * Adds a value to the next cell in the document
     * for the current row
     *
     * @param value The value to place in the cell
     */
    public void addValueToNextCell(String value) {
        if (currentCell < numberOfColumns) {
            document.get(currentRow).set(currentCell, value);
            currentCell++;
        }
    }

    /**
     * Adds the value to the last cell (based on current cell)
     * <p/>
     * If the row does not exist, a new row is created
     * If the row does exist, it is added to the row
     *
     * @param value The value to place into the cell
     */
    public void addValueToPreviousCell(String value) {
        int tempCurrentRow = currentRow;
        int cellNumber = currentCell - 1;
        while (isCellFilled(tempCurrentRow, cellNumber) && tempCurrentRow < document.size()) {
            tempCurrentRow++;
        }
        if (tempCurrentRow >= document.size()) {
            createNewRow();
            document.get(tempCurrentRow).set(0, document.get(currentRow).get(0));
            document.get(tempCurrentRow).set(cellNumber, value);
        } else {
            document.get(tempCurrentRow).set(cellNumber, value);
        }
    }

    /**
     * Checks if the specified row number exists in the document
     *
     * @param rowNumber the row number to check exists
     * @return If the row exists
     */
    private boolean rowExists(int rowNumber) {
        return rowNumber <= document.size() - 1;
    }

    /**
     * Checks if the specified cell has a value within it
     *
     * @param rowNumber  The row number to check
     * @param cellNumber The cell number to check
     * @return If the cell has a value within it
     */
    private boolean isCellFilled(int rowNumber, int cellNumber) {
        //Check the row exists
        if (rowExists(rowNumber)) {
            //Check if the cell exists
            if (document.get(rowNumber).size() > cellNumber) {
                //Check the length of the string in the cell
                return document.get(rowNumber).get(cellNumber).length() != 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Internal method used to append string to given row and cell
     *
     * @param cellNumber - Cell number to insert the value (0 index)
     * @param rowNumber  - Row number to insert the value (0 index)
     * @param value      - String value to insert
     */
    private void appendUnqiueValue(int cellNumber, int rowNumber, String value) {

        String currentValue = document.get(rowNumber).get(cellNumber);

        if (!currentValue.contains(value.trim())) {

            if (currentValue.trim().length() == 0) {
                currentValue = value;
            } else {
                currentValue += ", " + value;
            }

            document.get(rowNumber).set(cellNumber, currentValue);
        }
    }
}

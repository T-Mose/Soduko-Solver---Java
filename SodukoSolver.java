import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SodukoSolver {
    // https://www.sudoku-solutions.com/index.php?section=sudoku9by9 - To auto solve
    public static String gameString = "010020300004001050060000007005400060000100002080092000300005090000700106007000000";
    public static Matrix matrix; // To implement - hidden pairs, x-wing?
                                 // brute force

    public static void main(String[] args) {
        matrix = new Matrix();
        Solve();
        matrix.displayMatrix();
    }

    public static void Solve() {
        int len;
        int maxIterations = 20;
        int currentIteration = 0;
        int previousLen;

        while (currentIteration++ < maxIterations) {
            len = matrix.matrixToString().length();

            do {
                previousLen = len;
                checkPossible();
                checkSingelValues(0); // Rows
                checkPossible();
                checkSingelValues(1); // Columns
                checkPossible();
                checkSingelValues(2); // 3x3 Squares
                len = matrix.matrixToString().length();
            } while (len != previousLen);
            if (len == 81) {
                break; // Puzzle solved
            }

            checkPossible();
            pointed(1); // Columns
            checkPossible();
            pointed(0); // Rows
            checkPossible();
            matrix.displayMatrix();
            pointedSquare(true); // rows
            matrix.displayMatrix();
            checkPossible();
            pointedSquare(false); // Columns
            matrix.displayMatrix();
            if (len == matrix.matrixToString().length()) {
                System.out.println("Could not be solved");
                break;
            }
            // brute force requred if it could not be doen
        }

        if (matrix.matrixToString().length() == 81) {
            System.out.println("Sudoku solved! it took this many iterations: " + currentIteration);
            System.out.println("The complete string is: " + matrix.matrixToString());
        } else {
            System.out.println("Sudoku not solved in " + currentIteration + " iterations.");
        }
    }

    public static void pointedSquare(boolean row) {
        // Will try to merge this method with pointed
        Map<String, ArrayList<Integer>> order;
        String location;
        Set<Integer> three;
        Set<Integer> six;
        Set<Integer> uniqueElements;
        int rowCol;
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                order = new HashMap<>();
                for (int k = i; k < i + 3; k++) {
                    for (int l = j; l < j + 3; l++) {
                        location = l + "-" + k; // Row-column , could be column-row
                        order.put(location, matrix.getCell(k, l).getPossibleVals());
                    }
                } // That should populate the hashmap

                // Do some chechs in each 3x3 for adjacent unique values
                for (int a = 0; a < 3; a++) { // Since there are 3 columns/rows that need exploring
                    three = new LinkedHashSet<>();
                    six = new LinkedHashSet<>();
                    for (int k = i; k < i + 3; k++) {
                        for (int l = j; l < j + 3; l++) {
                            location = l + "-" + k;
                            if (order.get(location).size() != 1) {
                                if (row) {
                                    if (k % 3 == a) { // Row
                                        three.addAll(order.get(location));
                                    } else {
                                        six.addAll(order.get(location));
                                    }
                                } else if (!row) {
                                    if (l % 3 == a) { // The sought after column
                                        three.addAll(order.get(location));
                                    } else {
                                        six.addAll(order.get(location));
                                    }
                                }
                            }
                        }
                    }
                    uniqueElements = new HashSet<>(three);
                    uniqueElements.removeAll(six); // The ones left should be the unique elements
                    if (row) {
                        rowCol = i + a;
                        for (int o = 0; o < 9; o++) {
                            if (!(j <= o && o < j + 3)) {
                                for (Integer integer : uniqueElements) {
                                    if (matrix.getCell(rowCol, o).getPossibleVals().contains(integer)) {
                                        matrix.getCell(rowCol, o).removePossibleVals(integer);
                                        System.out
                                                .println("ROW, We are removing: " + integer + " from cell: " + rowCol
                                                        + "."
                                                        + o);
                                    }
                                }
                            }
                        }
                    } else if (!row) {
                        rowCol = j + a;
                        for (int o = 0; o < 9; o++) {
                            if (!(i <= o && o < i + 3)) {
                                for (Integer integer : uniqueElements) {
                                    if (matrix.getCell(o, rowCol).getPossibleVals().contains(integer)) {
                                        matrix.getCell(o, rowCol).removePossibleVals(integer);
                                        System.out
                                                .println(
                                                        "COLUMN, We are removing: " + integer + " from cell: " + o + "."
                                                                + rowCol);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void pointed(int row) {
        // This removes pointed double/tripples from a column/rows - squares perspective
        // Does not completly determin what the cell is going to be, but can remove what
        // options it has
        // Assume in a 3x3 7 can only be on two places on the same row
        // Then other 7:s on the same row elsewhere can be eliminated
        // Basically if one rule type influences anothers possibilities

        // Check all rows and columns
        // Find any number that exists only within a square
        // Eliminate this number from any other cell within the same square
        Map<Integer, ArrayList<Integer>> order;
        Set<Integer> three;
        Set<Integer> six;
        for (int i = 0; i < 9; i++) {
            order = new HashMap<>();
            for (int j = 0; j < 9; j++) {
                if (row == 0) { // Rows
                    order.put(j, new ArrayList<>(matrix.getCell(i, j).getPossibleVals()));
                } else if (row == 1) { // Columns
                    order.put(j, new ArrayList<>(matrix.getCell(j, i).getPossibleVals()));
                }
            } // The ordered hashmaps are created
              // Check if pointers occur

            // iterate first 3 - check if any values here never show upp later in the
            // hashmap
            Set<Integer> uniqueElements;
            for (int j = 0; j < 3; j++) { // Three different squares
                three = new LinkedHashSet<>();
                six = new LinkedHashSet<>();

                for (int k = 0; k < 9; k++) {
                    if (order.get(k).size() != 1) {
                        if (k < j * 3 + 3 && k >= j * 3) {
                            three.addAll(order.get(k));
                        } else {
                            six.addAll(order.get(k));
                        }
                    }
                }
                uniqueElements = new HashSet<>(three);
                uniqueElements.removeAll(six);

                if (!uniqueElements.isEmpty()) {
                    // These elements should be removed from other cells within the 3x3 that also
                    // i:th row, j:th 3x3
                    // loop through the 3x3
                    // halt for the specefied column
                    // if any cell cointains values that also exists in the uniqueelements
                    // remove them from the cell
                    if (row == 0) {
                        for (int c = (i / 3) * 3; c < (i / 3) * 3 + 3; c++) {
                            for (int d = 3 * j; d < 3 + 3 * j; d++) {
                                if (c != i % 3 + (i / 3) * 3) { // Rows
                                    for (Integer integer : uniqueElements) {
                                        matrix.getCell(c, d).removePossibleVals(integer);
                                    }
                                }
                            }
                        }
                    } else if (row == 1) {
                        for (int c = j * 3; c < j * 3 + 3; c++) {
                            for (int d = (i / 3) * 3; d < (i / 3) * 3 + 3; d++) {
                                if (d != i) { // Rows
                                    for (Integer integer : uniqueElements) {
                                        matrix.getCell(c, d).removePossibleVals(integer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkSingelValues(int row) { // 0-row, 1-col, 2-3x3
        Map<Integer, Integer> frequency;
        ArrayList<Integer> nums;
        for (int i = 0; i < 9; i++) {
            frequency = new HashMap<>();
            for (int j = 1; j <= 9; j++) {
                frequency.put(j, 0);
            }

            if (row < 2) {
                for (int j = 0; j < 9; j++) {
                    nums = row == 0 ? new ArrayList<>(matrix.getCell(i, j).getPossibleVals())
                            : new ArrayList<>(matrix.getCell(j, i).getPossibleVals());
                    if (nums.size() > 1) {
                        for (Integer value : nums) {
                            frequency.put(value, frequency.get(value) + 1);
                        }
                    }
                }
            } else if (i % 3 == 0) { // 0,3,6
                for (int j = 0; j < 9; j += 3) { // 0-2, 3-5, 6-8
                    for (int k = i; k < i + 3; k++) {
                        for (int l = j; l < j + 3; l++) {
                            nums = new ArrayList<>(matrix.getCell(k, l).getPossibleVals());
                            if (nums.size() > 1) {
                                for (Integer value : nums) {
                                    frequency.put(value, frequency.get(value) + 1);
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : frequency.entrySet()) {
                if (entry.getValue() == 1) {
                    nums = new ArrayList<>();
                    nums.add(entry.getKey());
                    if (row < 2) { // Columns and rows
                        for (int j = 0; j < 9; j++) {
                            if (row == 0 && matrix.getCell(i, j).getPossibleVals().contains(entry.getKey())) {
                                matrix.getCell(i, j).setPossibleVals(nums);
                            } else if (row == 1 && matrix.getCell(j, i).getPossibleVals().contains(entry.getKey())) {
                                matrix.getCell(j, i).setPossibleVals(nums);
                            }
                        }
                    } else if (row == 2 && i % 3 == 0) { // 3x3 - This iteration not requred for all i
                        for (int j = 0; j < 9; j += 3) { // 0-2, 3-5, 6-8
                            for (int k = i; k < i + 3; k++) {
                                for (int l = j; l < j + 3; l++) {
                                    if (matrix.getCell(k, l).getPossibleVals().contains(entry.getKey())) {
                                        matrix.getCell(k, l).setPossibleVals(nums);
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public static void checkPossible() {
        int len = matrix.matrixToString().length();
        while (true) { // Since the updated matrix might lead to changes - new update needed
            Cell cell;
            ArrayList<Integer> vals;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    cell = matrix.getCell(i, j);
                    vals = new ArrayList<>(cell.getPossibleVals());
                    if (vals.size() > 1) {
                        for (Cell adjacent : cell.getAdjacentCells()) {
                            if (adjacent.getValsInString().length() == 1) {
                                vals.remove(adjacent.getPossibleVals().get(0));
                            }
                        }
                    }
                    cell.setPossibleVals(vals);
                }
            }
            if (matrix.matrixToString().length() == len) {
                break;
            } else {
                len = matrix.matrixToString().length();
            }
        }
    }
}

class Matrix {
    public Cell[][] matrix;
    public static final boolean SIMPLE = false; // Display formatting
    public static final String ANSI_GREEN = "\u001B[32m"; // Display it in green
    public static final String ANSI_RESET = "\u001B[0m"; // Back to white

    public Matrix() {
        matrix = new Cell[9][9];
        setCells();
        setAdjacent();
    }

    public String matrixToString() {
        String all = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                all += matrix[i][j].getValsInString();
            }
        }
        return all;
    }

    public Cell[][] getMatrix() {
        return matrix;
    }

    public Cell getCell(int i, int j) {
        return matrix[i][j];
    }

    public void setCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                matrix[i][j] = new Cell(i, j, Character.getNumericValue(SodukoSolver.gameString.charAt(i * 9 + j)));
            }
        }
    }

    public void displayMatrix() {
        System.out.println();
        String line = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                line = SIMPLE && matrix[i][j].getValsInString().length() > 1 ? "0" : matrix[i][j].getValsInString();
                line += ((j + 1) % 3 == 0 && j + 1 != 9) ? "|" : " ";
                if (line.length() == 2 && line.charAt(0) != '0') {
                    System.out.print(ANSI_GREEN + line.charAt(0) + ANSI_RESET);
                    line = line.substring(1); // If solved, its green
                }
                System.out.print(line);
            }
            System.out.println();
            if ((i + 1) % 3 == 0 && i + 1 != 9) {
                System.out.println("-----------------"); // Formatting
            }
        }
    }

    public void setAdjacent() {
        // In the same square, row and column
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 9; k++) {
                    // Add cells in the same row and column
                    if (i != k)
                        matrix[i][j].addAdjacentCell(matrix[k][j]); // Column
                    if (j != k)
                        matrix[i][j].addAdjacentCell(matrix[i][k]); // Row
                }
            }
        }
        for (int squareRow = 0; squareRow < 9; squareRow += 3) {
            for (int squareCol = 0; squareCol < 9; squareCol += 3) {
                addCellsInSquare(squareRow, squareCol);
            }
        }
    }

    private void addCellsInSquare(int startRow, int startCol) {
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                for (int adjRow = startRow; adjRow < startRow + 3; adjRow++) {
                    for (int adjCol = startCol; adjCol < startCol + 3; adjCol++) {
                        if (row != adjRow || col != adjCol) {
                            matrix[row][col].addAdjacentCell(matrix[adjRow][adjCol]);
                        }
                    }
                }
            }
        }
    }
}

class Cell {
    public ArrayList<Integer> possibleVals = new ArrayList<>();
    public ArrayList<Cell> adjacentCells = new ArrayList<>();
    public int xCoordinate;
    public int yCoordinate;

    public Cell(int x, int y, int valPosition) {
        this.xCoordinate = x;
        this.yCoordinate = y;

        if (valPosition != 0) {
            possibleVals.add(valPosition);
        } else {
            for (int i = 1; i <= 9; i++) {
                possibleVals.add(i);
            }
        }
    }

    public String getName() {
        return "Cell" + String.valueOf(xCoordinate) + "_" + String.valueOf(yCoordinate);
    }

    public void removePossibleVals(int i) {
        if (possibleVals.contains(i)) {
            possibleVals.remove((Integer) i);
        }
    }

    public void setPossibleVals(ArrayList<Integer> possible) {
        this.possibleVals = new ArrayList<>(possible);
    }

    public ArrayList<Integer> getPossibleVals() {
        return this.possibleVals;
    }

    public int getX() { // Redundant
        return xCoordinate;
    }

    public int getY() { // Redundant
        return yCoordinate;
    }

    public void addAdjacentCell(Cell cell) {
        if (this != cell && !this.getAdjacentCells().contains(cell))
            adjacentCells.add(cell);
    }

    public ArrayList<Cell> getAdjacentCells() {
        return this.adjacentCells;
    }

    public String getValsInString() { // Turns the Cell possible values into a string that can be displayed
        String inLetters = "";
        for (int ints : possibleVals) {
            inLetters += String.valueOf(ints);
        }
        return inLetters;
    }
}
// Brute forcing - take the incomplete map, guess one of the cells, preferebly
// one with few possible
// Set said value, keep track of the others. Return the new map and try to solve
// it
// Implement a check to se if somthings gone wrong, in that case return to the
// other matrix, and
// Guess the other value/s. Will be difficult if multiple values needs to be
// guessed in a row.
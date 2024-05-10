package sheep.features.file;

import sheep.expression.CoreFactory;
import sheep.expression.Expression;
import sheep.expression.TypeError;
import sheep.features.Feature;
import sheep.parsing.ParseException;
import sheep.parsing.SimpleParser;
import sheep.sheets.CellLocation;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;
import sheep.ui.UI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads the sheet with a pre-saved sheet state from {@link FileSaving}
 */
public class FileLoading implements Feature {

    private final Sheet sheet;
    private final SimpleParser parser = new SimpleParser(new CoreFactory());

    private Expression[][] newSheet;
    private int rows = 1;
    private int columns = 0;

    private final Perform loadState = new Perform() {
        @Override
        public void perform(int row, int column, Prompt prompt) {
            // load the state
            try {
                loader(prompt.ask("Enter a file to load: filePath\\fileName")
                        .orElse("?<:>?")); //is made of forbidden ASCII characters across
            } catch (IOException e) {
                prompt.message("Error loading file failed");
            }
        }
    };

    @Override
    public void register(UI ui) {
        ui.addFeature("load-file", "Load File", loadState);
    }

    /**
     * Constructor method for {@link FileLoading}
     *
     * @param sheet is the sheet upon which the file will be loaded to
     */
    public FileLoading(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * loads from the saved file into the sheet.
     *
     * @param fileLocation the location of the file to read from
     * @throws IOException if there is an error while reading from the file
     */
    private void loader(String fileLocation) throws IOException {
        setRows(fileLocation);

        populateNewSheet(fileLocation);

        updateThisSheet();
    }

    /**
     * Sets up the new sheet, so that it is the same size of what is to be loaded
     *
     * @param fileLocation the file to read from
     * @throws IOException if the reader has issues
     */
    private void setRows(String fileLocation) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileLocation));

        columns = reader.readLine().split("[|]", -1).length;

        while (reader.readLine() != null) {
            rows++;
        }
        reader.close();

        newSheet = new Expression[rows][columns];
    }

    /**
     * populates the newSheet array with the information from the given file
     */
    private void populateNewSheet(String fileLocation) throws IOException {
        //create a new reader to read the given file
        BufferedReader reader = new BufferedReader(new FileReader(fileLocation));

        for (int row = 0; row < newSheet.length; row++) {
            String[] lineBits = reader.readLine().split("[|]", -1);

            for (int col = 0; col < lineBits.length; col++) {
                //try to parse the cell info
                try {
                    Expression result = parser.parse(lineBits[col]);
                    newSheet[row][col] = result;
                } catch (ParseException e) {
                    throw new IOException();
                }
            }
        }

        //close the reader to prevent any mishaps
        reader.close();
    }

    /**
     * clears the current sheet, and updates each cell according to what is in newSheet
     * @throws IOException is thrown when there is a problem with updating the sheet
     */
    private void updateThisSheet() throws IOException {
        //clear the sheet before uploading
        sheet.clear();

        for (int row = 0; row < newSheet.length; row++) {
            for (int column = 0; column < columns; column++) {
                if (!newSheet[row][column].render().isEmpty()) {
                    try {
                        sheet.update(new CellLocation(row, column), newSheet[row][column]);
                    } catch (TypeError error) {
                        throw new IOException();
                    }
                }
            }
        }
    }
}
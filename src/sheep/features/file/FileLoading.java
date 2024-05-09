package sheep.features.file;

import sheep.features.Feature;
import sheep.sheets.Sheet;
import sheep.ui.Perform;
import sheep.ui.Prompt;
import sheep.ui.UI;

/**
 * Loads the sheet with a pre-saved sheet state from {@link FileSaving}
 */
public class FileLoading implements Feature {

    private final Sheet sheet;

    private final Perform loadState = new Perform() {
        @Override
        public void perform(int row, int column, Prompt prompt) {
            //save the game state
            System.out.println("Loading sheet state");
        }
    };

    @Override
    public void register(UI ui) {
        ui.addFeature("load-file", "Load File", loadState);
    }

    public FileLoading(Sheet sheet) {
        this.sheet = sheet;
    }
}

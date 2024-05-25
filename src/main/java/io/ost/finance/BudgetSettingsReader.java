package io.ost.finance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joost
 */
public class BudgetSettingsReader {

    public static final String BUDGET_SETTINGS = "budget-settings.txt";

    private final JsonParser parser;

    public BudgetSettingsReader() {
        parser = new JsonParser();
    }

    public void read() {
        File file = new File(App.getConfigDirectory()+ BUDGET_SETTINGS);
        readFrom(file);
    }

    private void readFrom(File file) {
        String jsString = readFileAsString(file);
        String jsonString = jsString.split("`")[1];
        JsonArray settingsArray = parser.parse(jsonString).getAsJsonArray();
        Iterator<JsonElement> iterator = settingsArray.iterator();
        while (iterator.hasNext()) {
            JsonObject settingsObject = iterator.next().getAsJsonObject();
            SingleAccountBudget.firstOfMonth = getFirstOfMonthFrom(settingsObject);
            SingleAccountBudget.budgetedForCategory = getBudgetTemplateFrom(settingsObject);
        }
    }

    private String readFileAsString(File file) {
        String jsonString = "`[{\"firstOfMonth\": 1, \"budgetTemplate\": []}]";
        try {
            jsonString = Util.readFileAsString(file);
        } catch (IOException ex) {
            Logger.getLogger(BudgetSettingsReader.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return jsonString;
    }

    private int getFirstOfMonthFrom(JsonObject settingsObject) {
        return settingsObject.get("firstOfMonth").getAsInt();
    }

    private Map<String, Double> getBudgetTemplateFrom(JsonObject settingsObject) {
        Map<String, Double> result = new TreeMap<>();
        JsonArray budgetTemplate = settingsObject.get("budgetTemplate").getAsJsonArray();
        Iterator<JsonElement> iterator = budgetTemplate.iterator();
        while (iterator.hasNext()) {
            JsonObject budgetForCategory = iterator.next().getAsJsonObject();
            String category = budgetForCategory.get("category").getAsString();
            double budget = budgetForCategory.get("budget").getAsDouble();
            result.put(category, budget);
        }
        return result;
    }

}

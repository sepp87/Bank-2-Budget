package bank2budget.adapters.reader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import bank2budget.core.MultiAccountBudget;
import bank2budget.core.Util;
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


    private final JsonParser parser;
    private final File budgetSettingsFile;
    private int firstOfMonth = -1;
    private Map<String, Double> budgetTemplate;

    public BudgetSettingsReader(File budgetSettingsFile) {
        this.budgetSettingsFile = budgetSettingsFile;
        parser = new JsonParser();
    }
    
    public int getFirstOfMonth() {
        return firstOfMonth;
    }
    
    public Map<String, Double> getBudgetTemplate() {
        return budgetTemplate;
    }

    public void read() {
        if (budgetSettingsFile.exists()) {
            readFrom(budgetSettingsFile);
        } else {
            Logger.getLogger(BudgetSettingsReader.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", budgetSettingsFile.getPath());
        }
    }

    private void readFrom(File file) {
        String jsString = readFileAsString(file);
        String jsonString = jsString.split("`")[1];
        JsonArray settingsArray = parser.parse(jsonString).getAsJsonArray();
        Iterator<JsonElement> iterator = settingsArray.iterator();
        while (iterator.hasNext()) {
            JsonObject settingsObject = iterator.next().getAsJsonObject();
            firstOfMonth = getFirstOfMonthFrom(settingsObject);
            budgetTemplate = getBudgetTemplateFrom(settingsObject);
            MultiAccountBudget.setFirstOfMonth(firstOfMonth);
            MultiAccountBudget.setBudgetTemplate(budgetTemplate);
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

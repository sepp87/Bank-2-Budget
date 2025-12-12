package bank2budget.adapter.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import bank2budget.core.Util;
import bank2budget.core.rule.RuleConfig;
import bank2budget.ports.RuleReaderPort;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads serialized Rules from JSON and creates Rule objects out of them. Look
 * for config file processing-rules.json in resources for an example.
 *
 * @author joost
 */
public class RuleReaderNew implements RuleReaderPort {

    private final JsonParser parser;
    private final File rulesFile;

    public RuleReaderNew(File rulesFile) {
        this.rulesFile = rulesFile;
        parser = new JsonParser();
    }

    public List<RuleConfig> read() {
        String jsString = readFileAsString(rulesFile);
        String jsonString = jsString.split("`")[1];
        return readFrom(jsonString);
    }

    private String readFileAsString(File file) {
        // Fallback string so the following methods don't break
        String jsonString = "`[]";
        try {
            jsonString = Util.readFileAsString(file);
        } catch (IOException ex) {
            Logger.getLogger(RuleReaderNew.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return jsonString;
    }

    public List<RuleConfig> readFrom(String jsonString) {
        List<RuleConfig> ruleConfigs = new ArrayList<>();
        JsonArray ruleArray = parser.parse(jsonString).getAsJsonArray();
        Iterator<JsonElement> iterator = ruleArray.iterator();
        while (iterator.hasNext()) {
            JsonObject ruleObject = iterator.next().getAsJsonObject();
            RuleConfig ruleConfig = createRuleFrom(ruleObject);
            ruleConfigs.add(ruleConfig);
        }

        return ruleConfigs;
    }

    private RuleConfig createRuleFrom(JsonObject ruleObject) {

        String checkField = ruleObject.get("if").getAsJsonObject().entrySet().iterator().next().getKey();
        String checkValue = ruleObject.get("if").getAsJsonObject().entrySet().iterator().next().getValue().getAsString();

        String resultField = ruleObject.get("then").getAsJsonObject().entrySet().iterator().next().getKey();
        String resultValue = ruleObject.get("then").getAsJsonObject().entrySet().iterator().next().getValue().getAsString();

        return new RuleConfig(checkField, checkValue, resultField, resultValue);
    }

}

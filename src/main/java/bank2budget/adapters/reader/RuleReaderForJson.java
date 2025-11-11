package bank2budget.adapters.reader;

import bank2budget.core.CashTransaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import bank2budget.core.Rule;
import bank2budget.core.Rule.StatementType;
import bank2budget.core.Util;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads serialized Rules from JSON and creates Rule objects out of them. Look
 * for config file processing-rules.json in resources for an example.
 *
 * @author joost
 */
public class RuleReaderForJson {

    private final JsonParser parser;
    private final File rulesFile;

    public RuleReaderForJson(File rulesFile) {
        this.rulesFile = rulesFile;
        parser = new JsonParser();
        
    }

    public List<Rule> read() {
        return readFrom(rulesFile);
    }

    private List<Rule> readFrom(File file) {
        List<Rule> rules = new ArrayList<>();
        String jsString = readFileAsString(file);
        String jsonString = jsString.split("`")[1];
        return readFrom(jsonString);
    }

    private String readFileAsString(File file) {
        // Fallback string so the following methods don't break
        String jsonString = "`[]";
        try {
            jsonString = Util.readFileAsString(file);
        } catch (IOException ex) {
            Logger.getLogger(RuleReaderForJson.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return jsonString;
    }

    public List<Rule> readFrom(String jsonString) {
        List<Rule> rules = new ArrayList<>();
        JsonArray ruleArray = parser.parse(jsonString).getAsJsonArray();
        Iterator<JsonElement> iterator = ruleArray.iterator();
        while (iterator.hasNext()) {
            JsonObject ruleObject = iterator.next().getAsJsonObject();
            Rule rule = createRuleFrom(ruleObject);
            rules.add(rule);
        }
        return rules;
    }

    private Rule createRuleFrom(JsonObject ruleObject) {
        Rule rule = new Rule();
        JsonObject compareObject = ruleObject.get("if").getAsJsonObject();
        JsonObject outcomeObject = ruleObject.get("then").getAsJsonObject();
        rule = setStatementsToRuleByType(compareObject, rule, StatementType.COMPARE);
        rule = setStatementsToRuleByType(outcomeObject, rule, StatementType.OUTCOME);
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new GsonLocalDateAdapter()).disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
        rule.setCompareObject(gson.fromJson(compareObject, CashTransaction.class));
        rule.setOutcomeObject(gson.fromJson(outcomeObject, CashTransaction.class));
        JsonObject operatorObject = (ruleObject.get("operator") != null) ? ruleObject.get("operator").getAsJsonObject() : new JsonObject();
        rule = setOperatorsForIfStatementsToRule(operatorObject, rule);
        return rule;
    }

    private Rule setOperatorsForIfStatementsToRule(JsonObject operators, Rule rule) {
        for (Entry<String, JsonElement> entry : operators.entrySet()) {
            String property = entry.getKey();
            String operator = entry.getValue().getAsString();
            rule.addOperator(property, operator);
        }
        return rule;
    }

    private Rule setStatementsToRuleByType(JsonObject statements, Rule rule, StatementType type) {
        for (Entry<String, JsonElement> statement : statements.entrySet()) {
            String property = statement.getKey();
            rule.addStatement(property, type);
        }
        return rule;
    }
}

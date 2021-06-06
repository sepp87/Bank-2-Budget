package io.ost.finance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ost.finance.Rule.StatementType;
import java.io.File;
import java.io.IOException;
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
public class RuleParser {

    private final JsonParser parser;

    public RuleParser() {
        parser = new JsonParser();
    }

    public List<Rule> parse(File file) {
        List<Rule> rules = new ArrayList<>();
        String jsString = readFileAsString(file);
        String jsonString = jsString.split("`")[1];
        JsonArray ruleArray = parser.parse(jsonString).getAsJsonArray();
        Iterator<JsonElement> iterator = ruleArray.iterator();
        while (iterator.hasNext()) {
            JsonObject ruleObject = iterator.next().getAsJsonObject();
            Rule rule = createRuleFrom(ruleObject);
            rules.add(rule);
        }
        return rules;
    }

    private String readFileAsString(File file) {
        String jsonString = "`[]";
        try {
            jsonString = Util.readFileAsString(file);
        } catch (IOException ex) {
            Logger.getLogger(RuleParser.class.getName()).log(Level.INFO, "Could NOT read file, proceeding without {0}", file.getPath());
        }
        return jsonString;
    }

    private Rule createRuleFrom(JsonObject ruleObject) {
        Rule rule = new Rule();
        JsonObject compareObject = ruleObject.get("if").getAsJsonObject();
        JsonObject outcomeObject = ruleObject.get("then").getAsJsonObject();
        rule = setJsonObjectToRuleByStatementType(compareObject, rule, StatementType.COMPARE);
        rule = setJsonObjectToRuleByStatementType(outcomeObject, rule, StatementType.OUTCOME);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().create();
        rule.setCompareObject(gson.fromJson(compareObject, CashTransaction.class));
        rule.setOutcomeObject(gson.fromJson(outcomeObject, CashTransaction.class));
        return rule;
    }

    private Rule setJsonObjectToRuleByStatementType(JsonObject jsonObject, Rule rule, StatementType type) {
        for (Entry<String, JsonElement> statement : jsonObject.entrySet()) {
            String property = statement.getKey();
            rule.addStatement(property, type);
        }
        return rule;
    }
}

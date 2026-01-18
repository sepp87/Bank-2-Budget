package bank2budget.core;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class AccountMerger {

    public enum MergePolicy {
        OVERWRITE_CATEGORIES,
        ENRICH_CATEGORIES
    }
    
    public static Account merge(Account existing, Account incoming) {
        return merge(existing, incoming, MergePolicy.ENRICH_CATEGORIES);
    }

    public static Account merge(Account existing, Account incoming, MergePolicy policy) {
        var workingIndex = transactionIndex(existing);
        var existingTransactions = existing.transactionsAscending();
        var incomingTransactions = incoming.transactionsAscending();

        LocalDate[] overlap = CashTransactionDomainLogic.findOverlap(existingTransactions, incomingTransactions);

        if (overlap == null) { // no overlap, so add all incoming
            incomingTransactions.forEach(e -> workingIndex.put(e.transactionNumber(), e));

        } else { // merge overlap
            var from = overlap[0];
            var to = overlap[1];
            mergeOverlap(workingIndex, existingTransactions, incomingTransactions, from, to, policy);

            // finally add non-overlapping incoming transactions
            var otherIncoming = CashTransactionDomainLogic.filterByTimespanInverted(incomingTransactions, overlap[0], overlap[1]);
            otherIncoming.forEach(e -> workingIndex.put(e.transactionNumber(), e));
        }

        return new Account(existing.getAccountNumber(), workingIndex.values());
    }

    private static TreeMap<Integer, CashTransaction> transactionIndex(Account account) {
        var result = new TreeMap<Integer, CashTransaction>();
        account.transactionsAscending().forEach(e -> result.put(e.transactionNumber(), e));
        return result;
    }

    private static void mergeOverlap(
            Map<Integer, CashTransaction> workingIndex,
            List<CashTransaction> existing,
            List<CashTransaction> incoming,
            LocalDate from,
            LocalDate to,
            MergePolicy policy
    ) {
        var existingOverlap = CashTransactionDomainLogic.filterByTimespan(existing, from, to);
        var incomingOverlap = CashTransactionDomainLogic.filterByTimespan(incoming, from, to);

        List<LocalDate> range = DateUtil.dateRange(from, to);
        Map<LocalDate, List<CashTransaction>> existingByDays = CashTransactionDomainLogic.groupByDays(range, existingOverlap);
        Map<LocalDate, List<CashTransaction>> incomingByDays = CashTransactionDomainLogic.groupByDays(range, incomingOverlap);

        for (LocalDate day : range) {
            var existingDay = existingByDays.get(day);
            var incomingDay = incomingByDays.get(day);

            // if incoming transactions contains more entries, the existing ones should be replaced
            if (incomingDay.size() > existingDay.size()) {
                incomingDay.forEach(e -> workingIndex.put(e.transactionNumber(), e));
                enrichCategories(workingIndex, existingDay, MergePolicy.OVERWRITE_CATEGORIES); // existing categories should overwrite new categories
                enrichNotes(workingIndex, existingDay); // existing notes should be persisted

            } else {
                enrichCategories(workingIndex, incomingDay, policy); // new categories should enrich existing categories

            }
        }
    }

    private static void enrichCategories(Map<Integer, CashTransaction> workingIndex, Collection<CashTransaction> enrichers, MergePolicy policy) {
        for (var tx : enrichers) {
            var number = tx.transactionNumber();
            var working = workingIndex.get(number); // if this is null, there is a bug
            boolean areSame = CashTransactionDomainLogic.areSame(working, tx);
            if (areSame && tx.category() != null && (working.category() == null || policy == MergePolicy.OVERWRITE_CATEGORIES)) {
                var enriched = working.withCategory(tx.category());
                workingIndex.put(number, enriched);
            }
        }
    }

    private static void enrichNotes(Map<Integer, CashTransaction> workingIndex, Collection<CashTransaction> enrichers) {
        for (var tx : enrichers) {
            var number = tx.transactionNumber();
            var working = workingIndex.get(number); // if this is null, there is a bug
            boolean areSame = CashTransactionDomainLogic.areSame(working, tx);
            if (areSame && tx.notes() != null) {
                var enriched = working.withNotes(tx.notes());
                workingIndex.put(number, enriched);
            }
        }
    }

}

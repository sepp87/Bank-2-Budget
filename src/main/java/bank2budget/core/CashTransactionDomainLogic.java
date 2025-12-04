package bank2budget.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author joostmeulenkamp
 */
public class CashTransactionDomainLogic {

    /**
     * When comparing transactions, category and lastOfDay is not compared
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean areSame(CashTransaction a, CashTransaction b) {
        Set<Boolean> result = new HashSet<>();
        result.add(Util.compareMoney(a.getAmount(), b.getAmount()));
        result.add(a.getTransactionNumber() == b.getTransactionNumber());
        result.add((a.getDate() == null ? b.getDate() == null : a.getDate().equals(b.getDate())));
        result.add(Util.compareMoney(a.getAccountBalance(), b.getAccountBalance()));
        result.add((a.getAccountNumber() == null ? b.getAccountNumber() == null : a.getAccountNumber().equals(b.getAccountNumber())));
        result.add(a.getAccountName() == null ? b.getAccountName() == null : a.getAccountName().equals(b.getAccountName()));
        result.add(a.getAccountInstitution() == b.getAccountInstitution());
        result.add(a.getContraAccountNumber() == null ? b.getContraAccountNumber() == null : a.getContraAccountNumber().equals(b.getContraAccountNumber()));
        result.add(a.getContraAccountName() == null ? b.getContraAccountName() == null : a.getContraAccountName().equals(b.getContraAccountName()));
        result.add(Objects.equals(a.isInternal(), b.isInternal()));
        result.add(a.getPositionOfDay() == b.getPositionOfDay());
        result.add(a.getTransactionType() == b.getTransactionType());
        result.add(a.getDescription() == null ? b.getDescription() == null : a.getDescription().equals(b.getDescription()));

//        System.out.println(Util.compareMoney(a.getAmount(), b.getAmount()));
//        System.out.println(a.getTransactionNumber() == b.getTransactionNumber());
//        System.out.println(a.getDate() == null ? b.getDate() == null : a.getDate().equals(b.getDate()));
//        System.out.println(Util.compareMoney(a.getAccountBalance(), b.getAccountBalance()) + " " + a.getAccountBalance() + " " + b.getAccountBalance());
//        System.out.println(a.getAccountNumber() == null ? b.getAccountNumber() == null : a.getAccountNumber().equals(b.getAccountNumber()));
//        System.out.println(a.getAccountName() == null ? b.getAccountName() == null : a.getAccountName().equals(b.getAccountName()));
//        System.out.println(a.getAccountInstitution() == b.getAccountInstitution());
//        System.out.println(a.getContraAccountNumber() == null ? b.getContraAccountNumber() == null : a.getContraAccountNumber().equals(b.getContraAccountNumber()));
//        System.out.println(a.getContraAccountName() == null ? b.getContraAccountName() == null : a.getContraAccountName().equals(b.getContraAccountName()));
//        System.out.println(Objects.equals(a.isInternal(), b.isInternal()));
//        System.out.println(a.getPositionOfDay() == b.getPositionOfDay());
//        System.out.println(a.getTransactionType() == b.getTransactionType());
//        System.out.println(a.getDescription() == null ? b.getDescription() == null : a.getDescription().equals(b.getDescription()));
        return !result.contains(false);
    }

    public static List<CashTransaction> sortAscending(List<CashTransaction> transactions) {
        int n = transactions.size();
        CashTransaction temp = null;
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {
                if (transactions.get(j - 1).getTransactionNumber() > transactions.get(j).getTransactionNumber()) {
                    //swap elements  
                    temp = transactions.get(j - 1);
                    transactions.set(j - 1, transactions.get(j));
                    transactions.set(j, temp);
                }
            }
        }
        return transactions;
    }

    /**
     *
     * @param transactions
     * @param from
     * @param to
     * @return all transactions outside the given time frame, transactions on
     * boundary dates (from/to) are included
     */
    public static List<CashTransaction> filterByTimespanInverted(List<CashTransaction> transactions, LocalDate from, LocalDate to) {
        return filterByTimespan(transactions, from, to, true);
    }

    /**
     *
     * @param transactions
     * @param from
     * @param to
     * @return all transactions within the given time frame, transactions on
     * boundary dates (from/to) are included
     */
    public static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to) {
        return filterByTimespan(transactions, from, to, false);
    }

    private static List<CashTransaction> filterByTimespan(List<CashTransaction> transactions, LocalDate from, LocalDate to, boolean inverted) {
        List<CashTransaction> result = new ArrayList<>();
        from = from.minusDays(1);
        to = to.plusDays(1);
        for (CashTransaction transaction : transactions) {
            LocalDate date = transaction.getDate();
            boolean withinTimespan = date.isAfter(from) && date.isBefore(to);
            // Adding or excluding transactions based on 'inverted' flag and whether they are within the time span
            if ((withinTimespan && !inverted) || (!withinTimespan && inverted)) {
                result.add(transaction);
            }
        }
        return result;
    }

    /**
     *
     * @param list1 sorted list in ascending order by date
     * @param list2 sorted list in ascending order by date
     * @return
     */
    public static LocalDate[] findOverlap(List<CashTransaction> list1, List<CashTransaction> list2) {
        List<LocalDate> dates1 = getDatesFrom(list1);
        List<LocalDate> dates2 = getDatesFrom(list2);
        return Util.findOverlap(dates1, dates2);
    }

    private static List<LocalDate> getDatesFrom(List<CashTransaction> list) {
        List<LocalDate> result = new ArrayList<>();
        for (CashTransaction transaction : list) {
            result.add(transaction.getDate());
        }
        return result;
    }

    public static Map<String, List<CashTransaction>> groupByCategory(List<CashTransaction> transactions) {
        Map<String, List<CashTransaction>> grouped = new TreeMap<>();
        for (var transaction : transactions) {
            grouped.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }
        return grouped;
    }

    public static List<CashTransaction> categorized(List<CashTransaction> transactions) {
        return filterByCategorized(transactions, true);
    }

    public static List<CashTransaction> uncategorized(List<CashTransaction> transactions) {
        return filterByCategorized(transactions, false);
    }

    private static List<CashTransaction> filterByCategorized(List<CashTransaction> transactions, boolean wantCategorized) {
        List<CashTransaction> result = new ArrayList<>();
        for (var transaction : transactions) {
            String category = transaction.getCategory();
            boolean isCategorized = category != null && !category.isBlank();

            if (wantCategorized == isCategorized) {
                result.add(transaction);
            }
        }
        return result;
    }

}

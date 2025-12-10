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
    public static boolean areSame(Transaction a, Transaction b) {
        Set<Boolean> result = new HashSet<>();
        
        result.add(a.amount().compareTo(b.amount()) == 0);
        result.add(a.transactionNumber() == b.transactionNumber());
        result.add((a.date() == null ? b.date() == null : a.date().equals(b.date())));
        result.add(a.accountBalance().compareTo(b.accountBalance()) == 0);
        result.add((a.accountNumber() == null ? b.accountNumber() == null : a.accountNumber().equals(b.accountNumber())));
        result.add(a.accountName()== null ? b.accountName() == null : a.accountName().equals(b.accountName()));
        result.add(a.accountInstitution()== b.accountInstitution());
        result.add(a.contraAccountNumber()== null ? b.contraAccountNumber() == null : a.contraAccountNumber().equals(b.contraAccountNumber()));
        result.add(a.contraAccountName() == null ? b.contraAccountName() == null : a.contraAccountName().equals(b.contraAccountName()));
        result.add(Objects.equals(a.internal(), b.internal()));
        result.add(a.positionOfDay()== b.positionOfDay());
        result.add(a.transactionType()== b.transactionType());
        result.add(a.description()== null ? b.description() == null : a.description().equals(b.description()));

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

    /**
     *
     * @param transactions
     * @param from
     * @param to
     * @return all transactions outside the given time frame, transactions on
     * boundary dates (from/to) are included
     */
    public static List<Transaction> filterByTimespanInverted(List<Transaction> transactions, LocalDate from, LocalDate to) {
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
    public static  List<Transaction> filterByTimespan(List<Transaction> transactions, LocalDate from, LocalDate to) {
        return filterByTimespan(transactions, from, to, false);
    }

    private static List<Transaction> filterByTimespan(List<Transaction> transactions, LocalDate from, LocalDate to, boolean inverted) {
        List<Transaction> result = new ArrayList<>();
        from = from.minusDays(1);
        to = to.plusDays(1);
        for (var transaction : transactions) {
            LocalDate date = transaction.date();
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
    public static LocalDate[] findOverlap(List<Transaction> list1, List<Transaction> list2) {
        List<LocalDate> dates1 = getDatesFrom(list1);
        List<LocalDate> dates2 = getDatesFrom(list2);
        return Util.findOverlap(dates1, dates2);
    }

    private static List<LocalDate> getDatesFrom(List<Transaction> list) {
        List<LocalDate> result = new ArrayList<>();
        for (var transaction : list) {
            result.add(transaction.date());
        }
        return result;
    }

    public static Map<String, List<Transaction>> groupByCategory(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = new TreeMap<>();
        for (var transaction : transactions) {
            grouped.computeIfAbsent(transaction.category(), k -> new ArrayList<>()).add(transaction);
        }
        return grouped;
    }

    public static List<Transaction> categorized(List<Transaction> transactions) {
        return filterByCategorized(transactions, true);
    }

    public static List<Transaction> uncategorized(List<Transaction> transactions) {
        return filterByCategorized(transactions, false);
    }

    private static List<Transaction> filterByCategorized(List<Transaction> transactions, boolean wantCategorized) {
        List<Transaction> result = new ArrayList<>();
        for (var transaction : transactions) {
            String category = transaction.category();
            boolean isCategorized = category != null && !category.isBlank();

            if (wantCategorized == isCategorized) {
                result.add(transaction);
            }
        }
        return result;
    }

}

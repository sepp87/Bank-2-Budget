package bank2budget.ports;

import bank2budget.core.Account;
import java.io.File;
import java.util.List;

/**
 *
 * @author joostmeulenkamp
 */
public interface AccountImporterPort {

    List<Account> importFromTodo();

    List<Account> importFromFiles(List<File> files);
}

package bank2budget.ui;

/**
 *
 * @author joostmeulenkamp
 */
public interface Modal {
    
    void setOnCanceled();
    
    void setOnFinished();
    
    void commitChanges();
}

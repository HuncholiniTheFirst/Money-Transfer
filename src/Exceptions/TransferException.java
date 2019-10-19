package Exceptions;

public class TransferException extends Exception {
    public TransferException(String errorReason) {
        super(new StringBuilder("Error performing transfer: " + errorReason).toString());
    }
}

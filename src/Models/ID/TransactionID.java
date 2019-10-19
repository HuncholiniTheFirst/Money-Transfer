package Models.ID;

public class TransactionID extends ShortRandomID{

    public TransactionID(){
      super();
    }

    public TransactionID(String id){
        super(id);
    }

    public static TransactionID of(String id){
        return new TransactionID(id);
    }
}

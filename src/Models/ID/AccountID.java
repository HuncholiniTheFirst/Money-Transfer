package Models.ID;

public class AccountID extends ShortRandomID{

    public AccountID(){
        super();
    }

    public AccountID(String id){
        super(id);
    }

    public static AccountID of(String id){
        return new AccountID(id);
    }
}

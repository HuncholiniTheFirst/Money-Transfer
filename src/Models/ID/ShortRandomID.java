package Models.ID;

import org.apache.commons.lang3.RandomStringUtils;

public abstract class ShortRandomID {
    private final String id;

    public ShortRandomID() {
        this.id = RandomStringUtils.random(6, true, true);
    }

    public ShortRandomID(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public boolean equals(ShortRandomID ID){
        return this.id.equals(ID.getId());
    }

}

package Network;

public class Packet {
    private Action action;
    private Object payload;
    private String error;

    public Packet() {
    }

    public Action getAction() {
        return action;
    }

    public Object getPayload() {
        return payload;
    }


    public String getError() {
        return error;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
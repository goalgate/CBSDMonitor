package cn.cbdi.cbsdmonitor.Status;



public class OperationStatus {

    public static int working = 1;

    public static int adding = 2;

    public static int removing = 3;

    private int status;

    private static OperationStatus instance = null;

    public static OperationStatus getInstance() {
        if (instance == null) {
            instance = new OperationStatus();
        }
        return instance;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

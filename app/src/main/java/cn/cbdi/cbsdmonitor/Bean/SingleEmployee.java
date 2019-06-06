package cn.cbdi.cbsdmonitor.Bean;

public class SingleEmployee {

    private Employees em;

    private static SingleEmployee instance = null;

    public static SingleEmployee getInstance() {
        if (instance == null) {
            instance = new SingleEmployee();
        }
        return instance;
    }

    public void setEm(Employees em) {
        this.em = em;
    }

    public Employees getEm() {

        return em;
    }

    private SingleEmployee() {
        em = new Employees();
    }

    public void clear(){
        em = new Employees();

    }


}

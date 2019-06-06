package cn.cbdi.cbsdmonitor.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Employees {

    @Id
    private String CardID;

    private String name;

    private String FpID;

    private String FpTemp;

    private byte[] FaseBytes;

    private int type;

    @Generated(hash = 2110055720)
    public Employees(String CardID, String name, String FpID, String FpTemp,
            byte[] FaseBytes, int type) {
        this.CardID = CardID;
        this.name = name;
        this.FpID = FpID;
        this.FpTemp = FpTemp;
        this.FaseBytes = FaseBytes;
        this.type = type;
    }

    @Generated(hash = 1097432834)
    public Employees() {
    }

    public String getCardID() {
        return this.CardID;
    }

    public void setCardID(String CardID) {
        this.CardID = CardID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFpID() {
        return this.FpID;
    }

    public void setFpID(String FpID) {
        this.FpID = FpID;
    }

    public String getFpTemp() {
        return this.FpTemp;
    }

    public void setFpTemp(String FpTemp) {
        this.FpTemp = FpTemp;
    }

    public byte[] getFaseBytes() {
        return this.FaseBytes;
    }

    public void setFaseBytes(byte[] FaseBytes) {
        this.FaseBytes = FaseBytes;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }



   
}

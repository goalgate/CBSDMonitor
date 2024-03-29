package cn.cbdi.drv.card;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.cvr.device.CVRApi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import cn.cbdi.log.Lg;


// android:sharedUserId="android.uid.system"
//身份证读取类
public class CardInfoRk123x extends SerialPortCom implements ICardInfo {
    private final int t_name = 1;
    private final int t_sex = 2;
    private final int t_nation = 3;
    private final int t_birthday = 4;
    private final int t_address = 5;
    private final int t_cerdId = 6;
    private final int t_dept = 7;
    private final int t_validDateBegin = 8;
    private final int t_validDate = 9;

    private final int info_false = 0;
    private final int info_true = 1;
    private final int info_no = 2;
    private String ver="1.1";

    public String getVer() {
        return ver;
    }

    private String  sam_="";


    //姓名
    private String name_ = "";
    //姓别
    private String sex_ = "";
    //民族
    private String nation_ = "";
    //出生
    private String birthday_ = "";
    //住址
    private String address_ = "";
    //公民身份号码
    private String cardId_ = "";
    //签发机关
    private String dept_ = "";
    //有效期起始日期
    private String validDateBegin_ = "";
    //有效期截止日期
    private String validDate_ = "";

    //接收数据最后时间
    private long lastRevTime_;

    private String uid_="";

    //读取成功后的数据
    private boolean isReadOk_=false;


    private String filepath_="";

    private int readType_=0;  //读卡类型
    private int readState_=0;  //返回状态
    private String devType="rk312x";  //rk312x

    private boolean isIC=false;  //是否打开IC卡





    //检测是否有设备
    //02021100050302001103AA00010203040506070809B9037F7755
    //0202010091E1AC
    //private byte[] dt_check ={ 0x02, 0x00, 0x11, 0x03,(byte)0xAA, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, (byte)0xB9, 0x03 };
    //private byte[] dt_check_ ={ 0x02, 0x00, 0x11, 0x03, 0x00, 0x01, 0x03, 0x01, 0x07, 0x01, 0x03, 0x01, 0x0F, 0x01, 0x03, 0x18, 0x03 };

    private byte[] dt_check =new byte[]{ 0x02,0x02,0x11,0x00,0x05,0x03,0x02, 0x00, 0x11, 0x03,(byte)0xAA, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, (byte)0xB9, 0x03,(byte)0x7F,0x77,0x55 };
    private byte[] dt_check_ =new byte[]{0x02, 0x02, 0x01, 0x00,(byte)0x91,(byte)0xE1,(byte)0xAC};

    //读取SAM编号
    private byte[] dt_sam={0x02,0x02,0x0c,0x00,0x05,0x03,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96,0x69,0x00,0x03,0x12,(byte)0xFF,(byte)0xEE,0x10,(byte)0x8A,0x55};



    //检测是否有身份证
    //private byte[] dt_isCer ={ (byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22 };
    private byte[] dt_isCer =new byte[]{ 0x02,0x02,0x0C,0x00,0x05,0x03,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22,(byte)0xF1,0x70,0x55};
    private byte[] dt_isCer_no ={ (byte)0xAA,(byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x04, 0x00, 0x00,(byte)0x80,(byte)0x84 }; //11
    private byte[] dt_isCer_yes ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x08, 0x00, 0x00, (byte)0x9F, 0x00, 0x00, 0x00, 0x00, (byte)0x97 };  //15
    //选卡
    //private byte[] dt_selectCer ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21 };
    private byte[] dt_selectCer =new byte[]{ 0x02,0x02,0x0C,0x00,0x05,0x03,(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21,(byte)0xB1,(byte)0x81,0x55 };
    private byte[] dt_selectCer_no ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x04, 0x00, 0x00, (byte)0x81, (byte)0x85 }; //11
    private byte[] dt_selectCer_yes ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x0C, 0x00, 0x00, (byte)0x90, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x9C }; //19

    //读身份证
   //private byte[] dt_readCer ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32 };
    private byte[] dt_readCer =new byte[]{ 0x02,0x02,0x0C,0x00,0x05,0x03,(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32,(byte)0xF1,0x79,0x55  };
    private byte[] dt_readCer_no ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x00, 0x04, 0x00, 0x00, 0x41, 0x45 }; //11
    private byte[] dt_readCer_yes ={ (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x96, 0x69, 0x05, 0x08, 0x00, 0x00, (byte)0x90 }; //10


    //IC卡 type A卡
    private byte[] dt_ica_open=new byte[]{0x02,0x02,0x03,0x00,0x0c,0x01,0x01,0x60,0x53,0x55};
    private byte[] dt_ica_close=new byte[]{0x02,0x02,0x03,0x00,0x0c,0x01,0x00,(byte)0xa1,(byte)0x93,0x55};
    private byte[] dt_ica_read=new byte[]{0x02,0x02,0x03,0x00,0x12,0x02,0x52,0x40,(byte)0x98};
    private byte[] dt_ica_ok=new byte[]{0x02,0x02,0x01,0x00,0x00,0x20,0x00};
    private byte[] dt_ica_false=new byte[]{0x02,0x02,0x01,0x00,0x15,(byte)0xE1,(byte)0xCF};
    private byte[] dt_ica_set=new byte[]{0x02,0x02,0x03,0x00,0x08,0x01,0x41,0x20,0x62,0x55};
    private byte[] dt_ica_readOK=new byte[]{0x02,0x02,0x08,0x00,0x00,0x07,0x04};

    //命令类型
    private int cmdType = 0;
    private final int ct_check = 1;
    private final int ct_isCer = 2;
    private final int ct_selectCer = 3;
    private final int ct_readCer = 4;
    private final int ct_sam=20;
    private Timer terCheck = new Timer(); //检测是否读完


    //收到的数据
    private byte[] buf_ = new byte[2048];
    private byte[] wltBuf = new byte[1024]; //照片数据
    private byte[] bufic_=new byte[256];
    private int bufCount = 0;

    private boolean openState_ = false;
    private boolean isReadCer_ = false;
    private boolean isCheck_ = false;
    private int checkCount_ = 0;
    private boolean terCheck_=false;

    private boolean isGetCard_=false; //取身份证信息
    private int getCardCount_=0;  //读卡记数

    private int cmd_ic_type=0;
    private final int ct_ic_open = 1;
    private final int ct_ic_set = 2;
    private final int ct_ic_read = 3;
    private final int ct_ic_readOK = 14;


    //头像解码
    CVRApi picUnpack;
    Handler cvrHandler = new Handler() {};

    private ICardState iCardState_;  //事件接口

    public void readIC()
    {
        isIC=true;
    }

    public void stopReadIC()
    {
        isIC=false;
    }

    //取照片数据
    public byte[] getWltBuf()
    {
        return wltBuf;
    }

    public CardInfoRk123x(String port, ICardState iCardState)
    {
         if(!port.equals(""))
         setDevName(port);

        iCardState_=iCardState;
        filepath_= Environment.getExternalStorageDirectory().getAbsolutePath();
        try
        {
            String ph=filepath_+"/assets/wltlib";
            File file = new File(ph);
            if (!file.exists()) {
                file.mkdir();
            }
        }catch (Exception ex){
            Lg.e("CardInfo_CardInfo",ex.toString());
            }



    }


    public void readCard()
    {
        isGetCard_=true;
    }

    public void stopReadCard()
    {
        isGetCard_=false;
    }


    public int open()
    {
        int ix=open(115200);
        picUnpack=new CVRApi(cvrHandler);
        if(ix>=0) {
            terCheck.schedule(task, 0, 200);
            picUnpack=new CVRApi(cvrHandler);
        }
        cmd_ic_type=0;
        return ix;

    }


    //取头像数据
    public Bitmap getBmp()
    {
        return getBmp(wltBuf);
    }

    //取头像数据
    public Bitmap getBmp(byte[] wlt)
    {
        if(picUnpack!=null)
        {
              try
              {
                  byte[] bmpdata = new byte[38862];
                  String fp= Environment.getExternalStorageDirectory().getAbsolutePath() + "/wltlib";

                  //String fp="/storage/sdcard0/wltlib";
                  File fn=new File(fp+"/zp.bmp");
                  if(fn.exists())
                  {
                      fn.delete();
                  }
                  int len=picUnpack.Unpack(fp,wlt,bmpdata);
                  int i=len;
                  if(fn.exists()) {
                      FileInputStream fis = new FileInputStream(fp + "/zp.bmp");
                      Bitmap bmp = BitmapFactory.decodeStream(fis);
                      fis.close();
                      return bmp;
                  }
              }catch(Exception ex){
                  Lg.e("CardInfo_getBmp",ex.toString());
              };
        }
        return null;
    }



    public boolean isOpen()
    {
        if(getPortState()>=0)
        {
            return true;
        }else
        {
            return false;
        }
    }

    public void chearBuf()
    {

    }

    public boolean isReadCer()
    {
         return isReadCer_;
    }
    public String name()
    {
         return name_;
    }

    public String sex()
    {
         return sex_;
    }

    public String nation()
    {
        return nation_;
    }

    public String birthday()
    {
        return birthday_;
    }

    public String address()
    {
         return address_;
    }

    public String cardId()
    {
         return cardId_;
    }
    
    public String agency()
    {
    	return dept_;
    }

    public String validDateBegin()
    {
         return validDateBegin_;
    }

    public String validDate()
    {
        return validDate_;
    }

    private void sendData(int ct, byte[] bs)
    {
        try
        {
            cmdType = ct;
            clear();
            write(bs);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        }
        catch(Exception ex) {
            Lg.e("CardInfo_sendData",ex.toString());
        };

        terCheck_ = true;

    }


    private void sendData(byte[] bs)
    {
        try
        {
            write(bs);
            lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        }
        catch(Exception ex) {
            Lg.e("CardInfo_sendData",ex.toString());
        };

        terCheck_ = true;

    }

    public  void free()
    {
        terCheck.cancel();
        close();

    }



    public void check()
    {
        try
        {
            sendData(ct_check, dt_check);
        }
        catch(Exception ex) { };
        isCheck_ = false;
    }

    public void isCer()
    {
        sendData(ct_isCer, dt_isCer);
    }

    public void selectCer()
    {
        sendData(ct_selectCer, dt_selectCer);
    }

    public void readCer()
    {
        sendData(ct_readCer, dt_readCer);
    }

    public void isCheck()
    {
        if(getPortState()<0)
        {
            return ;
        }

        if (!isCheck_)
        {
            terCheck_ = false;
            isCer();
        }
        else
        {
            terCheck_ = true;
            //check();
            checkCount_++;
            if (checkCount_ > 2)
            {
                long l = (System.currentTimeMillis() - lastRevTime_) / 1000;
                if (l >1)
                {
                    check();
                    checkCount_ = 0;
                }
            }
        }
    }

    //发送IC卡命令
    public void sendCmdIc()
    {
        if(cmd_ic_type==0)
        {
            sendData(dt_ica_open);
        }else if(cmd_ic_type==ct_ic_open)
        {
            sendData(dt_ica_set);
        }else if(cmd_ic_type==ct_ic_set) {
            sendData(dt_ica_read);
        }else if(cmd_ic_type==ct_ic_read)
        {
            sendData(dt_ica_read);
        }
    }

    public String getUid()
    {
        return uid_;
    }

    //接收的IC卡数据
    public boolean readIC(byte[] bs)
    {
        if(cmd_ic_type==0)
        {
            if(isData(bs,dt_ica_ok))
            {
                cmd_ic_type=ct_ic_open;
                return true;
            }
        }else  if(cmd_ic_type==ct_ic_open)
        {
            if(isData(bs,dt_ica_ok))
            {
                cmd_ic_type=ct_ic_set;
                return true;
            }
        }else  if(cmd_ic_type==ct_ic_set)
        {
            if(isData(bs,dt_ica_readOK))
            {
                cmd_ic_type=ct_ic_read;
                byte[] bx=new byte[4];
                System.arraycopy(bs,7,bx,0,4);
                uid_=byteToStr(bx,4);
                //接收的IC卡数据
                dataInfo(ct_ic_readOK,1);
                return true;
            }
        }else if(cmd_ic_type==ct_ic_read)
        {
            if(isData(bs,dt_ica_false))
            {
                cmd_ic_type=ct_ic_set;
                return true;
            }
        }
        return false;
    }

    public  String formatStr(String str,int len)
    {
        String s="";
        if(str.length()==len)
        {
            s=str;
        }
        else if(str.length()<len)
        {
            for(int i=str.length();i<len;i++)
            {
                s='0'+s;
            }
            s=s+str;
        }else if (str.length()>len)
        {
            s=str.substring(str.length()-len);

        }

        return s;


    }

    public void readSam()
    {
        sendData(ct_sam,dt_sam);
    }


    public String intToStr(byte[] bs,int pos,int len)
    {
        String s="";
        if(bs.length<(pos+len))
        {
            return "";
        }else
        {
            long ii=0;long ix=1;
            try {
                for (int i = pos; i < pos + len; i++) {
                    ii += (bs[i]&0xff) * ix;
                    ix *= 256;
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            s=""+ii;
        }
        return s;
    }

    private void getSam_()
    {

        sam_=formatStr(intToStr(buf_,10+5,1),2)+formatStr(intToStr(buf_,12+5,1),2)+"-"+intToStr(buf_,14+5,4)+
                "-"+formatStr(intToStr(buf_,18+5,4),10)+"-"+formatStr(intToStr(buf_,22+5,4),10);

        /*
        String s="";
        for(int i=0;i<36;i++)
        {
           if(i==0)
           {
               s=byteToHex(buf_[0]);
           }else
           {
               s+=" "+byteToHex(buf_[i]);
           }
        }
       sam_=s;
       */

    }

    public String getSam()
    {
        return sam_;
    }


    //接收数据
    public void onRead(int fd,int len,byte[] buf)
    {
        if(buf==null){return;}
        if(buf.length<len){return;}
        lastRevTime_ = System.currentTimeMillis();    //记录最后一次串口接收数据的时间
        checkCount_ = 0;

        Lg.v("onRead",byteToStr(buf,len)+"_"+cmd_ic_type);
        int btr = len;

        byte[] by = new byte[btr];


        if (btr > 0)
        {

            /*
            if(devType.equals("rk312x")&&len>7)
            {
                System.arraycopy(buf, 5, by, 0, btr);        //依据串口数据长度BytesToRead来接收串口的数据并存放在by数组之中
            }else {
                System.arraycopy(buf, 0, by, 0, btr);        //依据串口数据长度BytesToRead来接收串口的数据并存放在by数组之中
            }
            */
            System.arraycopy(buf, 0, by, 0, btr);        //依据串口数据长度BytesToRead来接收串口的数据并存放在by数组之中

            if(isIC) {
                if (readIC(by)) {
                    return;
                }
            }

            if ((bufCount + btr) < 2048)
            {
                try
                {
                    System.arraycopy(by, 0,buf_,bufCount, btr);
                }catch(Exception ex)
                {

                }

            }else { bufCount = 0; }
            bufCount += btr;
            toData(bufCount);
        }

    }

    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF&b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }

    public String byteToStr(byte[] bs,int len)
    {
        if(bs.length>=len)
        {
            String s="";
            for(int i=0;i<len;i++)
            {
                s+=byteToHex(bs[i]);
            }
            return s;

        }else
        {
            return "";
        }
    }

    //数据处理
    public void toData(int bc)
    {
        try
        {

            //Lg.v("toData",byteToStr(buf_,bc));
            if (cmdType == ct_check)
            {
                //02 00 11 03 00 01 03 01 07 01 03 01 0F 01 03 18 03
                if (bc >= dt_check_.length)
                {
                    if (isData(buf_, dt_check_))
                    {
                        openState_ = true;
                        checkCount_=0;
                        dataInfo(ct_check, info_true);
                    }
                    else
                    {
                        openState_ = false;
                    }
                    clear();
                }
            }
            else if (cmdType == ct_isCer)
            {


                if (bc >= 11+7)
                {
                    if (isData_(buf_, dt_isCer_no))
                    {
                        openState_ = true;
                        dataInfo(ct_isCer, info_false);
                        isReadCer_ = false;

                    }
                    else if (isData_(buf_, dt_isCer_yes))
                    {
                        openState_ = true;
                        dataInfo(ct_isCer, info_true);
                    }

                }
            }
            else if (cmdType == ct_selectCer)
            {
                if (bc >= 11+7)
                {
                    if (isData_(buf_, dt_selectCer_no))
                    {
                        openState_ = true;
                        isReadCer_ = false;
                        dataInfo(ct_selectCer, info_false);

                    }
                    else if (isData_(buf_, dt_selectCer_yes))
                    {
                        openState_ = true;
                        dataInfo(ct_selectCer, info_true);
                    }

                }
            }
            else if (cmdType == ct_readCer)
            {
                if (bc >= 11+7 && bc < 15+7)
                {
                    if (isData_(buf_, dt_readCer_no))
                    {
                        isReadCer_ = false;
                        isCheck_ = false;
                        dataInfo(ct_readCer, info_false);
                    }
                }
                else if (bc >= 1294+7)
                {
                    if (isData_(buf_, dt_readCer_yes))
                    {
                        readCerd();
                        dataInfo(ct_readCer, info_true);
                    }
                    isCheck_ = false;
                }
            }else if (cmdType == ct_sam)
            {
                if(bc>=34)
                {
                    getSam_();
                    dataInfo(ct_sam, info_true);
                }
            }
        }
        catch(Exception ex) {
            Lg.e("CardInfo_toData",ex.toString());
        };
    }

    private void readCerd()
    {
        name_ = "";
        sex_ = "";
        nation_ = "";
        birthday_ = "";
        address_ = "";
        cardId_ = "";
        dept_ = "";
        validDateBegin_ = "";
        validDate_ = "";
        isReadCer_ = true;
        name_ = getCerInfo(buf_, t_name);
        sex_ = getCerInfo(buf_, t_sex);
        nation_ = getCerInfo(buf_, t_nation);
        birthday_ = getCerInfo(buf_, t_birthday);
        address_ = getCerInfo(buf_, t_address);
        cardId_ = getCerInfo(buf_, t_cerdId);
        dept_ = getCerInfo(buf_, t_dept);
        validDateBegin_ = getCerInfo(buf_, t_validDateBegin);
        validDate_ = getCerInfo(buf_, t_validDate);
        sex_ = getSex(sex_);
        nation_ = getNation(nation_);
        try
        {
            System.arraycopy(buf_,270+5,wltBuf,0,1024);
        }
        catch(Exception ex) {
            Lg.e("CardInfo_readCerd",ex.toString());
        }


    }


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    if(isGetCard_)
                    {
                        getCardCount_++;
                        if(getCardCount_>2)
                        {
                            getCardCount_=0;
                            isCheck();
                        }
                    }

                    if(isIC)
                    {
                        try {
                            Thread.sleep(50);
                        }catch (Exception ex){}
                        sendCmdIc();
                    }

                    if(terCheck_) {
                        terCheck_Tick();
                        break;
                    }
                    super.handleMessage(msg);
            }
        }
    };


    private TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 10;
            handler.sendMessage(message);
        }
    };



    //检测是否读完
    private void terCheck_Tick()
    {
        try
        {
            if (bufCount > 0)
            {
                long l = (System.currentTimeMillis() - lastRevTime_) / 1000;

                if (cmdType == ct_readCer)
                {
                    if (l >= 2)
                    {
                        if (bufCount >= 270+1024+7)
                        {
                            if (isData_(buf_, dt_readCer_yes))
                            {
                                readCerd();
                                dataInfo(ct_readCer, info_true);
                            }
                            else
                            {
                                dataInfo(ct_readCer, info_no);
                            }
                        }
                        else
                        {
                            if (isData_(buf_, dt_readCer_no))
                            {
                                dataInfo(ct_readCer, info_false);
                            }
                            else
                            {

                                dataInfo(ct_readCer, info_no);
                            }
                        }
                        isCheck_ = false;
                    }
                }
                else
                {
                    if (l >= 1)
                    {
                        dataInfo(cmdType, info_no);
                        openState_ = false;
                        isCheck_ = false;
                    }
                }
            }
        }
       catch (Exception ex) { }
    }

    //是否是数据
    public boolean isData(byte[] b1, byte[] b2)
    {
        if (b1 == null || b2 == null) { return false; }
        if (b1.length < b2.length) { return false; }
        for (int i = 0; i < b2.length; i++)
        {
            if (b1[i] != b2[i])
            {
                return false;
            }
        }
        return true;
    }

    public boolean isData_(byte[] b1, byte[] b2)
    {
        if (b1 == null || b2 == null) { return false; }
        if (b1.length < b2.length) { return false; }
        for (int i = 0; i < b2.length; i++)
        {
            if (b1[i+5] != b2[i])
            {
                return false;
            }
        }
        return true;
    }

    private void clear()
    {
        bufCount = 0;
        //checkCount_ = 0;
        int c = 0;
        terCheck_ = false;
        if (cmdType == ct_check)
        {
            c = 17+7;
        }
        else if (cmdType == ct_isCer)
        {
            c = 15+7;
        }
        else if (cmdType == ct_selectCer)
        {
            c = 19+7;
        }
        else if (cmdType == ct_readCer)
        {
            c = 270+7;
        }

        if (c < 2047)
        {
            for (int i = 0; i < c; i++)
            {
                buf_[i] = 0;
            }
        }

    }

    //取各段名称
    public String toUCS2(byte[] bx)
    {
        if(bx==null){return null;};
        byte b=0;
        for (int i = 0; i <(bx.length/2); i++)
        {
            b=bx[2*i];
            bx[2*i] =bx[2*i+1];
            bx[2*i+1]=b;
        }
        String s="";
        try
        {
            s= new String(bx, "ISO-10646-UCS-2");
            s=s.trim();
        }catch(Exception ex){s="";};
        return s;
    }

    public String getCerInfo(byte[] bs, int itype)
    {
        if (bs == null) { return ""; };
        String s = "";
        if (bs.length >= 256)
        {
            if (itype == 1)
            {
                byte[] bx = new byte[30];
                for (int i = 0; i < 30; i++)
                {
                    bx[i] = bs[i + 14+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 2)
            {
                byte[] bx = new byte[2];
                for (int i = 0; i < 2; i++)
                {
                    bx[i] = bs[i + 44+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 3)
            {
                byte[] bx = new byte[4];
                for (int i = 0; i < 4; i++)
                {
                    bx[i] = bs[i + 46+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 4)
            {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++)
                {
                    bx[i] = bs[i + 50+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 5)
            {
                byte[] bx = new byte[70];
                for (int i = 0; i < 70; i++)
                {
                    bx[i] = bs[i + 66+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 6)
            {
                byte[] bx = new byte[36];
                for (int i = 0; i < 36; i++)
                {
                    bx[i] = bs[i + 136+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 7)
            {
                byte[] bx = new byte[30];
                for (int i = 0; i < 30; i++)
                {
                    bx[i] = bs[i + 172+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 8)
            {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++)
                {
                    bx[i] = bs[i + 202+5];
                }
                s=toUCS2(bx);
            }
            else if (itype == 9)
            {
                byte[] bx = new byte[16];
                for (int i = 0; i < 16; i++)
                {
                    bx[i] = bs[i + 218+5];
                }
                s=toUCS2(bx);
            }
                /*
            else if (itype ==10)
            {
                byte[] bx = new byte[70];
                for (int i = 0; i <70; i++)
                {
                    bx[i] = bs[i + 234];
                }
                s = System.Text.Encoding.GetEncoding("UCS-2").GetString(bx).ToString().Trim();
            }
            */
        }
        return s;
    }

    //取性别
    private String getSex(String code)
    {
        if (code.trim().equals("2"))
        {
            return ("女");
        }
        else if (code.trim().equals("1"))
        {
            return ("男");
        }
        else
        {
            return "";
        }
    }

    //取民族
    private String getNation(String scode)
    {
        int code = 0;
        try
        {

            code = Integer.parseInt(scode);
        }
        catch(Exception ex) { code = 0; };

        switch (code)
        {
            case 01: return ("汉");
            case 02: return ("蒙古");
            case 03: return ("回");
            case 04: return ("藏");
            case 05: return ("维吾尔");
            case 06: return ("苗");
            case 07: return ("彝");
            case 8: return ("壮");
            case 9: return ("布依");
            case 10: return ("朝鲜");
            case 11: return ("满");
            case 12: return ("侗");
            case 13: return ("瑶");
            case 14: return ("白");
            case 15: return ("土家");
            case 16: return ("哈尼");
            case 17: return ("哈萨克");
            case 18: return ("傣");
            case 19: return ("黎");
            case 20: return ("傈僳");
            case 21: return ("佤");
            case 22: return ("畲");
            case 23: return ("高山");
            case 24: return ("拉祜");
            case 25: return ("水");
            case 26: return ("东乡");
            case 27: return ("纳西");
            case 28: return ("景颇");
            case 29: return ("柯尔克孜");
            case 30: return ("土");
            case 31: return ("达斡尔");
            case 32: return ("仫佬");
            case 33: return ("羌");
            case 34: return ("布朗");
            case 35: return ("撒拉");
            case 36: return ("毛南");
            case 37: return ("仡佬");
            case 38: return ("锡伯");
            case 39: return ("阿昌");
            case 40: return ("普米");
            case 41: return ("塔吉克");
            case 42: return ("怒");
            case 43: return ("乌孜别克");
            case 44: return ("俄罗斯");
            case 45: return ("鄂温克");
            case 46: return ("德昂");
            case 47: return ("保安");
            case 48: return ("裕固");
            case 49: return ("京");
            case 50: return ("塔塔尔");
            case 51: return ("独龙");
            case 52: return ("鄂伦春");
            case 53: return ("赫哲");
            case 54: return ("门巴");
            case 55: return ("珞巴");
            case 56: return ("基诺");
            case 97: return ("其他");
            case 98: return ("外国血统中国籍人士");
            default: return ("");
        }
    }


    //收到数据信息
    private void dataInfo(int ct, int state)
    {
        //Lg.d("CardInfo_dataInfo",ct+"_"+state);
        clear();
        if (ct == ct_isCer)
        {
            if (state == info_true)
            {
                selectCer();
                isCheck_ = true;
            }
        }
        else if (ct == ct_selectCer)
        {
            if (state == info_true)
            {
                readCer();
            }
            else
            {
                isCheck_ = false;
            }
        }
        else if (ct == ct_readCer)
        {
            isCheck_ = false;
        }

        try
        {
            readType_=ct;
            readState_=state;
            readHandler.sendEmptyMessage(0);
            if(ct==4&&state==1)
            {
                isReadOk_=true;
            }
        }
        catch(Exception ex) { };

    }

public boolean isReadOk()
{
    return isReadOk_;
}
    public void clearIsReadOk()
    {
        isReadOk_=false;
    }

    private Handler readHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what != 0) {
                return;
            }
            iCardState_.onCardState(readType_,readState_);
        }
    };



}

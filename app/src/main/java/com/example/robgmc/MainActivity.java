package com.example.robgmc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    static final String folder_xml_from_ftp = "XMLFilesFromsFTP";
    static final String folder_store_ci = "XMLFilesForCI";
    static final String folder_store_sabloane = "SABLOANE_XML";
    static final String CI_sablon_23 = "CI_10XRO-TEL------2_10XBG-ESO------A_1_23.xml";
    static final String CI_sablon_24 = "CI_10XRO-TEL------2_10XBG-ESO------A_1_24.xml";
    static final String CI_sablon_25 = "CI_10XRO-TEL------2_10XBG-ESO------A_1_25.xml";
    static final String EIC_RO_AREA = "10YRO-TEL------P";
    static final String EIC_BG_AREA = "10YCA-BULGARIA-R";
    static final String EIC_NORDPOOL = "11XNORDPOOLSPOT2";
    static final String EIC_IBEX = "32X001100101076N";
    static final String mRID = "MC_RESULTS_ESO-TEL_";
    static final int TopBottomPad = 15;
    static final int FontSize = 14;

    static final String hostESO = "bneb.eso.bg";
    static final String hostIPESO = "195.225.127.58";
    static final String userESO = "bgro-poda";
    static final String passwordESO = "Bgr0#p)DA45%";
    static final int portESO = 222;
    static final String FlowRemotePath = "/Flow/Processed/Processed";
    static final String PriceRemotePath = "/FPR/Processed/Processed";
    static final String GFCRemotePath = "/GFC/Processed";
    static final String keyString = "MV156FgOsnJxxy2xKY355wxb7ySlg0B9CpEUl/ONvmc=";
    static final String hostKey = "MV156FgOsnJxxy2xKY355wxb7ySlg0B9CpEUl/ONvmc=";
    static final String ssh_rsa = "MV156FgOsnJxxy2xKY355wxb7ySlg0B9CpEUl/ONvmc=";
    static final String command1 = "ls -ltr";

    final String hostBI = "ftp.bucurestiiloveyou.ro";
    final String userBI = "sftp@bucurestiiloveyou.ro";
    final String passwordBI = "Mugly11@";
    final int portBI = 21;

    final String hostETP_TEST =  "iop-transparency.entsoe.eu";
    final String hostIPETP_TEST = "62.209.222.11";
    final String hostETP = "transparency.entsoe.eu";
    final String hostIPETP = "62.209.222.10";
    final String userETP = "TEL_FTPS";
    final String passwordETP = "emfip.tel";
    final int portETP = 990;
    //final int port = 21;

    static final int SESSION_TIMEOUT = 5000;
    static final int CHANNEL_TIMEOUT = 5000;
    static final int DELAY_SHOW_BTN_1 = 1000;
    static final int DELAY_SHOW_BTN_2 = 3000;

    File path, storageDir, storageDirForCI, storageDirForSabloane;

    String NameOfFile, ExtOfFile;
    List<String> XMLFilesCollection = new ArrayList<>();
    String inArea = "", outArea = "", createdDateTime, startDeliveryPeriod, endDeliveryPeriod, revision, createdDateTimeOfCI;
    String pos, qty, price;
    Map<String, String> RO_BG_FLOW = new HashMap<>();
    Map<String, String> BG_RO_FLOW = new HashMap<>();
    Map<String, String> RO_PRICE = new HashMap<>();
    Map<String, String> BG_PRICE = new HashMap<>();
    Map<String, String> CI = new HashMap<>();

    String ver_ci, transactionDate, transactionDate_v1, deliveryDate, today, host;
    Vector ListOfFiles;

    Calendar transactionDayC, deliveryDayC, toDayC;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_v;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_v1;

    View v;
    TextView tv_ci, tv_data, tv_nameShip, tv_valueShip, tv_cilabel, tv_ci_ver;
    TextView[][] cell = new TextView[5][3];
    Button btn_send_ci, btn_req;
    Switch sw_send_ci, sw_host;


    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //determina identificarea obiectelor de tip view
        //=======================================================================================
        tv_data = findViewById(R.id.tv_data);
        tv_cilabel = findViewById(R.id.id_tv71);
        tv_ci = findViewById(R.id.id_tv72);
        tv_nameShip = findViewById(R.id.id_tv61);
        tv_valueShip = findViewById(R.id.id_tv62);
        tv_ci_ver = findViewById(R.id.tv_ci_ver);
        btn_send_ci = findViewById(R.id.btn_send_ci);
        btn_req = findViewById(R.id.btn_req);
        sw_send_ci = findViewById(R.id.sw_send);
        sw_host = findViewById(R.id.sw_host);
        btn_send_ci.setEnabled(false);
        v = findViewById(android.R.id.content);
        if (sw_send_ci.isChecked()){sw_send_ci.setChecked(false);}
        host = hostETP_TEST;

        String identStr;
        int id;

        //formateaza celulele tabelului
        //======================================================================================
        for (int r=0; r<5; r++){
            for (int c=0; c<3; c++){
                identStr = "id_tv"+ Integer.toString(r+1) + Integer.toString(c+1);
                id = getResources().getIdentifier(identStr, "id", this.getPackageName());
                cell[r][c] = findViewById(id);
                cell[r][c].setTextSize(FontSize);
                cell[r][c].setPadding(5,TopBottomPad,5,TopBottomPad);
            }
        }

        //formateaza obiectele de tip view
        //======================================================================================
        tv_nameShip.setTextSize(FontSize);
        tv_nameShip.setPadding(5,TopBottomPad,5,TopBottomPad);
        tv_valueShip.setTextSize(FontSize);
        tv_valueShip.setPadding(5,TopBottomPad,5,TopBottomPad);
        tv_ci.setTextSize(FontSize);
        tv_ci.setPadding(5,TopBottomPad,5,TopBottomPad);
        tv_cilabel.setTextSize(FontSize);
        tv_cilabel.setPadding(5,TopBottomPad,5,TopBottomPad);
        tv_data.setTextSize(16);
        tv_ci_ver.setTextSize(16);
        ver_ci = "1";

        storageDir = new File(getExternalFilesDir(folder_xml_from_ftp).toString());
        storageDirForCI = new File(getExternalFilesDir(folder_store_ci).toString());
        storageDirForSabloane = new File(getExternalFilesDir(folder_store_sabloane).toString());
        if (!storageDir.exists()) { storageDir.mkdirs(); }
        if (!storageDirForCI.exists()) { storageDirForCI.mkdirs(); }

        transactionDayC = Calendar.getInstance();
        deliveryDayC= Calendar.getInstance();
        deliveryDayC.add(Calendar.DATE, 1);
        toDayC = Calendar.getInstance();

        sdf = new SimpleDateFormat("yyyyMMdd");
        sdf_v = new SimpleDateFormat("dd.MM.yyyy");
        sdf_v1 = new SimpleDateFormat("yyyy-MM-dd");

        transactionDate = sdf.format(transactionDayC.getTime());
        deliveryDate = sdf_v.format(deliveryDayC.getTime());
        transactionDate_v1 = sdf_v.format(transactionDayC.getTime());

        tv_data.setText("Data livrare: " + deliveryDate);
        tv_ci_ver.setText("Versiune XML CI: " + ver_ci);

        findViewById(R.id.btn_decrease).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                transactionDayC.add(Calendar.DATE, -1);
                deliveryDayC.add(Calendar.DATE, -1);
                transactionDate = sdf.format(transactionDayC.getTime());
                deliveryDate = sdf_v.format(deliveryDayC.getTime());
                transactionDate_v1 = sdf_v.format(transactionDayC.getTime());
                tv_data.setText("Data livrare: " + deliveryDate);

            }
        });


        findViewById(R.id.btn_increase).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                transactionDayC.add(Calendar.DATE, 1);
                deliveryDayC.add(Calendar.DATE, 1);
                transactionDate = sdf.format(transactionDayC.getTime());
                deliveryDate = sdf_v.format(deliveryDayC.getTime());
                transactionDate_v1 = sdf_v.format(transactionDayC.getTime());
                tv_data.setText("Data livrare: " + deliveryDate);

            }
        });

        findViewById(R.id.btn_req).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected(getApplicationContext())){
                    //Snackbar.make(v, "Internet indisponibil !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Toast.makeText(getApplicationContext(), "Internet indisponibil !", Toast.LENGTH_SHORT).show();
                    return;
                }

                tv_ci.setText("on progress");
                tv_valueShip.setText("on progress");

                for (int r=1; r<5; r++){
                    for (int c=1; c<3; c++){
                        cell[r][c].setText("on progress");
                    }
                }

                btn_req.setEnabled(false);
                if(!ParseCollectionOfFiles()) {
                    new sFTPConnection().execute(storageDir.toString(), null, null);
                }
            }
        });

        findViewById(R.id.btn_decrease_ver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int version;
                version = Integer.parseInt(ver_ci);
                if (version != 1){version --;}else{
                    Toast.makeText(getApplicationContext(), "Versiunea nu poate fi mai mica de 1 !", Toast.LENGTH_SHORT).show();
                }

                ver_ci = String.valueOf(version);
                tv_ci_ver.setText("Versiune XML CI: " + ver_ci);

            }
        });

        findViewById(R.id.btn_increase_ver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int version;
                version = Integer.parseInt(ver_ci);
                version ++;

                ver_ci = String.valueOf(version);
                tv_ci_ver.setText("Versiune XML CI: " + ver_ci);

            }
        });

        findViewById(R.id.btn_create_ci).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create_ci_file();
            }
        });


        findViewById(R.id.sw_host).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sw_host.isChecked()){sw_host.setText("TEST Serv. "); host = hostETP_TEST;}
                else{sw_host.setText("PROD Serv. ");host = hostETP;}
            }
        });


        findViewById(R.id.sw_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected(getApplicationContext())){
                    //Snackbar.make(v, "Internet indisponibil !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    Toast.makeText(getApplicationContext(), "Internet indisponibil !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!sw_send_ci.isChecked()){btn_send_ci.setEnabled(false); sw_send_ci.setText("Locked SEND ");}
                else{btn_send_ci.setEnabled(true);sw_send_ci.setText("Unlocked SEND ");}
            }
        });

        findViewById(R.id.btn_send_ci).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("LOG_TAG","Version: " + host);
                new SendCI().execute(host, null, null);
            }
        });

    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) { return false;
        } else { return true; }
    }

    public void verify_connection_to_internet(){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())){
            Toast.makeText(getApplicationContext(), "Internet indisponibil First !", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(), "Internet indisponibil !", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @SuppressLint("SetTextI18n")
    public boolean ParseCollectionOfFiles(){
        //Log.d("LOG_TAG", "getExternalStorageDirectory: " + getActivity().getExternalFilesDir(folder_xml_from_ftp) );
        path = getExternalFilesDir(folder_xml_from_ftp);
        String pathString = path.toString();
        File directory = new File(pathString);
        File[] files = directory.listFiles();
        boolean bFlowsOK, bPricesOK;

        bFlowsOK = false;
        bPricesOK = false;

        XMLFilesCollection.clear();

        for (File f : files) {
            NameOfFile = f.getName();
            ExtOfFile = NameOfFile.substring(NameOfFile.lastIndexOf(".") + 1);
            if (ExtOfFile.compareTo("xml") == 0 && NameOfFile.contains(transactionDate)) {
                XMLFilesCollection.add(f.getName());
            }
        }

        RO_BG_FLOW.clear();
        BG_RO_FLOW.clear();
        RO_PRICE.clear();
        BG_PRICE.clear();
        CI.clear();

        for (int i = 0; i < XMLFilesCollection.size(); i++) {
            if (XMLFilesCollection.get(i).contains("Flows")){
                bFlowsOK = true;
                try {
                    File f = new File(path, XMLFilesCollection.get(i));
                    FileInputStream is = new FileInputStream(f);

                    XmlPullParserHandler prs = new XmlPullParserHandler();
                    prs.parse(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (XMLFilesCollection.get(i).contains("Prices")){
                bPricesOK = true;
                try {
                    File f = new File(path, XMLFilesCollection.get(i));
                    FileInputStream is = new FileInputStream(f);

                    XmlPullParserHandler prs = new XmlPullParserHandler();
                    prs.parse(is);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!((bFlowsOK) && (bPricesOK))){
            Toast.makeText(getApplicationContext(), "Nu exista fisiere\ndisponibile local !\nSe acceseaza folderele sFTP!", Toast.LENGTH_LONG).show();
            return false;
        }

        double dROPrice, dBGPrice, dMarketSpread;
        double dROBGFlow, dBGROFlow;
        double dCI, dci_pos;
        double dROBGFlowP, dROBGFlowN, dBGROFlowP, dBGROFlowN, dROBGValP, dROBGValN, dBGROValP, dBGROValN;
        double qtyShipper, ValShipper;

        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat df_ci = new DecimalFormat("#.##");
        df_ci.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        if (((RO_BG_FLOW.size() == 24) && (BG_RO_FLOW.size() == 24) && (RO_PRICE.size() == 24) && (BG_PRICE.size() == 24)) ||
                ((RO_BG_FLOW.size() == 23) && (BG_RO_FLOW.size() == 23) && (RO_PRICE.size() == 23) && (BG_PRICE.size() == 23)) ||
                ((RO_BG_FLOW.size() == 25) && (BG_RO_FLOW.size() == 25) && (RO_PRICE.size() == 25) && (BG_PRICE.size() == 25))) {
            //Log.d("LOG_TAG","BG_RO_FLOW: " + BG_RO_FLOW.size());

            dCI = 0.0f; qtyShipper = 0.0f; ValShipper = 0.0f;
            dROBGFlowP = 0.0f; dROBGFlowN = 0.0f;
            dBGROFlowP = 0.0f; dBGROFlowN = 0.0f;
            dROBGValP = 0.0f; dROBGValN = 0.0f;
            dBGROValP = 0.0f; dBGROValN = 0.0f;

            for(int i=0;i<RO_BG_FLOW.size();i++){

                dROBGFlow = Double.parseDouble(Objects.requireNonNull(RO_BG_FLOW.get(String.valueOf(i + 1))));
                dBGROFlow = Double.parseDouble(Objects.requireNonNull(BG_RO_FLOW.get(String.valueOf(i + 1))));

                dROPrice = Double.parseDouble(Objects.requireNonNull(RO_PRICE.get(String.valueOf(i + 1))));
                dBGPrice = Double.parseDouble(Objects.requireNonNull(BG_PRICE.get(String.valueOf(i + 1))));

                dMarketSpread = Math.round((dROPrice-dBGPrice) * 100);
                dMarketSpread = dMarketSpread /100;

                //Log.d("LOG_TAG","MarketSpread: " + i + ":  " + String.valueOf(dMarketSpread));
                //String.format("%.2f", MarketSpread)

                if (dMarketSpread >= 0.0f){
                    dCI = dCI + dMarketSpread * dBGROFlow;
                    dci_pos = dMarketSpread * dBGROFlow;
                    CI.put(String.valueOf(i + 1), df_ci.format(dci_pos));
                }else{
                    dCI = dCI - dMarketSpread * dROBGFlow;
                    dci_pos = - dMarketSpread * dBGROFlow;
                    CI.put(String.valueOf(i + 1), df_ci.format(dci_pos));
                }

                if (dROPrice >= 0.0f){dROBGFlowP = dROBGFlowP + dROBGFlow; dROBGValP = dROBGValP + dROBGFlow * dBGPrice; }
                if (dROPrice < 0.0f){dROBGFlowN = dROBGFlowN + dROBGFlow; dROBGValN = dROBGValN + dROBGFlow * dBGPrice; }

                if (dBGPrice >= 0.0f){dBGROFlowP = dBGROFlowP + dBGROFlow; dBGROValP = dBGROValP + dBGROFlow * dBGPrice; }
                if (dBGPrice < 0.0f){dBGROFlowN = dBGROFlowN + dBGROFlow; dBGROValN = dBGROValN - dBGROFlow * dBGPrice; }
            }

            qtyShipper = dROBGFlowP - dBGROFlowP - dROBGFlowN + dBGROFlowN;
            ValShipper = dROBGValP - dROBGValN - dBGROValP + dBGROValN;

            //TextView tv_ci = (TextView) requireView().findViewById(R.id.tv_ci);
            //TextView tv_shipper = (TextView) requireView().findViewById(R.id.tv_shipper);
            tv_ci.setText(df.format(dCI));

            cell[1][1].setText(df.format(dBGROFlowP));
            cell[2][1].setText(df.format(dROBGFlowP));
            cell[3][1].setText(df.format(dBGROFlowN));
            cell[4][1].setText(df.format(dROBGFlowN));

            cell[1][2].setText(df.format(dBGROValP));
            cell[2][2].setText(df.format(dROBGValP));
            cell[3][2].setText(df.format(dBGROValN));
            cell[4][2].setText(df.format(dROBGValN));

            if (ValShipper >= 0.0f){tv_nameShip.setText("IBEX Shipper");}
            else{ValShipper =  (-1) * ValShipper; tv_nameShip.setText("TEL Shipper");}
            tv_valueShip.setText(df.format(ValShipper));


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    btn_req.setEnabled(true);
                }
            }, DELAY_SHOW_BTN_1);


            return  true;
            
        }else{
            Toast.makeText(getApplicationContext(), "Eroare: Variabilele FLOW si PRICE nu au aceeasi dimensiune !\nVa rugam sa reluati !", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    public class XmlPullParserHandler {

        String text;
        //StringBuilder builder = new StringBuilder();
        boolean bDeliveryInterval, bSchedule_MarketDocument, bPublication_MarketDocument;

        public void parse(InputStream is) {

            try {

                bDeliveryInterval = false;
                bSchedule_MarketDocument= false;
                bPublication_MarketDocument = false;

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();

                parser.setInput(is, null);

                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = parser.getName();
                    switch (eventType) {

                        case XmlPullParser.START_DOCUMENT:
                            //Log.d("LOG_TAG","Start document: " + tagname);
                            break;

                        case XmlPullParser.START_TAG:
                            //text = parser.getName();
                            switch (tagname) {
                                case "schedule_Time_Period.timeInterval":
                                    bDeliveryInterval = true;
                                    break;
                                case "Schedule_MarketDocument":
                                    bSchedule_MarketDocument = true;
                                    break;
                                case "Publication_MarketDocument":
                                    bPublication_MarketDocument = true;
                                    break;
                            }

/*
                            if (tagname.equals("schedule_Time_Period.timeInterval")){bDeliveryInterval = true;}
                            else if (tagname.equals("Schedule_MarketDocument")){bSchedule_MarketDocument = true;}
                            else if (tagname.equals("Publication_MarketDocument")){bPublication_MarketDocument = true;}

 */
                            //Log.d("LOG_TAG","Start tag: " + tagname);
                            break;

                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            //Log.d("LOG_TAG","Element: " + text);
                            break;

                        case XmlPullParser.END_TAG:

                            if (bSchedule_MarketDocument){
                                if (tagname.equalsIgnoreCase("revisionNumber")) {
                                    revision = text;
                                } else if (tagname.equalsIgnoreCase("schedule_Time_Period.timeInterval")) {
                                    bDeliveryInterval = false;
                                }else if (tagname.equalsIgnoreCase("createdDateTime")){
                                    createdDateTime = text;
                                }else if (tagname.equalsIgnoreCase("start")){
                                    if (bDeliveryInterval) {startDeliveryPeriod = text; }
                                }else if (tagname.equalsIgnoreCase("end")){
                                    if (bDeliveryInterval) {endDeliveryPeriod = text; }
                                }else if (tagname.equalsIgnoreCase("in_Domain.mRID")){
                                    inArea = text;
                                }else if (tagname.equalsIgnoreCase("out_Domain.mRID")) {
                                    outArea = text;
                                }else if (tagname.equalsIgnoreCase("position")) {
                                    pos = text;
                                }else if (tagname.equalsIgnoreCase("quantity")) {
                                    qty = text;

                                    if (inArea.equals(EIC_RO_AREA) && outArea.equals(EIC_BG_AREA)){
                                        BG_RO_FLOW.put(pos, qty);
                                        //Log.d("LOG_TAG","inArea: " + inArea);
                                    }else if (outArea.equals(EIC_RO_AREA) && inArea.equals(EIC_BG_AREA)){
                                        RO_BG_FLOW.put(pos, qty);
                                        //Log.d("LOG_TAG","outArea: " + outArea);
                                    }
                                }
                            }else if(bPublication_MarketDocument) {
                                if (tagname.equalsIgnoreCase("in_Domain.mRID")) {
                                    inArea = text;
                                } else if (tagname.equalsIgnoreCase("out_Domain.mRID")) {
                                    outArea = text;
                                } else if (tagname.equalsIgnoreCase("position")) {
                                    pos = text;
                                } else if (tagname.equalsIgnoreCase("price.amount")) {
                                    price = text;

                                    if (inArea.equals(EIC_RO_AREA) && outArea.equals(EIC_RO_AREA)) {
                                        RO_PRICE.put(pos, price);
                                        //Log.d("LOG_TAG","inArea: " + inArea);
                                    } else if (outArea.equals(EIC_BG_AREA) && inArea.equals(EIC_BG_AREA)) {
                                        BG_PRICE.put(pos, price);
                                        //Log.d("LOG_TAG","outArea: " + outArea);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
                //Snackbar.make(v, "Parsarea fisierului XML a esuat !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Toast.makeText(getApplicationContext(), "Parsarea fisierului XML a esuat !", Toast.LENGTH_SHORT).show();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                //Snackbar.make(v, "Parsarea fisierului XML a esuat !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Toast.makeText(getApplicationContext(), "Parsarea fisierului XML a esuat !", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class sFTPConnection extends AsyncTask<String, Void, String> {

        Channel channel     = null;
        ChannelSftp channelSftp = null;
        Session session = null;
        String NameOfFile;
        Boolean bTag = false, bTag1 = false;

        @Override
        protected String doInBackground(String... params) {
            try {

                JSch jsch = new JSch();
                //jsch.getHostKeyRepository ().add ( hostKey, null );
                //jsch.getHostKeyRepository ().add ( hk, null );
                //InputStream is = new ByteArrayInputStream(ssh_rsa.getBytes());
                //jsch.setKnownHosts(is);
                //jsch.addIdentity("id_rsa");
                session = jsch.getSession(userESO, hostIPESO, portESO);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(passwordESO);

                //bg//"ssh-rsa 2048 b2:f3:87:46:83:5d:19:0b:84:5a:d4:9e:28:0d:56:82 MV156FgOsnJxxy2xKY355wxb7ySlg0B9CpEUl/ONvmc="
                session.connect(SESSION_TIMEOUT);
                //boolean conStatus = session.isConnected();
                //Log.d("LOG_TAG","sFTPConnection: " + conStatus);

                channel = session.openChannel("sftp");
                //InputStream in = channel.getInputStream();
                //((ChannelExec) channel).setErrStream(System.err);
                channel.connect(CHANNEL_TIMEOUT);
                channelSftp = (ChannelSftp) channel;

                channelSftp.cd(FlowRemotePath);
                ListOfFiles = channelSftp.ls(FlowRemotePath);

                for(int i=0; i<ListOfFiles.size();i++){
                    NameOfFile = ListOfFiles.get(i).toString().substring(ListOfFiles.get(i).toString().lastIndexOf(" ") + 1);
                    if (NameOfFile.contains(transactionDate)){ channelSftp.get(NameOfFile, params[0]);bTag = true;}
                }
                channelSftp.cd(PriceRemotePath);
                ListOfFiles = channelSftp.ls(PriceRemotePath);

                for(int i=0; i<ListOfFiles.size();i++){
                    NameOfFile = ListOfFiles.get(i).toString().substring(ListOfFiles.get(i).toString().lastIndexOf(" ") + 1);
                    //Log.d("LOG_TAG", "Fisiere: ! " + NameOfFile);
                    if (NameOfFile.contains(transactionDate)){ channelSftp.get(NameOfFile, params[0]);bTag1 = true;}
                }

            }catch (Exception e) {
                //Log.d("LOG_TAG","sFTPConnection Exceptie: " + e.getMessage());
                e.printStackTrace();
                Snackbar.make(v, "S-a produs o eroare la citirea serverului sFTP !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }finally {
                channel.disconnect();
                session.disconnect();
            }

            if (bTag && bTag1){ return "Executat";}
            else{return "NoFiles";}
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.d("LOG_TAG", "activitatea de downloading s-a finalizat: ! ");
            if (result.equals("Executat")) {ParseCollectionOfFiles();}
            else{
                for (int r=1; r<5; r++){
                    for (int c=1; c<3; c++){
                        cell[r][c].setText("no values");
                    }
                }
                tv_ci.setText("no values");
                tv_valueShip.setText("no values");
                Toast.makeText(getApplicationContext(), "Folderele sFTP nu contin fisierele pentru data solicitata !", Toast.LENGTH_SHORT).show();


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        btn_req.setEnabled(true);
                    }
                }, DELAY_SHOW_BTN_2);
                //Snackbar.make(v, "Folderele sFTP nu au fisierele solicitate !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Log.d("LOG_TAG", "Progress: ! " + values[0]);
        }
    }


    public void create_ci_file() {

        String text = "";
        String new_file;
        String UTCTime;
        boolean bDeliveryInterval, bSchedule_MarketDocument, bPublication_MarketDocument;
        File f;

        if ((CI.size() == 23) || (CI.size() == 24) || (CI.size() == 25)){

            bDeliveryInterval = false;
            bSchedule_MarketDocument= false;
            bPublication_MarketDocument = false;

            try {

                path = getExternalFilesDir(folder_store_sabloane);
                switch (CI.size()) {
                    case 23:
                        f = new File(path, CI_sablon_23);
                        break;
                    case 24:
                        f = new File(path, CI_sablon_24);
                        break;
                    case 25:
                        f = new File(path, CI_sablon_25);
                        break;
                    default:
                        f = new File(path, CI_sablon_24);
                }

                //Log.d("LOG_TAG", "F: " + f.toString());
                //new_file = CI_sablon_24.substring(0, CI_sablon_24.length() - 8);
                //new_file = date + "_" + new_file + ver_ci + ".xml";
                new_file = mRID + deliveryDate + "_v" + ver_ci + ".xml";

                DateFormat df = DateFormat.getTimeInstance();
                //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddThh:mm:ssZ");
                df.setTimeZone(TimeZone.getTimeZone("gmt"));
                String gmtTime = df.format(new Date());
                today = sdf_v1.format(toDayC.getTime());
                UTCTime = today + "T" + gmtTime + "Z";
                String pos = "1";
                String mRIDString = mRID + deliveryDate;

                Log.d("LOG_TAG", "UTCTime: " + UTCTime);

                FileInputStream is = new FileInputStream(f);
                SAXBuilder sax = new SAXBuilder();
                sax.setXMLFilter(null);
                Document doc = sax.build(is);
                Element rootNode = doc.getRootElement();

                List<Element> listChildrenNode = rootNode.getChildren();
                //Element tagRev = rootNode.getChild("revisionNumber");
                //if (tagRev != null){Log.d("LOG_TAG", "tagRev: " + tagRev.getValue());}
                //Log.d("LOG_TAG", "Fisierul nou este: " + listChildrenNode.size());
                //Log.d("LOG_TAG", "Fisierul nou este: " + UTCTime);

                //createdDateTimeOfCi = date_ci + "T" + gmtTime + "Z";
                // loop the elements
                for (Element staff : listChildrenNode) {
                    if (staff.getName().equals("mRID")) {
                        staff.setText(mRIDString);
                    }else if (staff.getName().equals("revisionNumber")) {
                        staff.setText(ver_ci);
                    } else if (staff.getName().equals("createdDateTime")) {
                        staff.setText(UTCTime);
                    } else if (staff.getName().equals("period.timeInterval")) {
                        List<Element> PeriodtimeInterval_Collection = staff.getChildren();
                        for (Element staff1 : PeriodtimeInterval_Collection) {
                            if (staff1.getName().equals("start")) {
                                staff1.setText(startDeliveryPeriod);
                            } else if (staff1.getName().equals("end")) {
                                staff1.setText(endDeliveryPeriod);
                            }
                        }

                        //Log.d("LOG_TAG", "Child: " + staff.getChild("start", Namespace.getNamespace("")).getText());
                        //if (listChildrenNodeTime(0).getName().equals("start")) { staff.setText(ver_ci);}
                        //if (staff.getName().equals("createdDateTime")) { staff.setText(createdDateTime);}
                    } else if (staff.getName().equals("TimeSeries")) {
                        //Log.d("LOG_TAG", "Fisierul nou este: Timeseries" );
                        List<Element> TimeSeries_Collection = staff.getChildren();
                        //Log.d("LOG_TAG", "TimeSeriesCollection");
                        for (Element staff2 : TimeSeries_Collection) {
                            if (staff2.getName().equals("Period")) {
                                //Log.d("LOG_TAG", "Period");
                                List<Element> Period_Collection = staff2.getChildren();
                                for (Element staff3 : Period_Collection) {
                                    if (staff3.getName().equals("timeInterval")) {
                                        //Log.d("LOG_TAG", "timeInterval");
                                        List<Element> timeInterval_Collection = staff3.getChildren();
                                        for (Element staff4 : timeInterval_Collection) {
                                            if (staff4.getName().equals("start")) {
                                                staff4.setText(startDeliveryPeriod);
                                                //Log.d("LOG_TAG", "poisition " + startDeliveryPeriod);
                                            } else if (staff4.getName().equals("end")) {
                                                staff4.setText(endDeliveryPeriod);
                                                //Log.d("LOG_TAG", "poisition " + endDeliveryPeriod);
                                            }
                                        }
                                    } else if (staff3.getName().equals("Point")) {
                                        //Log.d("LOG_TAG", "Point");
                                        List<Element> Point_Collection = staff3.getChildren();
                                        for (Element staff5 : Point_Collection) {
                                            if (staff5.getName().equals("position")) {
                                                pos = staff5.getText();
                                                //Log.d("LOG_TAG", "position " + pos);
                                            } else if (staff5.getName().equals("price.amount")) {
                                                //Log.d("LOG_TAG", "amanunt "+ CI.get(pos));
                                                staff5.setText(CI.get(pos));

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                //Log.d("LOG_TAG", "Nume: " + staff.getText());
                //if (staff.getName().equals("period.timeInterval")) {
                //Log.d("LOG_TAG", "Nume: " + staff.getChild());
                //}
                    /*
                    Log.d("LOG_TAG", "Nume: " + id.trim());

                    // if staff id is 1001
                    if ("createdDateTime".equals(id.trim())) {

                        // remove element role
                        //staff.removeChild("role");

                        Log.d("LOG_TAG", "Nume: " + staff.getText());
                        // update xml element `salary`, update attribute to MYR
                        staff.getChild("salary").setAttribute("currency", "MYR");

                    }

                    // if staff id is 1002
                    if ("1002".equals(id.trim())) {

                        // remove xml element `name`
                        staff.removeChild("name");

                        // add a new xml element `address` and CDATA content
                        staff.addContent(new Element("address")
                                .addContent(new CDATA("123 & abc")));

                        // update xml element `salary` to 2000
                        staff.getChild("salary").setText("2000");

                        // remove the xml element CDATA
                        staff.getChild("bio").setText("a & b & c"); // now the & will escape automatically
                    }

                    // Java 8 to remove all XML comments
                    staff.getContent().removeIf(
                            content -> content.getCType() == Content.CType.Comment);
                    */

                // remove the XML comments via iterator
            /*ListIterator<Content> iter = staff.getContent().listIterator();
            while (iter.hasNext()) {
                Content content = iter.next();
                if (content.getCType() == Content.CType.Comment) {
                    iter.remove();
                }
            }*/
                // add a new XML child node, staff id 1003
                /*
                rootNode.addContent(new Element("staff").setAttribute("id", "1003"));
                // print to console for testing

            */
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                path = getExternalFilesDir(folder_store_ci);
                String file = getExternalFilesDir(folder_store_ci).toString() + "/" +new_file;
                FileOutputStream output = new FileOutputStream(file);
                xmlOutput.output(doc, output);
                Toast.makeText(getApplicationContext(), "Fisierul " + new_file + " a fost creat !", Toast.LENGTH_SHORT).show();

            }catch(Exception e){
                Toast.makeText(getApplicationContext(), "Va rugam sa apasati mai intai butonul Request !", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Va rugam sa apasati mai intai butonul Request !", Toast.LENGTH_SHORT).show();
        }

    }


    class SendCI extends AsyncTask<String, Void, String> {

        Channel channel     = null;
        ChannelSftp channelSftp = null;
        String NameOfFile;

        @Override
        protected String doInBackground(String... params) {
            try {

                path = getExternalFilesDir(folder_store_ci);
                String pathString = path.toString();
                File directory = new File(pathString);
                File[] files = directory.listFiles();
                String version;

                NameOfFile = "";
                for (File f : files) {
                    NameOfFile = f.getName();
                    if (NameOfFile.contains(deliveryDate)){
                        version = NameOfFile.substring(29);
                        version = version.substring(0,version.lastIndexOf("."));
                        if (version.equals(ver_ci)){break;}
                    }
                }
                //Log.d("LOG_TAG","Version: " + path + "/" + NameOfFile);

                if (!NameOfFile.equals("")) {

                    FTPSClient client = new FTPSClient(true);
                    client.setDefaultTimeout(SESSION_TIMEOUT);
                    FileInputStream fis = null;
                    client.connect(params[0], portETP);
                    client.login(userETP, passwordETP);

                    //Log.d("LOG_TAG","Version: " + client.isConnected());

                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    client.enterLocalPassiveMode();

                    fis = new FileInputStream(path + "/" + NameOfFile);
                    client.storeFile(NameOfFile, fis);
                    client.logout();
                }

            }catch (Exception e) {
                //Log.d("LOG_TAG","sFTPConnection Exceptie: " + e.getMessage());
                e.printStackTrace();
                Snackbar.make(v, "S-a produs o eroare la conectarea serverului FTP ETP!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            //Log.d("LOG_TAG", "activitatea de downloading s-a finalizat: ! ");
            //ParseCollectionOfFiles();
            sw_send_ci.setChecked(false);
            sw_send_ci.setText("Locked SEND ");
            btn_send_ci.setEnabled(false);

            //Snackbar.make(v, "Actiunea de incarcare a fisierului pe serverul FTP ETP a reusit !", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            Toast.makeText(getApplicationContext(), "Actiunea de incarcare a fisierului pe serverul FTP ETP a reusit !", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Log.d("LOG_TAG", "Progress: ! " + values[0]);
        }
    }

}
package repsoldemo.ibm.com.demorepsol;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    JSONObject object;
    Button incendio,viento,stock,salir,contaminacion;
    double longitude ,latitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10, MIN_TIME_BW_UPDATES = 60000;
    Location location;
    int dataSource;
    String fecha,imei,imagenCod;
    Bitmap imagen;
    byte[] b;
    ByteArrayOutputStream baos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);


        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGPSEnabled && !isNetworkEnabled){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.gps_advice_tittle));
            dialog.setMessage(getString(R.string.gps_advice_text));
            dialog.setNeutralButton(getString(R.string.but_accept), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            dialog.create();
            dialog.show();
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.gps_advice_tittle));
        dialog.setMessage(getString(R.string.vpn_advice_text));
        dialog.setNeutralButton(getString(R.string.but_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.create();
        dialog.show();
        getGeoPosition();
        inicializacion();
    }

    private void inicializacion(){
        incendio = (Button)findViewById(R.id.incendio);
        incendio.setOnClickListener(this);
        viento = (Button)findViewById(R.id.viento);
        viento.setOnClickListener(this);
        stock = (Button)findViewById(R.id.stock);
        stock.setOnClickListener(this);
        salir = (Button)findViewById(R.id.exit);
        salir.setOnClickListener(this);
        contaminacion = (Button)findViewById(R.id.contaminacion);
        contaminacion.setOnClickListener(this);
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei=tel.getDeviceId();
    }

    @Override
    public void onClick(View v){
        int id= v.getId();
        object = new JSONObject();
        prepareTime();
        switch (id){
            case R.id.viento:
                dataSource = 53;
                imagen = BitmapFactory.decodeResource(getResources(),R.mipmap.viento);
                baos = new ByteArrayOutputStream();
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                b = baos.toByteArray();
                imagenCod = Base64.encodeToString(b, Base64.DEFAULT);
                mandarInfoViento();
                break;
            case R.id.contaminacion:
                dataSource = 54;
                imagen = BitmapFactory.decodeResource(getResources(),R.mipmap.contaminacion);
                baos = new ByteArrayOutputStream();
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                b = baos.toByteArray();
                imagenCod = Base64.encodeToString(b, Base64.DEFAULT);
                mandarInfoContaminacion();
                break;
            case R.id.incendio:
                dataSource = 55;
                imagen = BitmapFactory.decodeResource(getResources(),R.mipmap.incendio);
                baos = new ByteArrayOutputStream();
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                b = baos.toByteArray();
                imagenCod = Base64.encodeToString(b, Base64.DEFAULT);
                mandarInfoIncendio();
                break;
            case R.id.stock:
                dataSource = 56;
                imagen = BitmapFactory.decodeResource(getResources(),R.mipmap.stock);
                baos = new ByteArrayOutputStream();
                imagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                b = baos.toByteArray();
                imagenCod = Base64.encodeToString(b, Base64.DEFAULT);
                mandarInfoStock();
                break;
            case R.id.exit:
                finish();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private void prepareTime(){
        GregorianCalendar cal = new GregorianCalendar();
        java.text.SimpleDateFormat sdfMadrid = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        fecha= sdfMadrid.format(cal.getTime());
        fecha=fecha.replace(":",".").replace("+","").replace("T","-");
        fecha=fecha.substring(0,fecha.length()-1);
    }

    private void mandarInfoIncendio() {

        try {
            object.accumulate("NAME", "FUEGO");
            object.accumulate("IDALERTA", getCadenaanumAleatoria(6));
            object.accumulate("IMAGEN", imagenCod);
            object.accumulate("IMEI", imei);
            object.accumulate("LOCATION", "POINT(" + longitude + " " + latitude + ")");
            object.accumulate("TIPODEALERTA", "HUMO");
            object.accumulate("VELOCIDAD","30");
            object.accumulate("STARTDATETIME", fecha);
            object.accumulate("ENDDATETIME", fecha);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        enviar();
    }
    private void mandarInfoStock() {

        try {
            object.accumulate("NAME", "ROTURA DE STOCK");
            object.accumulate("IDALERTA", getCadenaanumAleatoria(6));
            object.accumulate("IMAGEN", imagenCod);
            object.accumulate("IMEI", imei);
            object.accumulate("LOCATION", "POINT(" + longitude + " " + latitude + ")");
            object.accumulate("TIPODEALERTA", "STOCK");
            object.accumulate("STOCKACTUAL","100");
            object.accumulate("TIPODEACTIVO","VALVULA");
            object.accumulate("STARTDATETIME", fecha);
            object.accumulate("ENDDATETIME", fecha);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        enviar();
    }

    private void mandarInfoViento() {

        try {
            object.accumulate("NAME", "RACHA DE VIENTO");
            object.accumulate("IDALERTAMET", getCadenaanumAleatoria(6));
            object.accumulate("IMAGEN", imagenCod);
            object.accumulate("IMEI", imei);
            object.accumulate("LOCATION", "POINT(" + longitude + " " + latitude + ")");
            object.accumulate("TIPODEALERTA", "VIENTO");
            object.accumulate("VELOCIDAD","30");
            object.accumulate("STARTDATETIME", fecha);
            object.accumulate("ENDDATETIME", fecha);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        enviar();
    }

    private void mandarInfoContaminacion() {

        try {
            object.accumulate("NAME", "ALTO VALOR DIOXIDO DE AZUFRE");
            object.accumulate("IDALERTACAI", getCadenaanumAleatoria(6));
            object.accumulate("IMAGEN", imagenCod);
            object.accumulate("IMEI", imei);
            object.accumulate("LOCATION", "POINT(" + longitude + " " + latitude + ")");
            object.accumulate("TIPODEALERTA", "SUPERACION SO2");
            object.accumulate("VALOR","100");
            object.accumulate("STARTDATETIME", fecha);
            object.accumulate("ENDDATETIME", fecha);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        enviar();
    }



    private void enviar(){
        PostTask task = new PostTask();
        task.execute();
    }

    private String getCadenaanumAleatoria(int longitud) {

        String cadenaAleatoria = "";
        long milis = new java.util.GregorianCalendar().getTimeInMillis();
        Random r = new Random(milis);
        int i = 0;
        while (i < longitud) {
            char c = (char) r.nextInt(255);
            if ((c >= '0' && c <= '9')) {
                cadenaAleatoria += c;
                i++;
            }
        }
        return cadenaAleatoria;
    }

    private class PostTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... data) {
            String authorize = Base64.encodeToString("sysadmin:z1ocpssc".getBytes(), Base64.DEFAULT).replace("\n", "");
            //String authorize = Base64.encodeToString("wpsadmin:z1ocpssc".getBytes(), Base64.DEFAULT).replace("\n", "");
            //String urlString = "https://iocdevsm.dcry.iccmop/ibm/ioc/api/data-injection-service/datablocks/39/dataitems";
            String urlString = "https://iocsms.dcry.iccmop/ibm/ioc/api/data-injection-service/datablocks/"+dataSource+"/dataitems";//viento
            //String urlString = "https://iocdevwe.dcry.iccmop/ibm/ioc/api/data-injection-service/datablocks/470/dataitems";
            HttpsURLConnection httpURLConnection=null;
            String msg="OK";
            try {


                URL url = new URL(urlString);
                trustAllHosts();
                httpURLConnection = (HttpsURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setHostnameVerifier(DO_NOT_VERIFY);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("IBM-Session-ID", "1");
                httpURLConnection.setRequestProperty("JSESSIONID", "1");
                //httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=1");
                //httpURLConnection.setRequestProperty("com.ibm.ioc.sessionid", "1");
                httpURLConnection.setFixedLengthStreamingMode(object.toString().length());

                httpURLConnection.setRequestProperty("Authorization", "Basic " + authorize);
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(object.toString());
                wr.flush();
                wr.close();
            }

            catch (IOException e4) {
                msg= e4.getMessage();
            }finally {

                if(httpURLConnection!=null){
                    httpURLConnection.disconnect();
                }
            }
            return msg;
        }
        public void onPostExecute(String result) {
         confirmDialog();
        }
    }
    private void confirmDialog(){
        AlertDialog.Builder dialogSend=new AlertDialog.Builder(this);
        dialogSend.setTitle(getString(R.string.info));
        dialogSend.setMessage(getString(R.string.info_text));
        dialogSend.setNeutralButton(getString(R.string.but_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialogSend.create();
        dialogSend.show();
    }
    private void getGeoPosition(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        MyLocationListener locationListener1 = new MyLocationListener();

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListener1 );

            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
            if (location == null) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener1);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }
    }
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {

            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType){
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

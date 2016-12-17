package br.com.fernandescruz.demowebservice;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.etUsuario)
    EditText etUsuario;

    @BindView(R.id.etPassword)
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnLogin)
    public void logar(){
        if(isConnected()){
            new LoginTask().execute("http://172.16.70.146:3000/login");

        }else{
            Toast.makeText(this,"Sem conexão!!",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isConnected() {
        ConnectivityManager connManager = (ConnectivityManager)
                getSystemService(Activity.CONNECTIVITY_SERVICE);

        NetworkInfo info = connManager.getActiveNetworkInfo();

        if (info != null && info.isConnected()){
            return true;
        }else{
            return false;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        String nomeUsuario;
        String senha;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nomeUsuario = etUsuario.getText().toString();
            senha = etPassword.getText().toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";

            try{
                URL url = new URL(params[0]);

                JSONObject usuario = new JSONObject();
                usuario.put("usuario", nomeUsuario);

                HttpURLConnection conn = (HttpURLConnection)
                        url.openConnection();

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(usuario));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }

                /*usuario.put("senha", senha);*/

            }catch (MalformedURLException e){
                result = e.getMessage();
            }catch (JSONException je){
                result = je.getMessage();
            }catch (IOException ioe){
                result = ioe.getMessage();
            } catch (Exception e) {
                result = e.getMessage();
            }
            return result;
        }

        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }
    }
}

package me.huljak.mcserverstatus;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.UnknownHostException;

import re.alwyn974.minecraftserverping.MinecraftServerPing;
import re.alwyn974.minecraftserverping.MinecraftServerPingInfos;
import re.alwyn974.minecraftserverping.MinecraftServerPingOptions;

public class ServerStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Launch", "Application has been launched.");

        EditText ipAddressEditText = findViewById(R.id.ipAddressEditText);
        Button showStatusButton = findViewById(R.id.showStatusButton);
        ImageView faviconImageView = findViewById(R.id.faviconImageView);
        TextView serverDescriptionTextView = findViewById(R.id.serverDescriptionTextView);
        TextView serverPlayersTextView = findViewById(R.id.serverPlayersTextView);
        TextView serverVersionTextView = findViewById(R.id.serverVersionTextView);
        TextView serverLatencyTextView = findViewById(R.id.serverLatencyTextView);


        faviconImageView.setImageBitmap(null);
        serverDescriptionTextView.setText(null);
        serverLatencyTextView.setText(null);
        serverPlayersTextView.setText(null);
        serverLatencyTextView.setText(null);
        serverVersionTextView.setText(null);


        showStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showServerInformation(serverDescriptionTextView, serverPlayersTextView, serverVersionTextView, serverLatencyTextView, faviconImageView, ipAddressEditText);

            }
        });
    }

    public void makeToast(Context context, String message) {
        Thread toastThread = new Thread() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        toastThread.start();
    }

    public void showServerInformation(TextView serverDescriptionTextView, TextView serverPlayersTextView, TextView serverVersionTextView, TextView serverLatencyTextView, ImageView faviconImageView, EditText ipAddressEditText) {
        Thread mainThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (ipAddressEditText.getText().toString().matches("")) {
                    makeToast(ServerStatusActivity.this, "You must enter an server address.");
                } else {
                    try {
                        String port = ipAddressEditText.getText().toString().substring(ipAddressEditText.getText().toString().lastIndexOf(":") + 1);
                        String addressWithoutPort = ipAddressEditText.getText().toString().replaceFirst(":\\d+", "");

                        MinecraftServerPingInfos serverData = new MinecraftServerPing().getPing(new MinecraftServerPingOptions().setHostname(ipAddressEditText.getText().toString().contains(":") ? addressWithoutPort : ipAddressEditText.getText().toString()).setPort(ipAddressEditText.getText().toString().contains(":") ? Integer.parseInt(port) : 25565));

                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                        runOnUiThread(new Runnable() {

                            public void run() {
                                serverDescriptionTextView.setText(serverData.getStrippedDescription());
                                serverPlayersTextView.setText(serverData.getPlayers().getOnline() + " / " + serverData.getPlayers().getMax());
                                serverVersionTextView.setText(serverData.getVersion().getName());
                                serverLatencyTextView.setText(serverData.getLatency() + "ms");
                                String encodedDataString = serverData.getFavicon();
                                encodedDataString = encodedDataString.replace("data:image/png;base64,", "");

                                byte[] imageAsBytes = Base64.decode(encodedDataString.getBytes(), 0);
                                faviconImageView.setImageBitmap(BitmapFactory.decodeByteArray(
                                        imageAsBytes, 0, imageAsBytes.length));
                                faviconImageView.getLayoutParams().width = 240;
                                faviconImageView.getLayoutParams().height = 240;
                            }
                        });
                        Log.i("ServerData", "Showing information about " + ipAddressEditText.getText().toString());
                    } catch (UnknownHostException e) {
                        makeToast(ServerStatusActivity.this, "You must enter a valid server address.");
                        Log.e("ServerData", "Unknown server: " + e);

                    } catch (Exception e) {
                        makeToast(ServerStatusActivity.this, "An error has occurred.");
                        Log.e("ServerData", "An error has occurred while this executing task: " + e);
                    }

                }
            }
        });


        mainThread.start();

    }


}
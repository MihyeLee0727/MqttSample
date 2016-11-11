package com.sample.mqtt;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.common.io.ByteStreams;
import com.sample.mqtt.databinding.ActivityMainBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String TOPIC = "a/b";
    private ActivityMainBinding binding;

    private MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = (ActivityMainBinding) DataBindingUtil.setContentView(this, R.layout.activity_main);

        create();

        binding.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        binding.publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish(binding.textPub.getText().toString());
            }
        });

        binding.subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribe();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            binding.text.setText(e.getMessage());
        }
    }

    private void create() {

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this, "tcp://192.168.88.98:1883", clientId);
        binding.text.setText("clientId : " + clientId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                binding.text.setText("connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                binding.text.setText("messageArrived : " + new String(message.getPayload(), 0, message.getPayload().length));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                binding.text.setText("deliveryComplete");
            }
        });
    }

    private void connect() {
        binding.text.setText("connecting......");

        try {
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
//            options.setConnectionTimeout(5);

            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    binding.text.setText("connect success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    binding.text.setText("connect fail");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            binding.text.setText(e.getMessage());
        }
    }

    private void subscribe() {
        if(!client.isConnected()) {
            binding.text.setText("not connected");
            return;
        }

        binding.text.setText("subscribe......");
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(TOPIC, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    binding.text.setText("subscribe success");
//                    String text = new String(asyncActionToken.getResponse().getPayload(), 0, asyncActionToken.getResponse().getPayload().length);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    binding.text.setText("subscribe success");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void publish(String text) {
        if(!client.isConnected()) {
            binding.text.setText("not connected");
            return;
        }

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = text.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(TOPIC, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
}

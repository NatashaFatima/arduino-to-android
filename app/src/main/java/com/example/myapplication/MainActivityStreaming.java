package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivityStreaming extends AppCompatActivity implements SerialInputOutputManager.Listener {

    private TextView textView2;
    private Button button;
    //   private boolean received;

    //serial communication
    private UsbManager manager;
    private List<UsbSerialDriver> availableDrivers;
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    private int numBytesRead;
    private String receivedMessage = "";
    SerialInputOutputManager usbIoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            port.close();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        textView2 = findViewById(R.id.textView2);
        button = findViewById(R.id.button);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }
        driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            //  manager.requestPermission();
            return;
        }
        port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            usbIoManager = new SerialInputOutputManager(port, this);
            usbIoManager.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewData(byte[] data) {
        System.out.println("IN NEW DATA");
        runOnUiThread(() -> {
            //    try{
            System.out.print(String.format("DATA: %s\n", String.valueOf(data)));

            ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default

            short num = wrapped.getShort();
            textView2.setText(String.valueOf(num));
            //  }
//            catch (BufferUnderflowException e) {
//            e.printStackTrace();
//        }
        });
    }

    @Override
    public void onRunError(Exception e) {
        System.out.println("IN ON RUN ERROR");
    }
}
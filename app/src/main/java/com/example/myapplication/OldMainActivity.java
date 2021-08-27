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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OldMainActivity extends AppCompatActivity {

    private TextView textView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


        button.setOnClickListener(v -> {
            try {
//                byte buffer[] = new byte[1];
//
//                port.write("p".getBytes(StandardCharsets.UTF_8), 5000);
//                numBytesRead = port.read(buffer, 5000);
//                String s = new String(buffer, StandardCharsets.UTF_8);
//                char c = s.toCharArray()[0];
////                ByteBuffer wrapped = ByteBuffer.wrap(buffer); // big-endian by default
////                double num = wrapped.getDouble();
////                System.out.println("HEYYYYYYYYYYYYYYYYY");
////                System.out.println(buffer);
//
//                while(c!='@') {
//                    receivedMessage.concat(s);
//                    numBytesRead = port.read(buffer, 5000);
//                    textView.setText(s);
//                    s = new String(buffer, StandardCharsets.UTF_8);
//                }
//                receivedMessage = "";
//                 //   receivedMessage = "";
                byte buffer[] = new byte[255];
                port.write("3000@".getBytes(StandardCharsets.UTF_8), 5000);
                numBytesRead = port.read(buffer, 5000);
                ByteBuffer wrapped = ByteBuffer.wrap(buffer); // big-endian by default
                short num = wrapped.getShort();

                textView.setText(String.valueOf(num));

            } catch (IOException e) {
                e.printStackTrace();
            }  catch (NullPointerException e) {
                e.printStackTrace();
            }


            //  received = !received;
        });
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
        textView = findViewById(R.id.textView);
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
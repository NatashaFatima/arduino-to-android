package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivityThreading extends AppCompatActivity implements SerialInputOutputManager.Listener
{

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
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    SerialData serialData = new SerialData(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        System.out.println("After init");

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
        System.out.println(manager);
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
        try{

            ByteBuffer wrapped = ByteBuffer.wrap(data); // big-endian by default
            int num = wrapped.getInt();
            serialData.setNum(num);
        }
        catch (BufferUnderflowException e) {
            e.printStackTrace();
        }
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
    }

    private Observable<SerialData> getObservable()  {
        return Observable.create ( (e) -> {
            if (!e.isDisposed()) {
                e.onNext(serialData);
            //    e.onComplete();
            }});

    }

    private Observer<SerialData> getObserver(){
        return new Observer<SerialData>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("onSubscribe");
            }

            @Override
            public void onNext(SerialData data) {
                System.out.println("onNext: " + data.getNum());
                textView2.setText(String.valueOf(data.getNum()));
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };
    }

    @Override
    public void onRunError(Exception e) {
        System.out.println("IN ON RUN ERROR");
    }
}

class SerialData {
    private int num;
    public SerialData(int number) {
        num = number;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}


package com.bjw.ComAssistant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.bjw.bean.AssistBean;
import com.bjw.bean.ComBean;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android_serialport_api.SerialPortFinder;

/**
 * serialport api和jni取自http://code.google.com/p/android-serialport-api/
 * @author benjaminwan
 * 串口助手，支持4串口同时读写
 * 程序载入时自动搜索串口设备
 * n,8,1，没得选
 */
public class ComAssistantActivity extends Activity {
	private static final String TAG = "ComAssistantActivity";
	private LinearLayout mLinearLayout;
	TextView editTextRecDisp;
	EditText editTextLines,editTextCOMA,editTextCOMB,editTextCOMC,editTextCOMD;
	EditText editTextTimeCOMA,editTextTimeCOMB,editTextTimeCOMC;
	CheckBox checkBoxAutoClear,checkBoxAutoCOMA,checkBoxAutoCOMB,checkBoxAutoCOMC,checkBoxAutoCOMD;
	Button ButtonClear,ButtonSendCOMA,ButtonSendCOMB,ButtonSendCOMC;
	ToggleButton toggleButtonCOMA,toggleButtonCOMB,toggleButtonCOMC,toggleButtonCOMD;
	Spinner SpinnerCOMA,SpinnerCOMB,SpinnerCOMC,SpinnerCOMD;
	Spinner SpinnerBaudRateCOMA,SpinnerBaudRateCOMB,SpinnerBaudRateCOMC,SpinnerBaudRateCOMD;
	RadioButton radioButtonTxt,radioButtonHex;
	SerialControl ComA,ComB,ComC,ComD;//4个串口
	DispQueueThread DispQueue;//刷新显示线程
	SerialPortFinder mSerialPortFinder;//串口设备搜索
	AssistBean AssistData;//用于界面数据序列化和反序列化
	int iRecLines=0;//接收区行数
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ComA = new SerialControl();
		ComB = new SerialControl();
		ComC = new SerialControl();
		ComD = new SerialControl();
		DispQueue = new DispQueueThread();
		DispQueue.start();
		AssistData = getAssistData();
		setControls();
	}
	@Override
	public void onDestroy(){
		CloseComPort(ComA);
		CloseComPort(ComB);
		CloseComPort(ComC);
		CloseComPort(ComD);
		super.onDestroy();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		CloseComPort(ComA);
		CloseComPort(ComB);
		CloseComPort(ComC);
		CloseComPort(ComD);
		setContentView(R.layout.main);
		setControls();
	}

	//----------------------------------------------------
	private void setControls()
	{
		String appName = "卡得智能门禁模块调试工具V2.0";
//		try {
//			PackageInfo pinfo = getPackageManager().getPackageInfo("com.bjw.ComAssistant", PackageManager.GET_CONFIGURATIONS);
//			String versionName = pinfo.versionName;
////			String versionCode = String.valueOf(pinfo.versionCode);
//			setTitle(appName);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
		mLinearLayout  =(LinearLayout)findViewById(R.id.id_layout);
		editTextRecDisp=(TextView) findViewById(R.id.editTextRecDisp);
		editTextLines=(EditText)findViewById(R.id.editTextLines);
		editTextCOMA=(EditText)findViewById(R.id.editTextCOMA);
		editTextCOMB=(EditText)findViewById(R.id.editTextCOMB);
		editTextCOMC=(EditText)findViewById(R.id.editTextCOMC);
		editTextCOMD=(EditText)findViewById(R.id.editTextCOMD);
		editTextTimeCOMA = (EditText)findViewById(R.id.editTextTimeCOMA);
		editTextTimeCOMB= (EditText)findViewById(R.id.editTextTimeCOMB);
		editTextTimeCOMC= (EditText)findViewById(R.id.editTextTimeCOMC);

		checkBoxAutoClear=(CheckBox)findViewById(R.id.checkBoxAutoClear);
		checkBoxAutoCOMA=(CheckBox)findViewById(R.id.checkBoxAutoCOMA);
		checkBoxAutoCOMB=(CheckBox)findViewById(R.id.checkBoxAutoCOMB);
		checkBoxAutoCOMC=(CheckBox)findViewById(R.id.checkBoxAutoCOMC);
		checkBoxAutoCOMD=(CheckBox)findViewById(R.id.checkBoxAutoCOMD);
		ButtonClear=(Button)findViewById(R.id.ButtonClear);
		ButtonSendCOMA=(Button)findViewById(R.id.ButtonSendCOMA);
		ButtonSendCOMB=(Button)findViewById(R.id.ButtonSendCOMB);
		ButtonSendCOMC=(Button)findViewById(R.id.ButtonSendCOMC);
		toggleButtonCOMA=(ToggleButton)findViewById(R.id.toggleButtonCOMA);
		toggleButtonCOMA.setTextOff("打开端口");
		toggleButtonCOMB=(ToggleButton)findViewById(R.id.ToggleButtonCOMB);
		toggleButtonCOMC=(ToggleButton)findViewById(R.id.ToggleButtonCOMC);
		toggleButtonCOMD=(ToggleButton)findViewById(R.id.ToggleButtonCOMD);
		SpinnerCOMA=(Spinner)findViewById(R.id.SpinnerCOMA);
		SpinnerCOMB=(Spinner)findViewById(R.id.SpinnerCOMB);
		SpinnerCOMC=(Spinner)findViewById(R.id.SpinnerCOMC);
		SpinnerCOMD=(Spinner)findViewById(R.id.SpinnerCOMD);
		SpinnerBaudRateCOMA=(Spinner)findViewById(R.id.SpinnerBaudRateCOMA);
		SpinnerBaudRateCOMB=(Spinner)findViewById(R.id.SpinnerBaudRateCOMB);
		SpinnerBaudRateCOMC=(Spinner)findViewById(R.id.SpinnerBaudRateCOMC);
		SpinnerBaudRateCOMD=(Spinner)findViewById(R.id.SpinnerBaudRateCOMD);
		radioButtonTxt=(RadioButton)findViewById(R.id.radioButtonTxt);
		radioButtonHex=(RadioButton)findViewById(R.id.radioButtonHex);

		editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMB.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMC.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMD.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMB.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMC.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		editTextCOMB.setOnFocusChangeListener(new FocusChangeEvent());
		editTextCOMC.setOnFocusChangeListener(new FocusChangeEvent());
		editTextCOMD.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMB.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMC.setOnFocusChangeListener(new FocusChangeEvent());


		radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
		radioButtonHex.setOnClickListener(new radioButtonClickEvent());
//		ButtonClear.setOnClickListener(new ButtonClickEvent());
//		ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());
//		ButtonSendCOMB.setOnClickListener(new ButtonClickEvent());
//		ButtonSendCOMC.setOnClickListener(new ButtonClickEvent());
		toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		toggleButtonCOMB.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		toggleButtonCOMC.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		toggleButtonCOMD.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());
		checkBoxAutoCOMB.setOnCheckedChangeListener(new CheckBoxChangeEvent());
		checkBoxAutoCOMC.setOnCheckedChangeListener(new CheckBoxChangeEvent());

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.baudrates_value,R.layout.simple_spinner_items);
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_items);
		SpinnerBaudRateCOMA.setAdapter(adapter);
		SpinnerBaudRateCOMB.setAdapter(adapter);
		SpinnerBaudRateCOMC.setAdapter(adapter);
		SpinnerBaudRateCOMD.setAdapter(adapter);
		SpinnerBaudRateCOMA.setSelection(12);
		SpinnerBaudRateCOMB.setSelection(12);
		SpinnerBaudRateCOMC.setSelection(12);
		SpinnerBaudRateCOMD.setSelection(12);

		mSerialPortFinder= new SerialPortFinder();
		String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		List<String> allDevices = new ArrayList<String>();
		for (int i = 0; i < entryValues.length; i++) {
			allDevices.add(entryValues[i]);
		}
		ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this,
				R.layout.simple_spinner_items, allDevices);
		aspnDevices.setDropDownViewResource(R.layout.simple_spinner_dropdown_items);
		SpinnerCOMA.setAdapter(aspnDevices);
		SpinnerCOMB.setAdapter(aspnDevices);
		SpinnerCOMC.setAdapter(aspnDevices);
		SpinnerCOMD.setAdapter(aspnDevices);
		if (allDevices.size()>0)
		{
			SpinnerCOMA.setSelection(4);
		}
		if (allDevices.size()>1)
		{
			SpinnerCOMB.setSelection(4);
		}
		if (allDevices.size()>2)
		{
			SpinnerCOMC.setSelection(4);
		}
		if (allDevices.size()>3)
		{
			SpinnerCOMD.setSelection(4);
		}
		SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerCOMB.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerCOMC.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerCOMD.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMB.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMC.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMD.setOnItemSelectedListener(new ItemSelectedEvent());
		KeyListener HexkeyListener = new NumberKeyListener()
		{
			public int getInputType()
			{
				return InputType.TYPE_CLASS_TEXT;
			}
			@Override
			protected char[] getAcceptedChars()
			{
				return new char[]{'0','1','2','3','4','5','6','7','8','9',
						'a','b','c','d','e','f','A','B','C','D','E','F'};
			}
		};
		editTextCOMA.setKeyListener(HexkeyListener);
		editTextCOMB.setKeyListener(HexkeyListener);
		editTextCOMC.setKeyListener(HexkeyListener);
		editTextCOMD.setKeyListener(HexkeyListener);
		AssistData.setTxtMode(false);
		DispAssistData(AssistData);
	}
	//----------------------------------------------------串口号或波特率变化时，关闭打开的串口
	class ItemSelectedEvent implements Spinner.OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			if ((arg0 == SpinnerCOMA) || (arg0 == SpinnerBaudRateCOMA))
			{
				CloseComPort(ComA);
				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMA.setChecked(false);
			}else if ((arg0 == SpinnerCOMB) || (arg0 == SpinnerBaudRateCOMB))
			{
				CloseComPort(ComB);
				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMB.setChecked(false);
			}else if ((arg0 == SpinnerCOMC) || (arg0 == SpinnerBaudRateCOMC))
			{
				CloseComPort(ComC);
				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMC.setChecked(false);
			}else if ((arg0 == SpinnerCOMD) || (arg0 == SpinnerBaudRateCOMD))
			{
				CloseComPort(ComD);
				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMD.setChecked(false);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0)
		{}

	}
	//----------------------------------------------------编辑框焦点转移事件
	class FocusChangeEvent implements EditText.OnFocusChangeListener{
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (v==editTextCOMA)
			{
				setSendData(editTextCOMA);
			} else if (v==editTextCOMB)
			{
				setSendData(editTextCOMB);
			} else if (v==editTextCOMC)
			{
				setSendData(editTextCOMC);
			} else if (v==editTextCOMD)
			{
				setSendData(editTextCOMD);
			}else if (v==editTextTimeCOMA)
			{
				setDelayTime(editTextTimeCOMA);
			}else if (v==editTextTimeCOMB)
			{
				setDelayTime(editTextTimeCOMB);
			}else if (v==editTextTimeCOMC)
			{
				setDelayTime(editTextTimeCOMC);
			}
		}
	}
	//----------------------------------------------------编辑框完成事件
	class EditorActionEvent implements EditText.OnEditorActionListener{
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if (v==editTextCOMA)
			{
				setSendData(editTextCOMA);
			} else if (v==editTextCOMB)
			{
				setSendData(editTextCOMB);
			} else if (v==editTextCOMC)
			{
				setSendData(editTextCOMC);
			} else if (v==editTextCOMD)
			{
				setSendData(editTextCOMD);
			}else if (v==editTextTimeCOMA)
			{
				setDelayTime(editTextTimeCOMA);
			}else if (v==editTextTimeCOMB)
			{
				setDelayTime(editTextTimeCOMB);
			}else if (v==editTextTimeCOMC)
			{
				setDelayTime(editTextTimeCOMC);
			}
			return false;
		}
	}
	//----------------------------------------------------Txt、Hex模式选择
	class radioButtonClickEvent implements RadioButton.OnClickListener{
		public void onClick(View v)
		{
//			if (v==radioButtonTxt)
//			{
//				KeyListener TxtkeyListener = new TextKeyListener(Capitalize.NONE, false);
//				editTextCOMA.setKeyListener(TxtkeyListener);
//				editTextCOMB.setKeyListener(TxtkeyListener);
//				editTextCOMC.setKeyListener(TxtkeyListener);
//				editTextCOMD.setKeyListener(TxtkeyListener);
//				AssistData.setTxtMode(true);
//			}else if (v==radioButtonHex) {
			KeyListener HexkeyListener = new NumberKeyListener()
			{
				public int getInputType()
				{
					return InputType.TYPE_CLASS_TEXT;
				}
				@Override
				protected char[] getAcceptedChars()
				{
					return new char[]{'0','1','2','3','4','5','6','7','8','9',
							'a','b','c','d','e','f','A','B','C','D','E','F'};
				}
			};
			editTextCOMA.setKeyListener(HexkeyListener);
			editTextCOMB.setKeyListener(HexkeyListener);
			editTextCOMC.setKeyListener(HexkeyListener);
			editTextCOMD.setKeyListener(HexkeyListener);
			AssistData.setTxtMode(false);
//			}
			editTextCOMA.setText(AssistData.getSendA());
			editTextCOMB.setText(AssistData.getSendB());
			editTextCOMC.setText(AssistData.getSendC());
			editTextCOMD.setText(AssistData.getSendD());
			setSendData(editTextCOMA);
			setSendData(editTextCOMB);
			setSendData(editTextCOMC);
			setSendData(editTextCOMD);
		}
	}
	//----------------------------------------------------自动发送
	class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == checkBoxAutoCOMA){
				if (!toggleButtonCOMA.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				SetLoopData(ComA,editTextCOMA.getText().toString());
				SetAutoSend(ComA,isChecked);
			} else if(buttonView == checkBoxAutoCOMB){
				if (!toggleButtonCOMB.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				SetLoopData(ComB,editTextCOMB.getText().toString());
				SetAutoSend(ComB,isChecked);
			} else if(buttonView == checkBoxAutoCOMC){
				if (!toggleButtonCOMC.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				SetLoopData(ComC,editTextCOMC.getText().toString());
				SetAutoSend(ComC,isChecked);
			} else if(buttonView == checkBoxAutoCOMD){
				if (!toggleButtonCOMD.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				SetLoopData(ComD,editTextCOMD.getText().toString());
				SetAutoSend(ComD,isChecked);
			}
		}
	}

	//----------------------------------------------------打开关闭串口
	class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Log.i(TAG, "onCheckedChanged: "+buttonView.getId());
			if (buttonView == toggleButtonCOMA){
				Log.i(TAG, "toggleButtonCOMA: "+isChecked);
				if (isChecked){
					toggleButtonCOMA.setTextOn("关闭端口");
					if (toggleButtonCOMB.isChecked() && SpinnerCOMA.getSelectedItemPosition()==SpinnerCOMB.getSelectedItemPosition())
					{
						ShowMessage("串口"+SpinnerCOMA.getSelectedItem().toString()+"已打开");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMC.isChecked() && SpinnerCOMA.getSelectedItemPosition()==SpinnerCOMC.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMA.getSelectedItem().toString()+"已打开");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMD.isChecked() && SpinnerCOMA.getSelectedItemPosition()==SpinnerCOMD.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMA.getSelectedItem().toString()+"已打开");
						buttonView.setChecked(false);
					}else {
//						ComA=new SerialControl("/dev/s3c2410_serial0", "9600");
						ComA.setPort(SpinnerCOMA.getSelectedItem().toString());
						ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem().toString());
						OpenComPort(ComA);
					}
				}else {
					toggleButtonCOMA.setTextOff("打开端口");
					CloseComPort(ComA);
					checkBoxAutoCOMA.setChecked(false);
				}
			} else if (buttonView == toggleButtonCOMB){
				Log.i(TAG, "toggleButtonCOMB: ");
				if (isChecked){
					if (toggleButtonCOMA.isChecked() && SpinnerCOMB.getSelectedItemPosition()==SpinnerCOMA.getSelectedItemPosition())
					{
						ShowMessage("串口"+SpinnerCOMB.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMC.isChecked() && SpinnerCOMB.getSelectedItemPosition()==SpinnerCOMC.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMB.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMD.isChecked() && SpinnerCOMB.getSelectedItemPosition()==SpinnerCOMD.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMB.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else {
//						ComB=new SerialControl("/dev/s3c2410_serial1", "9600");
						ComB.setPort(SpinnerCOMB.getSelectedItem().toString());
						ComB.setBaudRate(SpinnerBaudRateCOMB.getSelectedItem().toString());
						OpenComPort(ComB);
					}
				}else {
					CloseComPort(ComB);
					checkBoxAutoCOMB.setChecked(false);
				}
			}else if (buttonView == toggleButtonCOMC){
				Log.i(TAG, "toggleButtonCOMC: ");
				if (isChecked){
					if (toggleButtonCOMA.isChecked() && SpinnerCOMC.getSelectedItemPosition()==SpinnerCOMA.getSelectedItemPosition())
					{
						ShowMessage("串口"+SpinnerCOMC.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMB.isChecked() && SpinnerCOMC.getSelectedItemPosition()==SpinnerCOMB.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMC.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMD.isChecked() && SpinnerCOMC.getSelectedItemPosition()==SpinnerCOMD.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMC.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else {
						//					ComC=new SerialControl("/dev/s3c2410_serial2", "9600");
						ComC.setPort(SpinnerCOMC.getSelectedItem().toString());
						ComC.setBaudRate(SpinnerBaudRateCOMC.getSelectedItem().toString());
						OpenComPort(ComC);
					}
				}else {
					CloseComPort(ComC);
					checkBoxAutoCOMC.setChecked(false);
				}
			}else if (buttonView == toggleButtonCOMD){
				Log.i(TAG, "toggleButtonCOMD: ");
				if (isChecked){
					if (toggleButtonCOMA.isChecked() && SpinnerCOMD.getSelectedItemPosition()==SpinnerCOMA.getSelectedItemPosition())
					{
						ShowMessage("串口"+SpinnerCOMD.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMB.isChecked() && SpinnerCOMD.getSelectedItemPosition()==SpinnerCOMB.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMD.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else if (toggleButtonCOMC.isChecked() && SpinnerCOMD.getSelectedItemPosition()==SpinnerCOMC.getSelectedItemPosition()) {
						ShowMessage("串口"+SpinnerCOMD.getSelectedItem().toString()+"�Ѵ�");
						buttonView.setChecked(false);
					}else {
						//					ComD=new SerialControl("/dev/s3c2410_serial3", "9600");
						ComD.setPort(SpinnerCOMD.getSelectedItem().toString());
						ComD.setBaudRate(SpinnerBaudRateCOMD.getSelectedItem().toString());
						OpenComPort(ComD);
					}
				}else {
					CloseComPort(ComD);
					checkBoxAutoCOMD.setChecked(false);
				}
			}
		}
	}
	//----------------------------------------------------串口控制类
	private class SerialControl extends SerialHelper{

		//		public SerialControl(String sPort, String sBaudRate){
//			super(sPort, sBaudRate);
//		}
		public SerialControl(){
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData)
		{
			//数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
			//直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
			//用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
			//最终效果差不多-_-，线程定时刷新稍好一些。
			DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
//			Log.i(TAG, "onDataReceived: ");
//			runOnUiThread(new Runnable()//直接刷新显示
//			{
//				public void run()
//				{
//					DispRecData(ComRecData);
//				}
//			});
		}
	}
	//----------------------------------------------------刷新显示线程
	private class DispQueueThread extends Thread{
		private Queue<ComBean> QueueList = new LinkedList<ComBean>();
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				final ComBean ComData;
				while((ComData=QueueList.poll())!=null)
				{
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							DispRecData(ComData);
//							ComA.sendHex("AA02000055");
						}
					});
					try
					{
						Thread.sleep(100);//显示性能高的话，可以把此数值调小
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
			}
		}

		public synchronized void AddQueue(ComBean ComData){
			QueueList.add(ComData);
		}
	}
	//----------------------------------------------------ˢ�½�������
	private void DispAssistData(AssistBean AssistData)
	{
		editTextCOMA.setText(AssistData.getSendA());
		editTextCOMB.setText(AssistData.getSendB());
		editTextCOMC.setText(AssistData.getSendC());
		editTextCOMD.setText(AssistData.getSendD());
		setSendData(editTextCOMA);
		setSendData(editTextCOMB);
		setSendData(editTextCOMC);
		setSendData(editTextCOMD);
		if (AssistData.isTxt())
		{
			radioButtonTxt.setChecked(true);
		} else
		{
			radioButtonHex.setChecked(true);
		}
		editTextTimeCOMA.setText(AssistData.sTimeA);
		editTextTimeCOMB.setText(AssistData.sTimeB);
		editTextTimeCOMC.setText(AssistData.sTimeC);

		setDelayTime(editTextTimeCOMA);
		setDelayTime(editTextTimeCOMB);
		setDelayTime(editTextTimeCOMC);

	}

	//----------------------------------------------------
	private AssistBean getAssistData() {
		SharedPreferences msharedPreferences = getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
		AssistBean AssistData =	new AssistBean();
		try {
			String personBase64 = msharedPreferences.getString("AssistData", "");
			byte[] base64Bytes = Base64.decode(personBase64.getBytes(),0);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			AssistData = (AssistBean) ois.readObject();
			return AssistData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return AssistData;
	}
	//----------------------------------------------------设置自动发送延时
	private void setDelayTime(TextView v){
		if (v==editTextTimeCOMA)
		{
			AssistData.sTimeA = v.getText().toString();
			SetiDelayTime(ComA, v.getText().toString());
		}else if (v==editTextTimeCOMB)
		{
			AssistData.sTimeB = v.getText().toString();
			SetiDelayTime(ComB, v.getText().toString());
		}else if (v==editTextTimeCOMC)
		{
			AssistData.sTimeC = v.getText().toString();
			SetiDelayTime(ComC, v.getText().toString());
		}
	}
	//----------------------------------------------------设置自动发送数据
	private void setSendData(TextView v){
		if (v==editTextCOMA)
		{
			AssistData.setSendA(v.getText().toString());
			SetLoopData(ComA, v.getText().toString());
		} else if (v==editTextCOMB)
		{
			AssistData.setSendB(v.getText().toString());
			SetLoopData(ComB, v.getText().toString());
		} else if (v==editTextCOMC)
		{
			AssistData.setSendC(v.getText().toString());
			SetLoopData(ComC, v.getText().toString());
		} else if (v==editTextCOMD)
		{
			AssistData.setSendD(v.getText().toString());
			SetLoopData(ComD, v.getText().toString());
		}
	}
	//----------------------------------------------------设置自动发送延时
	private void SetiDelayTime(SerialHelper ComPort,String sTime){
		ComPort.setiDelay(Integer.parseInt(sTime));
	}
	//----------------------------------------------------设置自动发送数据
	private void SetLoopData(SerialHelper ComPort,String sLoopData){
		if (radioButtonTxt.isChecked())
		{
			ComPort.setTxtLoopData(sLoopData);
		} else if (radioButtonHex.isChecked())
		{
			ComPort.setHexLoopData(sLoopData);
		}
	}
	//----------------------------------------------------显示接收数据
	private void DispRecData(ComBean ComRecData){
//		Log.i(TAG, "DispRecData: ");
		StringBuilder sMsg=new StringBuilder();
		sMsg.append(ComRecData.sRecTime);
		sMsg.append("[");
		sMsg.append(ComRecData.sComPort);
		sMsg.append("]");
		if (radioButtonTxt.isChecked())
		{
			sMsg.append("[Txt] ");
			sMsg.append(new String(ComRecData.bRec));
		}else if (radioButtonHex.isChecked()) {
			sMsg.append("[Hex] ");
			sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
		}
		sMsg.append("\r\n");
		Log.i(TAG, "DispRecData: "+ sMsg );
		editTextRecDisp.append(sMsg);
		iRecLines++;
        Log.i(TAG, "DispRecData: "+editTextRecDisp.getText());
		editTextLines.setText(String.valueOf(iRecLines));
		if ((iRecLines > 500) && (checkBoxAutoClear.isChecked()))//达到500项自动清除
		{
			editTextRecDisp.setText("");
			editTextLines.setText("0");
			iRecLines=0;
		}
	}
	//----------------------------------------------------设置自动发送模式开关
	private void SetAutoSend(SerialHelper ComPort,boolean isAutoSend){
		if (isAutoSend)
		{
			ComPort.startSend();
		} else
		{
			ComPort.stopSend();
		}
	}
	//----------------------------------------------------设置自动发送模式开关
	private void sendPortData(SerialHelper ComPort,String sOut){
		if (ComPort!=null && ComPort.isOpen())
		{
			if (radioButtonTxt.isChecked())
			{
				ComPort.sendTxt(sOut);
			}else if (radioButtonHex.isChecked()) {
				ComPort.sendHex(sOut);
			}
		}
	}
	//----------------------------------------------------关闭串口
	private void CloseComPort(SerialHelper ComPort){
		if (ComPort!=null){
			ComPort.stopSend();
			ComPort.close();
			mLinearLayout.setVisibility(View.INVISIBLE);
			ShowMessage("串口已关闭，请打开串口!");
		}
	}
	//----------------------------------------------------打开串口
	private void OpenComPort(SerialHelper ComPort){
		try
		{
			ComPort.open();
			mLinearLayout.setVisibility(View.VISIBLE);
			ShowMessage("打开串口成功!");
		} catch (SecurityException e) {
			ShowMessage("打开串口失败!"+e.toString());
		} catch (IOException e) {
			ShowMessage("打开串口失败!");
		} catch (InvalidParameterException e) {
			ShowMessage("打开串口失败!");
		}
	}
	//------------------------------------------显示消息
	private void ShowMessage(String sMsg)
	{
		Log.i(TAG, "sMsg:打开串口"+sMsg);
		Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
	}

	public void open(View view)
	{
		if(ComA.isOpen())
		{
			Log.i(TAG, "open door");
			ComA.sendHex("AA02000055");
		}
	}
	public void alwaysaopen(View view)
	{
		if(ComA.isOpen())
		{
			ComA.sendHex("AA03000055");
		}
	}
	public void alwaysaclose(View view)
	{
		if(ComA.isOpen())
		{
			ComA.sendHex("AA04000055");
		}
	}
}
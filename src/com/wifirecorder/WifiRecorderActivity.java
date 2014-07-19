package com.wifirecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WifiRecorderActivity extends Activity 
{
	private Button btnRefresh;
	private Button btnRecord;//紀錄Wifi資訊
	private Button btnExit;
	private TextView txtTime;
	private Calendar time;
	private ListView listWifiResult;//顯示掃描到的Wifi資訊
	private List<ScanResult> WifiList;//掃描到的Wifi訊息
	private WifiManager mWifiMngr;//管理並控制Wifi
	private String[] WifiInfo;//存放Wifi詳細資訊 
	private String curTime;
	private Vector<String> WifiSelectedItem = new Vector<String>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//取得介面資源
		btnRefresh = (Button)findViewById(R.id.btnRefresh);
		btnRecord = (Button)findViewById(R.id.btnRecord);
		btnExit = (Button)findViewById(R.id.btnExit);
		txtTime = (TextView)findViewById(R.id.txtTime);
		listWifiResult = (ListView)findViewById(R.id.listResult);
		//設定Wifi裝置
		mWifiMngr = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);//取得WifiManager
		//啟用Wifi裝置
		OpenWifi();
		//取得Wifi列表
		GetWifiList();
		//設定按鈕功能
		btnRefresh.setOnClickListener(btnListener);
		btnRecord.setOnClickListener(btnListener);
		btnExit.setOnClickListener(btnListener);
		//設定ListView選取事件
		listWifiResult.setOnItemClickListener(listListener);
		listWifiResult.setOnItemLongClickListener(listLongListener);
	}
	
	private Button.OnClickListener btnListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId())
			{
				case R.id.btnRefresh:
					//取得Wifi列表
					GetWifiList();
					break;
				case R.id.btnRecord:
					RecordCheckWindow();
					break;
				case R.id.btnExit:
					CloseWifi();
					finish();
					break;
			}
		}
	};
	
	private ListView.OnItemClickListener listListener = new ListView.OnItemClickListener()
	{		
		int ItemSelectedInVector;
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			
			//如果被勾選就加入Vector
			if(listWifiResult.isItemChecked(position))
				WifiSelectedItem.add(WifiInfo[position]);
			//如果被取消勾選就從Vector移除
			else
			{
				//取得目前選取項目在Vector中的位置
				for(int i=0;i<WifiSelectedItem.size();i++)	
					if(WifiSelectedItem.get(i).equals(WifiInfo[position]))
						ItemSelectedInVector = i; 
				WifiSelectedItem.remove(ItemSelectedInVector);
			}
		}
		
	};
	private ListView.OnItemLongClickListener listLongListener = new ListView.OnItemLongClickListener()
	{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			// TODO Auto-generated method stub
			WifiInfo(position);
			return false;
		}
	};
	private void RecordCheckWindow()
	{
		final EditText edtFileName = new EditText(WifiRecorderActivity.this);
		new AlertDialog.Builder(WifiRecorderActivity.this)
		.setTitle("確認視窗")
		.setIcon(R.drawable.ic_launcher)
		.setMessage("請輸入欲存檔案名稱:")
		.setView(edtFileName)
		.setNegativeButton("取消", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		})
		.setPositiveButton("確定",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//將選取的List記錄並生成檔案
				DataFormer(edtFileName.getText().toString());
			}
		}).show();
	}
	private void WifiInfo(int index)
	{
		new AlertDialog.Builder(WifiRecorderActivity.this)
		.setTitle("詳細資料")
		.setIcon(R.drawable.ic_launcher)
		.setMessage(WifiInfo[index])
		.setNeutralButton("確定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		})
		.show();
	}
	private void DataFormer(String FileName)
	{
		String WifiDatas = curTime+"\r\n";
		File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"WifiDatas");
		//將Wifi資料存進WifDatas
		for(int i=0;i<WifiSelectedItem.size();i++)
			WifiDatas += WifiSelectedItem.elementAt(i).toString()+"\r\n";
		//建立檔案在SDCARD裡
		if(!directory.exists())//如果SD卡沒此資料夾就建立
			directory.mkdir();
		try {
			
			FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath()
							+"/WifiData/"+FileName+".txt",false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(WifiDatas);
			Toast.makeText(WifiRecorderActivity.this
							,FileName+".txt 已存至手機",Toast.LENGTH_LONG).show();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(WifiRecorderActivity.this
							,"存檔失敗!",Toast.LENGTH_LONG).show();
		}
	}
	//打開Wifi裝置
	private void OpenWifi()
	{
		//當Wifi是關閉時將它啟動
		if(!mWifiMngr.isWifiEnabled()){
			mWifiMngr.setWifiEnabled(true);
			Toast.makeText(WifiRecorderActivity.this,"WiFi啟動中...請稍候"
						   ,Toast.LENGTH_LONG).show();
			Toast.makeText(WifiRecorderActivity.this,"請按Refresh鍵更新列表"
					,Toast.LENGTH_LONG).show();
		}
	}
	//關閉Wifi裝置
	private void CloseWifi()
	{
		//當Wifi是開啟時將它開啟
		if(mWifiMngr.isWifiEnabled())
			mWifiMngr.setWifiEnabled(false);
	}
	private void GetWifiList()
	{
		//開始掃描Wifi熱點
		mWifiMngr.startScan();
		//得到掃描結果
		WifiList = mWifiMngr.getScanResults();
		//設定Wifi陣列
		String[] Wifis = new String[WifiList.size()];
		//取得目前時間
		time = Calendar.getInstance();
		curTime = (time.get(Calendar.YEAR))+"/"  
				+(time.get(Calendar.MONTH)+1)+"/"  
				+(time.get(Calendar.DAY_OF_MONTH))+"  "	
				+time.get(Calendar.HOUR_OF_DAY)+":"  
				+time.get(Calendar.MINUTE)+":"	
				+time.get(Calendar.SECOND);
		txtTime.setText("Time:"+curTime);
		//將Wifi資訊放入陣列中(多選清單用)
		for(int i=0;i<WifiList.size();i++)
			Wifis[i] = "SSID:"+WifiList.get(i).SSID +"\n" //SSID
						+"訊號強度:"+WifiList.get(i).level+"dBm";//訊號強弱  
		//將WifiSelectedItem中暫存的資料清空
		WifiSelectedItem.removeAllElements();
		//設定Wifi清單
		SetWifiList(Wifis);
	}
	private void SetWifiList(String[] Wifis)
	{
		//建立ArrayAdpter
		 ArrayAdapter<String> adapterWifis = new ArrayAdapter<String>(WifiRecorderActivity.this
						,android.R.layout.simple_list_item_checked,Wifis);
		//設定ListView為多選
		listWifiResult.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		//設定ListView來源
		listWifiResult.setAdapter(adapterWifis);
		
		//初始化WifiInfo陣列
		WifiInfo = null;
		//設定Wifi資訊放入陣列中(記錄存檔用)
		WifiInfo = new String[WifiList.size()];
		
		for(int i=0;i<WifiList.size();i++)
			WifiInfo[i] = "SSID:"+WifiList.get(i).SSID +"\r\n"      //SSID
						+"BSSID:"+WifiList.get(i).BSSID+"\r\n"   //BSSID
						+"訊號強度:"+WifiList.get(i).level+"dBm"+"\r\n" //訊號強弱 
						+"通道頻率:"+WifiList.get(i).frequency+"MHz"+"\r\n"; //通道頻率 
	}
	

}

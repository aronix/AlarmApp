package bum.boostcamp.alarmapp;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import bum.boostcamp.alarmapp.alarm.AlarmTask;
import bum.boostcamp.alarmapp.data.AlarmContract;
import bum.boostcamp.alarmapp.dialog.BellTypeDialog;
import bum.boostcamp.alarmapp.dialog.MemoDialog;
import bum.boostcamp.alarmapp.dialog.RepeatDialog;

import static bum.boostcamp.alarmapp.util.Utils.getTimeAMPM;
import static bum.boostcamp.alarmapp.util.Utils.getTimeHour;
import static bum.boostcamp.alarmapp.util.Utils.getTimeString;

public class UpdateAlarmActivity extends AppCompatActivity implements  View.OnClickListener {
    public static final String EXTRA_BELLTYPE ="belltype";
    private final int REQUEST_CODE_BELLTYPE = 0;

    private final int REQUEST_CODE_REPEAT = 1;
    public static final String EXTRA_WEEKS ="weeks";
    public static final String EXTRA_IS_REPEAT="is_repeat";
    public static final String EXTRA_YEAR="year";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_DAYS ="days";

    public static final String EXTRA_MEMO ="memo";
    private final int REQUEST_CODE_MEMO = 2;


    public static final int BELLTYPE_RING =0;
    public static final int BELLTYPE_VIBRATION =1;
    public static final int BELLTYPE_RING_AND_VIBRATION = 2;
    public static final int BELLTYPE_DEFAULT = BELLTYPE_RING;

    private Toast mToast;
    private TextView mTimeTextView;
    private int mHourofday,mHour,mMinute,mAMPM;
    private Button mUpdateButton;
    private ImageView mRingTypeImageView,mRepeatImageView,mMemoImageView;
    private TextView mRingTypeTextView,mRepeatTextView,mMemoTextView;
    //
    private int mBelltype;
    //
    private boolean mIsRepeat;
    private boolean[] mWeeks;
    private int mDateYear, mDateMonth ,mDateDays;
    //
    private String mMemo;

    private int mActive;

    private String[] mRingTypeString={"소리","진동","소리+진동"};
    private String[] mWeeksString ={"일","월","화","수","목","금","토"};
    private String mWeekBinary;

    private Uri mUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_alarm);

        mWeeks = new boolean[7];
        mTimeTextView = (TextView)findViewById(R.id.tv_time);
        mRingTypeImageView =(ImageView)findViewById(R.id.iv_bellType);
        mRepeatImageView =(ImageView)findViewById(R.id.iv_repeat);
        mMemoImageView =(ImageView)findViewById(R.id.iv_memo) ;
        mRingTypeTextView=(TextView)findViewById(R.id.tv_bellType);
        mRepeatTextView = (TextView)findViewById(R.id.tv_repeat);
        mMemoTextView = (TextView)findViewById(R.id.tv_memo);
        mUpdateButton = (Button)findViewById(R.id.UpdateBtn);

        mTimeTextView.setOnClickListener(this);
        mRingTypeImageView.setOnClickListener(this);
        mRepeatImageView.setOnClickListener(this);
        mMemoImageView.setOnClickListener(this);

        init();
    }

    private void init(){
        Intent intent = getIntent();
        String uriStr = intent.getStringExtra(MainActivity.EXTRA_URI);
        mUri = Uri.parse(uriStr);
        Cursor cursor = getContentResolver().query(mUri,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        mActive = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_ACTIVE));

        mBelltype = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_BELLTYPE));
        mWeekBinary =cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_DAYS));
        mIsRepeat = isRepeat(mWeekBinary);
        setMweeks();


        mHourofday = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_HOUR_OF_DAY));
        mMinute = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_MINUTES));
        mDateYear =cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_YEAR));
        mDateMonth =cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_MONTH));
        mDateDays = cursor.getInt(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_DAYS));
        mHour =getTimeHour(mHourofday);
        mAMPM = getTimeAMPM(mHourofday);
        mTimeTextView.setText(getTimeString(mHourofday,mMinute));

        mMemo = cursor.getString(cursor.getColumnIndex(AlarmContract.AlarmEntry.COLUMN_MEMO));

        Log.d("###",mBelltype+"/"+mWeekBinary+"/"+mIsRepeat);
        setBellTypeData();
        setRepeatData();
        setMemoData();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //Log.d("###","id="+v.getId());
        switch (id){
            case R.id.tv_time:
                setTime();
                break;
            case R.id.iv_bellType:
                clickBelltype();
                break;
            case R.id.iv_repeat:
                clickRepeat();
                break;
            case R.id.iv_memo:
                clickMemo();
                break;
            default:
        }
    }

    private void setTime(){
        TimePickerDialog dialog = new TimePickerDialog(this, listener, mHourofday, mMinute, false);
        dialog.show();
    }

    private TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mTimeTextView.setText(getTimeString(hourOfDay,minute));
            mHourofday = hourOfDay;
            mHour = getTimeHour(mHourofday);
            mAMPM = getTimeAMPM(mHourofday);
            mMinute = minute;
            showToast(mTimeTextView.getText().toString());
        }
    };



    private void clickBelltype(){
        Intent i = new Intent(this,BellTypeDialog.class);
        i.putExtra(EXTRA_BELLTYPE,mBelltype);
        startActivityForResult(i,REQUEST_CODE_BELLTYPE);
    }

    private void clickRepeat(){
        Intent i = new Intent(this,RepeatDialog.class);
        i.putExtra(EXTRA_WEEKS,mWeeks);
        i.putExtra(EXTRA_IS_REPEAT,mIsRepeat);
        i.putExtra(EXTRA_YEAR,mDateYear);
        i.putExtra(EXTRA_MONTH,mDateMonth-1);//달력표기에서 month는 1을 뺴줘야함
        i.putExtra(EXTRA_DAYS,mDateDays);
        startActivityForResult(i,REQUEST_CODE_REPEAT);
    }

    private void clickMemo(){
        Intent i = new Intent(this,MemoDialog.class);
        i.putExtra(EXTRA_MEMO,mMemo);
        startActivityForResult(i,REQUEST_CODE_MEMO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch(requestCode){
                case REQUEST_CODE_BELLTYPE:
                    mBelltype = data.getIntExtra(EXTRA_BELLTYPE,BELLTYPE_DEFAULT);
                    setBellTypeData();
                    break;

                case REQUEST_CODE_REPEAT:
                    mWeeks = data.getBooleanArrayExtra(EXTRA_WEEKS);
                    mIsRepeat = data.getBooleanExtra(EXTRA_IS_REPEAT,false);
                    if(!mIsRepeat) {
                        mDateYear = data.getIntExtra(EXTRA_YEAR, mDateYear);
                        mDateMonth = data.getIntExtra(EXTRA_MONTH, mDateMonth);
                        mDateDays = data.getIntExtra(EXTRA_DAYS, mDateDays);
                    }
                    setRepeatData();
                    break;

                case REQUEST_CODE_MEMO:
                    mMemo = data.getStringExtra(EXTRA_MEMO);
                    setMemoData();
                    break;
            }

        }
    }

    private void setBellTypeData() {
        switch(mBelltype){
            case BELLTYPE_RING:
                Picasso.with(this).load(R.mipmap.belltype_ring).into(mRingTypeImageView);
                mRingTypeTextView.setText(mRingTypeString[BELLTYPE_RING]);
                break;
            case BELLTYPE_VIBRATION:
                Picasso.with(this).load(R.mipmap.belltype_vib).into(mRingTypeImageView);
                mRingTypeTextView.setText(mRingTypeString[BELLTYPE_VIBRATION]);
                break;
            case BELLTYPE_RING_AND_VIBRATION:
                Picasso.with(this).load(R.mipmap.belltype_ring_vib).into(mRingTypeImageView);
                mRingTypeTextView.setText(mRingTypeString[BELLTYPE_RING_AND_VIBRATION]);
                break;
            default:

        }
    }

    private void setRepeatData(){
        if(!mIsRepeat){
            Picasso.with(this).load(R.mipmap.repeat_off).into(mRepeatImageView);
            mRepeatTextView.setText(mDateYear+"/"+mDateMonth+"/"+mDateDays +"\n한번 반복");
            mWeekBinary ="0000000";
        }
        else{
            Picasso.with(this).load(R.mipmap.repeat).into(mRepeatImageView);
            String binaryWeek="";
            mRepeatTextView.setText("");
            for(int i=0;i<7;i++){
                if(mWeeks[i]) {
                    binaryWeek= binaryWeek +"1";
                    mRepeatTextView.append(mWeeksString[i]);
                }
                else{
                    binaryWeek=binaryWeek +"0";
                }
            }
            //Log.d("####","binary="+binaryWeek);
            mWeekBinary = binaryWeek;
        }
    }

    private void setMemoData(){
        if(mMemo.equals("")){
            Picasso.with(this).load(R.mipmap.memo_off).into(mMemoImageView);
            mMemoTextView.setText(getString(R.string.add_alaram_tv_memo));
        }
        else{
            Picasso.with(this).load(R.mipmap.memo_on).into(mMemoImageView);
            mMemoTextView.setText(mMemo);
        }
    }

    private void showToast(String str){
        if(mToast!=null)
            mToast.cancel();
        Log.d("###","toast="+str);
        mToast = Toast.makeText(this,str,Toast.LENGTH_SHORT);
        mToast.show();
    }

    private boolean isEmpty(boolean[] weeks){
        boolean isempty = true;
        for(int i=0;i<7;i++){
            if(weeks[i]){
                isempty=false;
                break;
            }
        }
        return isempty;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.setting) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void update(View view) {
        mUpdateButton.setEnabled(false); //중복 업데이트를 막기위해서 false;

        ContentResolver contentResolver = this.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(AlarmContract.AlarmEntry.COLUMN_AMPM,mAMPM);
        cv.put(AlarmContract.AlarmEntry.COLUMN_HOUR,mHour);
        cv.put(AlarmContract.AlarmEntry.COLUMN_HOUR_OF_DAY,mHourofday);
        cv.put(AlarmContract.AlarmEntry.COLUMN_MINUTES,mMinute);
        cv.put(AlarmContract.AlarmEntry.COLUMN_DAYS,mWeekBinary);
        cv.put(AlarmContract.AlarmEntry.COLUMN_ACTIVE,mActive);
        cv.put(AlarmContract.AlarmEntry.COLUMN_MEMO,mMemo);
        cv.put(AlarmContract.AlarmEntry.COLUMN_DATE,getNowDate());
        cv.put(AlarmContract.AlarmEntry.COLUMN_BELLTYPE,mBelltype);
        cv.put(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_YEAR,mDateYear);
        cv.put(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_MONTH,mDateMonth);
        cv.put(AlarmContract.AlarmEntry.COLUMN_NO_REPEAT_DAYS,mDateDays);


         /*
         Log.d("####", mDateYear+"/"+mDateMonth+"/"+mDateDays);
         Log.d("####",mWeekBinary);*/

        contentResolver.update(mUri,cv,null,null);

        if(mActive==1)
            AlarmTask.registAlarm(this,mUri);

        finish();
    }

    private boolean isRepeat(String weekBinaryStr){
        boolean isRepeat = false;
        for(int i=0;i<7;i++){
           if(weekBinaryStr.charAt(i)=='1'){
               isRepeat=true;
               break;
           }
        }
        return isRepeat;

    }

    private void setMweeks(){
        //저장한 "1000111"같은 문자를 mWeek에다가 true,false,false,false,true,true,true 로 넣어주는 함수
        for(int i=0;i<7;i++){
            if(mWeekBinary.charAt(i)=='1'){
                mWeeks[i]=true;
            }
            else{
                mWeeks[i]=false;
            }
        }
    }

    private String getNowDate(){
        Calendar calendar = Calendar.getInstance( );  // 현재 날짜/시간 등의 각종 정보 얻기
        return calendar.get(Calendar.YEAR)+"."+ (calendar.get(Calendar.MONTH) + 1) +"." + calendar.get(Calendar.DAY_OF_MONTH);
    }
}

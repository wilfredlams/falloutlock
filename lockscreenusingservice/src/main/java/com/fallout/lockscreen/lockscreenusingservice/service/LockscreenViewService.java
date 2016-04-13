package com.fallout.lockscreen.lockscreenusingservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fallout.lockscreen.lockscreenusingservice.Lockscreen;
import com.fallout.lockscreen.lockscreenusingservice.LockscreenUtil;
import com.fallout.lockscreen.lockscreenusingservice.R;
import com.fallout.lockscreen.lockscreenusingservice.SharedPreferencesUtil;


public class LockscreenViewService extends Service implements View.OnClickListener {
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private View mLockscreenView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private RelativeLayout mainLayout = null;
    private boolean mIsLockEnable = false;
    private boolean mIsSoftkeyEnable = false;
    private int mServiceStartId = 0;
    private SendMassgeHandler mMainHandler = null;

    SeekBar seek1, seek2;
    ImageButton lock;
    ImageView one;
    ImageView two;
    ImageView minipin1,minipin2,minipin3,minipin4,minipin5;
    int invalid;
    Boolean advance_v;
    int bypass;

//    private boolean sIsSoftKeyEnable = false;

    private class SendMassgeHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //changeBackGroundLockView(mLastLayoutX);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferencesUtil.init(mContext);
//        sIsSoftKeyEnable = SharedPreferencesUtil.get(Lockscreen.ISSOFTKEY);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LockPref", 0);
        advance_v = pref.getBoolean("advance", false);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMainHandler = new SendMassgeHandler();
        if (isLockScreenAble()) {
            if (null != mWindowManager) {
                if (null != mLockscreenView) {
                    mWindowManager.removeView(mLockscreenView);
                }
                mWindowManager = null;
                mParams = null;
                mInflater = null;
                mLockscreenView = null;
            }
            initState();
            initView();
            attachLockScreenView();
        }
        return LockscreenViewService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        dettachLockScreenView();
    }

    private void initState() {

        mIsLockEnable = LockscreenUtil.getInstance(mContext).isStandardKeyguardState();
        if (mIsLockEnable) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                    PixelFormat.TRANSLUCENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsLockEnable && mIsSoftkeyEnable) {
                mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            } else {
                mParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
            }
        } else {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if (null == mWindowManager) {
            mWindowManager = ((WindowManager) mContext.getSystemService(WINDOW_SERVICE));
        }
    }

    private void initView() {
        if (null == mInflater) {
            mInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (null == mLockscreenView) {
            mLockscreenView = mInflater.inflate(R.layout.view_lockscreen, null);

        }
    }

    private boolean isLockScreenAble() {
        boolean isLock = SharedPreferencesUtil.get(Lockscreen.ISLOCK);
        if (isLock) {
            isLock = true;
        } else {
            isLock = false;
        }
        return isLock;
    }


    private void attachLockScreenView() {

        if (null != mWindowManager && null != mLockscreenView && null != mParams) {
            mLockscreenView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mWindowManager.addView(mLockscreenView, mParams);
            settingLockView();

            SharedPreferences SettingsActivity = getApplicationContext().getSharedPreferences("SettingsActivity", 0);
            int value = SettingsActivity.getInt("value", 0);
            if (value == 1)
            {
                SharedPreferences.Editor editor = SettingsActivity.edit();
                editor.putInt("value", 0); // Storing Int
                editor.commit();
                dettachLockScreenView();
            }

        }

    }


    private boolean dettachLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView) {
            mLockscreenView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            mWindowManager.removeView(mLockscreenView);
            mLockscreenView = null;
            mWindowManager = null;
            stopSelf(mServiceStartId);
            return true;
        } else {
            return false;
        }

    }



    private void settingLockView() {
        mLockscreenView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        //Find ImageButton
        lock = (ImageButton) mLockscreenView.findViewById(R.id.imageButton) ;
        one = (ImageView) mLockscreenView.findViewById(R.id.imageView1);
        two = (ImageView) mLockscreenView.findViewById(R.id.imageView2);

        pinleft();

        //ImageButton btnRotate= (ImageButton) findViewById(R.id.imageButton);
        if (advance_v == true) {
            lock.setOnClickListener(this);
        }

        final MediaPlayer turnsound = MediaPlayer.create(this, R.raw.turn);
        final MediaPlayer picksound = MediaPlayer.create(this, R.raw.pick);
        //final MediaPlayer snapsound = MediaPlayer.create(this, R.raw.snap);


        //Seekbar listener to adjust imageviews
        final SeekBar sk = (SeekBar) mLockscreenView.findViewById(R.id.seekBar1);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                picksound.start();
                seek1 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar1);
                int seekValue1 = seek1.getProgress();
                one = (ImageView) mLockscreenView.findViewById(R.id.imageView1);
                one.setRotation((float) (seekValue1 * 2.25));

                // Log.d(TAG, "seek value 1 = "+seekValue1*9);
            }

        });



        if (advance_v == true) {

            final SeekBar sl = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
            sl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()

                                          {

                                              @Override
                                              public void onStopTrackingTouch(SeekBar seekBar) {
                                                  // TODO Auto-generated method stub
                                              }

                                              @Override
                                              public void onStartTrackingTouch(SeekBar seekBar) {
                                                  // TODO Auto-generated method stub
                                              }

                                              @Override
                                              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                  turnsound.start();
                                                  seek2 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
                                                  int seekValue2 = seek2.getProgress();
                                                  two = (ImageView) mLockscreenView.findViewById(R.id.imageView2);
                                                  two.setRotation((float) (90 + seekValue2 * -2.25));

                                                  lock = (ImageButton) mLockscreenView.findViewById(R.id.imageButton);
                                                  lock.setRotation((float) (30 + seekValue2 * -0.75));

                                                  // Log.d(TAG, "seek value 2 = " + seekValue2*9);
                                              }

                                          }

            );
        } else {
            seek2 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
            seek2.setProgress(40);

            final Animation animVibrate = AnimationUtils.loadAnimation(this, R.anim.vibrate);
            final MediaPlayer vibratesound = MediaPlayer.create(this, R.raw.vibrate);

            final SeekBar sl = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
            sl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    turnsound.start();
                    seek2 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
                    int seekValue2 = seek2.getProgress();
                    two = (ImageView) mLockscreenView.findViewById(R.id.imageView2);
                    two.setRotation((float) (90 + seekValue2 * -2.25));

                    lock = (ImageButton) mLockscreenView.findViewById(R.id.imageButton);
                    lock.setRotation((float) (30 + seekValue2 * -0.75));

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("LockPref", 0);

                    int valuea_int = pref.getInt("valuea_store", 0);
                    seek1 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar1);
                    int seekValue1 = seek1.getProgress();

                    int seek_difference = (seekValue1 - valuea_int);

                    if (seek_difference < 0) {
                        seek_difference = (seek_difference / -1);
                    }

                    int stage = 0;

                    if(seek_difference >= 15) stage = 4;
                    if(seek_difference > 10 && seek_difference < 15) stage = 3;
                    if(seek_difference > 5 && seek_difference < 10) stage = 2;
                    if(seek_difference >= 3 && seek_difference < 5) stage = 1;
                    if(seek_difference == 0) stage = 0;

                    switch (stage) {
                        case 4:
                            if (seekValue2 <= 30) {
                                one.startAnimation(animVibrate);
                                vibratesound.start();
                            }
                            if (seekValue2 <= 25) {
                                one.clearAnimation();
                                seek2.setEnabled(false);
                                unlock();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        seek2.setEnabled(true);
                                        seek2.setProgress(40);
                                    }
                                }, 1000);
                            }
                            break;
                        case 3:
                            if (seekValue2 <= 20) {
                                one.startAnimation(animVibrate);
                                vibratesound.start();
                            }
                            if (seekValue2 <= 15) {
                                one.clearAnimation();
                                seek2.setEnabled(false);
                                unlock();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        seek2.setEnabled(true);
                                        seek2.setProgress(40);
                                    }
                                }, 1000);
                            }
                            break;
                        case 2:
                            if (seekValue2 <= 10) {
                                one.startAnimation(animVibrate);
                                vibratesound.start();
                            }
                            if (seekValue2 <= 5) {
                                one.clearAnimation();
                                seek2.setEnabled(false);
                                unlock();
                                //snapsound.start();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        seek2.setEnabled(true);
                                        seek2.setProgress(40);
                                    }
                                }, 1000);
                            }
                            break;
                        case 1:
                            if (seekValue2 <= 5) {
                                one.startAnimation(animVibrate);
                                vibratesound.start();
                            }
                            if (seekValue2 <= 3) {
                                one.clearAnimation();
                                seek2.setEnabled(false);
                                unlock();
                                //snapsound.start();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        seek2.setEnabled(true);
                                        seek2.setProgress(40);
                                    }
                                }, 1000);
                            }
                            break;
                        case 0:
                            if (seekValue2 == 0) {
                                one.clearAnimation();
                                unlock();
                            }
                            break;
                    }

                }

            });

        }
        //Fallback PIN function
        final Button unlockButton = (Button)mLockscreenView.findViewById(R.id.unlockButton);
        final MediaPlayer unlocksound = MediaPlayer.create(this, R.raw.unlock);

        unlockButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("LockPref", 0);
                int pin_num = pref.getInt("pin_store", 0000);
                EditText user_input = (EditText) mLockscreenView.findViewById(R.id.pin_number);
                if (user_input.getText().toString().trim().length() >= 4) {
                    int user_pin = Integer.parseInt(user_input.getText().toString());

                    if (user_pin == pin_num) {
                        unlocksound.start();
                        dettachLockScreenView();
                        bypass = 1;
                        invalid = 0;
                    }

                }
            }
        });


        }

    //Function when the unlockclass button is clicked
    public void onClick(View v) {
        unlock();
    }


    private void unlock() {
        final MediaPlayer unlocksound = MediaPlayer.create(this, R.raw.unlock);
        final MediaPlayer snapsound = MediaPlayer.create(this, R.raw.snap);

        final Animation animRotate = AnimationUtils.loadAnimation(this, R.anim.roatate);
        final Animation animRotate_fail = AnimationUtils.loadAnimation(this, R.anim.roatate_fail);

        if (invalid < 5) {

            // Execute Unlock function after 2 seconds have passed
            seek1 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar1);
            int seekValue1 = seek1.getProgress();

            seek2 = (SeekBar) mLockscreenView.findViewById(R.id.seekBar2);
            int seekValue2 = seek2.getProgress();

            SharedPreferences pref = getApplicationContext().getSharedPreferences("LockPref", 0);

            int valuea_int = pref.getInt("valuea_store", 0);
            int valueb_int;

            if (advance_v == false)
            {
                valueb_int = 0;
            }
            else
            {
                valueb_int = pref.getInt("valueb_store", 0);
            }

            //If combination matches
            if ((seekValue1 - valuea_int >= -2 && seekValue1 - valuea_int <= 2) && (seekValue2 - valueb_int >= -2 && seekValue2 - valueb_int <= 2)) {

                seek1.setEnabled(false);
                seek2.setEnabled(false);

                if (advance_v == true) {
                    lock.startAnimation(animRotate);
                    //one.startAnimation(animRotate);
                    two.startAnimation(animRotate);
                }

                unlocksound.start();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        //Instead of using finish(), this totally destroys the process
                        //android.os.Process.killProcess(android.os.Process.myPid());
                        invalid = 0;
                        dettachLockScreenView();
                    }
                }, 500);
            } else
            //When combination does not match
            {
                //Add one to invalid
                invalid = invalid + 1;
                snapsound.start();

                advance_v = pref.getBoolean("advance", false);

                //If not in advance mode
                if (advance_v == false) {

                    one = (ImageView) mLockscreenView.findViewById(R.id.imageView1);
                    one.setBackgroundResource(R.drawable.pin_broken);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            one.setBackgroundResource(R.drawable.pin);
                        }
                    }, 1000);

                    //If in advance mode
                } else {
                    lock.startAnimation(animRotate_fail);
                    two.startAnimation(animRotate_fail);
                    one = (ImageView) mLockscreenView.findViewById(R.id.imageView1);
                    one.setBackgroundResource(R.drawable.pin_broken);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            one.setBackgroundResource(R.drawable.pin);
                        }
                    }, 1000);


                }

            pinleft();

                if (invalid == 5) {
                    //Lock out when all picks are gone
                    new CountDownTimer(60000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            one.setBackgroundResource(0);
                            seek1.setEnabled(false);
                            seek2.setEnabled(false);

                            Context context = getApplicationContext();
                            CharSequence text = "Too many attempts, try again in " + millisUntilFinished / 1000 + " seconds.";
                            //int duration = Toast.LENGTH_SHORT;
                            //Toast toast = Toast.makeText(context, text, duration);

                            if (bypass == 1)
                            {
                                bypass = 0;
                                onFinish();
                                cancel();
                            }

                            final Toast toastcounter = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                            toastcounter.show();
                            new CountDownTimer(1000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    toastcounter.show();
                                }

                                public void onFinish() {
                                    toastcounter.cancel();
                                }
                            }.start();

                            toastcounter.show();
                        }

                        //Reset minipicks after lockout period
                        public void onFinish() {
                            invalid = 0;
                            minipin1.setBackgroundResource(R.drawable.mini_pin);
                            minipin2.setBackgroundResource(R.drawable.mini_pin);
                            minipin3.setBackgroundResource(R.drawable.mini_pin);
                            minipin4.setBackgroundResource(R.drawable.mini_pin);
                            minipin5.setBackgroundResource(R.drawable.mini_pin);
                            one.setBackgroundResource(R.drawable.pin);
                            seek1.setEnabled(true);
                            seek2.setEnabled(true);
                        }
                    }.start();
                }

        }
        }

        else
        //Show message when the person attempt to unlockclass during lockout period
        {
            Context context = getApplicationContext();
            CharSequence text = "You are still locked out! Wait until the counter finishes.";
            int duration = Toast.LENGTH_SHORT;

            //Toast toast = Toast.makeText(context, text, duration);

            final Toast toastcounter = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            toastcounter.show();
            new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    toastcounter.show();
                }

                public void onFinish() {
                    toastcounter.cancel();
                }
            }.start();


            toastcounter.show();

        }


    }

    private void pinleft() {
        //Pin left function

        minipin1 = (ImageView) mLockscreenView.findViewById(R.id.mini_pin1);
        minipin2 = (ImageView) mLockscreenView.findViewById(R.id.mini_pin2);
        minipin3 = (ImageView) mLockscreenView.findViewById(R.id.mini_pin3);
        minipin4 = (ImageView) mLockscreenView.findViewById(R.id.mini_pin4);
        minipin5 = (ImageView) mLockscreenView.findViewById(R.id.mini_pin5);

        switch (invalid) {
            case 1:
                invalid = 1;
                minipin5.setBackgroundResource(0);
                break;
            case 2:
                invalid = 2;
                minipin5.setBackgroundResource(0);
                minipin4.setBackgroundResource(0);
                break;
            case 3:
                invalid = 3;
                minipin5.setBackgroundResource(0);
                minipin4.setBackgroundResource(0);
                minipin3.setBackgroundResource(0);
                break;
            case 4:
                invalid = 4;
                minipin5.setBackgroundResource(0);
                minipin4.setBackgroundResource(0);
                minipin3.setBackgroundResource(0);
                minipin2.setBackgroundResource(0);
                break;
            case 5:
                invalid = 5;
                minipin5.setBackgroundResource(0);
                minipin4.setBackgroundResource(0);
                minipin3.setBackgroundResource(0);
                minipin2.setBackgroundResource(0);
                minipin1.setBackgroundResource(0);
                break;
        }
    }

}

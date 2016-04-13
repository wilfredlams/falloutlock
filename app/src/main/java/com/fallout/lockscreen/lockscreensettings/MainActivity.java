package com.fallout.lockscreen.lockscreensettings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fallout.lockscreen.lockscreenusingservice.Lockscreen;
import com.fallout.lockscreen.lockscreenusingservice.SharedPreferencesUtil;
import com.fallout.lockscreen.lockscreenusingservice.service.LockscreenViewService;


public class MainActivity extends AppCompatActivity {
    Button save;
    SeekBar seek1, seek2;
    ImageButton lock;
    ImageView one;
    ImageView two;
    CheckBox Advance;

    private SwitchCompat mSwitchd = null;
    private Context mContext = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        SharedPreferencesUtil.init(mContext);


        mSwitchd = (SwitchCompat) this.findViewById(R.id.switch_locksetting);
        mSwitchd.setTextOn("yes");
        mSwitchd.setTextOff("no");
        boolean lockState = SharedPreferencesUtil.get(Lockscreen.ISLOCK);
        if (lockState) {
            mSwitchd.setChecked(true);
        } else {
            mSwitchd.setChecked(false);

        }

        mSwitchd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences SettingsActivity = getApplicationContext().getSharedPreferences("SettingsActivity", 0);
                    SharedPreferences.Editor editor = SettingsActivity.edit();
                    editor.putInt("value", 1); // Storing Int
                    editor.commit();

                    SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, true);
                    Lockscreen.getInstance(mContext).startLockscreenService();

                } else {
                    SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, false);
                    Lockscreen.getInstance(mContext).stopLockscreenService();
                }

            }
        });

        save = (Button) findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mSwitchd.setChecked(false);
                                        mSwitchd.setChecked(true);
                                        seek1 = (SeekBar) findViewById(R.id.value_a);
                                        int seek_valuea = seek1.getProgress();
                                        seek2 = (SeekBar) findViewById(R.id.value_b);
                                        int seek_valueb = seek2.getProgress();
                                        Advance= (CheckBox) findViewById(R.id.advance);
                                        boolean advance_check = Advance.isChecked();

                                        EditText pin_text= (EditText) findViewById(R.id.pin_number);

                                        int pin_num = 0;

                                        if (pin_text.getText().toString().trim().length() < 4)
                                        {
                                            Context context = getApplicationContext();
                                            CharSequence text = "PIN is not 4-6 digits, PIN restored to 0000!";
                                            int duration = Toast.LENGTH_SHORT;

                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();

                                            pin_num = 0;
                                        }
                                        else if (pin_text.getText().toString().trim().length() >= 4) {
                                            pin_num = Integer.parseInt(pin_text.getText().toString());
                                        }

                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("LockPref", 0);
                                        SharedPreferences.Editor editor = pref.edit();

                                        editor.putInt("valuea_store", seek_valuea); // Storing Int
                                        editor.putInt("valueb_store", seek_valueb); // Storing Int
                                        editor.putInt("pin_store", pin_num); // Storing Int
                                        editor.putBoolean("advance", advance_check);

                                        editor.commit();

                                        Context context = getApplicationContext();
                                        CharSequence text = "Combination Saved!";
                                        int duration = Toast.LENGTH_SHORT;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }

                                }
        );

        //Seekbar listener to adjust imageviews
        final SeekBar sk = (SeekBar) findViewById(R.id.value_a);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek1 = (SeekBar) findViewById(R.id.value_a);
                int seekValue1 = seek1.getProgress();
                one = (ImageView) findViewById(R.id.imageView1);
                one.setRotation((float) (seekValue1 * 2.25));

            }

        });

        Advance = (CheckBox) findViewById(R.id.advance);
        Advance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                               @Override
                                               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                   if (Advance.isChecked()) {
                                                       final SeekBar sl = (SeekBar) findViewById(R.id.value_b);
                                                       sl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                                           @Override
                                                           public void onStopTrackingTouch(SeekBar seekBar) {

                                                           }

                                                           @Override
                                                           public void onStartTrackingTouch(SeekBar seekBar) {

                                                           }

                                                           @Override
                                                           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                               seek2 = (SeekBar) findViewById(R.id.value_b);
                                                               int seekValue2 = seek2.getProgress();
                                                               two = (ImageView) findViewById(R.id.imageView2);
                                                               two.setRotation((float) (90 + seekValue2 * -2.25));

                                                               lock = (ImageButton) findViewById(R.id.imageButton);
                                                               lock.setRotation((float) (30 + seekValue2 * -0.75));

                                                           }

                                                       });
                                                   } else {
                                                       seek2 = (SeekBar) findViewById(R.id.value_b);
                                                       seek2.setProgress(40);

                                                       final SeekBar sl = (SeekBar) findViewById(R.id.value_b);
                                                       sl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                                                           @Override
                                                           public void onStopTrackingTouch(SeekBar seekBar) {

                                                           }

                                                           @Override
                                                           public void onStartTrackingTouch(SeekBar seekBar) {

                                                           }

                                                           @Override
                                                           public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                                           }

                                                       });
                                                   }

                                               }
                                           }
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {

            Intent myIntent = new Intent(MainActivity.this, AboutActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivity(myIntent);

            //return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

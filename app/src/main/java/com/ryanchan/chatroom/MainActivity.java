package com.ryanchan.chatroom;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static com.ryanchan.chatroom.R.id.editText;
import static com.ryanchan.chatroom.R.layout.dialog_signin;


public class MainActivity extends ListActivity implements View.OnClickListener {

    //private static final String url = "https://chat-bbfbf.firebaseio.com/";
    private static String username = "";
    Firebase FB = new Firebase("https://chat-bbfbf.firebaseio.com/chat");

    private ChatListAdapter mChatListAdapter;
    private ValueEventListener mConnectedListener;
    private static final String TAG = "MainActivity";
    
    private DrawingView drawView;
    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton newBtn, drawBtn, eraseBtn, saveBtn, typeBtn;
    private Button sendBtn;
    private View drawArea, footer;
    private EditText input;

    private AlertDialog.Builder builder;

    private static final int REQUEST_WRITE_STORAGE = 112;

    MyService my_service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DRAWING STUFF
        drawView = (DrawingView) findViewById(R.id.drawing);
        drawArea = (View) findViewById(R.id.drawArea);
        footer = (View) findViewById(R.id.listFooter);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        drawView.setBrushSize(mediumBrush);

        drawBtn = (ImageButton) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        typeBtn = (ImageButton) findViewById(R.id.type_btn);
        typeBtn.setOnClickListener(this);

        sendBtn = (Button) findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(this);



        input = (EditText) findViewById(editText);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == event.ACTION_DOWN) {
                    send_message();
                }
                return true;

            }
        });
        if (getIntent().getExtras() != null) {
            for (String key: getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_message();
            }
        });


        findViewById(R.id.draw).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                drawArea.setVisibility(View.VISIBLE);
                footer.setVisibility(View.GONE);
                if (getCurrentFocus() != null) {


                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        } );



        if( username == ""){
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose a username:");
            LayoutInflater inflater = this.getLayoutInflater();

            final View builderView = inflater.inflate(dialog_signin, null);
            final EditText usernameS = (EditText) builderView.findViewById(R.id.usernameBox);
            builder.setView(builderView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            username = usernameS.getText().toString();

                            mChatListAdapter.setmUsername(username);


                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);



            usernameS.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 1 && drawBtn.getVisibility() == View.VISIBLE) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    }
                    else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                    }
                }
            });
        }


        Intent i = new Intent(this, MyService.class);
        bindService(i, myConnection, Context.BIND_AUTO_CREATE);



    }

    @Override
    public void onStart(){
        //when starting, we make a chatlistAdapter, and make sure we are connected to firebase
        super.onStart();

        final ListView listView = getListView();
        drawArea.setVisibility(View.GONE);


        mChatListAdapter = new ChatListAdapter(FB, this, R.layout.chat_message, username);
        listView.setAdapter(mChatListAdapter);
        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(mChatListAdapter.getCount() - 1);
            }
        });


        mConnectedListener = FB.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if(connected) {
                    //Toast.makeText(MainActivity.this, "CONNECTED", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(MainActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                //ok
            }
        });



    }



    @Override
    public void onStop() {
        super.onStop();
        FB.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
        mChatListAdapter.cleanup();
    }


    @Override
    public void onBackPressed() {
        //on back pressed, drawArea disappears
            drawArea.setVisibility(View.GONE);
            footer.setVisibility(View.VISIBLE);
    }


    private ServiceConnection myConnection = new ServiceConnection() {
        //starts myservice to enable notifications
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.MyLocalBinder binder = (MyService.MyLocalBinder) service;
            my_service = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onPause() {
        //turn notifications on when paused
        super.onPause();
        MyService.setNotify_on();
    }

    @Override
    protected void onResume() {
        //turn off notifications, put the view back on to typing
        super.onResume();
        drawArea.setVisibility(View.GONE);
        footer.setVisibility(View.VISIBLE);
        MyService.setNotify_off();
    }

    private void send_message() {
        //takes the text in edittext, converts it into a chat, pushes it onto firebasem, clears text
        EditText inputText = (EditText) findViewById(editText);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            Chat chat = new Chat(input, username);

            FB.push().setValue(chat);
            inputText.setText("");
        }
    }



    @Override
    public void onClick(View v) {
        //handles the clicks for all buttons on screen abnd what should be happening
        if (v.getId() == R.id.draw_btn) {
            //draw button
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();

        } else if (v.getId() == R.id.new_btn) {
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing? \n (you will lose the current drawing)");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (v.getId() == R.id.erase_btn) {
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();

        } else if (v.getId() == R.id.save_btn) {
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        savePic();
                        Log.d("PERMISSION GRANTED", "what the fuck man this shit fucking sucks");
                    } else {
                        Log.d("PERMISSION DENIED", "what the fuck man this shit fucking sucks");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(getBaseContext(), "save shit", Toast.LENGTH_SHORT).show();
                            }
                        }

                        Toast.makeText(getBaseContext(), "save shit yea", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_WRITE_STORAGE);

                    }


                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();

        } else if (v.getId() == R.id.type_btn){
            drawArea.setVisibility(View.GONE);
            footer.setVisibility(View.VISIBLE);
        }

        else if (v.getId() == R.id.send_btn){
            drawView.setDrawingCacheEnabled(true);
            Bitmap bitmap = drawView.getDrawingCache();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            byte[] byte_array = bytes.toByteArray();
            String base64Image = Base64.encodeToString(byte_array, Base64.DEFAULT);

            Chat chat = new Chat(base64Image, username);

            FB.push().setValue(chat);
            drawView.destroyDrawingCache();

            drawView.startNew();

        }

    }


    public void savePic(){
        drawView.setDrawingCacheEnabled(true);


        Bitmap bitmap = drawView.getDrawingCache();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                UUID.randomUUID().toString() + ".png", "drawing");



        Log.d("IMGSAVED@@@@@@@: ", imgSaved);
        //UploadTask uploadTask = storageRef
        if (imgSaved != null) {
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
            savedToast.show();
        } else {
            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
            unsavedToast.show();
        }

        drawView.destroyDrawingCache();
    }



}

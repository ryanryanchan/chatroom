package com.ryanchan.chatroom;

import android.app.ListActivity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;



public class MainActivity extends ListActivity {

    //private static final String url = "https://chat-bbfbf.firebaseio.com/";
    private static final String username = "AndroidStudio";
    Firebase FB = new Firebase("https://chat-bbfbf.firebaseio.com/chat");
    private ChatListAdapter mChatListAdapter;
    private ValueEventListener mConnectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        EditText input = (EditText) findViewById(R.id.editText);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == event.ACTION_DOWN) {
                    send_message();
                }
                return true;
            }
        });


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_message();
            }
        });



    }

    @Override
    public void onStart(){
        super.onStart();

        final ListView listView = getListView();

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
                    Toast.makeText(MainActivity.this, "CONNECTED TO THIS SHIT", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
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



    private void send_message() {
        EditText inputText = (EditText) findViewById(R.id.editText);
        String input = inputText.getText().toString();
        if(!input.equals("")) {
            Chat chat = new Chat(input, username);

            FB.push().setValue(chat);
            inputText.setText("");
        }
    }






}

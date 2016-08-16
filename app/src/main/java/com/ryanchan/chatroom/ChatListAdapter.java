package com.ryanchan.chatroom;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Query;

import org.w3c.dom.Text;

/**
 * Created by ryanchan on 7/18/16.
 */
public class ChatListAdapter extends FirebaseListAdapter<Chat> {

    private String mUsername;

    public ChatListAdapter(Query ref, Activity activity, int layout, String mUsername) {
        super(ref, Chat.class, layout, activity);
        this.mUsername  = mUsername;
    }

    @Override
    protected void populateView(View view, Chat chat) {
        String author = chat.getAuthor();
        TextView authorText = (TextView) view.findViewById(R.id.author);
        TextView messageText = (TextView) view.findViewById(R.id.message);
        authorText.setText(author + ": ");
        ImageView image = (ImageView) view.findViewById(R.id.image);
        if (author != null && author.equals(mUsername)) {
            authorText.setTextColor(Color.RED);
        } else {
            authorText.setTextColor(Color.BLUE);
        }
        if(chat.getMessage().length() < 500) { //TEMPORARY
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(chat.getMessage());
            image.setVisibility(View.GONE);

        }
        else {
            image.setVisibility(View.VISIBLE);
            messageText.setVisibility(View.GONE);
            byte[] data = Base64.decode(chat.getMessage(), Base64.DEFAULT);
            Bitmap pictureMap = BitmapFactory.decodeByteArray(data, 0, data.length);
            image.setImageBitmap(pictureMap);
        }
    }
}

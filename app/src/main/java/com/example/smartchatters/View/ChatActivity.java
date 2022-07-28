package com.example.smartchatters.View;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartchatters.Model.ChatActivityModel;
import com.example.smartchatters.R;
import com.example.smartchatters.logic.MultimediaFile;
import com.example.smartchatters.logic.ProfileName;
import com.example.smartchatters.logic.Singleton;
import com.example.smartchatters.logic.Usernode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String chatName;
    private Usernode user;
    private static final ChatActivityModel cAM= new ChatActivityModel();
    private final int chooseFinal = 1001;
    private Intent myFileIntent;
    private static int id=0;
    private static boolean backPressed;

    @Override
    public void onBackPressed() {
        backPressed=true;
        //System.out.println("Cancelled thread");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (String topic : Singleton.getInstance().getNeedRestart().keySet()) {
            if (topic.equals(chatName)) {
                user.startConsuming(chatName,Singleton.getInstance().getCounter(chatName));
                Singleton.getInstance().getNeedRestart().remove(chatName);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Singleton singleton=Singleton.getInstance();
        user=singleton.getUser();
        setContentView(R.layout.activity_chat);
        ImageButton ib = (ImageButton) findViewById(R.id.sendMessageButton);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText=(EditText) findViewById(R.id.typeMessageChat);
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = dateFormat.format(date);
                user.publish(chatName,editText.getText().toString(),strDate,null);
                editText.getText().clear();
            }
        });
        ImageButton attach_file_btn = findViewById(R.id.attachFileButton);
        attach_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent,chooseFinal);
            }
        });
        Toolbar chatToolbar=(Toolbar)findViewById(R.id.appToolbar);
        chatToolbar.setTitle("");
        TextView chatToolbarTitle=(TextView) findViewById(R.id.chatToolbarTitle);
        chatName=(String)getIntent().getSerializableExtra("chatName");
        chatToolbarTitle.setText(chatName);
        setSupportActionBar(chatToolbar);
        recyclerView=findViewById(R.id.recyclerViewChatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (cAM.getMessageAdapter(chatName)==null) {
            cAM.setMessageAdapter(chatName,new MessageAdapter(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),new LinkedList<>()));
            cAM.setIndex(chatName,0);
        }


        recyclerView.setAdapter(cAM.getMessageAdapter(chatName));
        backPressed=false;

        MessageAdapter helper = cAM.getMessageAdapter(chatName);
        BackGroundStart(helper);
//        thread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==chooseFinal) {
            if (resultCode == -1) {
                if (data.getData()!=null) {
                    String path = getPath(data.getData());
//                    String file = path[1].substring(0, path[1].indexOf("."));
//                    String extension = path[1].substring(path[1].indexOf(".") + 1);
//                    System.out.println("Path " + path);
//                    System.out.println("File " + file);
                    String file = path.substring(0,path.lastIndexOf("."));
                    String extension = path.substring(path.lastIndexOf(".")+1);
                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                    String strDate = dateFormat.format(date);
                    user.publish(chatName, file, strDate, extension);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    public  String getPath( final Uri uri) {
        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        Context context = this;
        // DocumentProvider
        if (isKitKat ) {
            // ExternalStorageProvider

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }


            // DownloadsProvider

            if (isDownloadsDocument(uri)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    }
                    finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));


                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }


                    }
                }
                else {
                    final String id = DocumentsContract.getDocumentId(uri);

                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    Uri contentUri = null;
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {

                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            }


            // MediaProvider
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri);
            }

            if(isWhatsAppFile(uri)){
                return getFilePathForWhatsApp(uri);
            }


            if ("content".equalsIgnoreCase(uri.getScheme())) {

                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri);
                }
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {

                    // return getFilePathFromURI(context,uri);
                    return copyFileToInternalStorage(uri,"userfiles");
                    // return getRealPathFromURI(context,uri);
                }
                else
                {
                    return getDataColumn(context, uri, null, null);
                }

            }
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        else {

            if(isWhatsAppFile(uri)){
                return getFilePathForWhatsApp(uri);
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {
                        MediaStore.Images.Media.DATA
                };
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }




        return null;
    }

    private  boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }

    private String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private String getDriveFilePath(Uri uri) {
        Uri returnUri = uri;
        Context context = this;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private String copyFileToInternalStorage(Uri uri,String newDirName) {
        Uri returnUri = uri;

        Context context = this;
        Cursor returnCursor = context.getContentResolver().query(returnUri, new String[]{
                OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE
        }, null, null, null);


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if(!newDirName.equals("")) {
            File dir = new File(context.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(context.getFilesDir() + "/" + newDirName + "/" + name);
        }
        else{
            output = new File(context.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();

        }
        catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        return output.getPath();
    }

    private String getFilePathForWhatsApp(Uri uri){
        return  copyFileToInternalStorage(uri,"whatsapp");
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private  boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private  boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private  boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private  boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public boolean isWhatsAppFile(Uri uri){
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }

    private  boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }




    private void BackGroundStart(MessageAdapter helper) {
        Singleton.getInstance().getExecutorService().execute(new Runnable() {
            public void run() {
                MultimediaFile currentItem=null;
                while (!backPressed) {
                    if (user.getMultimediaFiles(chatName) == null) {
                        //System.out.println("User's multimedia files list is null. Retrying...");
                        continue;
                    }
                    if (cAM.getIndex(chatName) < user.getMultimediaFiles(chatName).size()) {
                        currentItem = user.getMultimediaFiles(chatName).get(cAM.getIndex(chatName));
                        if (currentItem != null) {
                            if (currentItem.getExtension() != null) {
                                String path = Environment.getExternalStorageDirectory().getPath();
                                path = path + "/Download/";
                                if (currentItem.getExtension().equals("mkv") || currentItem.getExtension().equals("mp4") || currentItem.getExtension().equals("avi")) {
                                    if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
                                        helper.addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 6, currentItem.getDate(), currentItem.getProfileName().getUsername());
                                    else
                                        helper.addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 3, currentItem.getDate(), currentItem.getProfileName().getUsername());
                                } else {
                                    if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
                                        helper.addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 5, currentItem.getDate(), currentItem.getProfileName().getUsername());
                                    else
                                        helper.addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 2, currentItem.getDate(), currentItem.getProfileName().getUsername());
                                }


                            } else {
                                if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
                                    helper.addItem(currentItem.getName(), 4, currentItem.getDate(), currentItem.getProfileName().getUsername());
                                else
                                    helper.addItem(currentItem.getName(), 1, currentItem.getDate(), currentItem.getProfileName().getUsername());

                            }
                            cAM.setIndex(chatName, cAM.getIndex(chatName) + 1);
                        }
                    }
                }
            }
        });
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        private MessageAdapter adapter;
        private TextView textView;
        private ImageView imageView;
        private ImageButton imageButton;
        private TextView personalTextView;
        private ImageView personalImageView;
        private ImageButton personalImageButton;
        private TextView textViewChatItemMessageTimestamp;
        private TextView imageViewChatItemPictureMessageTimestamp;
        private TextView chatItemMessageVideoImageButtonTimestamp;
        private TextView personalChatItemStringMessageTimestamp;
        private TextView personalChatItemPictureMessageTimestamp;
        private TextView personalChatItemMessageVideoImageButtonTimestamp;
        private TextView textViewChatItemMessageName;
        private TextView imageViewChatItemPictureMessageName;
        private TextView chatItemMessageVideoImageButtonName;
        private TextView personalChatItemStringMessageName;
        private TextView personalChatItemPictureMessageName;
        private TextView personalChatItemMessageVideoImageButtonName;

        public MessageHolder(@NonNull View itemView) {
            super(itemView);

            textView=(TextView) itemView.findViewById(R.id.textViewChatItemMessage);
            imageView=(ImageView)itemView.findViewById(R.id.imageViewChatItemPictureMessage);
            imageButton=(ImageButton)itemView.findViewById(R.id.chatItemMessageVideoImageButton);
            personalTextView=(TextView) itemView.findViewById(R.id.personalChatItemStringMessage);
            personalImageView=(ImageView)itemView.findViewById(R.id.personalChatItemPictureMessage);
            personalImageButton=(ImageButton)itemView.findViewById(R.id.personalChatItemMessageVideoImageButton);
            textViewChatItemMessageTimestamp=(TextView)itemView.findViewById(R.id.textViewChatItemMessageTimestamp);
            imageViewChatItemPictureMessageTimestamp=(TextView)itemView.findViewById(R.id.imageViewChatItemPictureMessageTimestamp);
            chatItemMessageVideoImageButtonTimestamp=(TextView) itemView.findViewById(R.id.chatItemMessageVideoImageButtonTimestamp);
            personalChatItemStringMessageTimestamp=(TextView) itemView.findViewById(R.id.personalChatItemStringMessageTimestamp);
            personalChatItemPictureMessageTimestamp=(TextView)itemView.findViewById(R.id.personalChatItemPictureMessageTimestamp);
            personalChatItemMessageVideoImageButtonTimestamp=(TextView)itemView.findViewById(R.id.personalChatItemMessageVideoImageButtonTimestamp);
            textViewChatItemMessageName=(TextView)itemView.findViewById(R.id.textViewChatItemMessageName);
            imageViewChatItemPictureMessageName=(TextView)itemView.findViewById(R.id.imageViewChatItemPictureMessageName);
            chatItemMessageVideoImageButtonName=(TextView) itemView.findViewById(R.id.chatItemMessageVideoImageButtonName);
            personalChatItemStringMessageName=(TextView) itemView.findViewById(R.id.personalChatItemStringMessageName);
            personalChatItemPictureMessageName=(TextView)itemView.findViewById(R.id.personalChatItemPictureMessageName);
            personalChatItemMessageVideoImageButtonName=(TextView)itemView.findViewById(R.id.personalChatItemMessageVideoImageButtonName);

        }

        public MessageHolder linkAdapter(MessageAdapter adapter) {
            this.adapter = adapter;
            return this;
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageHolder>{

        List<String> items;
        List<Integer> allItemsTypes;
        List<String> dates;
        List<String> usernames;

        public MessageAdapter(List<String> items, List<Integer> itemsTypes,List<String> dates,List<String> usernames){
            this.items=new ArrayList<>(items);;
            this.allItemsTypes=new ArrayList<>(itemsTypes);
            this.dates=new ArrayList<>(dates);
            this.usernames=new ArrayList<>(usernames);
        }

        @NonNull
        @Override
        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType==1){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_string_message,parent,false);
                return new MessageHolder(view);
            }
            else if (viewType==2){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_picture_message,parent,false);
                return new MessageHolder(view);
            }
            else if (viewType==3){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_video_message,parent,false);
                return new MessageHolder(view);
            }
            else if (viewType==4){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_chat_item_string_message,parent,false);
                return new MessageHolder(view);
            }
            else if (viewType==5){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_chat_item_picture_message,parent,false);
                return new MessageHolder(view);
            }
            else if (viewType==6){
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_chat_item_video_message,parent,false);
                return new MessageHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
            int type=getItemViewType(position);
            if (type==1){
                holder.textView.setText(items.get(position));
                holder.textViewChatItemMessageTimestamp.setText(dates.get(position));
                holder.textViewChatItemMessageName.setText(usernames.get(position));
            }
            else if (type==2){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                final Bitmap b = BitmapFactory.decodeFile(items.get(position), options);
                holder.imageView.getLayoutParams().height = b.getHeight();
                holder.imageView.getLayoutParams().width = b.getWidth();
                holder.imageView.requestLayout();
                holder.imageView.setImageBitmap(b);
                holder.imageViewChatItemPictureMessageTimestamp.setText(dates.get(position));
                holder.imageViewChatItemPictureMessageName.setText(usernames.get(position));
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),ImageViewerActivity.class);
                        intent.putExtra("imagePath",items.get(holder.getAdapterPosition()));
                        startActivity(intent);
                    }
                });
            }
            else if (type==3){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                final Bitmap b = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.play_video));
                holder.imageButton.setImageBitmap(b);
                ImageButton ib=(ImageButton)holder.itemView.findViewById(R.id.chatItemMessageVideoImageButton);
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                        intent.putExtra("videoPath",items.get(holder.getAdapterPosition()));
                        startActivity(intent);
                    }
                });
                holder.chatItemMessageVideoImageButtonTimestamp.setText(dates.get(position));
                holder.chatItemMessageVideoImageButtonName.setText(usernames.get(position));

            }
            else if (type==4){
                holder.personalTextView.setText(items.get(position));
                holder.personalChatItemStringMessageTimestamp.setText(dates.get(position));
                holder.personalChatItemStringMessageName.setText(usernames.get(position));
            }
            else if (type==5){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                final Bitmap b = BitmapFactory.decodeFile(items.get(position), options);
                holder.personalImageView.getLayoutParams().height = b.getHeight();
                holder.personalImageView.getLayoutParams().width = b.getWidth();
                holder.personalImageView.requestLayout();
                holder.personalImageView.setImageBitmap(b);
                holder.personalChatItemPictureMessageTimestamp.setText(dates.get(position));
                holder.personalChatItemPictureMessageName.setText(usernames.get(position));
                holder.personalImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),ImageViewerActivity.class);
                        intent.putExtra("imagePath",items.get(holder.getAdapterPosition()));
                        startActivity(intent);
                    }
                });
            }
            else if (type==6){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                final Bitmap b = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.play_video));
                holder.personalImageButton.setImageBitmap(b);
                ImageButton ib=(ImageButton)holder.itemView.findViewById(R.id.personalChatItemMessageVideoImageButton);
                ib.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                        intent.putExtra("videoPath",items.get(holder.getAdapterPosition()));
                        startActivity(intent);
                    }
                });
                holder.personalChatItemMessageVideoImageButtonTimestamp.setText(dates.get(position));
                holder.personalChatItemMessageVideoImageButtonName.setText(usernames.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return allItemsTypes.get(position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void addItem(String item, Integer itemType, String date, String username) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    items.add(item);
                    allItemsTypes.add(itemType);
                    dates.add(date);
                    usernames.add(username);
                    notifyItemInserted(items.size() - 1);
                    recyclerView.smoothScrollToPosition(items.size()-1);
                }
            });
        }



//        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
//        protected class MessagesLoader extends AsyncTask<Void,Void,Void > {
//
//            private int tid;
//
//            MessagesLoader(){
//                tid=id++;
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//
//                MultimediaFile currentItem=null;
//                    while (!backPressed) {
//                        if (user.getMultimediaFiles(chatName)==null) {
//                            //System.out.println("User's multimedia files list is null. Retrying...");
//                            continue;
//                        }
//                        if  (cAM.getIndex(chatName) < user.getMultimediaFiles(chatName).size()) {
//                            currentItem = user.getMultimediaFiles(chatName).get(cAM.getIndex(chatName));
//                            if (currentItem.getExtension() != null) {
//                                String path = Environment.getExternalStorageDirectory().getPath();
//                                path = path + "/Download/";
//                                if (currentItem.getExtension().equals("mkv") || currentItem.getExtension().equals("mp4") || currentItem.getExtension().equals("avi")) {
//                                    if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
//                                        addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 6,currentItem.getDate(),currentItem.getProfileName().getUsername());
//                                    else
//                                        addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 3,currentItem.getDate(),currentItem.getProfileName().getUsername());
//                                } else {
//                                    if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
//                                        addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 5,currentItem.getDate(),currentItem.getProfileName().getUsername());
//                                    else
//                                        addItem(path + chatName + currentItem.getFileId() + "." + currentItem.getExtension(), 2,currentItem.getDate(),currentItem.getProfileName().getUsername());
//                                }
//
//
//                            } else {
//                                if (currentItem.getProfileName().getUsername().equals(user.getUsername()))
//                                    addItem(currentItem.getName(), 4,currentItem.getDate(),currentItem.getProfileName().getUsername());
//                                else addItem(currentItem.getName(), 1,currentItem.getDate(),currentItem.getProfileName().getUsername());
//
//                            }
//                            cAM.setIndex(chatName, cAM.getIndex(chatName) + 1);
//                        }
//                    }
//                    return null;
//            }
//        }
    }

}

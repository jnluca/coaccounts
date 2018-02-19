package com.microsoft.onedrive.apiexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
//import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

//import com.microsoft.onedrive.onedriveapiexplorer.R;
import com.onedrive.sdk.concurrency.AsyncMonitor;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.concurrency.IProgressCallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.OneDriveErrorCodes;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.Item;
import com.onedrive.sdk.extensions.ItemReference;
import com.onedrive.sdk.extensions.OneDriveClient;
import com.onedrive.sdk.extensions.Permission;
import com.onedrive.sdk.options.Option;
import com.onedrive.sdk.options.QueryOption;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;

public class ComptesControl extends Activity implements ItemFragment.OnFragmentInteractionListener{
    RadioGroup rg;
    RadioButton rb;
    private EditText label;
    private EditText amount;
    private EditText comment;
    private TextView date;
    private DatePickerDialog.OnDateSetListener mDateSetListerner;
    private String[] fields = new String[7];
    private String jl_vl = "JL";
    static  String appel;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        }

        final BaseApplication application = (BaseApplication)getApplication();
        setContentView(R.layout.activity_comptes_control);
        application.goToWifiSettingsIfDisconnected();


        rg = (RadioGroup) findViewById(R.id.rgroup);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            public void onCheckedChanged(RadioGroup radioGroup, int checkedId ){
                rb = (RadioButton) findViewById(checkedId);
                jl_vl = (String) rb.getText();
            }
        });
        button = (Button) findViewById(R.id.query_vroom);

        label = (EditText) findViewById(R.id.label);
        amount = (EditText) findViewById(R.id.amount);
        date = (TextView) findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH); // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        getBaseContext(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListerner,
                        year, month, day);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.getWindow().setBackgroundDrawable((new ColorDrawable((Color.TRANSPARENT))));
                dialog.show();
            }
        });
        mDateSetListerner = new DatePickerDialog.OnDateSetListener () {
            public void onDateSet (DatePicker datePicker, int year, int month, int day){
                month = month + 1;
                date.setText(day + "/" + month + "/" + year);
                fields[0] = String.valueOf(day);
                fields[1] = String.valueOf(month);
                fields[2] = String.valueOf(year);
            }
        };

        comment = (EditText) findViewById(R.id.comment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                button.setEnabled(false);
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH); // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);
                int hour = now.get(Calendar.HOUR);
                int mn = now.get(Calendar.MINUTE);
                int sec = now.get(Calendar.SECOND);
                appel  = String.valueOf(year) + String.valueOf(month) + String.valueOf(day)
                        + String.valueOf(hour) + String.valueOf(mn) + String.valueOf(sec);
                final BaseApplication app = (BaseApplication)getApplication();
                final ICallback<Void> serviceCreated = new DefaultCallback<Void>(getApplicationContext()) {
                    @Override
                    public void success(final Void result) {
                        OneDriveClient oneDriveClient = (OneDriveClient) app.getOneDriveClient();
                        final DefaultCallback<Item> itemCallback = new DefaultCallback<Item>(getApplicationContext()) {
                            @Override
                            public void success(final Item item) {
                                navigateToRoot(item);
                                // button.setEnabled(true);
                            }
                        };
                        oneDriveClient
                                .getDrive()
                                .getItems("root:/")
                                .buildRequest()
                                .get(itemCallback);
                    }
                };
                try {
                    OneDriveClient oneDriveClient = (OneDriveClient) app.getOneDriveClient();
                    final DefaultCallback<Item> itemCallback = new DefaultCallback<Item>(getApplicationContext()) {
                        @Override
                        public void success(final Item item) {
                            navigateToRoot(item);
                           // button.setEnabled(true);
                        }
                    };
                    oneDriveClient
                            .getDrive()
                            .getItems("root:/")
                            .buildRequest()
                            .get(itemCallback);
                } catch (final UnsupportedOperationException ignored) {
                    app.createOneDriveClient(ComptesControl.this, serviceCreated);
                }
            }
        });
    }

    private void navigateToRoot(Item item) {

        fields[3] = label.getText().toString();
        fields[6] = comment.getText().toString();
        fields[4] = "";
        fields[5] = "";
        if(jl_vl.equals("JL")){
            fields[5] = amount.getText().toString();
        }
        if(jl_vl.equals("VL")){
            fields[4] = amount.getText().toString();
        }
        String[] fields2 = new String[7];
        for(int i = 0; i<fields2.length; i++){
            fields2[i]= fields[i];
        }
        Thread t = new Thread(new Treat_thread(item, fields2, appel, this));
        t.start();
        try {
            t.join();
            File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String path = externalStoragePublicDirectory.getAbsolutePath();
            File file = new File(path + "/");
            boolean deleted = file.delete();
            Toast.makeText(this, "C'est ok",
                    Toast.LENGTH_LONG).show();
            label.setText("");
            fields[3] = "";
            comment.setText("");
            fields[6] = "";
            amount.setText("");
            fields[4] = "";
            fields[5] = "";
            button.setEnabled(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFragmentInteraction(DisplayItem item) {

    }
}

class Treat_thread implements Runnable{
    private String[] fields = new String[7];
    private Activity activity;
    private Item item;
    private byte[] file_contents;
    String num;
    private int called = 0;

    /**
     * Expansion options to get all children, thumbnails of children, and thumbnails
     */
    private static final String EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS = "children(expand=thumbnails),thumbnails";

    /**
     * Expansion options to get all children, thumbnails of children, and thumbnails when limited
     */
    private static final String EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS_LIMITED = "children,thumbnails";


    Treat_thread(Item item, String[] fields, String appel, Activity activity){
        this.fields = fields;
        this.activity = activity;
        this.item = item;
        this.num = appel;

    }
    @Override
    public void run() {
        download(item);
    }

    private void upload(final byte[] file_contents) {
            final BaseApplication application = (BaseApplication) activity.getApplication();
            final IOneDriveClient oneDriveClient = application.getOneDriveClient();
            final AsyncTask<Void, Void, Void> uploadFile = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    try {
                        final String filename = "";
                        final Option option = new QueryOption("@name.conflictBehavior", "fail");
                        oneDriveClient
                                .getDrive()
                                .getItems("root")
                                .getChildren()
                                .byId(filename)
                                .getContent()
                                .buildRequest(Collections.singletonList(option))
                                .put(file_contents,
                                        new DefaultCallback<Item>(activity) {
                                            @Override
                                            public void success(final Item item) {

                                            }
                                            @Override
                                            public void failure(final ClientException error) {
                                                if (error.isError(OneDriveErrorCodes.NameAlreadyExists)) {

                                                } else {

                                                }
                                            }

                                        });
                    } catch (final Exception e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                        Log.e(getClass().getSimpleName(), e.toString());
                    }
                    return null;
                }
            };
            uploadFile.execute();
        }

    private void download(final Item item) {
        final DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context
                .DOWNLOAD_SERVICE);
        final String downloadUrl = item.getRawObject().get("@content.downloadUrl").getAsString();
        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                activity.unregisterReceiver(this);
                if(called == 0) {
                    try {
                        SharedPreferences downloadids = ctxt.getSharedPreferences("DownloadIDS", 0);
                        long id = downloadids.getLong("savedDownloadIds", 0);
                        ParcelFileDescriptor file = downloadManager.openDownloadedFile(id);
                        FileDescriptor fd = file.getFileDescriptor();
                        FileInputStream is = new FileInputStream(fd);
                        byte[] temp = new byte[10000000];
                        int nb = is.read(temp);
                        StringBuilder newline = new StringBuilder("");
                        for (int i = 0; i < 7; i++) {
                            newline.append(fields[i] + ";");
                        }
                        newline.append("\n");
                        int nb2 = new String(newline).getBytes().length;
                        file_contents = new byte[nb+nb2];
                        for(int i=0; i<nb; i++){
                            file_contents[i] = temp[i];
                        }
                        for(int i=0; i<nb2; i++){
                            file_contents[i+nb] = new String(newline).getBytes()[i];
                        }
                        temp = null;
                        newline = null;
                        called = 1;
                        copy(item);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

            HandlerThread handlerThread = new HandlerThread("ht");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);
            this.activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),"android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS",handler);

            final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle("");
            request.setDescription(activity.getString(R.string.file_from_onedrive));
            request.allowScanningByMediaScanner();
            if (item.file != null) {
                request.setMimeType("text/csv");
            }
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Long c = downloadManager.enqueue(request);
            SharedPreferences settings = activity.getSharedPreferences("DownloadIDS", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("savedDownloadIds", c);
            editor.commit();
    }

    /**
     * Copies an item onto the current destination in the copy preferences
     * @param item The item to copy
     */
    private void copy(final Item item) {
        final BaseApplication app = (BaseApplication) activity.getApplication();
        final IOneDriveClient oneDriveClient = app.getOneDriveClient();
        final ItemReference parentReference = new ItemReference();
        //parentReference.id = getCopyPrefs().getString(COPY_DESTINATION_PREF_KEY, null);
        parentReference.path = "/drive/items/root:/";

        final IProgressCallback<Item> progressCallback = new IProgressCallback<Item>() {
            @Override
            public void progress(final long current, final long max) {
            }

            @Override
            public void success(final Item item) {
                final String string = R.string.copy_success_message + item.name + item.parentReference.path;
                navigateByPath(item, "root:/");
            }

            @Override
            public void failure(final ClientException error) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.error_title)
                        .setMessage(error.getMessage())
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        };

        final DefaultCallback<AsyncMonitor<Item>> callback
                = new DefaultCallback<AsyncMonitor<Item>>(activity) {
            @Override
            public void success(final AsyncMonitor<Item> itemAsyncMonitor) {
                final int millisBetweenPoll = 1000;
                itemAsyncMonitor.pollForResult(millisBetweenPoll, progressCallback);
            }
        };
        oneDriveClient
                .getDrive()
                .getItems(item.id)
                .getCopy(num+item.name, parentReference)
                .buildRequest()
                .create(callback);
    }

    /**
     * Navigates to an item by path
     * @param item the source item
     */
    private void navigateByPath(final Item item, String path) {
        final BaseApplication application = (BaseApplication) activity.getApplication();
        final IOneDriveClient oneDriveClient = application.getOneDriveClient();

            final DefaultCallback<Item> itemCallback = new DefaultCallback<Item>(activity) {
                @Override
                public void success(final Item item) {
                    //final ItemFragment fragment = ItemFragment.newInstance(item.id);
                    //navigateToFragment(fragment);
                    deleteItem(item);
                }

            };
            oneDriveClient
                    .getDrive()
                    .getItems(path)
                    //.getItemWithPath(path)
                    .buildRequest()
                    .expand(getExpansionOptions(oneDriveClient))
                    .get(itemCallback);
    }

    /**
     * Deletes the item represented by this fragment
     * @param item The item to delete
     */
    private void deleteItem(final Item item) {
        final BaseApplication application = (BaseApplication) activity
                .getApplication();
        application.getOneDriveClient()
                .getDrive()
                .getItems(item.id)
                .buildRequest()
                .delete(new DefaultCallback<Void>(application) {
                    @Override
                    public void success(final Void response) {
                        upload(file_contents);
                    }
                });
    }

    private String getExpansionOptions(final IOneDriveClient oneDriveClient) {
        final String expansionOption;
        switch (oneDriveClient.getAuthenticator().getAccountInfo().getAccountType()) {
            case MicrosoftAccount:
                expansionOption = EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS;
                break;

            default:
                expansionOption = EXPAND_OPTIONS_FOR_CHILDREN_AND_THUMBNAILS_LIMITED;
                break;
        }
        return expansionOption;
    }

    }

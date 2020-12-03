package com.example.tapmove;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.*;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.tensorflow.lite.Interpreter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String MODEL_PATH = "model.tflite";
    public static final String OP_COPY = "copy";
    public static final String OP_MOVE = "move";
    public static final int BATCH_SIZE = 1;
    public static final int inputSize = 224;
    public static final int PIXEL_SIZE = 3;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    public static final int INPUT_FOLDERPICKER_CODE = 1234;
    public static final int STORAGE_PERMISSION_CODE = 1111;

    private Interpreter interpreter;
    private ImageView folderSelectButton;
    private EditText chosenFolder;
    private TextView messageView;
    private FloatingActionButton next;
    private ProgressDialog progress;

    private Map<String, File> inputFiles;
    private List<GalleryImage> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    STORAGE_PERMISSION_CODE);
        }

        inputFiles = new HashMap<String, File>();
        imageList = new ArrayList<GalleryImage>();

        folderSelectButton = findViewById(R.id.choose_button);
        chosenFolder = findViewById(R.id.chosen_folder);
        messageView = findViewById(R.id.message);
        next = findViewById(R.id.next);

        chosenFolder.requestFocus();

        try {
            interpreter = new Interpreter(loadModelFile(MainActivity.this));
            Log.e("INTERPRETER", "Interpreter is ready");
        } catch (IOException e) {
            Log.e("INTERPRETER", e.getMessage());
        }

        folderSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), INPUT_FOLDERPICKER_CODE);
            }
        });

        chosenFolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String path = chosenFolder.getText().toString();

                if (path.length() > 45)
                    chosenFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                else if (path.length() > 40)
                    chosenFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                else
                    chosenFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String path = chosenFolder.getText().toString();
                File folder = new File(path);
                inputFiles.clear();
                if (! folder.exists())
                {
                    Spannable word = new SpannableString("* Path is invalid");
                    word.setSpan(new ForegroundColorSpan(Color.RED), 0, word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    messageView.setText(word);
                }
                else
                {
                    if (folder.isDirectory()) {
                        try {
                            final File[] allFiles = folder.listFiles(new FilenameFilter() {
                                public boolean accept(File dir, String name) {
                                    return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
                                }
                            });
                            if (allFiles != null) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (allFiles.length > 0) {
                                            String noFiles = String.valueOf(allFiles.length);
                                            Spannable word = new SpannableString("This folder contains " + noFiles + " images");
                                            word.setSpan(new ForegroundColorSpan(Color.CYAN), 21, 21 + noFiles.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            messageView.setText(word);
                                        } else {
                                            Spannable word = new SpannableString("* This folder doesn't contain any images");
                                            word.setSpan(new ForegroundColorSpan(Color.RED), 0, word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            messageView.setText(word);
                                        }
                                    }
                                });

                                for (int i = 0; i < allFiles.length; i++) {
                                    File currentFile = allFiles[i];
                                    inputFiles.put(currentFile.getName(), currentFile);
                                }
                            } else
                                Log.e("FILES", "No files");
                        } catch (Exception e) {
                            Log.e("ERROR", e.getMessage());
                        }
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Spannable word = new SpannableString("* This is not a folder");
                                word.setSpan(new ForegroundColorSpan(Color.RED), 0, word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                messageView.setText(word);
                            }
                        });
                    }
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageView.getText().toString();
                if (msg.startsWith("This folder"))
                {
                    createAlertDialog().show();
                }
                else {
                    chosenFolder.requestFocus();
                    if (msg.equals(""))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Spannable word = new SpannableString("Enter a path");
                                word.setSpan(new ForegroundColorSpan(Color.RED), 0, word.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                messageView.setText(word);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        interpreter.close();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INPUT_FOLDERPICKER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    final String chosenFolderPath = Environment.getExternalStorageDirectory()+"/"+data.getData().getPath().substring(14);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chosenFolder.setText(chosenFolderPath);
                        }
                    });
                }
                else
                    Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class InferenceTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int i=0;
            String chosenFolderPath = chosenFolder.getText().toString();
            for (Map.Entry<String, File> entry : inputFiles.entrySet())
            {
                float[][] result = new float[1][1];
                Bitmap bitmap = BitmapFactory.decodeFile(entry.getValue().getPath());
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
                ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
                interpreter.run(byteBuffer, result);
                if (result[0][0] < 0.5)
                    imageList.add(new GalleryImage(chosenFolderPath, entry.getKey()));
                i++;
                publishProgress((int)i);
            }
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            Intent intent = new Intent(getActivity(), GalleryActivity.class);
            Bundle args = new Bundle();
            Log.e("TRANSFER", String.valueOf(imageList.size()));
            args.putSerializable("ARRAYLIST",(Serializable) imageList);
            intent.putExtra("BUNDLE",args);
            startActivity(intent);
            imageList.clear();
            inputFiles.clear();
            chosenFolder.setText("");
        }
        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(MainActivity.this, R.style.MyAlertDialogStyle);
            progress.setMessage("Running...");
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setMax(inputFiles.size());
            progress.show();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            final int progressValue = values[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setProgress(progressValue);
                }
            });
        }
    }

    private AlertDialog createAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Do you wish to Proceed ?")
                .setMessage("This folder contains " + inputFiles.size() + " Images")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new InferenceTask().execute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize * bitmap.getWidth()];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return byteBuffer;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    }

    public Activity getActivity()
    {
        return MainActivity.this;
    }

    private String getModelPath()
    {
        return MODEL_PATH;
    }
}

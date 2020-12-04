package com.example.tapmove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView foundImagesView;
    private TextView noSelectedView;
    private EditText targetFolder;
    private ImageView targetFolderButton;
    private CheckBox selectAll;

    GalleryAdapter galleryAdapter;
    ArrayList<GalleryImage> imageList;
    public static final String DEFAULT_TARGET_PATH = "/storage/emulated/0/Pictures/Notes";
    public static final int TARGET_FOLDERPICKER_CODE = 4321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_gallery);

        recyclerView = (RecyclerView)findViewById(R.id.gallery);
        recyclerView.setHasFixedSize(true);
        foundImagesView = findViewById(R.id.found);
        noSelectedView = findViewById(R.id.no_selected);
        targetFolder = findViewById(R.id.target_folder);
        targetFolderButton = findViewById(R.id.target_button);
        selectAll = findViewById(R.id.select_all);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        this.imageList = (ArrayList<GalleryImage>) args.getSerializable("ARRAYLIST");

        Log.e("COUNT", "Total images: "+this.imageList.size());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Spannable spanText = new SpannableString("Found "+imageList.size()+" images");
                spanText.setSpan(new ForegroundColorSpan(Color.CYAN), 6, 6+String.valueOf(imageList.size()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                foundImagesView.setText(spanText);
                int noSelected = 0;
                spanText = new SpannableString(noSelected+" selected");
                spanText.setSpan(new ForegroundColorSpan(Color.CYAN), 0, 0+String.valueOf(noSelected).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                noSelectedView.setText(spanText);
                targetFolder.setText(DEFAULT_TARGET_PATH);
            }
        });

        CheckBoxSelect checkBoxSelect = new CheckBoxSelect() {
            @Override
            public void updateNoSelected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int count = 0;
                        for (GalleryImage galleryImage : imageList)
                        {
                            if (galleryImage.isSelected())
                                count++;
                        }
                        Spannable spanText = new SpannableString(count+" selected");
                        spanText.setSpan(new ForegroundColorSpan(Color.CYAN), 0, 0+String.valueOf(count).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        noSelectedView.setText(spanText);
                    }
                });
            }
        };

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);

        galleryAdapter = new GalleryAdapter(getApplicationContext(), this.imageList, checkBoxSelect);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 20, true));
        recyclerView.setAdapter(galleryAdapter);

        targetFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), TARGET_FOLDERPICKER_CODE);
            }
        });

        targetFolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String path = targetFolder.getText().toString();

                if (path.length() > 45)
                    targetFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                else if (path.length() > 40)
                    targetFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                else
                    targetFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String path = targetFolder.getText().toString();
                if (! path.startsWith("/storage/emulated/0/"))
                    targetFolder.setText(DEFAULT_TARGET_PATH);
            }
        });

        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        galleryAdapter.toggleAllCheckBoxes();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.imageList.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.imageList.size() > 0)
            getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TARGET_FOLDERPICKER_CODE)
        {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    File folder = new File(Environment.getExternalStorageDirectory(), data.getData().getPath().split("primary:")[1]);
                    final String targetFolderPath = folder.getPath();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            targetFolder.setText(targetFolderPath);
                        }
                    });
                } else
                    Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.copy_button: {
                int count = 0;
                for (GalleryImage galleryImage : this.imageList)
                {
                    if (galleryImage.isSelected()) {
                        this.copyFile(galleryImage.getParentPath(), galleryImage.getFileName(), false);
                        count++;
                    }
                }
                Toast.makeText(getApplicationContext(), String.valueOf(count) + " images are copied", Toast.LENGTH_LONG).show();
                finish();
                break;
            }

            case R.id.move_button:
            {
                int count = 0;
                for (GalleryImage galleryImage : this.imageList)
                {
                    if (galleryImage.isSelected()) {
                        this.copyFile(galleryImage.getParentPath(), galleryImage.getFileName(), true);
                        count++;
                    }
                }
                Toast.makeText(getApplicationContext(), String.valueOf(count) + " images are copied", Toast.LENGTH_LONG).show();
                finish();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyFile(String inputPath, String fileName, boolean move) {
        InputStream in = null;
        OutputStream out = null;
        String targetFolderPath = targetFolder.getText().toString();
        if (! inputPath.equals(targetFolderPath))
        {
            try {
                //create output directory if it doesn't exist
                File dir = new File(targetFolderPath);
                if (!dir.exists())
                {
                    dir.mkdirs();
                }

                in = new FileInputStream(inputPath+"/"+fileName);
                out = new FileOutputStream(targetFolderPath +"/"+ fileName);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                // write the output file
                out.flush();
                out.close();
                out = null;

                // delete the original file
                if (move) {
                    new File(inputPath + "/" + fileName).delete();
                }

            }
            catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
        }
    }

    public Context getActivity()
    {
        return GalleryActivity.this;
    }
}

interface CheckBoxSelect {
    public void updateNoSelected();
}

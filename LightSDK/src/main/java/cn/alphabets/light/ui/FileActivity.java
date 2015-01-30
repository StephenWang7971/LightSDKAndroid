package cn.alphabets.light.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cn.alphabets.light.R;
import cn.alphabets.light.application.ABActivity;
import cn.alphabets.light.model.GsonParser;
import cn.alphabets.light.model.ModelFile;
import cn.alphabets.light.network.Parameter;
import cn.alphabets.light.setting.Default;
import cn.alphabets.light.util.FileUtil;

/**
 * Created by sh on 15/1/28.
 */
public class FileActivity extends ABActivity {

    public static final String VALUE        = "value";
    public static final String VALUE_TITLE  = "value_title";
    public static final String LIST         = "list";
    public static final String TITLE        = "title";
    public static final String READONLY     = "readonly";
    public static final String THEME        = "theme";

    private Menu mMenu;
    private FileAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设定Theme，需要在setContentView之前调用
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int resourceId = extras.getInt(THEME, 0);
            if (resourceId != 0) {
                setTheme(resourceId);
            }
        }

        Helper.setNoIconBackActionBar(this, "Edit");
        setContentView(R.layout.activity_file);

        ArrayList<FileAdapter.FileItem> values = new ArrayList<FileAdapter.FileItem>();
        if (extras != null) {
            String title = extras.getString(TITLE);
            setTitle(title);

            if (extras.get(LIST) != null) {
                values.addAll((ArrayList<FileAdapter.FileItem>)extras.get(LIST));
            }
        }

        ListView fileList = (ListView) findViewById(R.id.file_list);
        mAdapter = new FileAdapter(getApplicationContext(), android.R.layout.simple_list_item_1);
        mAdapter.addAll(values);
        fileList.setAdapter(mAdapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileAdapter.FileItem item = mAdapter.getItem(i);
                if (item.file != null) {
                    FileUtil.openFile(FileActivity.this, item.file);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean isReadOnly = getIntent().getExtras().getBoolean(READONLY);
        if (!isReadOnly) {
            MenuItem item = menu.add(Menu.NONE, android.R.id.edit, 1, "Add");
            item.setIcon(R.drawable.tool_plus_white);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        this.mMenu = menu;

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // 返回
        if (id == android.R.id.home) {

            ArrayList<String> files = new ArrayList<>();
            ArrayList<String> titles = new ArrayList<>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                files.add(mAdapter.getItem(i).fileUrl);
                titles.add(mAdapter.getItem(i).fileName);
            }

            Intent data = new Intent();
            data.putStringArrayListExtra(VALUE, files);
            data.putStringArrayListExtra(VALUE_TITLE, titles);
            setResult(RESULT_OK, data);

            onBackPressed();
            return true;
        }

        // 添加
        if (id == android.R.id.edit) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)), 0);
            } catch (android.content.ActivityNotFoundException ex) {
                Dialog.toast(R.string.select_file_error);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            String path = FileUtil.getFileAbsolutePath(this, uri);
            if (path != null ) {
                final File file = new File(path);
                UPLOAD(Default.UrlSendFile, new Parameter().put(path, file), new Success() {
                    @Override
                    public void onResponse(JSONObject response) {

                        GsonParser<ModelFile> files = GsonParser.fromJson(response, ModelFile.getListTypeToken());
                        String fileId = files.getData().getItems().get(0).get_id();

                        mAdapter.add(new FileAdapter.FileItem(file.getName(), fileId, file));
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}

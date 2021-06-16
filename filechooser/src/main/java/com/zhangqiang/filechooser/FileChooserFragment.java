package com.zhangqiang.filechooser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.options.OnValueChangedListener;
import com.zhangqiang.options.Option;
import com.zhangqiang.options.Options;
import com.zhangqiang.options.store.shared.SharedValueStore;
import com.zhangqiang.permissionrequest.PermissionRequestHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileChooserFragment extends Fragment {


    private Option<String> currentPathOption;
    private RecyclerView recyclerView;
    private TextView tvTitle;
    private CellRVAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null) {
            currentPathOption = Options.ofString("last_string", null, new SharedValueStore(context, "download_config"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_chooser, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvTitle = view.findViewById(R.id.tv_title);
        mAdapter = new CellRVAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PermissionRequestHelper.requestPermission(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionRequestHelper.Callback() {
                    @Override
                    public void onPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
                        for (int i = 0; i < permissions.length; i++) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                                String lastPath = currentPathOption.get();
                                if (TextUtils.isEmpty(lastPath)) {
                                    lastPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                }
                                forward(lastPath);
                                updateCurrentPathView();
                            }
                        }
                    }
                });
        currentPathOption.addOnValueChangedListener(currentPathChangedListener);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentPathOption.removeOnValueChangedListener(currentPathChangedListener);
    }

    private void updateCurrentPathView() {

        tvTitle.setText(String.format(Locale.getDefault(),getResources().getString(R.string.current_path_is),currentPathOption.get()));
    }

    OnValueChangedListener currentPathChangedListener = new OnValueChangedListener() {
        @Override
        public void onValueChanged() {
            updateCurrentPathView();
        }
    };

    private List<Cell> makeCellList(String path) {

        List<Cell> cellList = new ArrayList<>();
        cellList.add(makeBackCell());
        File file = new File(path);
        File[] files = file.listFiles();
        List<File> fileList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (File child : files) {
                if (!child.isHidden()) {
                    fileList.add(child);
                }
            }
        }
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        for (int i = 0; i < fileList.size(); i++) {
            cellList.add(makeFileCell(fileList.get(i)));
        }
        return cellList;
    }

    private Cell makeBackCell() {
        return new MultiCell<>(R.layout.item_back, "", new ViewHolderBinder<String>() {
            @Override
            public void onBind(ViewHolder viewHolder, String s) {
                viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backward();
                    }
                });
            }
        });
    }

    private Cell makeFileCell(File file) {
        return new MultiCell<>(R.layout.item_file, file, new ViewHolderBinder<File>() {
            @Override
            public void onBind(ViewHolder viewHolder, final File file) {
                if (file.isFile()) {
                    viewHolder.setImageResource(R.id.iv_image, R.mipmap.file_chooser_icon_file);
                } else if (file.isDirectory()) {
                    viewHolder.setImageResource(R.id.iv_image, R.drawable.file_chooser_icon_dir);
                }
                Uri uri = Uri.fromFile(file);
                viewHolder.setText(R.id.tv_title, uri.getLastPathSegment());
                viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (file.isDirectory()) {
                            forward(file.getAbsolutePath());
                        }
                    }
                });
            }
        });
    }

    public boolean backward() {
        String path = currentPathOption.get();
        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(path)) {
            return false;
        }
        String parent = new File(path).getParent();
        if (TextUtils.isEmpty(parent)) {
            return false;
        }
        setCurrentPath(parent);
        return true;
    }

    public void forward(String path) {
        setCurrentPath(path);
    }

    private void setCurrentPath(String path) {
        currentPathOption.set(path);
        List<Cell> cellList = makeCellList(path);
        mAdapter.setDataList(cellList);
    }

    public String getCurrentPath() {
        return currentPathOption.get();
    }
}

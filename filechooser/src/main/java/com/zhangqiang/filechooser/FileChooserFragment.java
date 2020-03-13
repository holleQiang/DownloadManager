package com.zhangqiang.filechooser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.zhangqiang.keystore.OnValueChangedListener;
import com.zhangqiang.keystore.Option;
import com.zhangqiang.keystore.Options;
import com.zhangqiang.keystore.store.shared.SharedValueStore;
import com.zhangqiang.permissionrequest.PermissionRequestHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            currentPathOption = Options.ofString("last_string", null,new SharedValueStore(context,"download_config"));
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

        tvTitle.setText("当前路径:" + currentPathOption.get());
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
            File child = fileList.get(i);
            if (child.isDirectory()) {
                cellList.add(makeDirectoryCell(child.getAbsolutePath()));
            } else if (child.isFile()) {
                cellList.add(makeFileCell(child.getAbsolutePath()));
            }
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

    private Cell makeFileCell(String path) {
        return new MultiCell<>(R.layout.item_file, path, new ViewHolderBinder<String>() {
            @Override
            public void onBind(ViewHolder viewHolder, final String s) {
                Uri uri = Uri.fromFile(new File(s));
                viewHolder.setText(R.id.tv_title, uri.getLastPathSegment());
                viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        });
    }

    private Cell makeDirectoryCell(String directory) {
        return new MultiCell<>(R.layout.item_directory, directory, new ViewHolderBinder<String>() {
            @Override
            public void onBind(ViewHolder viewHolder, final String s) {
                Uri uri = Uri.fromFile(new File(s));
                viewHolder.setText(R.id.tv_title, uri.getLastPathSegment());
                viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forward(s);
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

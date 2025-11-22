package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.FirmShowAdapter;
import com.example.myapplication.models.FirmShow;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFirmService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivityManageFirm extends AppCompatActivity {
    String accessToken;
    private final int ADD_FIRM_REQUEST_CODE = 4; // Assuming this is the code for adding a firm
    private final int UPDATE_FIRM_REQUEST_CODE = 5; // Assuming this is the code for updating a firm
    private final int DELETE_FIRM_REQUEST_CODE = 6; // Assuming this is the code for deleting a firm

    ImageView imageHome, imageManageFirm, imageManageUser, imageManageRoom, imageUser;
    ImageView imageSearch;
    TextView textAppName;
    EditText editSearch;
    ImageView imageBack;

    FloatingActionButton fabAddFirm;
    RecyclerView recyclerViewFirm;
    FirmShowAdapter firmShowAdapter;
    ActivityResultLauncher<Intent> launcherDetailFirm;
    ActivityResultLauncher<Intent> launcherAddFirm;
    List<FirmShow> mListFirmShows;
    List<FirmShow> cacheFirmShows = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_acivity_manage_firm);
        setElementsByID();
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        setLauncherAddFirm();
        setLauncherDetailFirm();

        mListFirmShows = new ArrayList<>();
        // Set up the RecyclerView and Adapter
        firmShowAdapter = new FirmShowAdapter(mListFirmShows);

        recyclerViewFirm.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFirm.setAdapter(firmShowAdapter);

        // Load firms from API
        loadFirmsFromApi();

        // Set up click listeners for menu buttons
        listenMenuButtons();

        // Set up search functionality
        ListenerSetupSearchButton();


        // Set up click listener for firm items
        firmShowAdapter.setOnItemClickListener(new FirmShowAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FirmShow firmShow, int position) {
                Intent intent = new Intent(AdminActivityManageFirm.this, AdminDetailFirm.class);
                intent.putExtra("firm_id", firmShow.getId());
                intent.putExtra("position", position); // Pass the position for updates/deletes
                launcherDetailFirm.launch(intent);
            }
        });

    }

    void setElementsByID(){
        imageHome = findViewById(R.id.imageHome);
        imageManageFirm = findViewById(R.id.imageManageFirm);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        imageUser = findViewById(R.id.imageProfile);
        fabAddFirm = findViewById(R.id.buttonAddFirm);
        recyclerViewFirm = findViewById(R.id.firmShowsRecyclerView);
        imageSearch = findViewById(R.id.imageSearch);
        textAppName = findViewById(R.id.textAppName);
        editSearch = findViewById(R.id.editSearch);
        imageBack = findViewById(R.id.imageBack);
    }


    void listenMenuButtons() {
        imageHome.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFirm.this, AdminMainActivity.class);
            startActivity(intent);
        });


        imageManageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFirm.this, AdminActivityManageUser.class);
            startActivity(intent);
        });

        imageManageRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFirm.this, AdminActivityManageRoom.class);
            startActivity(intent);
        });

        imageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFirm.this, AdminActivityProfile.class);
            startActivity(intent);
        });

        ListenerAddFirm();
    }


    private void loadFirmsFromApi() {
        ApiFirmService apiFirmService = ApiClient.getRetrofit().create(ApiFirmService.class);
        Call<List<FirmShow>> call = apiFirmService.getAllFirms();

        call.enqueue(new Callback<List<FirmShow>>() {
            @Override
            public void onResponse(Call<List<FirmShow>> call, Response<List<FirmShow>> response) {
                try {
                    Log.e("API_RESPONSE", "Response code: " + response.message());
                    List<FirmShow> firmShows = response.body();
                    if (firmShows != null) {
                        cacheFirmShows = new ArrayList<>(firmShows);  // cache data
                        mListFirmShows.clear();
                        mListFirmShows.addAll(firmShows);
                        firmShowAdapter.notifyDataSetChanged();
                    } else {
                        throw new NullPointerException("Dữ liệu trả về là null");
                    }

                } catch (NullPointerException e) {
                    Log.e("API_ERROR", "Response body is null: " + e.getMessage());
                    Toast.makeText(AdminActivityManageFirm.this, "Lỗi: Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onFailure(Call<List<FirmShow>> call, Throwable t) {
                Toast.makeText(AdminActivityManageFirm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    void ListenerSetupSearchButton() {
        imageSearch.setOnClickListener(v -> {
            imageBack.setVisibility(View.VISIBLE);
            editSearch.setVisibility(View.VISIBLE);
            textAppName.setVisibility(View.GONE);
            editSearch.requestFocus();
        });

        imageBack.setOnClickListener(v -> {
            imageBack.setVisibility(View.GONE);
            editSearch.setVisibility(View.GONE);
            textAppName.setVisibility(View.VISIBLE);
            editSearch.setText("");

            // Quay về danh sách gốc
            mListFirmShows.clear();
            mListFirmShows.addAll(cacheFirmShows);
            firmShowAdapter.notifyDataSetChanged();

            // Ẩn bàn phím
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);

        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchFirms(s.toString()); // Tự động gọi tìm kiếm
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }

    private void searchFirms(String query) {
        Log.d("SearchFirms", "Searching for: " + query);
        List<FirmShow> filteredList = new ArrayList<>();
        if(cacheFirmShows != null) {
            for (FirmShow firm : cacheFirmShows) {
                if (firm.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(firm);
                }
            }
        }

        mListFirmShows.clear();
        mListFirmShows.addAll(filteredList);
        firmShowAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
        }
    }


//    Set up the ActivityResultLauncher for detail firm activity
    private void setLauncherDetailFirm(){
        launcherDetailFirm = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == UPDATE_FIRM_REQUEST_CODE) { // Assuming 5 is the code for successful update
                        Intent data = result.getData();
                        if (data != null) {
                            FirmShow updatedFirm = (FirmShow) data.getSerializableExtra("updated_firm");
                            if (updatedFirm != null) {
                                int position = data.getIntExtra("position", -1);
                                if (position >= 0 && position < mListFirmShows.size()) {
                                    mListFirmShows.set(position, updatedFirm);
                                    firmShowAdapter.notifyItemChanged(position);
                                    Toast.makeText(AdminActivityManageFirm.this, "Cập nhật phim thành công", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else if (result.getResultCode() == DELETE_FIRM_REQUEST_CODE) { // Assuming 6 is the code for successful deletion
                        Intent data = result.getData();
                        int position = data != null ? data.getIntExtra("position", -1) : -1;
                        String message = data != null ? data.getStringExtra("status") : null;
                        if (position >= 0 && position < mListFirmShows.size()) {
                            mListFirmShows.remove(position);
                            firmShowAdapter.notifyItemRemoved(position);
                            Toast.makeText(AdminActivityManageFirm.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    // Listener for adding a new firm
    private void setLauncherAddFirm(){
        launcherAddFirm = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == ADD_FIRM_REQUEST_CODE) { // Assuming 4 is the code for successful addition
                        Intent data = result.getData();
                        if (data != null) {
                            FirmShow newFirm = (FirmShow) data.getSerializableExtra("new_firm");
                            if (newFirm != null) {
                                mListFirmShows.add(newFirm);
                                firmShowAdapter.notifyItemInserted(mListFirmShows.size() - 1);
                                Toast.makeText(AdminActivityManageFirm.this, "Thêm phim mới thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void ListenerAddFirm(){
        fabAddFirm.setOnClickListener(v -> {
           Intent intent = new Intent(AdminActivityManageFirm.this, AdminActivityCreateNewFirm.class);
            launcherAddFirm.launch(intent);
        });
    }

}

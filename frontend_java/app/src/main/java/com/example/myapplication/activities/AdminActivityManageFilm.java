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
import com.example.myapplication.adapters.FilmShowAdapter;
import com.example.myapplication.models.FilmShow;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivityManageFilm extends AppCompatActivity {
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
    FilmShowAdapter filmShowAdapter;
    ActivityResultLauncher<Intent> launcherDetailFirm;
    ActivityResultLauncher<Intent> launcherAddFirm;
    List<FilmShow> mListFilmShows;
    List<FilmShow> cacheFilmShows = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_acivity_manage_firm);
        setElementsByID();
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);
        setLauncherAddFirm();
        setLauncherDetailFirm();

        mListFilmShows = new ArrayList<>();
        // Set up the RecyclerView and Adapter
        filmShowAdapter = new FilmShowAdapter(mListFilmShows);

        recyclerViewFirm.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFirm.setAdapter(filmShowAdapter);

        // Load firms from API
        loadFirmsFromApi();

        // Set up click listeners for menu buttons
        listenMenuButtons();

        // Set up search functionality
        ListenerSetupSearchButton();


        // Set up click listener for firm items
        filmShowAdapter.setOnItemClickListener(new FilmShowAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FilmShow filmShow, int position) {
                Intent intent = new Intent(AdminActivityManageFilm.this, AdminDetailFilm.class);
                intent.putExtra("firm_id", filmShow.getId());
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
            Intent intent = new Intent(AdminActivityManageFilm.this, AdminMainActivity.class);
            startActivity(intent);
        });


        imageManageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFilm.this, AdminActivityManageUser.class);
            startActivity(intent);
        });

        imageManageRoom.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFilm.this, AdminActivityManageRoom.class);
            startActivity(intent);
        });

        imageUser.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivityManageFilm.this, AdminActivityProfile.class);
            startActivity(intent);
        });

        ListenerAddFirm();
    }


    private void loadFirmsFromApi() {
        ApiFilmService apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
        Call<List<FilmShow>> call = apiFilmService.getAllFilms();

        call.enqueue(new Callback<List<FilmShow>>() {
            @Override
            public void onResponse(Call<List<FilmShow>> call, Response<List<FilmShow>> response) {
                try {
                    Log.e("API_RESPONSE", "Response code: " + response.message());
                    List<FilmShow> filmShows = response.body();
                    if (filmShows != null) {
                        cacheFilmShows = new ArrayList<>(filmShows);  // cache data
                        mListFilmShows.clear();
                        mListFilmShows.addAll(filmShows);
                        filmShowAdapter.notifyDataSetChanged();
                    } else {
                        throw new NullPointerException("Dữ liệu trả về là null");
                    }

                } catch (NullPointerException e) {
                    Log.e("API_ERROR", "Response body is null: " + e.getMessage());
                    Toast.makeText(AdminActivityManageFilm.this, "Lỗi: Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onFailure(Call<List<FilmShow>> call, Throwable t) {
                Toast.makeText(AdminActivityManageFilm.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            mListFilmShows.clear();
            mListFilmShows.addAll(cacheFilmShows);
            filmShowAdapter.notifyDataSetChanged();

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
        List<FilmShow> filteredList = new ArrayList<>();
        if(cacheFilmShows != null) {
            for (FilmShow firm : cacheFilmShows) {
                if (firm.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(firm);
                }
            }
        }

        mListFilmShows.clear();
        mListFilmShows.addAll(filteredList);
        filmShowAdapter.notifyDataSetChanged();

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
                            FilmShow updatedFirm = (FilmShow) data.getSerializableExtra("updated_firm");
                            if (updatedFirm != null) {
                                int position = data.getIntExtra("position", -1);
                                if (position >= 0 && position < mListFilmShows.size()) {
                                    mListFilmShows.set(position, updatedFirm);
                                    filmShowAdapter.notifyItemChanged(position);
                                    Toast.makeText(AdminActivityManageFilm.this, "Cập nhật phim thành công", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else if (result.getResultCode() == DELETE_FIRM_REQUEST_CODE) { // Assuming 6 is the code for successful deletion
                        Intent data = result.getData();
                        int position = data != null ? data.getIntExtra("position", -1) : -1;
                        String message = data != null ? data.getStringExtra("status") : null;
                        if (position >= 0 && position < mListFilmShows.size()) {
                            mListFilmShows.remove(position);
                            filmShowAdapter.notifyItemRemoved(position);
                            Toast.makeText(AdminActivityManageFilm.this, message, Toast.LENGTH_SHORT).show();
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
                            FilmShow newFirm = (FilmShow) data.getSerializableExtra("new_firm");
                            if (newFirm != null) {
                                mListFilmShows.add(newFirm);
                                filmShowAdapter.notifyItemInserted(mListFilmShows.size() - 1);
                                Toast.makeText(AdminActivityManageFilm.this, "Thêm phim mới thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    private void ListenerAddFirm(){
        fabAddFirm.setOnClickListener(v -> {
           Intent intent = new Intent(AdminActivityManageFilm.this, AdminActivityCreateNewFirm.class);
            launcherAddFirm.launch(intent);
        });
    }

}

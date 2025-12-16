package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.FilmShowAdapter;
import com.example.myapplication.models.FilmIdBroadcastToday;
import com.example.myapplication.models.FilmShow;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiFilmService;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserMainActivity extends AppCompatActivity {

    String accessToken;
    private RecyclerView FilmShowsRecyclerView;
    private List <FilmShow> mListFilmShows;
    private FilmShowAdapter filmShowAdapter;
    ImageView imageHome;
    ImageView imageHistory;
    ImageView imageUser;

    List <FilmShow> cacheFilmShows;
    ImageView imageSearch;
    TextView textAppName;
    EditText editSearch;
    ImageView imageBack;
    MaterialAutoCompleteTextView spinnerSort;
    FilmIdBroadcastToday filmIdBroadcastToday = new FilmIdBroadcastToday(new ArrayList<>());


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_main);
        imageHome = findViewById(R.id.imageHome);
        imageHistory = findViewById(R.id.imageHistory);
        imageUser = findViewById(R.id.imageUser);
        imageSearch = findViewById(R.id.imageSearch);
        textAppName = findViewById(R.id.textAppName);
        editSearch = findViewById(R.id.editSearch);
        imageBack = findViewById(R.id.imageBack);
        spinnerSort = findViewById(R.id.spinnerSort);

        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);

        FilmShowsRecyclerView = findViewById(R.id.filmShowsRecyclerView);
        FilmShowsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mListFilmShows = new ArrayList<>();
        filmShowAdapter = new FilmShowAdapter(mListFilmShows);

        FilmShowsRecyclerView.setAdapter(filmShowAdapter);
        loadFilmsFromApi();
        loadFilmIdsBroadcastToday();

//        thiết  lập sự kiện adapter
        filmShowAdapter.setOnItemClickListener(new FilmShowAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FilmShow filmShow, int position) {
                Intent intent = new Intent(UserMainActivity.this, UserDetailFilm.class);
                intent.putExtra("film_id", filmShow.getId());
                startActivity(intent);
            }
        });

        setAdapterSpiner();
//        ListenerSetupMenuButton();
        ListenerSetupMenuButton();

//        ListenerSetupSearchButton();
        ListenerSetupSearchButton();
        ListenerSpinnerClick();

    }
    private void loadFilmsFromApi() {
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
                    Toast.makeText(UserMainActivity.this, "Lỗi: Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onFailure(Call<List<FilmShow>> call, Throwable t) {
                Toast.makeText(UserMainActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    private void loadFilmIdsBroadcastToday() {
        ApiFilmService apiFilmService = ApiClient.getRetrofit().create(ApiFilmService.class);
        Call<FilmIdBroadcastToday> call = apiFilmService.getFilmIdsBroadcastToday("Bearer " + accessToken);

        call.enqueue(new Callback<FilmIdBroadcastToday>() {
            @Override
            public void onResponse(Call<FilmIdBroadcastToday> call, Response<FilmIdBroadcastToday> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filmIdBroadcastToday = response.body();
                    List<Integer> filmIds = filmIdBroadcastToday.getFilmIds();
                    Log.d("UserMainActivity", "Film IDs for today: " + filmIds);

                } else {
                    Log.e("UserMainActivity List id", "Failed to get film IDs for today");
                }
            }

            @Override
            public void onFailure(Call<FilmIdBroadcastToday> call, Throwable t) {
                Log.e("UserMainActivity", "Error: " + t.getMessage());
            }
        });
    }


    void  ListenerSetupMenuButton() {
        imageHistory.setOnClickListener(v -> {
            Intent intent = new Intent(UserMainActivity.this, UserActivityHistoryBookingTicket.class);
            startActivity(intent);
        });
        imageUser.setOnClickListener(v -> {
            Intent intent = new Intent(UserMainActivity.this, UserActivityProfile.class);
            startActivity(intent);
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
                searchFilms(s.toString()); // Tự động gọi tìm kiếm
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

    }

    private void searchFilms(String query) {
        Log.d("Searchls", "Searching for: " + query);
        List<FilmShow> filteredList = new ArrayList<>();
        if(cacheFilmShows != null) {
            for (FilmShow film : cacheFilmShows) {
                if (film.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(film);
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


    void setAdapterSpiner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                R.layout.spinner_dropdown_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        // Optional: set listener
        spinnerSort.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            // Xử lý lọc, sắp xếp ở đây
            Toast.makeText(this, "Bạn chọn: " + selected, Toast.LENGTH_SHORT).show();
        });
    }


    void ListenerSpinnerClick(){
        spinnerSort.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOption = (String) parent.getItemAtPosition(position);

            switch (selectedOption) {
                case "Lịch chiếu hôm nay":
                    // Gọi hàm load phim hôm nay
                    loadTodayShows();
                    break;

                case "Sắp xếp theo Rating ↑":
                    // Gọi hàm sort tăng dần rating
                    sortByRatingAsc();
                    break;

                case "Sắp xếp theo Rating ↓":
                    // Gọi hàm sort giảm dần rating
                    sortByRatingDesc();
                    break;

                case "Sắp xếp theo Tên A-Z":
                    // Gọi hàm sort theo tên
                    sortByName();
                    break;
            }

            // Optional: Toast test
            Toast.makeText(this, "Bạn chọn: " + selectedOption, Toast.LENGTH_SHORT).show();
        });
    }

    void  loadTodayShows() {
        List<FilmShow> todayShows = new ArrayList<>();
        for (FilmShow film : cacheFilmShows) {
            if (filmIdBroadcastToday.getFilmIds().contains(film.getId())) {
                todayShows.add(film);
            }
        }
        mListFilmShows.clear();
        mListFilmShows.addAll(todayShows);
        filmShowAdapter.notifyDataSetChanged();
    }
    void sortByRatingAsc() {
        mListFilmShows.clear();
        mListFilmShows.addAll(cacheFilmShows);
        mListFilmShows.sort((film1, film2) -> Double.compare(film1.getRating(), film2.getRating()));
        filmShowAdapter.notifyDataSetChanged();
    }
    void sortByRatingDesc() {
        mListFilmShows.clear();
        mListFilmShows.addAll(cacheFilmShows);
        mListFilmShows.sort((film1, film2) -> Double.compare(film2.getRating(), film1.getRating()));
        filmShowAdapter.notifyDataSetChanged();
    }
    void sortByName() {
        mListFilmShows.clear();
        mListFilmShows.addAll(cacheFilmShows);
        mListFilmShows.sort((filmShow, filmShow1) -> filmShow.getName().compareToIgnoreCase(filmShow1.getName()));
        filmShowAdapter.notifyDataSetChanged();
    }

}

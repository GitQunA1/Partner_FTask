package com.example.partner_ftask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.District;
import com.example.partner_ftask.data.model.UpdateDistrictsRequest;
import com.example.partner_ftask.data.model.UpdateUserInfoRequest;
import com.example.partner_ftask.data.model.UserInfoResponse;
import com.example.partner_ftask.ui.activity.PhoneLoginActivity;
import com.example.partner_ftask.ui.adapter.DistrictSelectionAdapter;
import com.example.partner_ftask.ui.fragment.JobsFragment;
import com.example.partner_ftask.ui.fragment.MyJobsFragment;
import com.example.partner_ftask.ui.fragment.NotificationFragment;
import com.example.partner_ftask.ui.fragment.ProfileFragment;
import com.example.partner_ftask.utils.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private PreferenceManager preferenceManager;
    private ApiService apiService;
    private boolean isCheckingUserInfo = false;
    private boolean isShowingDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiClient.init(this);
        preferenceManager = new PreferenceManager(this);
        apiService = ApiClient.getApiService();

        if (!preferenceManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new JobsFragment());
        }

        checkUserInfoAndShowDialogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceManager.isLoggedIn() && !isCheckingUserInfo && !preferenceManager.hasCompletedSetup()) {
            checkUserInfoAndShowDialogs();
        }
    }

    private void checkUserInfoAndShowDialogs() {
        if (isCheckingUserInfo) return;
        if (preferenceManager.hasCompletedSetup()) return;
        if (isShowingDialog) return;
        
        UserInfoResponse userInfo = preferenceManager.getUserInfo();
        if (userInfo == null) {
            fetchUserInfo();
            return;
        }

        if (userInfo.getFullName() == null || userInfo.getFullName().isEmpty()) {
            isShowingDialog = true;
            showNameInputDialog(userInfo);
        } else {
            checkDistricts();
        }
    }

    private void fetchUserInfo() {
        isCheckingUserInfo = true;
        apiService.getUserInfo().enqueue(new Callback<ApiResponse<UserInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserInfoResponse>> call, Response<ApiResponse<UserInfoResponse>> response) {
                isCheckingUserInfo = false;
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserInfoResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        UserInfoResponse userInfo = apiResponse.getResult();
                        preferenceManager.saveUserInfo(userInfo);
                        checkUserInfoAndShowDialogs();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserInfoResponse>> call, Throwable t) {
                isCheckingUserInfo = false;
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_jobs) {
                fragment = new JobsFragment();
            } else if (itemId == R.id.nav_my_jobs) {
                fragment = new MyJobsFragment();
            } else if (itemId == R.id.nav_notifications) {
                fragment = new NotificationFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, PhoneLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showNameInputDialog(UserInfoResponse userInfo) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Nhập thông tin của bạn");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);
        android.widget.EditText etFullName = dialogView.findViewById(R.id.et_full_name);
        android.widget.RadioGroup rgGender = dialogView.findViewById(R.id.rg_gender);

        builder.setView(dialogView);
        builder.setPositiveButton("Xác nhận", null);
        builder.setCancelable(false);

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            android.widget.Button positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String fullName = etFullName.getText().toString().trim();
                String gender = "UNKNOWN";
                int selectedId = rgGender.getCheckedRadioButtonId();
                if (selectedId == R.id.rb_male) {
                    gender = "MALE";
                } else if (selectedId == R.id.rb_female) {
                    gender = "FEMALE";
                }

                if (!fullName.isEmpty()) {
                    dialog.dismiss();
                    isShowingDialog = false;
                    updateUserInfo(fullName, gender, userInfo);
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.setOnDismissListener(d -> {
            isShowingDialog = false;
        });

        dialog.show();
    }

    private void updateUserInfo(String fullName, String gender, UserInfoResponse userInfo) {
        UpdateUserInfoRequest request = new UpdateUserInfoRequest(gender, null, fullName);
        apiService.updateUserInfo(request).enqueue(new Callback<ApiResponse<UserInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserInfoResponse>> call, Response<ApiResponse<UserInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserInfoResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        UserInfoResponse updatedUserInfo = apiResponse.getResult();
                        preferenceManager.saveUserInfo(updatedUserInfo);
                        refreshProfileFragment();
                        checkDistricts();
                    } else {
                        Toast.makeText(MainActivity.this, "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                        checkDistricts();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    checkDistricts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserInfoResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                checkDistricts();
            }
        });
    }

    private void checkDistricts() {
        if (isShowingDialog) return;
        
        apiService.getPartnerDistricts().enqueue(new Callback<ApiResponse<List<District>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<District>>> call, Response<ApiResponse<List<District>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<District>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<District> registeredDistricts = apiResponse.getResult();
                        if (registeredDistricts == null || registeredDistricts.isEmpty()) {
                            if (!isShowingDialog) {
                                isShowingDialog = true;
                                showDistrictSelectionDialog();
                            }
                        } else {
                            // Đã có fullName và districts → hoàn thành setup
                            preferenceManager.setHasCompletedSetup(true);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<District>>> call, Throwable t) {
            }
        });
    }

    private void showDistrictSelectionDialog() {
        apiService.getAllDistricts().enqueue(new Callback<ApiResponse<List<District>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<District>>> call, Response<ApiResponse<List<District>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<District>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<District> allDistricts = apiResponse.getResult();
                        showDistrictDialog(allDistricts);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<District>>> call, Throwable t) {
            }
        });
    }

    private void showDistrictDialog(List<District> districts) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn các quận bạn muốn làm việc");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_district_selection, null);
        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_districts);
        
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        DistrictSelectionAdapter adapter = new DistrictSelectionAdapter(districts);
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Xác nhận", null);
        builder.setCancelable(false);

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            android.widget.Button positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                List<Long> selectedIds = adapter.getSelectedDistrictIds();
                if (selectedIds.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn ít nhất một quận", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    isShowingDialog = false;
                    updatePartnerDistricts(selectedIds);
                }
            });
        });

        dialog.setOnDismissListener(d -> {
            isShowingDialog = false;
        });

        dialog.show();
    }

    private void updatePartnerDistricts(List<Long> districtIds) {
        UpdateDistrictsRequest request = new UpdateDistrictsRequest(districtIds);
        apiService.updatePartnerDistricts(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(MainActivity.this, "Đã cập nhật quận làm việc thành công!", Toast.LENGTH_SHORT).show();
                        // Đã có fullName và districts → hoàn thành setup
                        preferenceManager.setHasCompletedSetup(true);
                        refreshProfileFragment();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
            }
        });
    }

    private void refreshProfileFragment() {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ProfileFragment) {
            ProfileFragment profileFragment = (ProfileFragment) currentFragment;
            profileFragment.refreshUserInfo();
        }
    }
}
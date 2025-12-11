package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.PaymentStatusResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiPaymentService;
import com.example.myapplication.network.ApiTicketService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity thanh toán qua PayOS với WebView
 * Hỗ trợ Deep Link để mở ứng dụng ngân hàng
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    
    // Intent extras keys
    public static final String EXTRA_CHECKOUT_URL = "checkout_url";
    public static final String EXTRA_ORDER_CODE = "order_code";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_BROADCAST_ID = "broadcast_id";
    public static final String EXTRA_SEAT_ID = "seat_id";
    
    // Result codes
    public static final int RESULT_PAYMENT_SUCCESS = 100;
    public static final int RESULT_PAYMENT_CANCELLED = 101;
    public static final int RESULT_PAYMENT_FAILED = 102;

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private LinearLayout btnMomo, btnZaloPay, btnViettelMoney, btnVnpay, btnVietcombank, btnMBBank;

    private String checkoutUrl;
    private long orderCode;
    private int amount;
    private int broadcastId;
    private int seatId;
    private String accessToken;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable paymentStatusChecker;
    private boolean isCheckingStatus = false;
    
    // Package names chính xác cho các ứng dụng ngân hàng
    // Đã verify trên Google Play Store
    private static final String MOMO_PACKAGE = "com.mservice.momotransfer";
    private static final String ZALOPAY_PACKAGE = "vn.com.vng.zalopay";
    private static final String VIETTEL_MONEY_PACKAGE = "com.viettel.viettelpost"; // Viettel Money
    private static final String VNPAY_PACKAGE = "com.vnpay.ebanking"; // VNPay chính thức
    private static final String VIETCOMBANK_PACKAGE = "com.VCB"; // Vietcombank Digibank
    private static final String MBBANK_PACKAGE = "com.mbmobile"; // MB Bank App

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Lấy token từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL);
        orderCode = intent.getLongExtra(EXTRA_ORDER_CODE, 0);
        amount = intent.getIntExtra(EXTRA_AMOUNT, 0);
        broadcastId = intent.getIntExtra(EXTRA_BROADCAST_ID, -1);
        seatId = intent.getIntExtra(EXTRA_SEAT_ID, -1);

        Log.d(TAG, "Payment URL: " + checkoutUrl);
        Log.d(TAG, "Order Code: " + orderCode);
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "Broadcast ID: " + broadcastId);
        Log.d(TAG, "Seat ID: " + seatId);

        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Không có link thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupWebView();
        setupBankButtons();
        
        // Load URL thanh toán
        webView.loadUrl(checkoutUrl);
        
        // Bắt đầu kiểm tra trạng thái thanh toán định kỳ
        startPaymentStatusChecker();
    }

    private void initViews() {
        webView = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        
        btnMomo = findViewById(R.id.btnMomo);
        btnZaloPay = findViewById(R.id.btnZaloPay);
        btnViettelMoney = findViewById(R.id.btnViettelMoney);
        btnVnpay = findViewById(R.id.btnVnpay);
        btnVietcombank = findViewById(R.id.btnVietcombank);
        btnMBBank = findViewById(R.id.btnMBBank);

        btnBack.setOnClickListener(v -> showCancelConfirmDialog());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile; rv:88.0) Gecko/88.0 Firefox/88.0");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                
                // Kiểm tra nếu URL chứa success/cancel
                checkUrlForPaymentResult(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private boolean handleUrl(String url) {
        Log.d(TAG, "Loading URL: " + url);
        
        // Nếu là HTTP/HTTPS bình thường, kiểm tra kết quả thanh toán
        if (url.startsWith("http://") || url.startsWith("https://")) {
            checkUrlForPaymentResult(url);
            return false; // Để WebView xử lý
        }

        // Nếu là Deep Link (intent://, momo://, zalopay://, etc.)
        try {
            Intent intent;
            
            if (url.startsWith("intent://")) {
                // Parse intent:// URL
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } else {
                // Parse các scheme khác (momo://, zalopay://, etc.)
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                return true;
            } else {
                // App không được cài đặt
                String appName = getAppNameFromScheme(url);
                Toast.makeText(this, "Vui lòng cài đặt ứng dụng " + appName, Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling deep link: " + e.getMessage());
            return true;
        }
    }

    private void checkUrlForPaymentResult(String url) {
        if (url.contains("success") || url.contains("thanh-cong") || url.contains("payment-success")) {
            Log.d(TAG, "Payment success detected from URL");
            onPaymentSuccess();
        } else if (url.contains("cancel") || url.contains("huy") || url.contains("payment-cancel")) {
            Log.d(TAG, "Payment cancelled detected from URL");
            onPaymentCancelled();
        } else if (url.contains("fail") || url.contains("loi") || url.contains("payment-fail")) {
            Log.d(TAG, "Payment failed detected from URL");
            onPaymentFailed("Thanh toán thất bại");
        }
    }

    private String getAppNameFromScheme(String url) {
        if (url.contains("momo")) return "MoMo";
        if (url.contains("zalopay")) return "ZaloPay";
        if (url.contains("viettel")) return "Viettel Money";
        if (url.contains("vnpay")) return "VNPay";
        if (url.contains("vcb") || url.contains("vietcombank")) return "Vietcombank";
        if (url.contains("mbmobile") || url.contains("mbbank")) return "MB Bank";
        return "ngân hàng";
    }

    private void setupBankButtons() {
        btnMomo.setOnClickListener(v -> openBankApp(MOMO_PACKAGE, "MoMo"));
        btnZaloPay.setOnClickListener(v -> openBankApp(ZALOPAY_PACKAGE, "ZaloPay"));
        btnViettelMoney.setOnClickListener(v -> openBankApp(VIETTEL_MONEY_PACKAGE, "Viettel Money"));
        btnVnpay.setOnClickListener(v -> openBankApp(VNPAY_PACKAGE, "VNPay"));
        btnVietcombank.setOnClickListener(v -> openBankApp(VIETCOMBANK_PACKAGE, "Vietcombank"));
        btnMBBank.setOnClickListener(v -> openBankApp(MBBANK_PACKAGE, "MB Bank"));
    }

    private void openBankApp(String packageName, String appName) {
        try {
            // Cách 1: Thử mở app bằng getLaunchIntentForPackage
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                Toast.makeText(this, "Đang mở " + appName + "...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Cách 2: Thử kiểm tra xem app có tồn tại không bằng cách khác
            try {
                getPackageManager().getPackageInfo(packageName, 0);
                // App tồn tại nhưng không có launcher intent, thử mở bằng ACTION_MAIN
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this, "Đang mở " + appName + "...", Toast.LENGTH_SHORT).show();
                return;
            } catch (Exception ignored) {
                // App không tồn tại
            }
            
            // Cách 3: Thử mở bằng deep link scheme
            String deepLinkScheme = getDeepLinkScheme(packageName);
            if (deepLinkScheme != null) {
                try {
                    Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkScheme));
                    if (deepLinkIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(deepLinkIntent);
                        Toast.makeText(this, "Đang mở " + appName + "...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception ignored) {}
            }
            
            // App chưa được cài đặt, mở Play Store
            showInstallAppDialog(packageName, appName);
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening bank app: " + e.getMessage());
            showInstallAppDialog(packageName, appName);
        }
    }
    
    private String getDeepLinkScheme(String packageName) {
        switch (packageName) {
            case MOMO_PACKAGE:
                return "momo://";
            case ZALOPAY_PACKAGE:
                return "zalopay://";
            case VIETTEL_MONEY_PACKAGE:
                return "viettelmoney://";
            case VNPAY_PACKAGE:
                return "vnpayapp://";
            case VIETCOMBANK_PACKAGE:
                return "vcbdigibank://";
            case MBBANK_PACKAGE:
                return "mbmobile://";
            default:
                return null;
        }
    }

    private void showInstallAppDialog(String packageName, String appName) {
        new AlertDialog.Builder(this)
            .setTitle("Chưa cài đặt ứng dụng")
            .setMessage("Bạn chưa cài đặt " + appName + ". Bạn có muốn tải về không?")
            .setPositiveButton("Tải về", (dialog, which) -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + packageName)));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void startPaymentStatusChecker() {
        if (orderCode == 0) return;
        
        paymentStatusChecker = new Runnable() {
            @Override
            public void run() {
                if (!isCheckingStatus) {
                    checkPaymentStatus();
                }
                handler.postDelayed(this, 5000); // Kiểm tra mỗi 5 giây
            }
        };
        handler.postDelayed(paymentStatusChecker, 5000);
    }

    private void stopPaymentStatusChecker() {
        if (paymentStatusChecker != null) {
            handler.removeCallbacks(paymentStatusChecker);
        }
    }

    private void checkPaymentStatus() {
        if (accessToken == null || orderCode == 0) return;
        
        isCheckingStatus = true;
        
        ApiPaymentService apiPaymentService = ApiClient.getRetrofit().create(ApiPaymentService.class);
        Call<PaymentStatusResponse> call = apiPaymentService.getPaymentStatus(
                "Bearer " + accessToken, orderCode);
        
        call.enqueue(new Callback<PaymentStatusResponse>() {
            @Override
            public void onResponse(Call<PaymentStatusResponse> call, Response<PaymentStatusResponse> response) {
                isCheckingStatus = false;
                
                if (response.isSuccessful() && response.body() != null) {
                    PaymentStatusResponse statusResponse = response.body();
                    
                    if (statusResponse.isPaid()) {
                        Log.d(TAG, "Payment confirmed as PAID");
                        stopPaymentStatusChecker();
                        onPaymentSuccess();
                    } else if (statusResponse.isCancelled()) {
                        Log.d(TAG, "Payment confirmed as CANCELLED");
                        stopPaymentStatusChecker();
                        onPaymentCancelled();
                    }
                    // Nếu PENDING, tiếp tục kiểm tra
                }
            }

            @Override
            public void onFailure(Call<PaymentStatusResponse> call, Throwable t) {
                isCheckingStatus = false;
                Log.e(TAG, "Error checking payment status: " + t.getMessage());
            }
        });
    }

    private void onPaymentSuccess() {
        stopPaymentStatusChecker();
        
        // Tạo vé thực sự sau khi thanh toán thành công
        if (broadcastId != -1 && seatId != -1) {
            createTicketAfterPayment();
        } else {
            // Trả về kết quả thành công
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("order_code", orderCode);
            resultIntent.putExtra("status", "PAID");
            setResult(RESULT_PAYMENT_SUCCESS, resultIntent);
            finish();
        }
    }

    private void createTicketAfterPayment() {
        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        BookingTicketRequest ticketRequest = new BookingTicketRequest(broadcastId, seatId);
        
        Call<BookingTicketResponse> call = apiTicketService.createTicket(
                "Bearer " + accessToken, ticketRequest);
        
        call.enqueue(new Callback<BookingTicketResponse>() {
            @Override
            public void onResponse(Call<BookingTicketResponse> call, Response<BookingTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BookingTicketResponse ticketResponse = response.body();
                    
                    Toast.makeText(PaymentActivity.this, 
                            "Thanh toán và đặt vé thành công!", Toast.LENGTH_LONG).show();
                    
                    // Chuyển đến màn hình chi tiết vé
                    Intent resultIntent = new Intent(PaymentActivity.this, UserAdminShowDetailTicket.class);
                    resultIntent.putExtra("bookingTicketResponse", ticketResponse);
                    resultIntent.putExtra("order_code", orderCode);
                    resultIntent.putExtra("status", "PAID");
                    
                    // Set result cho activity gọi
                    setResult(RESULT_PAYMENT_SUCCESS, resultIntent);
                    
                    // Mở màn hình chi tiết vé
                    startActivity(resultIntent);
                    finish();
                } else {
                    Log.e(TAG, "Failed to create ticket after payment");
                    Toast.makeText(PaymentActivity.this, 
                            "Thanh toán thành công nhưng lỗi tạo vé. Vui lòng liên hệ hỗ trợ.",
                            Toast.LENGTH_LONG).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("order_code", orderCode);
                    resultIntent.putExtra("status", "PAID_BUT_TICKET_ERROR");
                    setResult(RESULT_PAYMENT_SUCCESS, resultIntent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<BookingTicketResponse> call, Throwable t) {
                Log.e(TAG, "Error creating ticket: " + t.getMessage());
                Toast.makeText(PaymentActivity.this, 
                        "Thanh toán thành công nhưng lỗi tạo vé. Vui lòng liên hệ hỗ trợ.",
                        Toast.LENGTH_LONG).show();
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("order_code", orderCode);
                resultIntent.putExtra("status", "PAID_BUT_TICKET_ERROR");
                setResult(RESULT_PAYMENT_SUCCESS, resultIntent);
                finish();
            }
        });
    }

    private void onPaymentCancelled() {
        stopPaymentStatusChecker();
        Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("order_code", orderCode);
        resultIntent.putExtra("status", "CANCELLED");
        setResult(RESULT_PAYMENT_CANCELLED, resultIntent);
        finish();
    }

    private void onPaymentFailed(String message) {
        stopPaymentStatusChecker();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("order_code", orderCode);
        resultIntent.putExtra("status", "FAILED");
        resultIntent.putExtra("error_message", message);
        setResult(RESULT_PAYMENT_FAILED, resultIntent);
        finish();
    }

    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy")
            .setMessage("Bạn có chắc muốn hủy thanh toán?")
            .setPositiveButton("Hủy thanh toán", (dialog, which) -> {
                onPaymentCancelled();
            })
            .setNegativeButton("Tiếp tục thanh toán", null)
            .show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            showCancelConfirmDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra trạng thái thanh toán khi quay lại từ app ngân hàng
        if (orderCode != 0) {
            checkPaymentStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPaymentStatusChecker();
        if (webView != null) {
            webView.destroy();
        }
    }
}

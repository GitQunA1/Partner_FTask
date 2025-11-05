# Partner FTask - Ứng dụng Partner cho hệ thống FTask

## Mô tả
Ứng dụng Android dành cho Partner để quản lý và thực hiện các công việc trong hệ thống FTask.

## Tính năng chính

### 1. Authentication (Đăng nhập)
- Đăng nhập bằng Access Token
- Lưu trữ thông tin đăng nhập
- Tự động chuyển đến màn hình chính nếu đã đăng nhập

### 2. Luồng nhận việc (Tìm việc)
- Xem danh sách công việc khả dụng (PENDING, PARTIALLY_ACCEPTED)
- Xem chi tiết công việc
- Nhận việc (Claim job)
- Làm mới danh sách (Pull to refresh)

### 3. Quản lý công việc (Công việc của tôi)
- Xem danh sách công việc đã nhận
- Lọc theo trạng thái:
  - Tất cả
  - Đã nhận (JOINED)
  - Đang làm (WORKING)
  - Hoàn thành (COMPLETED)
- Bắt đầu công việc
- Hoàn thành công việc
- Hủy công việc

### 4. Quản lý cá nhân
- Xem thông tin Partner
- Đăng xuất

## Cấu trúc Project

```
app/src/main/java/com/example/partner_ftask/
├── data/
│   ├── api/
│   │   ├── ApiClient.java          # Retrofit client
│   │   └── ApiService.java         # API endpoints
│   └── model/
│       ├── ApiResponse.java        # Generic API response
│       ├── PageResponse.java       # Pagination response
│       ├── Booking.java            # Booking model
│       ├── Customer.java           # Customer model
│       ├── Partner.java            # Partner model
│       ├── Variant.java            # Service variant model
│       ├── Address.java            # Address model
│       └── BookingPartner.java     # Booking-Partner relationship
├── ui/
│   ├── activity/
│   │   ├── LoginActivity.java      # Login screen
│   │   ├── MainActivity.java       # Main screen with bottom nav
│   │   └── BookingDetailActivity.java  # Booking detail screen
│   ├── adapter/
│   │   ├── BookingAdapter.java     # Adapter for available jobs
│   │   └── MyJobsAdapter.java      # Adapter for my jobs
│   └── fragment/
│       ├── JobsFragment.java       # Available jobs fragment
│       ├── MyJobsFragment.java     # My jobs fragment
│       └── ProfileFragment.java    # Profile fragment
├── utils/
│   ├── PreferenceManager.java      # SharedPreferences manager
│   └── DateTimeUtils.java          # Date/time utilities
├── MainActivity.java               # Main activity
└── PartnerFTaskApplication.java    # Application class
```

## Cách sử dụng

### Bước 1: Build Project
1. Mở project trong Android Studio
2. Đợi Gradle sync hoàn tất
3. Build project: **Build > Make Project** hoặc `Ctrl+F9`

### Bước 2: Chạy ứng dụng
1. Kết nối thiết bị Android hoặc khởi động emulator
2. Click **Run** hoặc `Shift+F10`

### Bước 3: Đăng nhập
1. Nhập Access Token (lấy từ API đăng nhập hoặc team Backend)
2. Click "Đăng nhập"

### Bước 4: Sử dụng app
- **Tab "Tìm việc"**: Xem và nhận các công việc khả dụng
- **Tab "Công việc"**: Quản lý các công việc đã nhận
- **Tab "Cá nhân"**: Xem thông tin và đăng xuất

## API Endpoints

### Base URL
```
https://ftask.anhtudev.works/
```

### Authentication
Tất cả các request cần header:
```
Authorization: Bearer {accessToken}
```

### Endpoints sử dụng

1. **GET /bookings** - Lấy danh sách bookings
   - Query params: `status`, `page`, `size`, etc.

2. **GET /bookings/{id}** - Lấy chi tiết booking

3. **POST /partners/bookings/{bookingId}/claim** - Nhận việc

4. **POST /partners/bookings/{bookingId}/start** - Bắt đầu làm việc

5. **POST /partners/bookings/{bookingId}/complete** - Hoàn thành công việc

6. **POST /partners/bookings/{bookingId}/cancel** - Hủy công việc

## Dependencies

- **Retrofit 2.9.0** - HTTP client
- **OkHttp 4.12.0** - HTTP client
- **Gson 2.10.1** - JSON parser
- **Glide 4.16.0** - Image loading
- **AndroidX Material Components** - UI components
- **AndroidX Lifecycle** - ViewModel and LiveData

## Lưu ý

### Permissions
App yêu cầu các permissions sau trong `AndroidManifest.xml`:
- `INTERNET` - Để gọi API
- `ACCESS_NETWORK_STATE` - Để kiểm tra kết nối mạng

### Clear Text Traffic
App đã enable `usesCleartextTraffic="true"` để hỗ trợ kết nối HTTP nếu cần.

### SDK Version
- **minSdk**: 31
- **targetSdk**: 36
- **compileSdk**: 36

## Troubleshooting

### Lỗi "Cannot resolve symbol 'retrofit2'"
- Đảm bảo Gradle đã sync: **File > Sync Project with Gradle Files**
- Clean và rebuild: **Build > Clean Project** sau đó **Build > Rebuild Project**

### Lỗi kết nối API
- Kiểm tra internet connection
- Kiểm tra Access Token còn hiệu lực
- Kiểm tra server API đang hoạt động

### App crash khi mở
- Kiểm tra Logcat để xem log lỗi
- Đảm bảo đã đăng nhập và có token hợp lệ

## Demo Account

Để test app, bạn cần:
1. Lấy Access Token từ API đăng nhập với role PARTNER
2. Hoặc liên hệ team Backend để lấy token test

## TODO / Improvements

- [ ] Implement proper authentication flow (OTP)
- [ ] Add pagination for job lists
- [ ] Add search and filter functionality
- [ ] Add real-time updates (WebSocket/FCM)
- [ ] Add map view for job locations
- [ ] Add job history
- [ ] Add earnings tracking
- [ ] Improve UI/UX
- [ ] Add unit tests
- [ ] Add integration tests

## Contact

Nếu có vấn đề hoặc câu hỏi, vui lòng liên hệ:
- Email: support@ftask.com
- Phone: 0123456789

---

**Phiên bản**: 1.0
**Ngày cập nhật**: 05/11/2025


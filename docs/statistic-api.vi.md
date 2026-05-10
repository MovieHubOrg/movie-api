# Tài liệu API Statistic

## Thông tin chung

- Base path: `/v1/statistic`
- Response wrapper chung:

- Tất cả API trong `StatisticController` yêu cầu token có quyền `STAT_V`.

- Định dạng ngày giờ cho query param: `dd/MM/yyyy HH:mm:ss`, ví dụ `01/01/2026 00:00:00`.
- `fromDate` và `toDate` là optional. Nếu không truyền, API trả thống kê all-time.
- Nếu truyền `fromDate` lớn hơn `toDate`, API trả lỗi bad request.

## 1. Tổng quan hệ thống

### API

```http
GET /v1/statistic/overview
```

### Ý nghĩa

Dùng cho các thẻ tổng quan trên dashboard, ví dụ tổng user, tổng phim, tổng lượt xem, tổng bình luận, tổng review.

### Query params

| Param | Bắt buộc | Kiểu | Ý nghĩa |
| --- | --- | --- | --- |
| `fromDate` | Không | Date | Ngày bắt đầu thống kê, format `dd/MM/yyyy HH:mm:ss`. |
| `toDate` | Không | Date | Ngày kết thúc thống kê, format `dd/MM/yyyy HH:mm:ss`. |

### Ví dụ request

```http
GET /v1/statistic/overview?fromDate=01/05/2026 00:00:00&toDate=31/05/2026 00:00:00
```

### Ví dụ response

```json
{
  "result": true,
  "code": null,
  "data": {
    "totalUsers": 120,
    "totalMovies": 45,
    "totalSingleMovies": 30,
    "totalSeriesMovies": 15,
    "totalViews": 20480,
    "totalComments": 320,
    "totalReviews": 86,
    "totalFavourites": 410,
    "averageRating": 4.2
  },
  "message": "Get statistic overview success"
}
```

### Gợi ý dùng cho frontend

- Hiển thị `totalUsers`, `totalMovies`, `totalViews`, `totalComments`, `totalReviews` thành các metric cards.
- `totalSingleMovies` và `totalSeriesMovies` có thể dùng cho biểu đồ phân loại phim.
- `averageRating` nên format 1 chữ số thập phân, ví dụ `4.2`.
- Khi user chọn khoảng ngày trên dashboard, gọi lại API với `fromDate` và `toDate`.

## 2. Top phim

### API

```http
GET /v1/statistic/top-movies
```

### Ý nghĩa

Dùng cho bảng hoặc chart top phim theo lượt xem, bình luận, review hoặc điểm rating trung bình.

### Query params

| Param | Bắt buộc | Kiểu | Giá trị hợp lệ | Mặc định | Ý nghĩa |
| --- | --- | --- | --- | --- | --- |
| `fromDate` | Không | Date | `dd/MM/yyyy HH:mm:ss` | All-time | Lọc phim theo ngày tạo. |
| `toDate` | Không | Date | `dd/MM/yyyy HH:mm:ss` | All-time | Lọc phim theo ngày tạo. |
| `sortBy` | Không | String | `viewCount`, `commentCount`, `reviewCount`, `averageRating` | `viewCount` | Trường dùng để xếp hạng top phim. |
| `page` | Không | Number | `0, 1, 2, ...` | `0` | Trang hiện tại, bắt đầu từ 0. |
| `size` | Không | Number | `1, 2, 10, ...` | Theo Spring Pageable | Số item mỗi trang. |

Nếu `sortBy` không hợp lệ, backend tự fallback về `viewCount`.

### Ví dụ request

```http
GET /v1/statistic/top-movies?sortBy=viewCount&page=0&size=10
```

```http
GET /v1/statistic/top-movies?fromDate=01/05/2026 00:00:00&toDate=31/05/2026 00:00:00&sortBy=averageRating&page=0&size=5
```

### Ví dụ response

```json
{
  "result": true,
  "code": null,
  "data": {
    "content": [
      {
        "id": 101,
        "title": "Movie title",
        "thumbnailUrl": "https://example.com/movie.jpg",
        "viewCount": 9800,
        "commentCount": 120,
        "reviewCount": 35,
        "averageRating": 4.6
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  },
  "message": "Get top movies statistic success"
}
```

### Gợi ý dùng cho frontend

- Dùng `content` để render table hoặc bar chart.
- Dùng `totalElements` và `totalPages` cho pagination.
- Cho người dùng chọn kiểu sắp xếp bằng select:
  - `viewCount`: Top lượt xem
  - `commentCount`: Top bình luận
  - `reviewCount`: Top review
  - `averageRating`: Top điểm đánh giá
- Nên hiển thị ảnh từ `thumbnailUrl`, tên phim từ `title`, và metric chính tương ứng với `sortBy`.

## 3. Phân bố phim

### API

```http
GET /v1/statistic/movie-distribution
```

### Ý nghĩa

Dùng cho pie chart, donut chart hoặc bar chart để xem phim đang phân bố theo loại, quốc gia, ngôn ngữ hoặc độ tuổi.

### Query params

| Param | Bắt buộc | Kiểu | Giá trị hợp lệ | Mặc định | Ý nghĩa |
| --- | --- | --- | --- | --- | --- |
| `groupBy` | Không | String | `type`, `country`, `language`, `ageRating` | `type` | Nhóm dữ liệu thống kê phim. |

Nếu `groupBy` không hợp lệ, API trả lỗi bad request.

### Ví dụ request

```http
GET /v1/statistic/movie-distribution?groupBy=country
```

### Ví dụ response

```json
{
  "result": true,
  "code": null,
  "data": [
    {
      "label": "US",
      "value": 20
    },
    {
      "label": "KR",
      "value": 12
    },
    {
      "label": "Unknown",
      "value": 2
    }
  ],
  "message": "Get movie distribution statistic success"
}
```

### Mapping label gợi ý

Với `groupBy=type`:

| Label từ API | Hiển thị gợi ý |
| --- | --- |
| `1` | Phim lẻ |
| `2` | Phim bộ |
| `Unknown` | Không xác định |

Với `groupBy=ageRating`, frontend có thể map theo rule đang dùng trong hệ thống:

| Label từ API | Hiển thị gợi ý |
| --- | --- |
| `1` | G |
| `2` | PG |
| `3` | PG-13 |
| `4` | R |
| `5` | NC-17 |
| `6` | 18+ |
| `Unknown` | Không xác định |

## Lưu ý nghiệp vụ

- `overview.totalViews` all-time lấy từ counter `Movie.viewCount`.
- Khi có `fromDate` hoặc `toDate`, `overview.totalViews` được tính từ `WatchHistory.timesWatched` của bản ghi tổng theo phim. Đây là thống kê theo dữ liệu hiện có, không phải event log từng lần xem.
- `top-movies` khi truyền `fromDate/toDate` lọc theo ngày tạo phim, không lọc theo ngày phát sinh lượt xem.
- Các thống kê chỉ tính dữ liệu đang active theo `status = 1`.

## Ví dụ tích hợp frontend

```ts
const headers = {
  Authorization: `Bearer ${accessToken}`,
  Accept: "application/json",
};

const overview = await fetch(
  "/v1/statistic/overview?fromDate=01/05/2026 00:00:00&toDate=31/05/2026 00:00:00",
  { headers }
).then((res) => res.json());

const topMovies = await fetch(
  "/v1/statistic/top-movies?sortBy=viewCount&page=0&size=10",
  { headers }
).then((res) => res.json());

const distribution = await fetch(
  "/v1/statistic/movie-distribution?groupBy=type",
  { headers }
).then((res) => res.json());
```

# TraceVault Backend — cara menjalankan

## 1. Siapkan database
Install PostgreSQL, lalu buat database kosong:

```sql
CREATE DATABASE tracevault;
```

Tabel dibuat otomatis oleh Flyway dari `src/main/resources/db/migration/V1__init_schema.sql`.

## 2. Atur koneksi (kalau berbeda dari default)
Default di `application.yaml`: user `postgres`, password `postgres`, port `5432`.
Kalau berbeda, set environment variable sebelum menjalankan:

```bash
# Windows PowerShell
$env:DB_PASSWORD="passwordkamu"
# Linux / macOS
export DB_PASSWORD=passwordkamu
```

Variabel lain: `DB_URL`, `DB_USERNAME`, `APP_JWT_SECRET` (wajib diganti saat deploy), `APP_CORS_ORIGINS`.

## 3. Jalankan

```bash
cd backend
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

Backend jalan di `http://localhost:8080`.

## 4. Sambungkan frontend
Di `frontend/js/api.js` ubah:

```js
const MOCK_MODE = false;
```

Buka frontend lewat Live Server (port 5500) atau server lokal lain yang ada di `app.cors.allowed-origins`.

## Endpoint

| Method | Endpoint | Login? | Fungsi |
|---|---|---|---|
| POST | `/api/auth/register` | tidak | Daftar akun |
| POST | `/api/auth/login` | tidak | Masuk, dapat token |
| POST | `/api/auth/password-reset` | tidak | (belum kirim email sungguhan) |
| POST | `/api/analyses` | ya | Upload file (form-data: `file`, `type`, `notes`) |
| GET | `/api/analyses` | ya | Riwayat analisis |
| GET | `/api/analyses/{id}` | ya | Detail analisis |
| DELETE | `/api/analyses/{id}` | ya | Hapus analisis |
| GET | `/api/analyses/{id}/report` | ya | Laporan teks |

## Struktur paket

```
com.tracevault.backend
├── config/     SecurityConfig, JwtService, JwtAuthFilter  (login & token)
├── user/       User, UserRepository                       (tabel users)
├── auth/       AuthController, AuthService, AuthDtos      (/api/auth)
├── analysis/   AnalysisController, AnalysisService, Analysis, AnalysisRepository,
│               AnalysisResponse, ReportService            (/api/analyses)
├── forensic/   ForensicService + DocumentInspector, EmailInspector, MalwareInspector,
│               ForensicUtils, SummaryWriter               (pemeriksaan file)
└── common/     GlobalExceptionHandler                     (error → JSON {message})
```

## Catatan
- File yang diupload **tidak disimpan** dan **tidak dijalankan**; hanya hash dan hasil pemeriksaan yang disimpan.
- "AI summary" saat ini dibuat dengan aturan dari temuan (belum memakai model AI).
- `BackendApplicationTests` butuh database aktif; jalankan `./mvnw test` hanya jika PostgreSQL sudah menyala.

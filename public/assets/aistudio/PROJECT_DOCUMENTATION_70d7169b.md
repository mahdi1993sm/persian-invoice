# 📋 مستند کامل پروژه سیستم صدور فاکتور فروش

## 🎯 معرفی پروژه

این یک **اپلیکیشن تحت وب** برای مدیریت فاکتورهای فروش است که با **React + TypeScript + Tailwind CSS** ساخته شده. تمام داده‌ها در **localStorage** مرورگر ذخیره می‌شوند و نیازی به سرور ندارند.

---

## 🛠️ تکنولوژی‌های استفاده شده

| تکنولوژی | نسخه | کاربرد |
|-----------|------|--------|
| React | 18 | فریم‌ورک اصلی UI |
| TypeScript | 5 | تایپ‌سیفتینگ |
| Vite | 6 | Build Tool |
| Tailwind CSS | 4 | استایل‌دهی |
| Zustand | 5 | مدیریت State |
| jalaali-js | - | تقویم شمسی |
| jsPDF | 2.5.2 | تولید PDF |
| html2canvas | 1.4.1 | تبدیل HTML به تصویر |
| xlsx | - | خروجی/ورودی اکسل |
| Font Awesome | 6.4 | آیکون‌ها |
| Vazirmatn | 33 | فونت فارسی |

---

## 📁 ساختار پروژه

```
project-root/
├── index.html                 # فایل HTML اصلی (فونت وزیرمتن + FA)
├── package.json               # وابستگی‌ها
├── vite.config.js             # تنظیمات Vite
├── tailwind.config.js         # تنظیمات Tailwind
├── tsconfig.json              # تنظیمات TypeScript
│
├── src/
│   ├── main.tsx               # نقطه ورود
│   ├── App.tsx                # کامپوننت اصلی (Router + Layout)
│   ├── index.css              # استایل‌های سراسری + انیمیشن‌ها
│   │
│   ├── types/
│   │   └── index.ts           # تعریف تمام TypeScript interfaces
│   │
│   ├── store/
│   │   └── useStore.ts        # Zustand Store (تمام state + actions)
│   │
│   ├── utils/
│   │   ├── jalali.ts          # توابع تقویم شمسی
│   │   └── excel.ts           # توابع خروجی/ورودی اکسل
│   │
│   └── components/
│       ├── Sidebar.tsx         # منوی کناری (responsive + collapsible)
│       ├── SearchBar.tsx       # نوار جستجوی پیشرفته
│       ├── JalaliDatePicker.tsx # تقویم شمسی پاپ‌آپ (با Portal)
│       ├── Dashboard.tsx       # داشبورد و آمار
│       ├── InvoiceForm.tsx     # فرم صدور/ویرایش فاکتور
│       ├── InvoiceList.tsx     # لیست فاکتورها
│       ├── InvoiceView.tsx     # مشاهده/چاپ/PDF فاکتور
│       ├── Customers.tsx       # مدیریت مشتریان
│       ├── Products.tsx        # مدیریت کالاها
│       ├── BankAccounts.tsx    # مدیریت حساب‌های بانکی
│       ├── SellerSettings.tsx  # اطلاعات فروشنده (امضا/مهر)
│       ├── Settings.tsx        # تنظیمات کامل برنامه
│       └── DataManager.tsx     # مدیریت داده‌ها (بکاپ)
│
└── dist/                       # خروجی build
```

---

## 🧩 توضیح کامپوننت‌ها

### 1. `App.tsx` - کامپوننت اصلی
- **وظیفه**: Layout اصلی، routing بین صفحات، مدیریت dark mode
- **ویژگی‌ها**:
  - تشخیص موبایل/دسکتاپ
  - اعمال تنظیمات تم (رنگ، فونت)
  - مودال تنظیمات سریع (Quick Settings)
  - میانبرهای کیبورد (Ctrl+D, Ctrl+N, Ctrl+L)

### 2. `Sidebar.tsx` - منوی کناری
- **وظیفه**: ناوبری بین صفحات
- **ویژگی‌ها**:
  - در موبایل: کاملاً بسته (باز شدن با دکمه)
  - در دسکتاپ: قابل جمع شدن (فقط آیکون‌ها)
  - رنگ منوی فعال با تم رنگی هماهنگ
  - Tooltip هنگام جمع شدن

### 3. `Dashboard.tsx` - داشبورد
- **وظیفه**: نمایش آمار و نمودارها
- **ویژگی‌ها**:
  - ۴ دکمه دسترسی سریع
  - کارت‌های آماری (فروش، دریافتی، مطالبات)
  - نمودار میله‌ای فروش ماهانه (شمسی)
  - برترین مشتریان
  - پرفروش‌ترین کالاها (با واحد واقعی)

### 4. `InvoiceForm.tsx` - فرم صدور فاکتور
- **وظیفه**: ایجاد و ویرایش فاکتور
- **ویژگی‌ها**:
  - جستجوی مشتری (مودال)
  - افزودن سریع مشتری جدید
  - جدول اقلام با ویرایش inline
  - انتخاب کالا از لیست یا ثبت کالای جدید
  - تخفیف و مالیات
  - نحوه تسویه (نقدی/نسیه/چک)
  - اطلاعات چک صیادی
  - اعتبارسنجی فیلدهای اجباری (قرمز شدن)
  - حالت ویرایش (load داده‌های فاکتور)
  - نمایش نتیجه در پاپ‌آپ

### 5. `InvoiceList.tsx` - لیست فاکتورها
- **وظیفه**: نمایش و مدیریت فاکتورها
- **ویژگی‌ها**:
  - جستجو و فیلتر
  - جدول کامل با badge‌های رنگی
  - دکمه مشاهده/ویرایش/حذف
  - آمار کلی

### 6. `InvoiceView.tsx` - مشاهده فاکتور
- **وظیفه**: نمایش، چاپ و PDF فاکتور
- **ویژگی‌ها**:
  - طراحی استاندارد فاکتور ایرانی
  - تنظیمات A4/A5 و عمودی/افقی
  - انتخاب امضا و مهر برای چاپ
  - دکمه‌های پرینت، PDF، اشتراک
  - اشتراک در واتساپ/تلگرام/ایتا/بله
  - فونت وزیرمتن در خروجی

### 7. `Customers.tsx` - مدیریت مشتریان
- **وظیفه**: CRUD مشتریان
- **فیلدها**: نام، تلفن، موبایل، آدرس، کدپستی، استان، شهر، کدملی، کداقتصادی
- **ویژگی‌ها**: جستجو، آمار، اعتبارسنجی

### 8. `Products.tsx` - مدیریت کالاها
- **وظیفه**: CRUD کالاها
- **فیلدها**: نام، کد، واحد، قیمت، دسته‌بندی، توضیحات
- **ویژگی‌ها**: دو حالت نمایش (Grid/List)، فیلتر دسته‌بندی

### 9. `BankAccounts.tsx` - حساب‌های بانکی
- **وظیفه**: CRUD حساب‌ها
- **فیلدها**: نام بانک، شماره حساب، کارت، شبا، صاحب حساب
- **ویژگی‌ها**: کپی با کلیک، رنگ‌های gradient

### 10. `SellerSettings.tsx` - اطلاعات فروشنده
- **وظیفه**: تنظیم اطلاعات فروشگاه
- **ویژگی‌ها**: آپلود امضا و مهر، پیش‌نمایش

### 11. `Settings.tsx` - تنظیمات کامل
- **تب‌ها**:
  - **عمومی**: حالت تاریک، رنگ تم، اندازه فونت، واحد پول، میانبرها
  - **فاکتور**: پیش‌شماره، مالیات/تخفیف پیش‌فرض، متن پایین
  - **چاپ و PDF**: اندازه، جهت، نمایش اطلاعات
  - **داده‌ها**: بکاپ JSON/Excel، ورود Excel، حذف
  - **درباره**: اطلاعات برنامه، بازنشانی

### 12. `JalaliDatePicker.tsx` - تقویم شمسی
- **ویژگی‌ها**:
  - استفاده از `createPortal` (رندر در body)
  - محاسبه موقعیت هوشمند
  - بستن با scroll یا کلیک بیرون
  - سازگار با موبایل و دسکتاپ

### 13. `SearchBar.tsx` - جستجوی پیشرفته
- **ویژگی‌ها**: جستجو در مشتریان، کالاها، فاکتورها

---

## 🗄️ ساختار داده‌ها (Store)

```typescript
interface AppState {
  // ناوبری
  currentPage: PageType;
  selectedInvoiceId: string | null;
  editingInvoiceId: string | null;

  // داده‌های اصلی
  customers: Customer[];
  products: Product[];
  bankAccounts: BankAccount[];
  invoices: Invoice[];

  // جستجو
  searchQuery: string;
  searchResults: SearchResult[];

  // تنظیمات UI
  sidebarCollapsed: boolean;
  darkMode: boolean;
  currencyUnit: 'toman' | 'rial' | 'custom';
  customCurrencyLabel: string;

  // اطلاعات فروشنده
  sellerInfo: {
    name, phone, mobile, address, postalCode,
    province, city, nationalCode, economicCode,
    signature, stamp  // base64 images
  };

  // تنظیمات برنامه
  settings: {
    fontSize: 'small' | 'medium' | 'large';
    themeColor: string;  // hex color
    invoicePrefix: string;
    invoiceStartNumber: number;
    defaultTax: number;
    defaultDiscount: number;
    defaultPrintSize: 'A4' | 'A5';
    defaultPrintOrientation: 'portrait' | 'landscape';
    showBankInfo: boolean;
    showSellerInfo: boolean;
    invoiceFooterText: string;
    autoBackup: boolean;
  };
}
```

---

## 📄 ساختار Invoice

```typescript
interface Invoice {
  id: string;
  invoiceNumber: string;
  customerId: string;
  customerName: string;
  items: InvoiceItem[];
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
  paid: number;
  remaining: number;
  status: 'paid' | 'credit' | 'partial' | 'overdue';
  bankAccountId: string;
  description: string;
  createdAt: string;      // Jalali date: YYYY/MM/DD
  dueDate: string;
  paymentType: 'cash' | 'credit' | 'check';
  cashPaymentMethod?: 'pos' | 'card_to_card' | 'paya' | 'satna' | 'cash';
  cashPaymentDate?: string;
  checkInfo?: {
    bankName: string;
    checkNumber: string;
    serialNumber: string;
    sayadiId: string;     // 16 digits
    amount: number;
    dueDate: string;
    issuerName: string;
  };
}
```

---

## 🎨 سیستم تم رنگی

**نحوه کار:**
1. رنگ انتخابی در `settings.themeColor` ذخیره می‌شود
2. در `App.tsx` به CSS variable تبدیل می‌شود:
   ```javascript
   document.documentElement.style.setProperty('--theme-color', settings.themeColor);
   ```
3. نسخه تیره‌تر به صورت خودکار محاسبه می‌شود
4. در المان‌ها با `style={{ backgroundColor: 'var(--theme-color)' }}` استفاده می‌شود

**المان‌های تحت تأثیر:**
- Sidebar (منوی فعال)
- Header فاکتور
- دکمه ثبت
- آیکون‌های عنوان
- نمودار فروش

---

## 📅 سیستم تقویم شمسی

**کتابخانه**: `jalaali-js`

**توابع اصلی:**
- `toJalaali(gy, gm, gd)` - تبدیل میلادی به شمسی
- `toGregorian(jy, jm, jd)` - تبدیل شمسی به میلادی
- `formatJalaliDate(date)` - فرمت‌بندی تاریخ
- `getTodayJalali()` - تاریخ امروز شمسی
- `parseJalaliDate(str)` - پارس تاریخ شمسی

**JalaliDatePicker:**
- از `createPortal` استفاده می‌کند (رندر در body)
- محاسبه موقعیت هوشمند (بالا/پایین)
- بستن با scroll یا کلیک بیرون

---

## 🖨️ سیستم چاپ و PDF

**نحوه کار:**
1. HTML فاکتور با `generateStandardInvoiceHTML()` تولید می‌شود
2. برای PDF:
   - یک container موقت ایجاد می‌شود
   - `html2canvas` آن را به تصویر تبدیل می‌کند
   - `jsPDF` تصویر را به PDF تبدیل می‌کند
3. برای چاپ:
   - یک پنجره جدید باز می‌شود
   - HTML در آن رندر می‌شود
   - `window.print()` صدا زده می‌شود

**طراحی فاکتور استاندارد:**
- هدر با نام فروشنده
- مشخصات فروشنده و خریدار
- شماره و تاریخ فاکتور
- نحوه پرداخت
- جدول اقلام
- جمع‌بندی مالی
- شرایط فروش
- محل امضا و مهر

---

## 📊 سیستم خروجی/ورودی اکسل

**کتابخانه**: `xlsx` (SheetJS)

**خروجی:**
- ۴ شیت: مشتریان، کالاها، حساب‌ها، فاکتورها
- ستون‌های فارسی
- فرمت استاندارد

**ورودی:**
- خواندن تمام شیت‌ها
- تبدیل به data structure مناسب
- ثبت در store

---

## 🔐 سیستم اعتبارسنجی

**الگو:**
```typescript
const [errors, setErrors] = useState<Record<string, boolean>>({});

const validate = () => {
  const newErrors: Record<string, boolean> = {};
  if (!field) newErrors.field = true;
  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};

// در JSX:
<input className={errors.field ? 'border-red-500 bg-red-50' : ''} />
{errors.field && <p className="text-red-500">پیام خطا</p>}
```

**فیلدهای اجباری:**
- InvoiceForm: مشتری، اقلام، مقدار، قیمت
- Customers: نام
- Products: نام، قیمت
- BankAccounts: نام بانک

---

## 🎬 سیستم انیمیشن‌ها

**انیمیشن‌های تعریف شده در CSS:**
- `fadeIn`, `slideInRight`, `slideInUp`
- `scaleIn`, `bounceIn`, `shake`
- `fadeInBackdrop`

**کلاس‌های کمکی:**
- `.page-transition` - انیمیشن تغییر صفحه
- `.stagger-1` تا `.stagger-6` - انیمیشن پلکانی
- `.hover-lift` - افکت شناور
- `.input-error` - حالت خطا

---

## 🌙 سیستم Dark Mode

**نحوه کار:**
1. `darkMode` در store ذخیره می‌شود
2. در `App.tsx`:
   ```javascript
   useEffect(() => {
     if (darkMode) {
       document.documentElement.classList.add('dark');
     } else {
       document.documentElement.classList.remove('dark');
     }
   }, [darkMode]);
   ```
3. در Tailwind با `dark:` prefix استفاده می‌شود

---

## 🔗 لینک‌های اشتراک‌گذاری

```javascript
const shareText = `فاکتور شماره ${invoice.invoiceNumber}...`;
const encodedText = encodeURIComponent(shareText);

// واتساپ
`https://wa.me/?text=${encodedText}`

// تلگرام
`https://t.me/share/url?url=&text=${encodedText}`

// ایتا
`https://eitaa.com/share/url?url=&text=${encodedText}`

// بله
`https://ble.ir/share?url=&text=${encodedText}`
```

---

## ⌨️ میانبرهای کیبورد

| کلید | عملکرد |
|------|--------|
| `Ctrl + D` | تغییر حالت تاریک/روشن |
| `Ctrl + N` | فاکتور جدید |
| `Ctrl + L` | لیست فاکتورها |
| `Ctrl + ,` | تنظیمات |

---

## 💾 سیستم ذخیره‌سازی

**محل**: `localStorage`
**کلید**: `invoice-app-storage`
**فرمت**: JSON (با Zustand persist)

**ساختار:**
```json
{
  "state": {
    "customers": [...],
    "products": [...],
    "invoices": [...],
    "bankAccounts": [...],
    "sellerInfo": {...},
    "settings": {...},
    ...
  }
}
```

---

## 🚀 نحوه اجرا

```bash
# نصب وابستگی‌ها
npm install

# اجرای در حالت توسعه
npm run dev

# ساخت نسخه production
npm run build

# پیش‌نمایش نسخه build
npm run preview
```

---

## 📝 تغییرات انجام شده (Timeline)

### فاز ۱: ساختار اولیه
- ایجاد پروژه با Vite + React + TypeScript
- نصب Tailwind CSS
- تعریف types و store
- ساخت Sidebar و App

### فاز ۲: صفحات اصلی
- Dashboard با نمودار
- InvoiceForm با تمام قابلیت‌ها
- InvoiceList با جدول
- InvoiceView با چاپ/PDF

### فاز ۳: مدیریت داده‌ها
- Customers (CRUD)
- Products (CRUD)
- BankAccounts (CRUD)

### فاز ۴: قابلیت‌های پیشرفته
- تقویم شمسی با Portal
- سیستم تم رنگی
- Dark Mode
- اعتبارسنجی فیلدها
- انیمیشن‌ها

### فاز ۵: بهینه‌سازی
- رفع باگ‌های موبایل
- بهبود responsive
- بهینه‌سازی performance
- رفع مشکلات z-index

### فاز ۶: قابلیت‌های نهایی
- خروجی/ورودی اکسل
- تنظیمات کامل
- میانبرهای کیبورد
- اشتراک‌گذاری

---

## 🐛 باگ‌های رفع شده

1. **سیاه شدن صفحه هنگام جستجوی مشتری** → استفاده از مودال به جای dropdown
2. **تقویم در دسکتاپ باز نمی‌شد** → استفاده از `createPortal`
3. **آیکون‌ها نمایش داده نمی‌شدند** → تنظیم `font-family` جداگانه برای FA
4. **مهر در قسمت اشتباه** → اصلاح HTML فاکتور
5. **نمودار میله‌ای** → بازنویسی با flexbox صحیح
6. **واحد پول** → سیستم قابل تنظیم

---

## 🎯 قابلیت‌های کلیدی

✅ صدور فاکتور با طراحی استاندارد ایرانی
✅ مدیریت مشتریان، کالاها، حساب‌ها
✅ تقویم شمسی در تمام بخش‌ها
✅ چاپ و PDF با تنظیمات A4/A5
✅ اشتراک‌گذاری در پیام‌رسان‌ها
✅ سیستم تم رنگی
✅ Dark Mode
✅ اعتبارسنجی فیلدها
✅ خروجی/ورودی اکسل
✅ میانبرهای کیبورد
✅ Responsive کامل
✅ انیمیشن‌های حرفه‌ای

---

## 📚 منابع و مستندات

- [React Docs](https://react.dev)
- [Zustand Docs](https://docs.pmnd.rs/zustand)
- [Tailwind CSS](https://tailwindcss.com)
- [jalaali-js](https://github.com/jalaali/jalaali-js)
- [SheetJS](https://sheetjs.com)
- [jsPDF](https://github.com/parallax/jsPDF)

---

## 👨‍💻 توسعه‌دهنده

**ساخته شده با**: AI Assistant
**تاریخ**: ۱۴۰۳
**نسخه**: ۱.۰.۰

---

## 📞 پشتیبانی

برای سوالات یا مشکلات، به مستندات هر کامپوننت مراجعه کنید یا کد منبع را بررسی نمایید.

---

**پایان مستند**

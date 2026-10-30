# 🔧 راهنمای فنی و معماری پروژه

## 📐 معماری کلی

```
┌─────────────────────────────────────────────────────────────┐
│                         App.tsx                              │
│  ┌──────────┐  ┌──────────────────────────────────────────┐ │
│  │ Sidebar  │  │           Main Content                   │ │
│  │          │  │  ┌────────────────────────────────────┐  │ │
│  │ Navigation│  │  │         Header                   │  │ │
│  │          │  │  │  - Page Title                      │  │ │
│  │          │  │  │  - SearchBar                       │  │ │
│  │          │  │  │  - Dark Mode Toggle                │  │ │
│  │          │  │  │  - Settings Button                 │  │ │
│  │          │  │  └────────────────────────────────────┘  │ │
│  │          │  │  ┌────────────────────────────────────┐  │ │
│  │          │  │  │                                    │  │ │
│  │          │  │  │      Page Component                │  │ │
│  │          │  │  │  (Dashboard/InvoiceForm/etc)       │  │ │
│  │          │  │  │                                    │  │ │
│  │          │  │  └────────────────────────────────────┘  │ │
│  └──────────┘  └──────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 جریان داده (Data Flow)

```
┌──────────────┐
│   User       │
│   Action     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Component   │
│  (React)     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Zustand    │
│   Store      │
│  (State)     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ localStorage │
│  (Persist)   │
└──────────────┘
```

---

## 📦 State Management با Zustand

### ساختار Store:

```typescript
// src/store/useStore.ts

export const useStore = create<AppState>()(
  persist(
    (set, get) => ({
      // State
      currentPage: 'dashboard',
      customers: [],
      products: [],
      invoices: [],

      // Actions
      setPage: (page) => set({ currentPage: page }),
      addCustomer: (customer) => set((state) => ({
        customers: [...state.customers, customer]
      })),

      // Derived state
      getCurrencyLabel: () => {
        const state = get();
        switch (state.currencyUnit) {
          case 'toman': return 'تومان';
          case 'rial': return 'ریال';
          default: return state.customCurrencyLabel;
        }
      }
    }),
    {
      name: 'invoice-app-storage',
      partialize: (state) => state // ذخیره تمام state
    }
  )
);
```

### استفاده در کامپوننت:

```typescript
// دسترسی به state
const { customers, addCustomer } = useStore();

// دسترسی به تمام state
const state = useStore.getState();
```

---

## 🎨 سیستم تم رنگی

### نحوه پیاده‌سازی:

```typescript
// App.tsx
useEffect(() => {
  // تنظیم رنگ اصلی
  document.documentElement.style.setProperty('--theme-color', settings.themeColor);

  // محاسبه و تنظیم رنگ تیره‌تر
  const hex = settings.themeColor.replace('#', '');
  const r = Math.max(0, parseInt(hex.substring(0, 2), 16) - 30);
  const g = Math.max(0, parseInt(hex.substring(2, 4), 16) - 30);
  const b = Math.max(0, parseInt(hex.substring(4, 6), 16) - 30);
  const darkerColor = `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
  document.documentElement.style.setProperty('--theme-color-dark', darkerColor);
}, [settings.themeColor]);
```

### استفاده در کامپوننت‌ها:

```tsx
// روش ۱: با style
<div style={{ backgroundColor: 'var(--theme-color)' }}>

// روش ۲: با gradient
<div style={{ background: 'linear-gradient(to left, var(--theme-color), var(--theme-color-dark))' }}>
```

---

## 📅 سیستم تقویم شمسی

### JalaliDatePicker با Portal:

```typescript
// src/components/JalaliDatePicker.tsx

export default function JalaliDatePicker({ value, onChange }) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState({ top: 0, left: 0 });

  // محاسبه موقعیت پاپ‌آپ
  useEffect(() => {
    if (isOpen && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const spaceBelow = window.innerHeight - rect.bottom;

      // اگر فضای کافی در پایین نیست، بالای فیلد باز شود
      let top = spaceBelow > 350 ? rect.bottom + 8 : rect.top - 350;
      top = Math.max(8, Math.min(top, window.innerHeight - 360));

      setPosition({ top, left: rect.left });
    }
  }, [isOpen]);

  // رندر با Portal
  return (
    <>
      <div ref={containerRef} onClick={() => setIsOpen(!isOpen)}>
        {/* Input field */}
      </div>
      {isOpen && createPortal(
        <div style={{ position: 'fixed', top: position.top, left: position.left, zIndex: 99999 }}>
          {/* Calendar */}
        </div>,
        document.body
      )}
    </>
  );
}
```

### چرا Portal؟
- جلوگیری از تداخل با `overflow: hidden` والد
- z-index مستقل
- رندر مستقیم در body
- حل مشکل سیاه شدن صفحه

---

## 🖨️ سیستم چاپ و PDF

### تولید HTML فاکتور:

```typescript
const generateStandardInvoiceHTML = (includeSignature, includeStamp) => {
  return `
    <div style="direction:rtl;font-family:'Vazirmatn',sans-serif;">
      <!-- Header -->
      <div style="text-align:center;border:2px solid #333;">
        <h1>صورتحساب فروش کالا و خدمات</h1>
      </div>

      <!-- Customer Info -->
      <table>
        <tr>
          <td>مشخصات فروشنده</td>
          <td>مشخصات خریدار</td>
        </tr>
      </table>

      <!-- Items Table -->
      <table>
        <thead>...</thead>
        <tbody>
          ${items.map(item => `<tr>...</tr>`).join('')}
        </tbody>
      </table>

      <!-- Totals -->
      <table>...</table>

      <!-- Signatures -->
      <table>
        <tr>
          <td>مهر و امضاء خریدار</td>
          <td>مهر و امضاء فروشنده</td>
        </tr>
      </table>
    </div>
  `;
};
```

### تولید PDF:

```typescript
const executePDF = async (includeSignature, includeStamp) => {
  // ۱. ایجاد container موقت
  const container = document.createElement('div');
  container.style.position = 'fixed';
  container.style.left = '-9999px';
  container.style.width = '794px'; // A4 width
  container.innerHTML = generateStandardInvoiceHTML(includeSignature, includeStamp);
  document.body.appendChild(container);

  // ۲. تبدیل به canvas
  const canvas = await html2canvas(container, {
    scale: 2,
    useCORS: true,
    backgroundColor: '#ffffff'
  });

  // ۳. حذف container
  document.body.removeChild(container);

  // ۴. تبدیل به PDF
  const imgData = canvas.toDataURL('image/png');
  const pdf = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  });

  const pdfWidth = pdf.internal.pageSize.getWidth();
  const pdfHeight = pdf.internal.pageSize.getHeight();
  const margin = 5;
  const contentWidth = pdfWidth - (margin * 2);
  const contentHeight = (canvas.height * contentWidth) / canvas.width;

  pdf.addImage(imgData, 'PNG', margin, margin, contentWidth, contentHeight);
  pdf.save(`فاکتور-${invoice.invoiceNumber}.pdf`);
};
```

---

## 📊 سیستم خروجی/ورودی اکسل

### خروجی:

```typescript
import * as XLSX from 'xlsx';

export const exportToExcel = (data) => {
  const workbook = XLSX.utils.book_new();

  // شیت مشتریان
  const customersData = data.customers.map(c => ({
    'شناسه': c.id,
    'نام': c.name,
    'تلفن': c.phone,
    // ...
  }));
  const customersSheet = XLSX.utils.json_to_sheet(customersData);
  XLSX.utils.book_append_sheet(workbook, customersSheet, 'مشتریان');

  // شیت کالاها
  // ...

  // تولید و دانلود
  const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `backup-${new Date().toLocaleDateString('fa-IR')}.xlsx`;
  a.click();
};
```

### ورودی:

```typescript
export const importFromExcel = (file: File): Promise<Data> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      const data = new Uint8Array(e.target?.result as ArrayBuffer);
      const workbook = XLSX.read(data, { type: 'array' });

      const result = { customers: [], products: [], ... };

      // خواندن شیت مشتریان
      if (workbook.SheetNames.includes('مشتریان')) {
        const sheet = workbook.Sheets['مشتریان'];
        const json = XLSX.utils.sheet_to_json(sheet);
        result.customers = json.map(row => ({
          id: row['شناسه'] || crypto.randomUUID(),
          name: row['نام'] || '',
          // ...
        }));
      }

      resolve(result);
    };

    reader.readAsArrayBuffer(file);
  });
};
```

---

## 🔐 سیستم اعتبارسنجی

### الگو:

```typescript
const [errors, setErrors] = useState<Record<string, boolean>>({});

const validate = (): boolean => {
  const newErrors: Record<string, boolean> = {};

  // بررسی فیلدهای اجباری
  if (!customerId) newErrors.customer = true;
  if (items.length === 0) newErrors.items = true;

  // بررسی مقادیر
  items.forEach((item, index) => {
    if (item.quantity <= 0) newErrors[`item_qty_${index}`] = true;
    if (item.unitPrice <= 0) newErrors[`item_price_${index}`] = true;
  });

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};

const handleSubmit = () => {
  if (!validate()) return; // توقف اگر خطا وجود دارد
  // ... submit logic
};
```

### نمایش در UI:

```tsx
<select className={`${inputClass} ${errors.customer ? 'input-error' : ''}`}>
  <option value="">-- انتخاب مشتری --</option>
</select>
{errors.customer && (
  <p className="text-xs text-red-500 mt-1 flex items-center gap-1">
    <i className="fa-solid fa-circle-exclamation"></i>
    لطفاً مشتری را انتخاب کنید
  </p>
)}
```

### پاک کردن خودکار خطا:

```tsx
<select
  onChange={(e) => {
    setCustomerId(e.target.value);
    setErrors({ ...errors, customer: false }); // پاک کردن خطا
  }}
>
```

---

## 🎬 سیستم انیمیشن‌ها

### تعریف در CSS:

```css
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounceIn {
  0% { opacity: 0; transform: scale(0.3); }
  50% { opacity: 1; transform: scale(1.05); }
  70% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

.animate-fade-in { animation: fadeIn 0.3s ease-out; }
.animate-bounce-in { animation: bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55); }
```

### استفاده در کامپوننت:

```tsx
<div className="animate-slide-in-up stagger-1">
  {/* Content */}
</div>
```

### انیمیشن پلکانی:

```css
.stagger-1 { animation-delay: 0.05s; animation-fill-mode: both; }
.stagger-2 { animation-delay: 0.1s; animation-fill-mode: both; }
.stagger-3 { animation-delay: 0.15s; animation-fill-mode: both; }
```

---

## 🌙 سیستم Dark Mode

### پیاده‌سازی:

```typescript
// App.tsx
useEffect(() => {
  if (darkMode) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
}, [darkMode]);
```

### استفاده در Tailwind:

```tsx
<div className="bg-white dark:bg-slate-800">
  <h1 className="text-slate-800 dark:text-slate-200">Title</h1>
</div>
```

### CSS Variables:

```css
:root {
  --bg-primary: #ffffff;
  --text-primary: #1e293b;
}

.dark {
  --bg-primary: #1e293b;
  --text-primary: #ffffff;
}
```

---

## 🔗 سیستم اشتراک‌گذاری

### لینک‌های واقعی:

```typescript
const handleShare = (platform: string) => {
  const shareText = `فاکتور شماره ${invoice.invoiceNumber}
مشتری: ${invoice.customerName}
مبلغ: ${formatNumber(invoice.total)} ${currency}
وضعیت: ${getStatusText(invoice.status)}
تاریخ: ${formatDate(invoice.createdAt)}`;

  const encodedText = encodeURIComponent(shareText);

  let url = '';
  switch (platform) {
    case 'whatsapp':
      url = `https://wa.me/?text=${encodedText}`;
      break;
    case 'telegram':
      url = `https://t.me/share/url?url=&text=${encodedText}`;
      break;
    case 'eitaa':
      url = `https://eitaa.com/share/url?url=&text=${encodedText}`;
      break;
    case 'bale':
      url = `https://ble.ir/share?url=&text=${encodedText}`;
      break;
  }

  window.open(url, '_blank');
};
```

---

## ⌨️ سیستم میانبرهای کیبورد

### پیاده‌سازی:

```typescript
// App.tsx
useEffect(() => {
  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.ctrlKey || e.metaKey) {
      switch (e.key) {
        case 'd':
          e.preventDefault();
          toggleDarkMode();
          break;
        case 'n':
          e.preventDefault();
          setPage('new-invoice');
          break;
        case 'l':
          e.preventDefault();
          setPage('invoices');
          break;
      }
    }
  };

  window.addEventListener('keydown', handleKeyDown);
  return () => window.removeEventListener('keydown', handleKeyDown);
}, []);
```

---

## 📱 سیستم Responsive

### تشخیص موبایل:

```typescript
const [isMobile, setIsMobile] = useState(false);

useEffect(() => {
  const check = () => setIsMobile(window.innerWidth < 768);
  check();
  window.addEventListener('resize', check);
  return () => window.removeEventListener('resize', check);
}, []);
```

### استفاده:

```tsx
{isMobile && (
  <button onClick={toggleSidebar}>
    <i className="fa-solid fa-bars"></i>
  </button>
)}

<div className="hidden md:block">
  {/* Only on desktop */}
</div>

<div className="flex flex-col md:flex-row">
  {/* Responsive layout */}
</div>
```

---

## 🎯 الگوهای طراحی (Design Patterns)

### ۱. Compound Components:

```tsx
// استفاده از Portal برای مودال
{showModal && createPortal(
  <Modal>
    <Modal.Header>...</Modal.Header>
    <Modal.Body>...</Modal.Body>
  </Modal>,
  document.body
)}
```

### ۲. Render Props:

```tsx
// استفاده از store با selector
const customers = useStore((state) => state.customers);
```

### ۳. Custom Hooks:

```typescript
// hook سفارشی برای تاریخ
function useJalaliDate() {
  const [date, setDate] = useState(getTodayJalali());

  const updateDate = (newDate: string) => {
    setDate(newDate);
  };

  return { date, updateDate };
}
```

### ۴. Higher Order Components:

```tsx
// کامپوننت با قابلیت dark mode
const withDarkMode = (Component) => {
  return (props) => {
    const darkMode = useStore((state) => state.darkMode);
    return <Component {...props} darkMode={darkMode} />;
  };
};
```

---

## 🔧 نکات فنی مهم

### ۱. Performance:

```typescript
// استفاده از useMemo برای محاسبات سنگین
const stats = useMemo(() => {
  return invoices.reduce((acc, inv) => {
    acc.total += inv.total;
    return acc;
  }, { total: 0 });
}, [invoices]);

// استفاده از useCallback برای توابع
const handleSave = useCallback(() => {
  // ...
}, [dependencies]);
```

### ۲. Memory Management:

```typescript
// پاک کردن event listeners
useEffect(() => {
  const handler = () => {};
  window.addEventListener('resize', handler);
  return () => window.removeEventListener('resize', handler);
}, []);

// پاک کردن URLs
const url = URL.createObjectURL(blob);
// ... استفاده
URL.revokeObjectURL(url);
```

### ۳. Error Handling:

```typescript
try {
  const data = await importFromExcel(file);
  // ...
} catch (error) {
  console.error('Import error:', error);
  showMessage('خطا در وارد کردن فایل');
}
```

---

## 📋 چک‌لیست توسعه

### قبل از commit:

- [ ] تمام کامپوننت‌ها responsive هستند
- [ ] dark mode تست شده
- [ ] اعتبارسنجی فرم‌ها کار می‌کند
- [ ] انیمیشن‌ها smooth هستند
- [ ] هیچ console.error وجود ندارد
- [ ] build موفقیت‌آمیز است

### قبل از release:

- [ ] تمام قابلیت‌ها تست شده‌اند
- [ ] performance بهینه است
- [ ] accessibility رعایت شده
- [ ] مستندات کامل است
- [ ] README آپدیت شده

---

## 🚀 Future Improvements

### قابلیت‌های پیشنهادی:

1. **Backend Integration**
   - اتصال به API
   - ذخیره‌سازی در database
   - همگام‌سازی بین دستگاه‌ها

2. **Authentication**
   - سیستم لاگین
   - مدیریت کاربران
   - سطوح دسترسی

3. **Advanced Features**
   - گزارش‌گیری پیشرفته
   - نمودارهای تعاملی
   - export به فرمت‌های دیگر

4. **Performance**
   - Code splitting
   - Lazy loading
   - Caching strategies

5. **UX Improvements**
   - Drag & drop
   - Keyboard navigation
   - Accessibility (a11y)

---

**پایان مستند فنی**

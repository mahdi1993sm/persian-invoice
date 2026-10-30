# 👨‍💻 راهنمای توسعه‌دهنده - Developer Guide

## 🎯 این فایل برای کیست؟

این فایل برای **توسعه‌دهندگانی** است که می‌خواهند پروژه را توسعه دهند، قابلیت‌های جدید اضافه کنند یا کد موجود را تغییر دهند.

---

## 📋 پیش‌نیازها

### دانش مورد نیاز
- ✅ React (Hooks, Components, State)
- ✅ TypeScript (Interfaces, Types, Generics)
- ✅ Tailwind CSS (Utility classes, Responsive)
- ✅ Zustand (Store, Actions, Selectors)
- ✅ Git (Version control)

### ابزارها
- ✅ Node.js 18+
- ✅ VS Code (توصیه شده)
- ✅ ESLint + Prettier
- ✅ React DevTools

---

## 🏗️ معماری پروژه

### ساختار کامپوننت‌ها

```
src/
├── components/          # کامپوننت‌های UI
│   ├── Dashboard.tsx
│   ├── InvoiceForm.tsx
│   └── ...
├── store/              # State Management
│   └── useStore.ts
├── utils/              # توابع کمکی
│   ├── jalali.ts
│   └── excel.ts
└── types/              # TypeScript definitions
    └── index.ts
```

### جریان داده

```
User Action → Component → Store Action → State Update → UI Re-render
```

---

## 🎨 اضافه کردن کامپوننت جدید

### مرحله ۱: ایجاد فایل

```bash
# در پوشه src/components
touch src/components/NewComponent.tsx
```

### مرحله ۲: ساختار پایه

```typescript
import { useState } from 'react';
import { useStore } from '../store/useStore';

interface NewComponentProps {
  // props types
}

export default function NewComponent({ /* props */ }: NewComponentProps) {
  // State
  const [state, setState] = useState<Type>(initialValue);

  // Store
  const { data, action } = useStore();

  // Handlers
  const handleClick = () => {
    // logic
  };

  // Render
  return (
    <div className="p-4 bg-white dark:bg-slate-800 rounded-xl">
      {/* JSX */}
    </div>
  );
}
```

### مرحله ۳: اضافه کردن به App.tsx

```typescript
// Import
import NewComponent from './components/NewComponent';

// در renderPage
const renderPage = () => {
  switch (currentPage) {
    case 'new-page': return <NewComponent />;
    // ...
  }
};
```

### مرحله ۴: اضافه کردن به Sidebar

```typescript
// در Sidebar.tsx
const menuItems: { page: PageType; icon: string; label: string }[] = [
  // ...
  { page: 'new-page', icon: 'fa-icon', label: 'عنوان' },
];
```

---

## 📄 اضافه کردن صفحه جدید

### مرحله ۱: تعریف PageType

```typescript
// src/types/index.ts
export type PageType =
  | 'dashboard'
  | 'new-page'  // اضافه کنید
  // ...
```

### مرحله ۲: ایجاد کامپوننت

```typescript
// src/components/NewPage.tsx
export default function NewPage() {
  return (
    <div className="p-6 page-transition">
      <h2 className="text-2xl font-bold">عنوان صفحه</h2>
      {/* Content */}
    </div>
  );
}
```

### مرحله ۳: اضافه کردن به Router

```typescript
// src/App.tsx
import NewPage from './components/NewPage';

const renderPage = () => {
  switch (currentPage) {
    case 'new-page': return <NewPage />;
    // ...
  }
};

const pageTitle: Record<string, string> = {
  'new-page': 'عنوان صفحه',
  // ...
};
```

---

## 🗄️ کار با Store

### دسترسی به State

```typescript
// در کامپوننت
const { customers, addCustomer } = useStore();

// با selector (بهینه‌تر)
const customers = useStore((state) => state.customers);
```

### اضافه کردن Action

```typescript
// src/store/useStore.ts

interface AppState {
  // State
  newField: string;

  // Actions
  setNewField: (value: string) => void;
}

export const useStore = create<AppState>()(
  persist(
    (set, get) => ({
      // State
      newField: '',

      // Actions
      setNewField: (value: string) => set({ newField: value }),
    }),
    { name: 'invoice-app-storage' }
  )
);
```

### استفاده از Action

```typescript
const { newField, setNewField } = useStore();

<input
  value={newField}
  onChange={(e) => setNewField(e.target.value)}
/>
```

---

## 🎨 کار با استایل‌ها

### استفاده از Tailwind

```tsx
// کلاس‌های پایه
<div className="p-4 bg-white dark:bg-slate-800 rounded-xl">

// Responsive
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

// Hover effects
<button className="bg-emerald-600 hover:bg-emerald-700 transition-colors">

// Dark mode
<h1 className="text-slate-800 dark:text-slate-200">
```

### استفاده از تم رنگی

```tsx
// با style
<div style={{ backgroundColor: 'var(--theme-color)' }}>

// با gradient
<div style={{
  background: `linear-gradient(to left, var(--theme-color), var(--theme-color-dark))`
}}>
```

### انیمیشن‌ها

```tsx
// انیمیشن‌های آماده
<div className="animate-fade-in">
<div className="animate-slide-in-up">
<div className="animate-bounce-in">

// انیمیشن پلکانی
<div className="animate-slide-in-up stagger-1">
<div className="animate-slide-in-up stagger-2">
<div className="animate-slide-in-up stagger-3">
```

---

## 📅 کار با تقویم شمسی

### استفاده از JalaliDatePicker

```tsx
import JalaliDatePicker from './JalaliDatePicker';

const [date, setDate] = useState(getTodayJalali());

<JalaliDatePicker
  value={date}
  onChange={setDate}
  placeholder="1403/01/01"
/>
```

### توابع کمکی

```typescript
import {
  formatJalaliDate,
  getTodayJalali,
  parseJalaliDate
} from '../utils/jalali';

// فرمت تاریخ
const formatted = formatJalaliDate(new Date()); // "1403/08/05"

// تاریخ امروز
const today = getTodayJalali(); // "1403/08/05"

// پارس تاریخ
const parsed = parseJalaliDate("1403/08/05"); // { jy: 1403, jm: 8, jd: 5 }
```

---

## 🖨️ کار با چاپ و PDF

### تولید HTML فاکتور

```typescript
const generateInvoiceHTML = (includeSignature, includeStamp) => {
  return `
    <div style="direction:rtl;font-family:'Vazirmatn',sans-serif;">
      <!-- Header -->
      <!-- Content -->
      <!-- Footer -->
    </div>
  `;
};
```

### تولید PDF

```typescript
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const executePDF = async () => {
  // ۱. ایجاد container
  const container = document.createElement('div');
  container.innerHTML = generateInvoiceHTML(true, true);
  document.body.appendChild(container);

  // ۲. تبدیل به canvas
  const canvas = await html2canvas(container, {
    scale: 2,
    useCORS: true,
    backgroundColor: '#ffffff'
  });

  // ۳. تبدیل به PDF
  const imgData = canvas.toDataURL('image/png');
  const pdf = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4'
  });

  pdf.addImage(imgData, 'PNG', 10, 10, 190, 277);
  pdf.save('invoice.pdf');

  // ۴. پاکسازی
  document.body.removeChild(container);
};
```

---

## 📊 کار با اکسل

### خروجی

```typescript
import * as XLSX from 'xlsx';

const exportToExcel = (data) => {
  const workbook = XLSX.utils.book_new();

  // شیت ۱
  const sheet1 = XLSX.utils.json_to_sheet(data.customers);
  XLSX.utils.book_append_sheet(workbook, sheet1, 'مشتریان');

  // شیت ۲
  const sheet2 = XLSX.utils.json_to_sheet(data.products);
  XLSX.utils.book_append_sheet(workbook, sheet2, 'کالاها');

  // دانلود
  const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'export.xlsx';
  a.click();
};
```

### ورودی

```typescript
const importFromExcel = (file: File) => {
  const reader = new FileReader();

  reader.onload = (e) => {
    const data = new Uint8Array(e.target?.result as ArrayBuffer);
    const workbook = XLSX.read(data, { type: 'array' });

    // خواندن شیت
    const sheet = workbook.Sheets['مشتریان'];
    const json = XLSX.utils.sheet_to_json(sheet);

    // پردازش داده‌ها
    json.forEach(row => {
      // ...
    });
  };

  reader.readAsArrayBuffer(file);
};
```

---

## 🔐 اعتبارسنجی فرم‌ها

### الگوی پایه

```typescript
const [errors, setErrors] = useState<Record<string, boolean>>({});

const validate = (): boolean => {
  const newErrors: Record<string, boolean> = {};

  // بررسی فیلدها
  if (!field1) newErrors.field1 = true;
  if (!field2) newErrors.field2 = true;
  if (field3 <= 0) newErrors.field3 = true;

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};

const handleSubmit = () => {
  if (!validate()) return;
  // submit logic
};
```

### نمایش خطا

```tsx
<input
  className={`
    ${errors.field1
      ? 'border-red-500 bg-red-50 dark:bg-red-900/20'
      : 'border-slate-200 dark:border-slate-600'
    }
  `}
  onChange={(e) => {
    setValue(e.target.value);
    setErrors({ ...errors, field1: false }); // پاک کردن خطا
  }}
/>

{errors.field1 && (
  <p className="text-xs text-red-500 mt-1 flex items-center gap-1">
    <i className="fa-solid fa-circle-exclamation"></i>
    پیام خطا
  </p>
)}
```

---

## 🎯 بهترین روش‌ها (Best Practices)

### ۱. کامپوننت‌ها

```typescript
// ✅ خوب
export default function MyComponent() {
  const [state, setState] = useState<Type>(initialValue);

  const handleClick = useCallback(() => {
    // logic
  }, [dependencies]);

  return <div>{/* JSX */}</div>;
}

// ❌ بد
export function MyComponent() { // بدون default
  const [state, setState] = useState(); // بدون type
  return <div>{/* JSX */}</div>;
}
```

### ۲. State Management

```typescript
// ✅ خوب
const customers = useStore((state) => state.customers);

// ❌ بد
const { customers } = useStore(); // re-render غیرضروری
```

### ۳. TypeScript

```typescript
// ✅ خوب
interface Props {
  name: string;
  age?: number; // optional
}

// ❌ بد
interface Props {
  name: any;
  age: object;
}
```

### ۴. استایل‌ها

```tsx
// ✅ خوب
<div className="p-4 bg-white dark:bg-slate-800 rounded-xl">

// ❌ بد
<div style={{ padding: '16px', background: 'white' }}>
```

### ۵. Performance

```typescript
// ✅ خوب
const expensiveValue = useMemo(() => {
  return computeExpensiveValue(a, b);
}, [a, b]);

// ❌ بد
const expensiveValue = computeExpensiveValue(a, b); // هر render
```

---

## 🐛 دیباگ کردن

### Console.log

```typescript
console.log('Value:', value);
console.table(array);
console.group('Group');
console.log('Item 1');
console.log('Item 2');
console.groupEnd();
```

### React DevTools

1. نصب اکستنشن React DevTools
2. باز کردن DevTools (F12)
3. تب Components
4. بررسی state و props

### TypeScript Errors

```bash
# بررسی خطاهای TypeScript
npm run build

# یا در VS Code
# خطاهای قرمز را بررسی کنید
```

### Network Issues

1. F12 > Network tab
2. بررسی درخواست‌ها
3. بررسی پاسخ‌ها
4. بررسی status codes

---

## 🧪 تست کردن

### تست دستی

1. **تست قابلیت‌های اصلی**
   - صدور فاکتور
   - مدیریت مشتریان
   - مدیریت کالاها

2. **تست Responsive**
   - موبایل (320px - 768px)
   - تبلت (768px - 1024px)
   - دسکتاپ (1024px+)

3. **تست Dark Mode**
   - تمام صفحات
   - تمام کامپوننت‌ها

4. **تست اعتبارسنجی**
   - فیلدهای اجباری
   - مقادیر نامعتبر

### تست خودکار (آینده)

```typescript
// Unit Test
import { render, screen } from '@testing-library/react';
import MyComponent from './MyComponent';

test('renders component', () => {
  render(<MyComponent />);
  expect(screen.getByText('Hello')).toBeInTheDocument();
});
```

---

## 📦 Build و Deploy

### Build

```bash
# ساخت نسخه production
npm run build

# خروجی در پوشه dist/
```

### Deploy

#### Vercel
```bash
npm install -g vercel
vercel
```

#### Netlify
```bash
npm install -g netlify-cli
netlify deploy --prod
```

#### GitHub Pages
```bash
npm run build
# آپلود پوشه dist/ به branch gh-pages
```

---

## 🔧 ابزارهای توسعه

### VS Code Extensions

- ESLint
- Prettier
- Tailwind CSS IntelliSense
- TypeScript
- React Snippets

### Browser Extensions

- React DevTools
- Redux DevTools (برای Zustand)

### Terminal

```bash
# Format code
npx prettier --write .

# Check types
npx tsc --noEmit

# Lint
npx eslint src/
```

---

## 📚 منابع یادگیری

### React
- [React Docs](https://react.dev)
- [React TypeScript Cheatsheet](https://react-typescript-cheatsheet.netlify.app)

### TypeScript
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook)
- [TypeScript Deep Dive](https://basarat.gitbook.io/typescript)

### Tailwind CSS
- [Tailwind Docs](https://tailwindcss.com/docs)
- [Tailwind Cheat Sheet](https://tailwindcomponents.com/cheatsheet)

### Zustand
- [Zustand Docs](https://docs.pmnd.rs/zustand)
- [Zustand Examples](https://github.com/pmndrs/zustand/tree/main/examples)

---

## 💡 نکات مهم

### ۱. کامنت‌گذاری

```typescript
// ✅ خوب - توضیح چرا
// استفاده از Portal برای جلوگیری از تداخل z-index
const modal = createPortal(<Modal />, document.body);

// ❌ بد - توضیح چه چیزی
// ایجاد modal
const modal = createPortal(<Modal />, document.body);
```

### ۲. نام‌گذاری

```typescript
// ✅ خوب
const customerName = 'John';
const handleCustomerClick = () => {};
const CustomerCard = () => {};

// ❌ بد
const n = 'John';
const click = () => {};
const Card = () => {};
```

### ۳. خطاها

```typescript
// ✅ خوب
try {
  const data = await fetchData();
} catch (error) {
  console.error('Fetch error:', error);
  showMessage('خطا در دریافت داده‌ها');
}

// ❌ بد
try {
  const data = await fetchData();
} catch (e) {
  // هیچ کاری نمی‌کند
}
```

---

## 🎯 چک‌لیست توسعه

### قبل از commit
- [ ] کد تست شده
- [ ] TypeScript errors رفع شده
- [ ] ESLint warnings رفع شده
- [ ] کامنت‌ها اضافه شده
- [ ] مستندات آپدیت شده

### قبل از PR
- [ ] Build موفقیت‌آمیز
- [ ] تمام قابلیت‌ها تست شده
- [ ] Responsive تست شده
- [ ] Dark Mode تست شده
- [ ] Performance بررسی شده

### قبل از merge
- [ ] Code review انجام شده
- [ ] تمام کامنت‌ها رفع شده
- [ ] تست‌ها پاس شده
- [ ] مستندات کامل است

---

## 🚀 شروع توسعه

### ۱. Fork و Clone

```bash
git clone https://github.com/your-username/project.git
cd project
```

### ۲. نصب وابستگی‌ها

```bash
npm install
```

### ۳. ایجاد Branch

```bash
git checkout -b feature/new-feature
```

### ۴. توسعه

```bash
npm run dev
# تغییرات را اعمال کنید
```

### ۵. Commit

```bash
git add .
git commit -m "feat: add new feature"
```

### ۶. Push

```bash
git push origin feature/new-feature
```

### ۷. Pull Request

- PR ایجاد کنید
- توضیحات اضافه کنید
- Review درخواست کنید

---

**موفق باشید! 🎉**

---

**نسخه: ۱.۰.۰**
**تاریخ: ۱۴۰۳**
**توسعه‌دهنده: AI Assistant**

---

**پایان راهنمای توسعه‌دهنده**

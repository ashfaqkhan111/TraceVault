# TraceVault — Static Dashboard

A pixel-faithful, static HTML/CSS recreation of the TraceVault dashboard screenshot.
**No JavaScript. No frameworks.** Just HTML and CSS.

---

## Files

| File | Purpose |
|---|---|
| `frontend/pages/dashboard.html` | The dashboard page (all HTML, inline SVGs) |
| `frontend/css/pages/dashboard-static.css` | All styles for this page |
| `README-dashboard.md` | This file |

---

## How to open

1. Open **VS Code** (or any editor).
2. Right-click `frontend/pages/dashboard.html` → **Open with Live Server**
   (install the *Live Server* VS Code extension if you don't have it).
3. Alternatively, just double-click the file in File Explorer — it opens in your browser directly.

> The page uses Google Fonts (Inter) via a `<link>` tag, so you need an internet connection
> for the font to load. It falls back to `'Segoe UI', system-ui, sans-serif` when offline.

---

## How to edit

### Change a colour
Open `dashboard-static.css` and find the `:root { }` block at the top.
Every colour is a CSS variable. For example, to change the green:

```css
:root {
  --color-primary: #10b981;   /* ← change this hex value */
}
```

### Change a number or caption
Open `dashboard.html` and find the text you want.
The file is heavily commented so every section is easy to locate.

### Change a card layout
All cards use a shared `.card` class. The layout is controlled by:
- **Stats cards** → `.stats-grid` (CSS Grid, 2 equal columns)
- **Breakdown cards** → `.breakdown-grid` (CSS Grid, 3 equal columns)
- **Bottom row** → `.bottom-row` (CSS Grid, 1.6fr + 1fr)

To make 3 stat cards instead of 2, change `grid-template-columns: 1fr 1fr` to
`grid-template-columns: repeat(3, 1fr)` inside `.stats-grid`.

---

## Layout explained (5 lines)

1. **App shell** is a `flexbox` row: the fixed-width `<aside>` sidebar sits left, and `<main>` fills the rest.
2. **Main content** is a `flexbox` column — each section stacks top-to-bottom with a `gap`.
3. **Stat cards** and **Analysis Breakdown** use `CSS Grid` with equal columns (`1fr 1fr` and `repeat(3, 1fr)`).
4. **Bottom row** uses a two-column `CSS Grid` with `1.6fr 1fr` so the timeline gets more space than the right-side cards.
5. Individual **cards** use `flexbox` internally (icon + text side by side, header row with space-between) — no nesting tricks needed.

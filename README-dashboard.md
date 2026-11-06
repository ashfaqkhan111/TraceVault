# TraceVault Dashboard — Static HTML/CSS

A pixel-faithful, static implementation of the **TraceVault – Forensic Intelligence Suite** dashboard.  
No JavaScript. No frameworks. Just HTML and CSS.

---

## How to Open

1. Double-click `index.html` (project root) — it auto-redirects to the dashboard.  
2. Or open `frontend/pages/dashboard.html` directly in any modern browser
   (Chrome, Firefox, Edge, Safari all work).  
3. No build step, no server required. Just a browser.

---

## File Structure

```
TraceVault/
├── index.html                          ← Root entry; redirects to dashboard
│
└── frontend/
    ├── pages/
    │   └── dashboard.html              ← The main dashboard page (all markup)
    │
    └── css/
        └── pages/
            └── dashboard.css           ← All styles for the dashboard
```

---

## How to Edit

### Change a color
Open `dashboard.css` and find **Section 1 — Design Tokens** at the top.
Every color is a CSS variable on `:root`. For example:

```css
--clr-green: #10b981;   /* change this one line → updates buttons, nav, links */
```

### Change layout widths
Also in the design tokens:

```css
--sidebar-w: 185px;   /* sidebar width */
--main-pad:  24px;    /* main content padding */
```

### Add a nav item
In `dashboard.html`, copy any `<li>` inside `<ul class="nav-list">` and swap the SVG icon path and label text.

### Add a table row
In the `<tbody>` of `.verif-table`, copy an existing `<tr>` block and change the text content and badge class (`badge--green` or `badge--amber`).

### Adjust progress bars
Each breakdown card has a `<div class="progress__fill" style="width: 47%">`.  
Change the inline `width` percentage to update the bar length.

---

## Responsive Behavior

| Viewport   | Layout change |
|------------|---------------|
| ≥ 1024px   | Full two-column bottom row; 3-column breakdown grid |
| < 1024px   | Bottom row stacks; breakdown cards wrap to 2 columns |
| < 768px    | Sidebar becomes a top bar; stat cards stack; table scrolls horizontally |

---

## Design Tokens Quick Reference

| Token                | Value       | Used for |
|----------------------|-------------|----------|
| `--clr-green`        | `#10b981`   | Buttons, active nav, links |
| `--clr-green-tint`   | `#d1fae5`   | Icon backgrounds, active pill |
| `--clr-green-dark`   | `#047857`   | Text on green-tint backgrounds |
| `--clr-amber-bg`     | `#fef3c7`   | Processing / Pending badge background |
| `--clr-amber-text`   | `#b45309`   | Processing / Pending badge text |
| `--clr-page`         | `#f8fafc`   | Page background |
| `--clr-heading`      | `#0f172a`   | H1, values, filenames |
| `--clr-muted`        | `#64748b`   | Captions, dates, secondary text |

# Styling Approaches

There is no single "right" way to style a React application. React is unopinionated about CSS, so the ecosystem offers several approaches—from plain stylesheets to scoped CSS Modules, utility-first frameworks like Tailwind CSS, and CSS-in-JS libraries like Styled Components. This article walks through the most common options, their trade-offs, and when to reach for each.

## Plain CSS and Global Stylesheets

The simplest approach is to write a regular `.css` file and import it.

```jsx
import './App.css';

function App() {
  return <h1 className="title">Hello World</h1>;
}
```

```css
/* App.css */
.title {
  color: #2c3e50;
  font-size: 2rem;
}
```

- **Pros:** Zero setup, familiar, works everywhere.
- **Cons:** All class names are **global**, so naming collisions become likely as the app grows. Conventions like BEM help but require discipline.

## Inline Styles

React lets you pass a style object directly via the `style` prop. Properties are camelCased.

```jsx
function Badge() {
  const style = {
    backgroundColor: '#e74c3c',
    color: 'white',
    padding: '4px 8px',
    borderRadius: '4px',
  };

  return <span style={style}>New</span>;
}
```

- **Pros:** Truly scoped to the element, easy to compute dynamically from props or state.
- **Cons:** No pseudo-classes (`:hover`), media queries, or keyframes. Not ideal for anything beyond small dynamic tweaks.

## CSS Modules

CSS Modules give you locally scoped class names automatically. Name the file `*.module.css` and import it as an object.

```css
/* Button.module.css */
.button {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
}
```

```jsx
import styles from './Button.module.css';

function Button({ children }) {
  return <button className={styles.button}>{children}</button>;
}
```

At build time, `styles.button` becomes a unique class like `Button_button__a1b2c`, eliminating collisions.

- **Pros:** Scoped by default, plain CSS syntax, no runtime cost, great with Vite/Create React App/Next.js.
- **Cons:** Dynamic styling still needs conditional class logic; sharing values with JS requires extra tooling.

## Tailwind CSS (Utility-First)

[Tailwind CSS](https://tailwindcss.com/) provides low-level utility classes you compose directly in your markup, so you rarely write custom CSS.

```bash
npm install tailwindcss @tailwindcss/vite
```

```jsx
function Card() {
  return (
    <div className="max-w-sm rounded-lg shadow-md p-6 bg-white">
      <h2 className="text-xl font-bold text-gray-800">Title</h2>
      <p className="mt-2 text-gray-600">Some description text.</p>
      <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
        Action
      </button>
    </div>
  );
}
```

- **Pros:** Fast to build, consistent design tokens, responsive and state variants (`hover:`, `md:`) built in, small production bundles via purging.
- **Cons:** Markup can look cluttered with long class strings; there is a learning curve for the utility vocabulary.

Extract repeated patterns into components (or `@apply` in CSS) to keep markup readable.

## Styled Components (CSS-in-JS)

[Styled Components](https://styled-components.com/) lets you write actual CSS inside JavaScript using tagged template literals, producing scoped, component-based styles.

```bash
npm install styled-components
```

```jsx
import styled from 'styled-components';

const Button = styled.button`
  background: ${(props) => (props.primary ? '#3498db' : '#ecf0f1')};
  color: ${(props) => (props.primary ? 'white' : '#333')};
  padding: 8px 16px;
  border: none;
  border-radius: 4px;

  &:hover {
    opacity: 0.9;
  }
`;

function App() {
  return (
    <div>
      <Button primary>Save</Button>
      <Button>Cancel</Button>
    </div>
  );
}
```

- **Pros:** Fully scoped, dynamic styling from props, supports pseudo-classes/media queries, colocated with components, theming support.
- **Cons:** Runtime overhead, larger bundle, and the library's future is uncertain as the ecosystem shifts toward zero-runtime solutions.

## Other Notable Options

- **Emotion** — a flexible CSS-in-JS library similar to Styled Components with a `css` prop.
- **Sass/SCSS** — adds variables, nesting, and mixins to plain CSS; pairs well with CSS Modules.
- **Vanilla Extract / Linaria** — zero-runtime CSS-in-JS that generates static CSS at build time, blending the ergonomics of CSS-in-JS with the performance of CSS Modules.

## Choosing an Approach

| Approach | Scoping | Dynamic Styling | Runtime Cost | Best For |
|----------|---------|-----------------|--------------|----------|
| Plain CSS | Global | Manual | None | Tiny apps, prototypes |
| Inline styles | Element | Easy | Minimal | Small dynamic tweaks |
| CSS Modules | Local | Class toggling | None | Most apps wanting plain CSS |
| Tailwind CSS | Utility | Variants | None (purged) | Rapid, consistent UIs |
| Styled Components | Local | Props-driven | Runtime | Component libraries, theming |

Some guidelines:

- Reach for **CSS Modules** or **Tailwind CSS** as safe, modern defaults for most projects.
- Use **CSS-in-JS** (Styled Components/Emotion) when styles depend heavily on props or you want theming colocated with components.
- Keep **inline styles** for small, computed values only.

## Conclusion

React gives you the freedom to style however you like. Global CSS is fine to start, but scoped solutions—**CSS Modules**, **Tailwind CSS**, or **CSS-in-JS**—scale far better in real applications. Pick one primary approach per project for consistency, and choose based on your team's preferences, performance needs, and how dynamic your styling has to be.

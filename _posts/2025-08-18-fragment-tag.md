# Fragment Tag

In **React**, the purpose of the `<Fragment>` tag is to group multiple elements together **without adding an extra DOM node** (like a `<div>`).

Normally, a React component must return a single parent element, but sometimes you want to return multiple sibling elements without introducing unnecessary markup. That’s where `Fragment` comes in.

### Example without Fragment (extra `<div>` gets added):

```jsx
<Card>
  <div>
    <Card.Header>Title</Card.Header>
    <Card.Body>Content</Card.Body>
  </div>
</Card>
```

Here, the extra `<div>` wraps the header and body, which might break Bootstrap’s styling/layout.

### Example with Fragment (no extra DOM element):

```jsx
<Card>
  <>
    <Card.Header>Title</Card.Header>
    <Card.Body>Content</Card.Body>
  </>
</Card>
```

or explicitly:

```jsx
<Card>
  <React.Fragment>
    <Card.Header>Title</Card.Header>
    <Card.Body>Content</Card.Body>
  </React.Fragment>
</Card>
```

### ✅ Why use `Fragment` in React Bootstrap?

* Keeps Bootstrap components structured correctly without extra wrappers.
* Avoids invalid DOM nesting (important with Bootstrap grids, tables, and cards).
* Improves performance slightly (fewer unnecessary elements in the DOM).

👉 In short: **`Fragment` lets you group children without breaking Bootstrap’s expected HTML structure.**
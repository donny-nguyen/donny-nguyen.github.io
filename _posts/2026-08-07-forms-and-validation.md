# Forms & Validation

Forms are one of the most common ways users interact with a web application—logging in, signing up, searching, checkout, and more. In React, forms require a bit of extra thought because the UI is driven by state. This article covers how to build forms, capture user input, validate that input, and manage complex forms with popular libraries like **React Hook Form**, **Formik**, and **Yup**.

## Controlled vs. Uncontrolled Forms

React offers two ways to handle form inputs:

- **Controlled components** — form data is stored in React state and the input value is driven by that state. React is the "single source of truth."
- **Uncontrolled components** — form data is handled by the DOM itself, and you read the value with a `ref` when you need it.

Controlled components are the recommended default because they make validation, conditional logic, and dynamic UI easier.

## Building a Controlled Form

```jsx
import { useState } from 'react';

function LoginForm() {
  const [form, setForm] = useState({ email: '', password: '' });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Submitting:', form);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        name="email"
        type="email"
        value={form.email}
        onChange={handleChange}
        placeholder="Email"
      />
      <input
        name="password"
        type="password"
        value={form.password}
        onChange={handleChange}
        placeholder="Password"
      />
      <button type="submit">Log In</button>
    </form>
  );
}

export default LoginForm;
```

- A single `handleChange` handler updates state for all fields using the input's `name`.
- `e.preventDefault()` stops the browser from doing a full page reload on submit.

## Manual Validation

For simple forms, you can validate in the submit handler and store error messages in state.

```jsx
import { useState } from 'react';

function SignupForm() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});

  const validate = () => {
    const newErrors = {};
    if (!form.email) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      newErrors.email = 'Email is invalid';
    }
    if (form.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }
    return newErrors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const validationErrors = validate();
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length === 0) {
      console.log('Valid form:', form);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div>
        <input
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="Email"
        />
        {errors.email && <span className="error">{errors.email}</span>}
      </div>
      <div>
        <input
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Password"
        />
        {errors.password && <span className="error">{errors.password}</span>}
      </div>
      <button type="submit">Sign Up</button>
    </form>
  );
}

export default SignupForm;
```

Manual validation works, but it becomes repetitive and error-prone as forms grow. This is where dedicated libraries shine.

## React Hook Form

[React Hook Form](https://react-hook-form.com/) is a lightweight, performant library that uses uncontrolled inputs under the hood (via refs) to minimize re-renders. It is the most popular form library in the modern React ecosystem.

```bash
npm install react-hook-form
```

```jsx
import { useForm } from 'react-hook-form';

function SignupForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = (data) => {
    console.log('Valid form:', data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input
        placeholder="Email"
        {...register('email', {
          required: 'Email is required',
          pattern: {
            value: /\S+@\S+\.\S+/,
            message: 'Email is invalid',
          },
        })}
      />
      {errors.email && <span className="error">{errors.email.message}</span>}

      <input
        type="password"
        placeholder="Password"
        {...register('password', {
          required: 'Password is required',
          minLength: { value: 8, message: 'At least 8 characters' },
        })}
      />
      {errors.password && <span className="error">{errors.password.message}</span>}

      <button type="submit">Sign Up</button>
    </form>
  );
}

export default SignupForm;
```

Key benefits:

- **Performance** — fewer re-renders because inputs are uncontrolled.
- **Less boilerplate** — no manual `useState` or `onChange` wiring.
- **Built-in validation** — declarative rules like `required`, `minLength`, and `pattern`.

## Formik

[Formik](https://formik.org/) is another widely used library that keeps form state in React state (controlled). It provides helpers for values, errors, touched fields, and submission.

```bash
npm install formik
```

```jsx
import { useFormik } from 'formik';

function SignupForm() {
  const formik = useFormik({
    initialValues: { email: '', password: '' },
    validate: (values) => {
      const errors = {};
      if (!values.email) errors.email = 'Email is required';
      if (values.password.length < 8) errors.password = 'At least 8 characters';
      return errors;
    },
    onSubmit: (values) => {
      console.log('Valid form:', values);
    },
  });

  return (
    <form onSubmit={formik.handleSubmit}>
      <input
        name="email"
        onChange={formik.handleChange}
        value={formik.values.email}
        placeholder="Email"
      />
      {formik.errors.email && <span className="error">{formik.errors.email}</span>}

      <input
        name="password"
        type="password"
        onChange={formik.handleChange}
        value={formik.values.password}
        placeholder="Password"
      />
      {formik.errors.password && <span className="error">{formik.errors.password}</span>}

      <button type="submit">Sign Up</button>
    </form>
  );
}

export default SignupForm;
```

## Schema Validation with Yup

Writing validation logic by hand gets messy. [Yup](https://github.com/jquense/yup) lets you define a validation **schema** declaratively and reuse it across forms. It integrates cleanly with both React Hook Form and Formik.

```bash
npm install yup @hookform/resolvers
```

```jsx
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

const schema = yup.object({
  email: yup.string().required('Email is required').email('Email is invalid'),
  password: yup
    .string()
    .required('Password is required')
    .min(8, 'At least 8 characters'),
});

function SignupForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema) });

  const onSubmit = (data) => console.log('Valid form:', data);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input placeholder="Email" {...register('email')} />
      {errors.email && <span className="error">{errors.email.message}</span>}

      <input type="password" placeholder="Password" {...register('password')} />
      {errors.password && <span className="error">{errors.password.message}</span>}

      <button type="submit">Sign Up</button>
    </form>
  );
}

export default SignupForm;
```

A single schema keeps validation rules in one place, making forms easier to read, test, and maintain.

## Best Practices

- **Validate on the client and the server.** Client-side validation improves UX, but the server must always be the final gatekeeper for security.
- **Give clear, immediate feedback.** Show errors near the relevant field and consider validating on blur or on change for a smoother experience.
- **Disable the submit button while submitting** to prevent duplicate submissions.
- **Use a library for anything beyond trivial forms.** React Hook Form + Yup is a popular, powerful combination.
- **Keep accessibility in mind.** Use `<label>` elements, associate errors with inputs via `aria-describedby`, and make error messages screen-reader friendly.

## Conclusion

Forms in React revolve around managing input state and validating that input reliably. Start with controlled components and manual validation to understand the fundamentals, then reach for **React Hook Form** or **Formik**—paired with a schema library like **Yup**—to build robust, maintainable forms with minimal boilerplate.

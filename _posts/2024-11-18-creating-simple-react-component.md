# Creating Simple React component

Creating a simple React component is straightforward! Here's a step-by-step guide to building a basic React functional component:

---

### Steps to Create a React Component

1. **Set up our React project:**
   - If we don't already have a React project, create one using:
     ```bash
     npx create-react-app my-app
     cd my-app
     ```
   - If we're using Vite, we can set it up with:
     ```bash
     npm create vite@latest my-app --template react
     cd my-app
     npm install
     ```

2. **Create a new component:**
   - Navigate to the `src` folder and create a new file, e.g., `MyComponent.js`.

3. **Write the React component:**
   Here's an example of a simple functional component:
   ```jsx
   import React from 'react';

   const MyComponent = () => {
       return (
           <div>
               <h1>Hello, World!</h1>
               <p>This is a simple React component.</p>
           </div>
       );
   };

   export default MyComponent;
   ```

4. **Use the component in our app:**
   - Open `App.js` (or any other parent component where we want to use our new component).
   - Import our new component and include it in the JSX:
     ```jsx
     import React from 'react';
     import MyComponent from './MyComponent';

     function App() {
         return (
             <div>
                 <MyComponent />
             </div>
         );
     }

     export default App;
     ```

5. **Run the project:**
   - Start the development server:
     ```bash
     npm start
     ```
   - Open our browser and go to `http://localhost:3000` to see our component in action!

---

### Explanation
- **Functional Component:** A function that returns JSX to define the UI.
- **JSX:** A syntax extension for JavaScript that looks like HTML. It is used to describe what the UI should look like.

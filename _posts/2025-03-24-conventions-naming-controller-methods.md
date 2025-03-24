# Conventions for Naming Controller Methods

In Adonis.js, particularly when working with resourceful routes, there are conventions for naming controller methods that align with typical RESTful API actions. Here's a breakdown:

* **Resourceful Routes:**
    * When you use `Route.resource()` in your `routes.ts` file, Adonis.js expects your controller to have specific method names. These names correspond to common CRUD (Create, Read, Update, Delete) operations.
    * Here's a standard mapping:
        * `index()`: To display a list of resources.
        * `create()`: To display the form for creating a new resource.
        * `store()`: To store a newly created resource.
        * `show()`: To display a specific resource.
        * `edit()`: To display the form for editing a resource.
        * `update()`: To update a specific resource.
        * `destroy()`: To delete a specific resource.

* **General Controller Methods:**
    * While the above conventions are strong for resourceful routes, you're not strictly limited to them.
    * In general, you can name your controller methods as you see fit, as long as your routes file correctly points to those methods.
    * However, adhering to common naming conventions (like those used in resourceful routes) improves code readability and maintainability.

* **Single Action Controllers:**
    * Adonis.js also supports single-action controllers, where a controller has only one primary function. In these cases, the convention is to use a `handle()` method.

Key takeaways:

* Adonis.js encourages RESTful conventions, which are reflected in the standard method names for resourceful controllers.
* While those conventions exist, you have flexability to name methods as needed, as long as your routing is correct.
* Single action controllers use the "handle()" method.

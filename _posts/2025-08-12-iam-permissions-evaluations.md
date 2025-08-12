# IAM Permissions Evaluations

AWS Identity and Access Management (IAM) permissions are evaluated with a simple, yet powerful, logic that determines whether a user, role, or other identity can perform a specific action. The core principle is that all requests are **denied by default**, and you must explicitly allow an action for it to be permitted.

This evaluation is not a simple "first-match-wins" system. Instead, AWS evaluates all applicable policies and then follows a specific set of rules to make the final decision. The hierarchy of these rules is the most critical part of the evaluation logic.

---

### Evaluation Hierarchy 

The AWS IAM policy evaluation process can be summarized in three key rules, which are evaluated in the following order:

1.  **Explicit Deny Overrides Everything** 🚫
    If any single policy contains an `Effect: "Deny"` statement that applies to the request, the request is immediately denied, regardless of any `Allow` statements in other policies. This is the highest precedence rule. A single explicit deny will always win.

2.  **Explicit Allow** ✅
    If there are no explicit deny statements that apply, the next step is to look for an `Effect: "Allow"` statement. If at least one policy has an `Allow` statement that permits the requested action, the request is allowed.

3.  **Default Deny** ❌
    If there are no explicit `Deny` statements and no `Allow` statements that apply to the request, the request is denied by default. This is the foundation of the least-privilege security model in AWS.



---

### How it Works in Practice

Imagine you have an IAM user named "Alice" who needs to access a specific S3 bucket.

* **Scenario 1: Granting Access**
    * You attach a policy to Alice that explicitly allows her to perform `s3:GetObject` on the `my-sensitive-data` S3 bucket.
    * When Alice tries to get an object from that bucket, AWS finds an `Allow` statement and no `Deny` statements. The request is **allowed**.

* **Scenario 2: Denying Specific Access**
    * You want to allow Alice to manage most S3 buckets but prevent her from ever deleting objects from `my-critical-logs`.
    * You attach a policy with an `Allow` for `s3:*` on all S3 resources (`"Resource": "*"`) to her IAM user.
    * You also attach a separate policy (or add a statement to the same policy) with an **explicit deny** (`Effect: "Deny"`) for the `s3:DeleteObject` action on the `my-critical-logs` bucket.
    * When Alice tries to delete an object from `my-critical-logs`, the **explicit deny** takes precedence over the general `Allow` statement. The request is **denied**.

* **Scenario 3: No Policy Applied**
    * Alice tries to perform an action for which she has no attached policies that either allow or deny the action.
    * Because there's no `Allow` and no `Deny`, the **default deny** rule applies, and her request is **denied**.

This simple but strict logic ensures that you can grant broad permissions and then use explicit denies to create "security holes" to prevent specific dangerous actions, providing a robust security framework.
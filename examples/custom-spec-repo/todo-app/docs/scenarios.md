# Scenarios

## Update todo

### SCN-001: Update persists new title and completed flag
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java
Why: confirms update is saved, not just echoed back
- Given todo 1 exists with title "Buy milk" and not completed
- When the client updates todo 1 with title "Buy oat milk" and completed true
- Then the response is 200 with the new title and completed true
- And a later fetch of todo 1 returns the same values

### SCN-002: Blank title rejected on update
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java
Why: bad update must not partly apply
- Given todo 1 exists with title "Buy milk"
- When the client updates todo 1 with an empty title
- Then the response is 400
- And todo 1 still has title "Buy milk"

### SCN-003: Missing completed field rejected on update
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java
Why: a completed todo must not silently become incomplete (data loss). Client must send completed explicitly
- Given todo 1 exists with completed true
- When the client updates todo 1 with a title but no completed field
- Then the response is 400
- And todo 1 is unchanged (title and completed still true)

### SCN-004: Malformed body rejected on update
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java, src/test/java/com/example/todoapp/TodoErrorBodyTest.java
Why: broken input must not change data or leak internals
- Given todo 1 exists with title "Buy milk"
- When the client updates todo 1 with a body that is not valid JSON
- Then the response is 400 with an empty body
- And todo 1 is unchanged

### SCN-005: Non-numeric id on update
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java, src/test/java/com/example/todoapp/TodoErrorBodyTest.java
Why: bad path input should be a client error, not a server error, and must not leak internals
- Given any set of todos
- When the client updates todo "abc"
- Then the response is 400 with an empty body

### SCN-006: Body id ignored on update
Status: implemented in src/test/java/com/example/todoapp/TodoControllerTest.java
Why: a client must not be able to overwrite a different todo. Guess: ignoring body id is the intended behavior
- Given todos 1 and 2 exist
- When the client updates todo 1 with a body that contains id 2
- Then todo 1 is updated
- And todo 2 is unchanged

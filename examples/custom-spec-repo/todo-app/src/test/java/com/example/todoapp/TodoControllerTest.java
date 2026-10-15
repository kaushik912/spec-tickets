package com.example.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDatabase() {
        todoRepository.deleteAll();
    }

    @Test
    void createReturnsCreatedTodo() throws Exception {
        mockMvc.perform(post("/todos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Buy milk"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createWithBlankTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/todos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllReturnsAllTodos() throws Exception {
        todoRepository.save(new Todo("Buy milk"));
        todoRepository.save(new Todo("Walk dog"));

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title").value(org.hamcrest.Matchers.containsInAnyOrder("Buy milk", "Walk dog")));
    }

    @Test
    void getByIdReturnsTodoWhenExists() throws Exception {
        Todo saved = todoRepository.save(new Todo("Buy milk"));

        mockMvc.perform(get("/todos/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Buy milk"));
    }

    @Test
    void getByIdReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/todos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturnsUpdatedTodoWhenExists() throws Exception {
        Todo saved = todoRepository.save(new Todo("Buy milk"));

        mockMvc.perform(put("/todos/" + saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Buy oat milk", "completed", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Buy oat milk"))
                .andExpect(jsonPath("$.completed").value(true));

        // SCN-001: a later fetch returns the same values
        mockMvc.perform(get("/todos/" + saved.getId()))
                .andExpect(jsonPath("$.title").value("Buy oat milk"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void updateReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(put("/todos/999999")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "x", "completed", false))))
                .andExpect(status().isNotFound());
    }

    // SCN-002
    @Test
    void updateWithBlankTitleReturnsBadRequestAndLeavesTodoUnchanged() throws Exception {
        // Given
        Todo saved = todoRepository.save(new Todo("Buy milk"));

        // When
        mockMvc.perform(put("/todos/" + saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "", "completed", true))))
                // Then
                .andExpect(status().isBadRequest());
        assertThat(todoRepository.findById(saved.getId()).orElseThrow().getTitle()).isEqualTo("Buy milk");
    }

    // SCN-003
    @Test
    void updateWithoutCompletedReturnsBadRequestAndLeavesTodoUnchanged() throws Exception {
        // Given
        Todo todo = new Todo("Buy milk");
        todo.setCompleted(true);
        Todo saved = todoRepository.save(todo);

        // When
        mockMvc.perform(put("/todos/" + saved.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "Buy oat milk"))))
                // Then
                .andExpect(status().isBadRequest());
        Todo after = todoRepository.findById(saved.getId()).orElseThrow();
        assertThat(after.getTitle()).isEqualTo("Buy milk");
        assertThat(after.isCompleted()).isTrue();
    }

    // SCN-004
    @Test
    void updateWithMalformedJsonReturnsEmptyBadRequestAndLeavesTodoUnchanged() throws Exception {
        // Given
        Todo saved = todoRepository.save(new Todo("Buy milk"));

        // When
        mockMvc.perform(put("/todos/" + saved.getId())
                        .contentType("application/json")
                        .content("{not json"))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
        assertThat(todoRepository.findById(saved.getId()).orElseThrow().getTitle()).isEqualTo("Buy milk");
    }

    // SCN-005
    @Test
    void updateWithNonNumericIdReturnsEmptyBadRequest() throws Exception {
        // When / Then
        mockMvc.perform(put("/todos/abc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("title", "x", "completed", false))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    // SCN-006
    @Test
    void updateIgnoresIdInBodyAndLeavesOtherTodoUnchanged() throws Exception {
        // Given
        Todo one = todoRepository.save(new Todo("One"));
        Todo two = todoRepository.save(new Todo("Two"));

        // When
        mockMvc.perform(put("/todos/" + one.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", two.getId(), "title", "One updated", "completed", true))))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(one.getId()));
        assertThat(todoRepository.findById(one.getId()).orElseThrow().getTitle()).isEqualTo("One updated");
        assertThat(todoRepository.findById(two.getId()).orElseThrow().getTitle()).isEqualTo("Two");
    }

    @Test
    void deleteRemovesTodoAndSubsequentGetIsNotFound() throws Exception {
        Todo saved = todoRepository.save(new Todo("Buy milk"));

        mockMvc.perform(delete("/todos/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/todos/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(delete("/todos/999999"))
                .andExpect(status().isNotFound());
    }
}

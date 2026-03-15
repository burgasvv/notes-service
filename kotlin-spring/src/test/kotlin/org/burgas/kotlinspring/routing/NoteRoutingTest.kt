package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.identity.Authority
import org.burgas.kotlinspring.entity.identity.Identity
import org.burgas.kotlinspring.entity.note.NoteRequest
import org.burgas.kotlinspring.repository.IdentityRepository
import org.burgas.kotlinspring.repository.NoteRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation::class)
class NoteRoutingTest(
    @Autowired private final val mockMvc: MockMvc,
    @Autowired private final val noteRepository: NoteRepository,
    @Autowired private final val identityRepository: IdentityRepository
) {
    @Test
    @Order(value = 1)
    fun `create note test`() {

        var identity = Identity().apply {
            this.authority = Authority.ADMIN
            this.username = "admin"
            this.password = "admin"
            this.email = "admin@gmail.com"
            this.enabled = true
            this.firstname = "Admin"
            this.lastname = "Admin"
            this.patronymic = "Admin"
        }
        identity = this.identityRepository.save(identity)

        val noteRequest = NoteRequest(
            title = "Test Note",
            content = "Content in Test Note",
            identityId = identity.id
        )
        val objectMapper = ObjectMapper()
        val noteString = objectMapper.writeValueAsString(noteRequest)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/notes/create")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(noteString)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    @Order(value = 2)
    fun `find all notes test`() {
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/notes")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { result -> println(result.response.contentAsString) }
            .andReturn()
    }

    @Test
    @Order(value = 3)
    fun `find note by id test`() {
        val note = this.noteRepository.findNoteByTitle("Test Note").orElseThrow()
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/notes/by-id")
                    .accept(MediaType.APPLICATION_JSON)
                    .param("noteId", note.id.toString())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { result -> println(result.response.contentAsString) }
            .andReturn()
    }

    @Test
    @Order(value = 4)
    fun `update note test`() {
        val note = this.noteRepository.findNoteByTitle("Test Note").orElseThrow()
        val noteRequest = NoteRequest(
            id = note.id,
            title = "Test Note by Admin"
        )

        val objectMapper = ObjectMapper()
        val noteString = objectMapper.writeValueAsString(noteRequest)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/notes/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(noteString)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    @Order(value = 5)
    fun `delete note test`() {
        val note = this.noteRepository.findNoteByTitle("Test Note").orElseThrow()
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/notes/delete")
                    .accept(MediaType.APPLICATION_JSON)
                    .param("noteId", note.id.toString())
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
        val identity = identityRepository.findIdentityByEmail("admin@gmail.com").orElseThrow()
        identityRepository.delete(identity)
    }
}
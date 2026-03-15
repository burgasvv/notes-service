package org.burgas.kotlinspring.routing

import org.burgas.kotlinspring.entity.identity.Authority
import org.burgas.kotlinspring.entity.identity.IdentityRequest
import org.burgas.kotlinspring.repository.IdentityRepository
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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
class IdentityRoutingTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val identityRepository: IdentityRepository
) {

    @Test
    @Order(value = 1)
    fun `create identity test`() {
        val identityRequest = IdentityRequest(
            authority = Authority.ADMIN,
            username = "admin",
            password = "admin",
            email = "admin@gmail.com",
            enabled = true,
            firstname = "Admin",
            lastname = "Admin",
            patronymic = "Admin"
        )

        val objectMapper = ObjectMapper()
        val requestString = objectMapper.writeValueAsString(identityRequest)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/identities/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(requestString)
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    @Order(value = 2)
    fun `find all identities test`() {
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/identities")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@gmail.com", "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { result -> println(result.response.contentAsString) }
            .andReturn()
    }

    @Test
    @Order(value = 3)
    fun `find identity by id test`() {
        val email = "admin@gmail.com"
        val identity = this.identityRepository.findIdentityByEmail(email).orElseThrow()
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/identities/by-id")
                    .accept(MediaType.APPLICATION_JSON)
                    .param("identityId", identity.id.toString())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(email, "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { result -> println(result.response.contentAsString) }
            .andReturn()
    }

    @Test
    @Order(value = 4)
    fun `update identity test`() {
        val email = "admin@gmail.com"
        val identity = this.identityRepository.findIdentityByEmail(email).orElseThrow()
        val identityRequest = IdentityRequest(
            id = identity.id,
            username = "admin test"
        )

        val objectMapper = ObjectMapper()
        val requestString = objectMapper.writeValueAsString(identityRequest)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/identities/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestString)
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(email, "admin"))
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    @Order(value = 5)
    fun `delete identity test`() {
        val email = "admin@gmail.com"
        val identity = this.identityRepository.findIdentityByEmail(email).orElseThrow()

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/identities/delete")
                    .accept(MediaType.APPLICATION_JSON)
                    .param("identityId", identity.id.toString())
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic(email, "admin"))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }
}
package org.burgas.kotlinspring.repository

import org.burgas.kotlinspring.entity.image.Image
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ImageRepository : JpaRepository<Image, UUID>
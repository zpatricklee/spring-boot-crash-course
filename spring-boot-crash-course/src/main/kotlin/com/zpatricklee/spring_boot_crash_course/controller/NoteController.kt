package com.zpatricklee.spring_boot_crash_course.controller

import com.zpatricklee.spring_boot_crash_course.database.model.Note
import com.zpatricklee.spring_boot_crash_course.database.repository.NoteRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/notes")
class NoteController(
    private val noteRepository: NoteRepository
) {

    data class NoteRequest(
        val id: String?,
        @field:NotBlank(message = "Title cannot be blank.")
        val title: String,
        val content: String
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val createdAt: Instant
    )

    @PostMapping
    fun save(
        @Valid @RequestBody body: NoteRequest
    ): NoteResponse {
        val ownerId = SecurityContextHolder.getContext().authentication!!.principal as String
        val note = noteRepository.save(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.content,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )

        return NoteResponse(
            id = note.id.toHexString(),
            title = note.title,
            content = note.content,
            createdAt = note.createdAt
        )
    }

    @GetMapping
    fun findByOwnerId(

    ): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication!!.principal as String

        return noteRepository.findByOwnerId(ObjectId(ownerId)).map {
            NoteResponse(
                id = it.id.toHexString(),
                title = it.title,
                content = it.content,
                createdAt = it.createdAt
            )
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        val note = noteRepository.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("Note not found.")
        }
        val ownerId = SecurityContextHolder.getContext().authentication!!.principal as String
        if (note.ownerId.toHexString() == ownerId) {
            noteRepository.deleteById(ObjectId(id))
        }
    }
}
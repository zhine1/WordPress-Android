package org.wordpress.android.ui.posts.prepublishing.home.usecases

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.wordpress.android.BaseUnitTest

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.wordpress.android.TEST_DISPATCHER
import org.wordpress.android.fluxc.model.PostModel
import org.wordpress.android.fluxc.model.page.PageStatus.DRAFT
import org.wordpress.android.fluxc.model.post.PostStatus
import org.wordpress.android.fluxc.model.post.PostStatus.SCHEDULED
import org.wordpress.android.ui.posts.EditPostRepository
import org.wordpress.android.util.DateTimeUtilsWrapper

class PublishPostImmediatelyUseCaseTest : BaseUnitTest() {
    private lateinit var useCase: PublishPostImmediatelyUseCase
    private lateinit var editPostRepository: EditPostRepository

    @Mock lateinit var dateTimeUtilsWrapper: DateTimeUtilsWrapper

    @InternalCoroutinesApi
    @Before
    fun setup() {
        useCase = PublishPostImmediatelyUseCase(dateTimeUtilsWrapper)
        editPostRepository = EditPostRepository(mock(), mock(), mock(), TEST_DISPATCHER, TEST_DISPATCHER)
        editPostRepository.set { PostModel() }
    }

    @Test
    fun `if publishPost is true then the PostStatus should be a PUBLISHED`() {
        // arrange
        val publishPost = true
        val expectedPostStatus = PostStatus.PUBLISHED

        // act
        useCase.updatePostToPublishImmediately(editPostRepository, publishPost)

        assertThat(editPostRepository.status.toString()).isEqualTo(expectedPostStatus.toString())
    }

    @Test
    fun `if publishPost is false then the PostStatus should be a DRAFT`() {
        // arrange
        val publishPost = false
        val expectedPostStatus = PostStatus.DRAFT

        // act
        useCase.updatePostToPublishImmediately(editPostRepository, publishPost)

        assertThat(editPostRepository.status.toString()).isEqualTo(expectedPostStatus.toString())
    }

    @Test
    fun `EditPostRepository's PostModel should be set with the currentDate if SCHEDULED`() {
        // arrange
        val currentDate = "2020-05-05T20:33:20+0200"
        whenever(dateTimeUtilsWrapper.currentTimeInIso8601()).thenReturn(currentDate)
        editPostRepository.set { PostModel().apply { setStatus(SCHEDULED.toString()) } }

        // act
        useCase.updatePostToPublishImmediately(editPostRepository, false)

        assertThat(editPostRepository.dateCreated).isEqualTo(currentDate)
    }

    @Test
    fun `EditPostRepository's PostModel should have the same date if not SCHEDULED`() {
        // arrange
        val dateCreated = "2020-05-05T20:33:20+0200"
        editPostRepository.set {
            PostModel().apply {
                setDateCreated(dateCreated)
                setStatus(DRAFT.toString())
            }
        }

        // act
        useCase.updatePostToPublishImmediately(editPostRepository, false)

        assertThat(editPostRepository.dateCreated).isEqualTo(dateCreated)
    }
}

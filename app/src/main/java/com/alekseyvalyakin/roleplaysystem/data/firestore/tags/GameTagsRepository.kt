package com.alekseyvalyakin.roleplaysystem.data.firestore.tags

import com.alekseyvalyakin.roleplaysystem.data.firestore.FirestoreCollection
import com.alekseyvalyakin.roleplaysystem.data.firestore.core.HasId
import com.alekseyvalyakin.roleplaysystem.data.firestore.core.game.BaseGameFireStoreRepository
import com.alekseyvalyakin.roleplaysystem.data.firestore.core.game.GameFireStoreRepository
import com.google.firebase.firestore.*
import com.rxfirebase2.RxFirestore
import io.reactivex.Completable
import io.reactivex.Flowable
import timber.log.Timber

class GameTagsRepositoryImpl : BaseGameFireStoreRepository<Tag>(Tag::class.java), GameTagsRepository {

    private val instance = FirebaseFirestore.getInstance()

    override fun observeTagsOrdered(gameId: String): Flowable<List<Tag>> {
        val query = getCollection(gameId)
                .orderBy(HasId.ID_FIELD, Query.Direction.ASCENDING)
        return observeQueryCollection(query, gameId)
    }

    override fun getCollection(gameId: String): CollectionReference {
        return FirestoreCollection.TagsInGame(gameId).getDbCollection()
    }

    override fun addSkill(id: String, skillId: String, gameId: String): Completable {
        val tag = Tag(id = id, skillIds = listOf(skillId))
        val tagReference = getDocumentReference(id, gameId)
        return RxFirestore.runTransaction(instance, Transaction.Function { transaction ->
            val documentSnapshot = transaction.get(tagReference)
            if (!documentSnapshot.exists()) {
                Timber.d("creating document $tag")
                transaction.set(tagReference, tag)
            } else {
                Timber.d("Document exists")
                transaction.update(tagReference, Tag.SKILL_IDS_FIELD, FieldValue.arrayUnion(skillId))
            }
        })
    }

    override fun removeSkill(id: String, skillId: String, gameId: String): Completable {
        val tag = Tag(id = id, skillIds = listOf(skillId))
        val tagReference = getDocumentReference(id, gameId)

        return RxFirestore.runTransaction(instance, Transaction.Function { transaction ->
            val documentSnapshot = transaction.get(tagReference)
            if (!documentSnapshot.exists()) {
                Timber.d("no document $tag")
            } else {
                Timber.d("Document exists")
                transaction.update(tagReference, Tag.SKILL_IDS_FIELD, FieldValue.arrayRemove(skillId))
            }
        })
    }

}

interface GameTagsRepository : GameFireStoreRepository<Tag> {
    fun observeTagsOrdered(gameId: String): Flowable<List<Tag>>

    fun addSkill(id: String, skillId: String, gameId: String): Completable

    fun removeSkill(id: String, skillId: String, gameId: String): Completable
}
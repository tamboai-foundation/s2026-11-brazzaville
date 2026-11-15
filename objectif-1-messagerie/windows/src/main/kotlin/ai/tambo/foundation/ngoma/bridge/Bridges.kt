package ai.tambo.foundation.ngoma.bridge

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class User(val id: Int, val name: String)

data class Message(
    val id: Long,
    val conversationId: Long,
    val authorId: Int,
    val text: String,
    val time: Long,
    val replyToId: Long? = null,
    val edited: Boolean = false,
    val deleted: Boolean = false,
    val reactions: Map<String, Int> = emptyMap(),
)

data class Conversation(
    val id: Long,
    val title: String,
    val unread: Int = 0,
    val pinned: Boolean = false,
    val muted: Boolean = false,
    val archived: Boolean = false,
)

/** Interface UI <-> TModeler pour le compte. Remplacer le mock par une implémentation TModeler. */
interface AccountBridge {
    val user: StateFlow<User?>
    suspend fun restoreSession(): User?
    suspend fun login(identifier: String, password: String): User
    suspend fun register(name: String, identifier: String, password: String): User
    suspend fun logout()
}

/** Interface UI <-> TModeler pour la messagerie. Remplacer le mock par une implémentation TModeler. */
interface MessagingBridge {
    val conversations: StateFlow<List<Conversation>>
    fun messages(conversationId: Long): StateFlow<List<Message>>
    fun lastMessage(conversationId: Long): Message?
    fun send(conversationId: Long, text: String, replyToId: Long? = null)
    fun edit(messageId: Long, text: String)
    fun delete(messageId: Long)
    fun react(messageId: Long, emoji: String)
    fun markRead(conversationId: Long)
    fun setPinned(conversationId: Long, value: Boolean)
    fun setMuted(conversationId: Long, value: Boolean)
    fun setArchived(conversationId: Long, value: Boolean)
    fun createConversation(title: String): Long
}

class AccountBridgeMock : AccountBridge {
    private val state = MutableStateFlow<User?>(null)
    override val user: StateFlow<User?> = state

    override suspend fun restoreSession(): User? {
        delay(1200)
        return state.value
    }

    override suspend fun login(identifier: String, password: String): User {
        delay(500)
        return User(1, identifier.ifBlank { "Moi" }).also { state.value = it }
    }

    override suspend fun register(name: String, identifier: String, password: String): User {
        delay(500)
        return User(1, name.ifBlank { "Moi" }).also { state.value = it }
    }

    override suspend fun logout() {
        state.value = null
    }
}

class MessagingBridgeMock : MessagingBridge {
    private val me = 1
    private var nextId = 100L
    private val all = mutableListOf<Message>()
    private val perConversation = HashMap<Long, MutableStateFlow<List<Message>>>()
    private val convs = MutableStateFlow(
        listOf(
            Conversation(1, "Awa Traoré", unread = 2, pinned = true),
            Conversation(2, "Équipe NGoma", unread = 0),
            Conversation(3, "Kofi Mensah", unread = 1),
            Conversation(4, "Fatou Diop", unread = 0, muted = true),
        )
    )
    override val conversations: StateFlow<List<Conversation>> = convs

    init {
        val now = System.currentTimeMillis()
        seed(1, 2, "Salut ! Tu as vu le nouveau projet ?", now - 3_600_000)
        seed(1, 2, "On commence demain matin.", now - 3_500_000)
        seed(2, 3, "Bienvenue dans l'équipe NGoma.", now - 86_400_000)
        seed(2, me, "Merci, content d'être là !", now - 86_000_000)
        seed(3, 4, "Tu peux m'envoyer le rapport ?", now - 7_200_000)
        seed(4, 5, "Bonne journée à tous.", now - 172_800_000)
    }

    private fun seed(conv: Long, author: Int, text: String, time: Long) {
        all += Message(nextId++, conv, author, text, time)
        flow(conv).value = all.filter { it.conversationId == conv }
    }

    private fun flow(id: Long) = perConversation.getOrPut(id) { MutableStateFlow(emptyList()) }

    private fun publish(conv: Long) {
        flow(conv).value = all.filter { it.conversationId == conv }
    }

    override fun messages(conversationId: Long): StateFlow<List<Message>> = flow(conversationId)

    override fun lastMessage(conversationId: Long) =
        all.lastOrNull { it.conversationId == conversationId && !it.deleted }

    override fun send(conversationId: Long, text: String, replyToId: Long?) {
        all += Message(nextId++, conversationId, me, text, System.currentTimeMillis(), replyToId)
        publish(conversationId)
        touch(conversationId)
    }

    private fun touch(conversationId: Long) {
        convs.value = convs.value.toList()
    }

    private fun update(id: Long, f: (Message) -> Message) {
        val i = all.indexOfFirst { it.id == id }
        if (i < 0) return
        all[i] = f(all[i])
        publish(all[i].conversationId)
        touch(all[i].conversationId)
    }

    override fun edit(messageId: Long, text: String) = update(messageId) { it.copy(text = text, edited = true) }
    override fun delete(messageId: Long) = update(messageId) { it.copy(deleted = true, text = "") }
    override fun react(messageId: Long, emoji: String) = update(messageId) {
        it.copy(reactions = it.reactions + (emoji to ((it.reactions[emoji] ?: 0) + 1)))
    }

    private fun updateConv(id: Long, f: (Conversation) -> Conversation) {
        convs.value = convs.value.map { if (it.id == id) f(it) else it }
    }

    override fun markRead(conversationId: Long) = updateConv(conversationId) { it.copy(unread = 0) }
    override fun setPinned(conversationId: Long, value: Boolean) = updateConv(conversationId) { it.copy(pinned = value) }
    override fun setMuted(conversationId: Long, value: Boolean) = updateConv(conversationId) { it.copy(muted = value) }
    override fun setArchived(conversationId: Long, value: Boolean) = updateConv(conversationId) { it.copy(archived = value) }

    override fun createConversation(title: String): Long {
        val id = (convs.value.maxOfOrNull { it.id } ?: 0) + 1
        convs.value = convs.value + Conversation(id, title)
        return id
    }
}

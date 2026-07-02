package io.legado.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.legado.app.data.entities.AgentMemory

@Dao
interface AgentMemoryDao {

    @Query(
        """
        select * from agentMemories
        where (:status = '' or status = :status)
            and (:scopeType = '' or scopeType = :scopeType)
            and (:scopeKey = '' or scopeKey = :scopeKey)
            and (:subject = '' or subject like '%' || :subject || '%')
            and (:domain = '' or domain = :domain)
            and (:memoryType = '' or memoryType = :memoryType)
            and (
                :keyword = ''
                or title like '%' || :keyword || '%'
                or content like '%' || :keyword || '%'
                or subject like '%' || :keyword || '%'
                or tags like '%' || :keyword || '%'
            )
        order by updatedAt desc
        limit :limit offset :offset
        """
    )
    fun search(
        scopeType: String,
        scopeKey: String,
        subject: String,
        domain: String,
        memoryType: String,
        keyword: String,
        status: String,
        offset: Int,
        limit: Int
    ): List<AgentMemory>

    @Query(
        """
        select count(*) from agentMemories
        where (:status = '' or status = :status)
            and (:scopeType = '' or scopeType = :scopeType)
            and (:scopeKey = '' or scopeKey = :scopeKey)
            and (:subject = '' or subject like '%' || :subject || '%')
            and (:domain = '' or domain = :domain)
            and (:memoryType = '' or memoryType = :memoryType)
            and (
                :keyword = ''
                or title like '%' || :keyword || '%'
                or content like '%' || :keyword || '%'
                or subject like '%' || :keyword || '%'
                or tags like '%' || :keyword || '%'
            )
        """
    )
    fun count(
        scopeType: String,
        scopeKey: String,
        subject: String,
        domain: String,
        memoryType: String,
        keyword: String,
        status: String
    ): Int

    @Query("select * from agentMemories where id = :id")
    fun get(id: String): AgentMemory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(memory: AgentMemory)

    @Query("update agentMemories set status = :status, updatedAt = :updatedAt where id in (:ids)")
    fun updateStatus(ids: List<String>, status: String, updatedAt: Long): Int
}

package com.example.fitswap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitswap.data.local.entity.ExerciseEntity
import com.example.fitswap.data.local.entity.SubstituteLinkEntity

@Dao
interface SubstituteDao {
    @Query("SELECT COUNT(*) FROM substitute_links")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<SubstituteLinkEntity>)

    @Query("SELECT groupId FROM substitute_links WHERE exerciseId = :exerciseId")
    suspend fun groupsFor(exerciseId: String): List<String>

    @Query(
        """
        SELECT DISTINCT e.* FROM exercises e
        INNER JOIN substitute_links l ON l.exerciseId = e.id
        WHERE l.groupId IN (:groupIds) AND e.id != :excludingExerciseId
        """
    )
    suspend fun exercisesInGroups(groupIds: List<String>, excludingExerciseId: String): List<ExerciseEntity>
}

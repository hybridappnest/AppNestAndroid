/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ymy.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ymy.core.database.TableConst
import com.ymy.core.user.UserInfoDB

/**
 * The Data Access Object for the Plant class.
 */
@Dao
interface UserInfoDao {

    @Query("SELECT * FROM ${TableConst.userInfoTableName} WHERE id = :accountId")
    fun getUser(accountId: Long): UserInfoDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userDB: UserInfoDB)

    @Query("DELETE FROM ${TableConst.userInfoTableName} WHERE id = :accountId")
    fun deleteUser(accountId: Long)

}

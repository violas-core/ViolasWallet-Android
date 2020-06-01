package com.violas.wallet.repository.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = TokenDo.TABLE_NAME,
    indices = [
        Index(unique = true, value = ["address", "module", "name", "account_id"])
    ]
)
data class TokenDo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,
    @ColumnInfo(name = "account_id")
    var account_id: Long = 0,
    @ColumnInfo(name = "address")
    var address: String = "00000000000000000000000000000000",
    @ColumnInfo(name = "module")
    var module: String = "LBR",
    @ColumnInfo(name = "name")
    var name: String = "T",
    @ColumnInfo(name = "enable")
    var enable: Boolean = false,
    @ColumnInfo(name = "amount")
    var amount: Long = 0,
    @ColumnInfo(name = "logo")
    var logo: String = ""
) {
    companion object {
        const val TABLE_NAME = "token"
    }
}
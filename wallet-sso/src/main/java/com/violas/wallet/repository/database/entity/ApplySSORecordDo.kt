package com.violas.wallet.repository.database.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = ApplySSORecordDo.TABLE_NAME,
    indices = [
        Index(unique = true, value = ["account_id", "child_number"])
    ]
)
data class ApplySSORecordDo(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,
    @ColumnInfo(name = "account_id")
    var account_id: Long = 0,
    @ColumnInfo(name = "child_number")
    var childNumber: Long = 0,
    @ColumnInfo(name = "wallet_address")
    var walletAddress: String = "",
    @ColumnInfo(name = "token_address")
    var tokenAddress: String = "",
    @ColumnInfo(name = "status")
    var status: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    companion object {
        const val TABLE_NAME = "apply_sso_record"

        @JvmField
        val CREATOR: Parcelable.Creator<ApplySSORecordDo> =
            object : Parcelable.Creator<ApplySSORecordDo> {
                override fun createFromParcel(parcel: Parcel): ApplySSORecordDo {
                    return ApplySSORecordDo(parcel)
                }

                override fun newArray(size: Int): Array<ApplySSORecordDo?> {
                    return arrayOfNulls(size)
                }
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeLong(account_id)
        parcel.writeLong(childNumber)
        parcel.writeString(walletAddress)
        parcel.writeString(tokenAddress)
        parcel.writeInt(status)
    }

    override fun describeContents(): Int {
        return 0
    }
}
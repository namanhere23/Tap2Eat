package com.example.tap2eat

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class CartItems (
    var item: String,
    var price: Int,
    var quantity: Int,
    var photo: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(item)
        parcel.writeInt(price)
        parcel.writeInt(quantity)
        parcel.writeInt(photo)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CartItems> {
        override fun createFromParcel(parcel: Parcel): CartItems = CartItems(parcel)
        override fun newArray(size: Int): Array<CartItems?> = arrayOfNulls(size)
    }
}

package com.example.tap2eat

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class CartItems (
    var item: String="",
    var price: Int=0,
    var quantity: Int=0,
    var photo: String=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(item)
        parcel.writeInt(price)
        parcel.writeInt(quantity)
        parcel.writeString(photo)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CartItems> {
        override fun createFromParcel(parcel: Parcel): CartItems = CartItems(parcel)
        override fun newArray(size: Int): Array<CartItems?> = arrayOfNulls(size)
    }
}

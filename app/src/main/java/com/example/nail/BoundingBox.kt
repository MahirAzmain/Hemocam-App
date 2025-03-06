package com.example.nail
import android.os.Parcel
import android.os.Parcelable
import android.graphics.Rect



data class BoundingBox(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(left)
        parcel.writeInt(top)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoundingBox> {
        // Convert Rect to BoundingBox
        fun fromRect(rect: Rect): BoundingBox {
            return BoundingBox(rect.left, rect.top, rect.width(), rect.height())
        }

        // Convert BoundingBox to Rect
        fun BoundingBox.toRect(): Rect {
            return Rect(left, top, left + width, top + height)
        }

        override fun createFromParcel(parcel: Parcel): BoundingBox {
            return BoundingBox(parcel)
        }

        override fun newArray(size: Int): Array<BoundingBox?> {
            return arrayOfNulls(size)
        }
    }
}






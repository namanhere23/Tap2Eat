import com.google.gson.annotations.SerializedName

data class EphemeralKeyModel(
    val id: String,
    @SerializedName("object") val objectType: String,
    val secret: String,
    val created: Long,
    val livemode: Boolean
)

